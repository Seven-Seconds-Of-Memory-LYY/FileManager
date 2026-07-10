package com.seven.file.manager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seven.file.manager.entity.StorageSpace
import com.seven.file.manager.ui.components.CustomStatusBar
import com.seven.file.manager.viewModel.MediaViewModel

@Composable
fun FileDirectoryScreen(storageSpace: StorageSpace, navController: NavController, mediaViewModel: MediaViewModel = viewModel()) {
    var selectedDestination by rememberSaveable { mutableStateOf(storageSpace.rootUri) }
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

                )
            }
        }
    }
}