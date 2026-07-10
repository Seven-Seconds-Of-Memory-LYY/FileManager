package com.seven.file.manager.tool

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.seven.file.manager.entity.MediaCategory
import com.seven.file.manager.entity.MediaCount
import com.seven.file.manager.entity.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {

    /**
     * 1. 纯粹的计数扫描（一次性拉取数量）
     * 采用 SQL 聚合查询，只返回数字，速度极快，百毫秒级响应
     */
    suspend fun getMediaCounts(): MediaCount = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        // 关键优化：我们只需要查询 MIME_TYPE，并且利用 SQL 分组或者直接查总量
        // 这里使用更直观的按类型分别查询，因为系统索引优化得极好，分开查单次耗时几乎为 0
        val queryUri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf("COUNT(${MediaStore.Files.FileColumns._ID})")

        fun getCountForMime(mimePrefix: String): Int {
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?"
            val selectionArgs = arrayOf(mimePrefix)

            resolver.query(queryUri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0)
                }
            }
            return 0
        }
        val imageCount = getCountForMime(MediaCategory.IMAGE.toMimePrefix())
        val audioCount = getCountForMime(MediaCategory.AUDIO.toMimePrefix())
        val videoCount = getCountForMime(MediaCategory.VIDEO.toMimePrefix())

        return@withContext MediaCount(imageCount, audioCount, videoCount)
    }

    /**
     * 2. 纯粹的计数监听（实时响应数量变动）
     * 只要本地文件增删，UI 上的数字就会自动刷新
     */
    fun observeMediaCounts(): Flow<MediaCount> = callbackFlow {
        // 首次进来，异步获取一次数量发送
        launch(Dispatchers.IO) {
            send(getMediaCounts())
        }

        // 创建观察者
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                // 收到媒体库变动，重新计算数量并发送
                launch(Dispatchers.IO) {
                    send(getMediaCounts())
                }
            }
        }

        val resolver = context.contentResolver
        // 注册对全部媒体类型的监听
        resolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, observer)
        resolver.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer)
        resolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, observer)

        // 释放监听
        awaitClose {
            resolver.unregisterContentObserver(observer)
        }
    }

    /**
     * 1. 纯粹的扫描逻辑（一次性拉取）
     * @param categories 需要扫描的类型集合（例如：setOf(MediaCategory.IMAGE, MediaCategory.VIDEO)）
     */
    suspend fun scanMedia(categories: Set<MediaCategory> = MediaCategory.entries.toSet()): List<MediaFile> =
        withContext(Dispatchers.IO) {
            if (categories.isEmpty()) return@withContext emptyList()

            val mediaList = mutableListOf<MediaFile>()
            val resolver = context.contentResolver

            // 查询目标列
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE
            )

            // 动态构建 Selection 语句。例如：(mime_type LIKE ?) OR (mime_type LIKE ?)
            val selection = categories.joinToString(separator = " OR ") {
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?"
            }
            val selectionArgs = categories.map { it.toMimePrefix() }.toTypedArray()

            val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            val queryUri = MediaStore.Files.getContentUri("external")

            resolver.query(queryUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Unknown"
                    val size = cursor.getLong(sizeColumn)
                    val mimeType = cursor.getString(mimeColumn) ?: ""

                    // 判定当前行具体属于哪种类型
                    val type = when {
                        mimeType.startsWith("image") -> MediaCategory.IMAGE
                        mimeType.startsWith("audio") -> MediaCategory.AUDIO
                        mimeType.startsWith("video") -> MediaCategory.VIDEO
                        else -> continue
                    }

                    // 分区存储规范：根据类型组装对应的公有区 Uri
                    val baseUri = when (type) {
                        MediaCategory.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        MediaCategory.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        MediaCategory.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    val fileUri = ContentUris.withAppendedId(baseUri, id)

                    mediaList.add(MediaFile(id, name, size, mimeType, type, fileUri))
                }
            }
            return@withContext mediaList
        }

    /**
     * 2. 纯粹的监听逻辑（实时响应变动）
     * @param categories 需要监听的类型集合
     */
    fun observeMedia(categories: Set<MediaCategory> = MediaCategory.entries.toSet()): Flow<List<MediaFile>> =
        callbackFlow {
            launch {
                // 首次进来，立刻执行一次扫描发送给 UI
                send(scanMedia(categories))
            }

            // 创建内容观察者
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    launch {
                        // 当监听到数据库变动，重新调用 scanMedia 扫描指定类型
                        send(scanMedia(categories))
                    }
                }
            }

            val resolver = context.contentResolver

            // 根据传入的 categories，动态注册对应的系统 Uri 监听
            // 这样如果是纯音频页面，图片变动就不会触发此处的重新扫描
            if (categories.contains(MediaCategory.IMAGE)) {
                resolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, observer)
            }
            if (categories.contains(MediaCategory.AUDIO)) {
                resolver.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer)
            }
            if (categories.contains(MediaCategory.VIDEO)) {
                resolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, observer)
            }

            // 当 Flow 的收集者（ViewModel/UI）销毁时，自动注销监听
            awaitClose {
                resolver.unregisterContentObserver(observer)
            }
        }
}