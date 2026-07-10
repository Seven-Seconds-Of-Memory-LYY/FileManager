package com.seven.file.manager.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

// 定义切角方向的枚举
enum class CutCornerDirection {
    TopLeft, TopRight, BottomLeft, BottomRight
}

/**
 * CreateData:     2026/4/20
 *
 * Author:         ly2
 *
 * Description:    自定义切角Shape
 */
class CustomCutCornerShape(
    private val cornerSize: Dp,
    private val directions: Set<CutCornerDirection> = setOf(CutCornerDirection.TopLeft)
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerPx = with(density) { cornerSize.toPx() }
        val path = Path().apply {
            // 1. 起点定位在左上角区域
            if (directions.contains(CutCornerDirection.TopLeft)) {
                moveTo(0f, cornerPx)
                lineTo(cornerPx, 0f)
            } else {
                moveTo(0f, 0f)
            }

            // 2. 连线到右上角
            if (directions.contains(CutCornerDirection.TopRight)) {
                lineTo(size.width - cornerPx, 0f)
                lineTo(size.width, cornerPx)
            } else {
                lineTo(size.width, 0f)
            }

            // 3. 连线到右下角
            if (directions.contains(CutCornerDirection.BottomRight)) {
                lineTo(size.width, size.height - cornerPx)
                lineTo(size.width - cornerPx, size.height)
            } else {
                lineTo(size.width, size.height)
            }

            // 4. 连线到左下角
            if (directions.contains(CutCornerDirection.BottomLeft)) {
                lineTo(cornerPx, size.height)
                lineTo(0f, size.height - cornerPx)
            } else {
                lineTo(0f, size.height)
            }

            close()
        }
        return Outline.Generic(path)
    }
}