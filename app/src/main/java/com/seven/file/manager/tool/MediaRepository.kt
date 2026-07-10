package com.seven.file.manager.tool

import android.app.usage.StorageStatsManager
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.MediaStore
import com.seven.basis.timberTool.TimberTool
import com.seven.file.manager.R
import com.seven.file.manager.entity.MediaCategory
import com.seven.file.manager.entity.MediaCount
import com.seven.file.manager.entity.MediaFile
import com.seven.file.manager.entity.StorageSpace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class MediaRepository(private val context: Context) {
    /**
     * 单次获取内部和外部存储空间（兼容 API 29+）
     */
    suspend fun getStorageSpaces(): List<StorageSpace> = withContext(Dispatchers.IO) {
        TimberTool.iArgs("getStorageSpaces start")
        val storageList = mutableListOf<StorageSpace>()
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

        // 获取所有存储卷
        val volumes: List<StorageVolume> = storageManager.storageVolumes

        for (volume in volumes) {
            // 核心过滤：只处理已经成功挂载（可读写）的物理设备
            if (volume.state != android.os.Environment.MEDIA_MOUNTED) {
                continue
            }

            try {
                val uuidString = volume.uuid
                val uuid: UUID = if (uuidString == null) {
                    StorageManager.UUID_DEFAULT
                } else {
                    UUID.fromString(uuidString)
                }

                // 统一高精度获取容量
                val totalBytes = storageStatsManager.getTotalBytes(uuid)
                val freeBytes = storageStatsManager.getFreeBytes(uuid)

                // 获取本地化盘符名称（系统会自动识别出诸如 "SD 卡" 或 U盘品牌的名称）
                val description = volume.getDescription(context) ?: when {
                    volume.isPrimary -> context.getString(R.string.internal_storage)
                    volume.isRemovable -> context.getString(R.string.external_storage_devices)
                    else -> context.getString(R.string.unknown_storage)
                }

                storageList.add(
                    StorageSpace(
                        id = uuidString ?: "primary",
                        description = description,
                        isPrimary = volume.isPrimary,
                        isRemovable = volume.isRemovable, // 关键属性：识别是否为外置多设备
                        totalBytes = totalBytes,
                        freeBytes = freeBytes
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // 针对 API 29 的标准 File API 降级物理多设备兼容
                val path = if (volume.isPrimary) {
                    android.os.Environment.getExternalStorageDirectory()
                } else {
                    File("/storage/${volume.uuid}")
                }

                if (path.exists() && path.canRead()) {
                    val description = volume.getDescription(context) ?: if (volume.isPrimary) {
                        context.getString(R.string.internal_storage)
                    } else {
                        context.getString(R.string.external_storage_disk)
                    }
                    storageList.add(
                        StorageSpace(
                            id = volume.uuid ?: "primary_fallback",
                            description = description,
                            isPrimary = volume.isPrimary,
                            isRemovable = volume.isRemovable,
                            totalBytes = path.totalSpace,
                            freeBytes = path.freeSpace
                        )
                    )
                }
            }
        }
        TimberTool.iArgs("getStorageSpaces storageList size ${storageList.size}")
        return@withContext storageList
    }

    /**
     * 实时监听存储变动（通过定时轮询 + 随媒体监听器联动刷新）
     */
    fun observeStorageSpaces(pollIntervalMs: Long = 20000): Flow<List<StorageSpace>> = callbackFlow {
        // 定时轮询器：因为系统不会高频回调存储大小变动，通过每 20 秒（可调）轮询一次最新值
        val job = launch(Dispatchers.IO) {
            while (isActive) {
                send(getStorageSpaces())
                delay(pollIntervalMs.milliseconds)
            }
        }

        awaitClose {
            job.cancel()
        }
    }

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