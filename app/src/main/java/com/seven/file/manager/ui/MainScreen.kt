package com.seven.file.manager.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.seven.basis.dimens.adp
import com.seven.basis.extensions.noRippleClickable
import com.seven.file.manager.R
import com.seven.file.manager.entity.MediaCategory
import com.seven.file.manager.entity.MediaCount
import com.seven.file.manager.entity.StorageSpace
import com.seven.file.manager.extensions.bytesToGB
import com.seven.file.manager.screenRoute.ScreenRoute
import com.seven.file.manager.ui.dialog.BasisPromptDialog
import com.seven.file.manager.ui.theme.Color_Blue
import com.seven.file.manager.ui.theme.FileManagerTheme
import com.seven.file.manager.viewModel.MediaViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun MainScreen(navController: NavController) {
    val mediaViewModel: MediaViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val mediaCount by mediaViewModel.mediaCountState.collectAsStateWithLifecycle()
    val storageList by mediaViewModel.storageState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // 注册 Activity 结果回调：当用户从系统设置页返回时触发
    val startSettingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (mediaViewModel.hasFullStoragePermission()) {
            Toast.makeText(context, "已获得全部文件访问权限！", Toast.LENGTH_SHORT).show()
            mediaViewModel.storageSpace.value?.let { storageSpace ->
                navController.navigate(ScreenRoute.FileDirectory(storageSpace))
                mediaViewModel.updateStorageSpace(null)
            }
        } else {
            Toast.makeText(context, "未获得权限，部分功能可能无法使用。", Toast.LENGTH_SHORT).show()
        }
    }

    val storageSpace = mediaViewModel.storageSpace.collectAsStateWithLifecycle()
    storageSpace.value?.let {
        BasisPromptDialog(
            onDismissRequest = {
                mediaViewModel.updateStorageSpace(null)
            },
            onConfirm = {
                mediaViewModel.repository.requestAllFilesPermission(launchIntent = {
                    startSettingLauncher.launch(it)
                })
            },
            title = "权限请求",
            text = "获取全部文件访问权限"
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController)
        },
        content = {
            MainContent(
                navController = navController,
                mediaViewModel = mediaViewModel,
                mediaCount = mediaCount,
                storageList = storageList,
                drawerShow = {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) {
                                open()
                            } else {
                                close()
                            }
                        }
                    }
                }
            )
        }
    )
}

@Composable
private fun DrawerContent(navController: NavController) {
    val containerWidthDp: Dp = with(LocalDensity.current) {
        (LocalWindowInfo.current.containerSize.width * 0.5f).toDp()
    }
    // 动态计算宽度：屏幕宽度的 50%，但最大不超过 360.dp（符合 M3 规范限制）
    val drawerWidth = containerWidthDp.coerceAtMost(360.adp)
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .width(width = drawerWidth)
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(topEnd = 12.adp, bottomEnd = 12.adp)
            )
            .padding(horizontal = 10.adp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(48.adp),
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(width = 5.adp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(height = 50.adp))

        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(state = scrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 40.adp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_setting),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(width = 5.adp))
                Text(
                    text = stringResource(R.string.set),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    navController: NavController,
    mediaViewModel: MediaViewModel,
    drawerShow: () -> Unit = {},
    mediaCount: MediaCount,
    storageList: List<StorageSpace>
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .padding(horizontal = 15.adp)
                    .statusBarsPadding()
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 40.adp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(size = 20.adp)
                        )
                        .padding(horizontal = 10.adp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.adp)
                ) {
                    Icon(
                        modifier = Modifier
                            .size(size = 24.adp)
                            .noRippleClickable(enabled = true, onClick = {
                                drawerShow.invoke()
                            }),
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        modifier = Modifier.weight(weight = 1f),
                        text = "搜索“确认”",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        modifier = Modifier.size(size = 24.adp),
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(horizontal = 15.adp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(weight = 1f)
                    .fillMaxWidth()
            ) {

                item {
                    Spacer(modifier = Modifier.height(height = 20.adp))
                }

                item {
                    Text(
                        text = stringResource(R.string.category),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(height = 15.adp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.adp)
                    ) {
                        MediaCategoryEntrance(
                            navController = navController,
                            count = mediaCount.imageCount,
                            type = MediaCategory.IMAGE
                        )

                        MediaCategoryEntrance(
                            navController = navController,
                            count = mediaCount.audioCount,
                            type = MediaCategory.AUDIO
                        )

                        MediaCategoryEntrance(
                            navController = navController,
                            count = mediaCount.videoCount,
                            type = MediaCategory.VIDEO
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(height = 20.adp))
                }

                if (storageList.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.all_store_space),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(height = 15.adp))

                        storageList.forEachIndexed { index, space ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(height = 10.adp))
                            }
                            StorageEntrance(
                                navController = navController,
                                storageSpace = space,
                                mediaViewModel = mediaViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.StorageEntrance(
    navController: NavController,
    storageSpace: StorageSpace,
    mediaViewModel: MediaViewModel
) {
    val progress = BigDecimal(storageSpace.usedBytes)
        .divide(
            BigDecimal(storageSpace.totalBytes),
            10,
            RoundingMode.HALF_DOWN
        ).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(size = 12.adp)
            )
            .noRippleClickable(enabled = true, onClick = {
                if (mediaViewModel.hasFullStoragePermission()) {
                    navController.navigate(ScreenRoute.FileDirectory(storageSpace))
                } else {
                    mediaViewModel.updateStorageSpace(storageSpace)
                }
            })
            .padding(horizontal = 10.adp, vertical = 10.adp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(28.adp),
                painter = painterResource(R.drawable.ic_phone_android),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(width = 10.adp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = storageSpace.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(height = 5.adp))
                Text(
                    text = buildString {
                        append("剩余:")
                        append(storageSpace.freeBytes.bytesToGB())
                        append("/总共:")
                        append(storageSpace.totalBytes.bytesToGB())
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(height = 10.adp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color_Blue.copy(alpha = 0.1f)),
            progress = { progress },
            color = MaterialTheme.colorScheme.onBackground,
            trackColor = MaterialTheme.colorScheme.background,
            gapSize = 0.adp,
            drawStopIndicator = {}
        )
    }
}

/**
 * Media category entrance on the receiver [RowScope] 媒体分类入口
 *
 * @param count 文件数量
 * @param type 媒体类型
 */
@Composable
private fun RowScope.MediaCategoryEntrance(navController: NavController, count: Int, type: MediaCategory) {
    val label: Int
    val icon: Int
    when (type) {
        MediaCategory.IMAGE -> {
            label = R.string.image
            icon = R.drawable.ic_image
        }

        MediaCategory.AUDIO -> {
            label = R.string.audio
            icon = R.drawable.ic_audio
        }

        MediaCategory.VIDEO -> {
            label = R.string.video
            icon = R.drawable.ic_video
        }
    }
    Card(
        modifier = Modifier
            .weight(weight = 1f)
            .aspectRatio(ratio = 1f)
            .noRippleClickable(enabled = true, onClick = {
                navController.navigate(ScreenRoute.MediaFiles(MediaCategory.IMAGE.name))
            }),
        shape = RoundedCornerShape(size = 12.adp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                modifier = Modifier
                    .size(60.adp)
                    .align(alignment = Alignment.BottomEnd)
                    .padding(end = 8.adp, bottom = 8.adp),
                painter = painterResource(icon),
                contentDescription = null
            )
            Column(
                modifier = Modifier.padding(start = 10.adp, top = 10.adp)
            ) {
                Text(
                    text = stringResource(label),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(5.adp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun DrawerContentPreView() {
    DrawerContent(navController = rememberNavController())
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    FileManagerTheme {
        MainContent(
            navController = rememberNavController(),
            mediaViewModel = viewModel(),
            mediaCount = MediaCount(),
            storageList = listOf(
                StorageSpace(isPrimary = true, description = "内部存储", freeBytes = 1, totalBytes = 5),
                StorageSpace(isPrimary = false, description = "外部存储", freeBytes = 1, totalBytes = 5)
            )
        )
    }
}