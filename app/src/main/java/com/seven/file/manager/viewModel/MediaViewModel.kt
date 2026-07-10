package com.seven.file.manager.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.seven.basis.net.BasisViewModel
import com.seven.file.manager.entity.MediaCount
import com.seven.file.manager.entity.MediaFile
import com.seven.file.manager.entity.StorageSpace
import com.seven.file.manager.tool.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class MediaViewModel(application: Application) : BasisViewModel(application) {
    private val repository: MediaRepository by lazy {
        MediaRepository(mContext)
    }

    val storageState: StateFlow<List<StorageSpace>> by lazy {
        repository.observeStorageSpaces()
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    @OptIn(FlowPreview::class)
    val mediaCountState: StateFlow<MediaCount> by lazy {
        repository.observeMediaCounts()
            .debounce(timeoutMillis = 300)
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = MediaCount()
            )
    }

    val mediaState: StateFlow<List<MediaFile>> by lazy {
        repository.observeMedia()
            .flowOn(Dispatchers.IO) // 强制切到 IO 线程执行扫描
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}