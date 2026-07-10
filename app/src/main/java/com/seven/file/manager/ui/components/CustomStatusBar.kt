package com.seven.file.manager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.seven.basis.dimens.adp
import com.seven.basis.dimens.asp
import com.seven.file.manager.R

/**
 * CreateData:     2026/4/14
 *
 * Author:         ly2
 *
 * Description:    自定义顶部导航栏
 */
@Composable
fun CustomStatusBar(
    title: String = "",
    showBack: Boolean = true,
    isImmersive: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    onBackClick: () -> Unit = {},
    rightContent: @Composable (RowScope.() -> Unit)? = null,
    titleContent: @Composable (() -> Unit)? = null,
) {
    Surface(
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth(),
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isImmersive) Modifier.statusBarsPadding() else Modifier)
                .height(44.adp) // 保持标准导航栏高度
                .padding(horizontal = 4.adp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 左侧按钮：只有在需要显示时才占用空间
            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.adp) // 固定点击区域大小
                ) {
                    Icon(
                        modifier = Modifier.size(24.adp),
                        painter = painterResource(R.drawable.ic_back),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                    )
                }
            } else {
                // 如果不显示返回键，可以加个 12.adp 的间距，或者根据视觉需求不加
                Spacer(modifier = Modifier.width(8.adp))
            }

            // 2. 中间区域：使用 weight(1f) 占据所有剩余空间
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.adp), // 与两侧按钮保持微小间距
                contentAlignment = Alignment.CenterStart // 标题默认居左，如需居中改为 Center
            ) {
                if (titleContent != null) {
                    titleContent()
                } else {
                    Text(
                        text = title,
                        fontSize = 17.asp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis // 自动省略
                    )
                }
            }

            // 3. 右侧区域：根据内容自动撑开
            if (rightContent != null) {
                Row(
                    modifier = Modifier.padding(end = 12.adp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.adp),
                    content = rightContent
                )
            } else if (showBack) {
                // 视觉对齐技巧：如果显示了左侧返回键但没有右侧内容，
                // 可以放一个等宽的 Spacer，让标题在视觉上真正“居中”
                Spacer(modifier = Modifier.width(40.adp))
            }
        }
    }
}