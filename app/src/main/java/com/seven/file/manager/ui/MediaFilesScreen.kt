package com.seven.file.manager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seven.file.manager.entity.MediaCategory
import com.seven.file.manager.ui.components.CustomStatusBar
import com.seven.file.manager.viewModel.MediaViewModel

/**
 * CreateData:     2026/7/13
 *
 * Author:         ly2
 *
 * Description:    MediaFilesScreen 媒体文件列表
 */
@Composable
fun MediaFilesScreen(mediaCategory: String, navController: NavController, mediaViewModel: MediaViewModel = viewModel()) {
    Scaffold(
        topBar = {
            CustomStatusBar(title = "Media Files", onBackClick = {
                navController.popBackStack()
            })
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
        }
    }
}