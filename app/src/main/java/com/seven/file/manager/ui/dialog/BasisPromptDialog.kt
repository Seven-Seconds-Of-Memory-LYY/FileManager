package com.seven.file.manager.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seven.basis.dimens.adp

@Composable
fun BasisPromptDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.85f,
    confirmButtonText: String = "确定",
    cancelButtonText: String = "取消",
    showCancelButton: Boolean = true,
    dismissOnClickOutside: Boolean = true
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnClickOutside,
            dismissOnClickOutside = dismissOnClickOutside,
            usePlatformDefaultWidth = false // 关键点：禁用平台默认宽度限制，允许我们自定义百分比宽度
        )
    ) {
        Surface(
            // 使用 fillMaxWidth(widthFraction) 动态控制宽度
            modifier = modifier
                .fillMaxWidth(widthFraction)
                .wrapContentHeight(),
            shape = CardDefaults.shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.adp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.adp)
            ) {
                // 1. 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.adp)
                )

                // 2. 正文内容
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.adp)
                )

                // 3. 底部操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showCancelButton) {
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.padding(end = 8.adp)
                        ) {
                            Text(
                                text = cancelButtonText,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    TextButton(onClick = onConfirm) {
                        Text(
                            text = confirmButtonText,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}