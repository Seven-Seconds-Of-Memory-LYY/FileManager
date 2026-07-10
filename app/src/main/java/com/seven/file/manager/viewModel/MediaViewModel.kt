package com.seven.file.manager.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.seven.basis.net.BasisViewModel
import com.seven.file.manager.entity.MediaCount
import com.seven.file.manager.entity.MediaFile
import com.seven.file.manager.tool.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class MediaViewModel(application: Application) : BasisViewModel(application) {

    private val repository: MediaRepository by lazy {
        MediaRepository(mContext)
    }

    @OptIn(FlowPreview::class)
    var mediaCountState: StateFlow<MediaCount> = repository.observeMediaCounts()
        .debounce(timeoutMillis = 300)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MediaCount()
        )
    var mediaState: StateFlow<List<MediaFile>> = repository.observeMedia()
        .flowOn(Dispatchers.IO) // 强制切到 IO 线程执行扫描
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}