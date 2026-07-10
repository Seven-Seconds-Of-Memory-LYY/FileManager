package com.seven.basis.net

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.seven.basis.BasisAction
import com.seven.basis.R
import com.seven.basis.timberTool.TimberTool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * CreateData:     2025/12/12
 *
 * Author:         ly2
 *
 * Description:    基础ViewModel
 */
open class BasisViewModel(application: Application) : AndroidViewModel(application), IEventViewModel, LifecycleEventObserver {
    protected val mContext by lazy {
        application.applicationContext
    }

    protected var lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_ANY

    // 使用 MutableSharedFlow 作为事件通道
    private val mActionFlow = MutableSharedFlow<BasisAction>(
        replay = 0,                      // 不缓存旧事件（一次性事件）
        extraBufferCapacity = 0          // 无额外缓冲区
        // onBufferOverflow = BufferOverflow.SUSPEND
        // 默认暂停模式，如果 UI 没准备好，ViewModel 等待
    )

    override val actionEvent: SharedFlow<BasisAction>
        get() = mActionFlow.asSharedFlow()

    // 发送事件的方法
    protected fun sendAction(action: BasisAction) {
        viewModelScope.launch {
            // 使用 emit 发送事件
            mActionFlow.emit(action)
        }
    }

    /**
     * Run IO 程序运行 IO
     *
     * @param block 执行内容
     * @param error 错误执行
     */
    fun runIO(block: suspend CoroutineScope.() -> Unit, error: (Throwable) -> Unit = {}) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            block.invoke(this)
        }.onFailure {
            it.printStackTrace()
            error.invoke(it)
        }
    }

    /**
     * Run main 程序运行 Main
     *
     * @param block 执行内容
     * @param error 错误执行
     */
    fun runMain(block: suspend CoroutineScope.() -> Unit, error: (Throwable) -> Unit = {}) = viewModelScope.launch(Dispatchers.Main) {
        runCatching {
            block.invoke(this)
        }.onFailure {
            it.printStackTrace()
            error.invoke(it)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleEvent = event
    }

    //----------------------------- 网络请求 ------------------------------
    /**
     * 基础请求方法 (Callback 模式)
     * @param request 挂起函数，执行具体的网络请求并返回实体类
     * @param onStart 开始请求回调
     * @param onSuccess 成功回调 (仅在 code == 200 时触发)
     * @param onError 失败回调 (包括业务错误和网络异常)
     * @param onFinally 结束回调
     * @param showDefaultLoading 是否显示加载弹窗
     */
    fun <T> launchRequest(
        request: suspend () -> BasisResponseEntity<T>?,
        onStart: (() -> Unit)? = null,
        onSuccess: ((T?) -> Unit)? = null,
        onError: ((BasisHttpException) -> Unit)? = null,
        onFinally: (() -> Unit)? = null,
        showDefaultLoading: Boolean = false // 是否显示默认 Loading
    ) {
        viewModelScope.launch {
            try {
                // 开始如果外部没传 onStart 且需要显示 Loading，则发送 ShowLoading
                if (onStart != null) {
                    onStart.invoke()
                } else if (showDefaultLoading) {
                    sendAction(BasisAction.ShowLoading())
                }

                val response = withContext(Dispatchers.IO) { request.invoke() }

                // 解析
                if (null == response) {
                    throw ConnectException()
                } else if (response.isSuccess()) {
                    onSuccess?.invoke(response.data)
                } else {
                    // 业务逻辑错误：如果外部没传子逻辑，默认弹吐司
                    throw BasisHttpException(response.code, response.msg)
                }
            } catch (e: Exception) {
                // 异常解析
                parseException(exception = e, onError = onError)
            } finally {
                // 结束
                if (onFinally != null) {
                    onFinally.invoke()
                } else if (showDefaultLoading) {
                    sendAction(BasisAction.DismissLoading)
                }
            }
        }
    }

    /**
     * LiveData 版本也同步支持默认 UI 逻辑
     */
    /**
     * Request to live data
     * @param T 数据类型对象
     * @param showDefaultLoading 是否显示弹窗
     * @param onError 如果外部传入了 onError，则走外部的，否则 launchRequest 内部弹吐司
     * @param request
     * @return LiveData<T?>
     */
    fun <T> requestToLiveData(
        showDefaultLoading: Boolean = false,
        onError: ((BasisHttpException) -> Unit)? = null,
        request: suspend () -> BasisResponseEntity<T>?
    ): LiveData<T?> {
        val liveData = MutableLiveData<T?>()
        launchRequest(
            request = request,
            showDefaultLoading = showDefaultLoading,
            onSuccess = { liveData.postValue(it) },
            onError = onError
        )
        return liveData
    }

    //------------------ 顺序执行 start -------------------
    /**
     * 执行请求，成功则返回数据，失败直接抛出异常中断逻辑
     */
    protected suspend fun <T> (suspend () -> BasisResponseEntity<T>?).requestOrThrow(): T? {
        val response = withContext(Dispatchers.IO) { this@requestOrThrow.invoke() }
        if (response == null) {
            throw ConnectException("Response is null")
        } else if (response.isSuccess()) {
            return response.data
        } else {
            // 关键：抛出异常，触发外部 launchSequence 的 catch 逻辑
            throw BasisHttpException(response.code, response.msg)
        }
    }

    /**
     * Launch sequence 顺序执行请求队列
     *
     * @param showDefaultLoading
     * @param onError
     * @param onFinally
     * @param block
     * @receiver
     */
    fun launchSequence(
        showDefaultLoading: Boolean = true,
        onError: ((BasisHttpException) -> Unit)? = null,
        onFinally: (() -> Unit)? = null,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (showDefaultLoading) sendAction(BasisAction.ShowLoading())

                // 执行业务块
                block()

            } catch (e: Exception) {
                //异常解析
                parseException(exception = e, onError = onError)
            } finally {
                if (onFinally != null) {
                    onFinally.invoke()
                } else if (showDefaultLoading) {
                    sendAction(BasisAction.DismissLoading)
                }
            }
        }
    }
    //---------------- 顺序执行 end ---------------------

    /**
     * Parse exception 解析异常，二次处理
     *
     * @param exception
     */
    protected fun parseException(exception: Exception, onError: ((e: BasisHttpException) -> Unit)? = null) {
        val endException = handleException(exception)
        TimberTool.iArgs("Request error", endException.errCode, endException.message)
        when (endException.errCode) {
            HttpCode.CODE_TOKEN_EXPIRED -> {
                sendAction(BasisAction.TokenExpired)
            }

            else -> {
                if (null == onError) {
                    val errorMsg = exception.message
                    if (errorMsg.isNullOrBlank()) {
                        sendAction(BasisAction.ShowToastRes(R.string.please_try_again_later))
                    } else {
                        sendAction(BasisAction.ShowToast(errorMsg))
                    }
                } else {
                    onError.invoke(endException)
                }
            }
        }
    }

    /**
     * 异常解析转换器
     */
    private fun handleException(e: Exception): BasisHttpException {
        return when (e) {
            is BasisHttpException -> e
            is HttpException -> BasisHttpException(e.code(), "Server connection failed")
            is SocketTimeoutException -> BasisHttpException(HttpCode.CODE_TIMEOUT, "Network request timed out")
            is JSONException -> BasisHttpException(HttpCode.CODE_PARSE_ERROR, "Data parsing failed")
            is ConnectException -> BasisHttpException(HttpCode.CODE_UNREACHABLE, "Unable to connect to the server")
            else -> BasisHttpException(HttpCode.CODE_UNKNOW, "Unknown error: ${e.message}")
        }
    }
}