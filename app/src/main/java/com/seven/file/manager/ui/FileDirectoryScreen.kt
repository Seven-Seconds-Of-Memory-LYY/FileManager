package com.seven.file.manager.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seven.basis.dimens.adp
import com.seven.file.manager.entity.StorageSpace
import com.seven.file.manager.ui.components.CustomStatusBar
import com.seven.file.manager.viewModel.MediaViewModel

@Composable
fun FileDirectoryScreen(storageSpace: StorageSpace, navController: NavController, mediaViewModel: MediaViewModel = viewModel()) {
    var fileAbsolutePath by rememberSaveable { mutableStateOf(storageSpace.absolutePath) }

    LaunchedEffect(Unit) {
        mediaViewModel.getAllFilesFromFile(fileAbsolutePath)
    }

    val files by mediaViewModel.fileList.collectAsState()

    Scaffold(
        topBar = {
            CustomStatusBar(
                title = storageSpace.description,
                onBackClick = {
                    navController.popBackStack()
                },
                rightContent = {

                })
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            stickyHeader {
                PrimaryTabRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 36.adp),
                    selectedTabIndex = 0
                ) {
                    Text(
                        text = ""
                    )
                }
            }

            if (files.isNotEmpty()) {
                items(
                    count = files.size,
                    itemContent = { index ->
                        Text(
                            text = files.getOrNull(index)?.name.orEmpty()
                        )
                    }
                )
            }
        }
    }
}