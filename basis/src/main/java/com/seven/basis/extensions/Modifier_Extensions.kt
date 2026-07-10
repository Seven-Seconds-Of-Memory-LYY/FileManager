package com.seven.basis.extensions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.seven.basis.dimens.adp

/**
 * No ripple clickable 无点击效果点击事件
 *
 * @param enabled 是否可用
 * @param onClick
 */
@Composable
fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}

@Composable
fun Modifier.dashBorder(
    color: Color,
    width: Dp = 1.adp,
    cornerRadius: Dp = 0.adp,
    dashLength: Dp = 3.adp,
    gapLength: Dp = 3.adp
) = this.drawBehind {
    val strokeWidth = width.toPx()
    val dashPathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx()),
        phase = 0f
    )

    val rect = Rect(offset = Offset.Zero, size = size)
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                rect = rect.deflate(strokeWidth / 2), // 缩放以防止边框溢出
                cornerRadius = CornerRadius(cornerRadius.toPx())
            )
        )
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            pathEffect = dashPathEffect
        )
    )
}