package com.seven.file.manager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.seven.basis.dimens.adp
import com.seven.file.manager.R
import com.seven.file.manager.entity.MediaCategory
import com.seven.file.manager.ui.components.CustomStatusBar
import com.seven.file.manager.ui.theme.FileManagerTheme
import com.seven.file.manager.viewModel.MediaViewModel

@Composable
fun MainScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val mediaViewModel: MediaViewModel = viewModel()
    val mediaCount by mediaViewModel.mediaCountState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CustomStatusBar(
                title = "分类",
                rightContent = {
                    Icon(
                        modifier = Modifier.size(24.adp),
                        painter = painterResource(R.drawable.ic_refresh),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                    )

                    Icon(
                        modifier = Modifier.size(24.adp),
                        painter = painterResource(R.drawable.ic_setting),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                })
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {

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
                    Spacer(modifier = Modifier.height(height = 10.adp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.adp)
                    ) {
                        MediaCategoryEntrance(count = mediaCount.imageCount, type = MediaCategory.IMAGE)

                        MediaCategoryEntrance(count = mediaCount.audioCount, type = MediaCategory.AUDIO)

                        MediaCategoryEntrance(count = mediaCount.videoCount, type = MediaCategory.VIDEO)
                    }
                }
            }

        }
    }
}

@Composable
private fun RowScope.MediaCategoryEntrance(count: Int, type: MediaCategory) {
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
            .aspectRatio(ratio = 1f),
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
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(5.adp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    FileManagerTheme {
        MainScreen(navController = rememberNavController())
    }
}