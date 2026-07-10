package com.seven.file.manager.toast

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.seven.basis.dimens.adp
import com.seven.basis.dimens.asp
import kotlinx.coroutines.launch

/**
 * CreateData:     2026/4/15
 *
 * Author:         ly2
 *
 * Description:    全局吐司
 */
@Composable
fun GlobalToastHost(
    // 增加可选参数，用于预览或特殊场景强行显示
    previewMessage: String? = null,
    previewVisible: Boolean = false
) {
    // 1. 状态绑定：如果是预览模式则取预览值，否则取单例值
    val msg = previewMessage ?: ToastManager.message
    val isVisible = if (previewMessage != null) previewVisible else ToastManager.isVisible

    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.9f) }

    // 2. 文字锁定逻辑（解决退出动画时的空档期）
    var displayMessage by remember { mutableStateOf("") }
    LaunchedEffect(msg) {
        if (msg.isNotBlank()) {
            displayMessage = msg
        }
    }

    // 3. 动画执行逻辑
    LaunchedEffect(isVisible) {
        if (isVisible) {
            launch { alpha.animateTo(1f, tween(300)) }
            launch { scale.animateTo(1f, tween(300, easing = LinearOutSlowInEasing)) }
        } else {
            launch { alpha.animateTo(0f, tween(250)) }
            launch { scale.animateTo(0.9f, tween(250)) }
        }
    }

    // 4. UI 渲染层
    if (alpha.value > 0f) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.adp)
                    .padding(horizontal = 10.adp)
                    .graphicsLayer {
                        this.alpha = alpha.value
                        this.scaleX = scale.value
                        this.scaleY = scale.value
                    },
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(10.adp),
            ) {
                Text(
                    text = displayMessage,
                    color = Color.White,
                    fontSize = 14.asp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.adp, vertical = 10.adp)
                )
            }
        }
    }
}

/*@Preview(showBackground = true, name = "正常显示状态")
@Composable
fun PreviewToastVisible() {
    MaterialTheme(
        content = {
            GlobalToastHost(
                previewMessage = "这是一条预览消息",
                previewVisible = true
            )
        }
    )
}*/
