package com.seven.basis.dimens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * App dimens repo
 * 维度自适应仓库：负责管理缩放倍率及数值缓存
 * 使用 @Immutable 标记，告诉 Compose 编译器此类数据不会改变，优化重组性能
 * @property scaleFactor 缩放倍率
 */
@Immutable
class AppDimensRepo(val scaleFactor: Float) {
    // 动态缓存，按需生长
    private val dpCache = mutableMapOf<Int, Float>()
    private val spCache = mutableMapOf<Int, Float>()

    fun getDp(index: Int): Dp = dpCache.getOrPut(index) { index * scaleFactor }.dp
    fun getSp(index: Int): TextUnit = spCache.getOrPut(index) { index * scaleFactor }.sp
}

/**
 * Local app dimens
 * 定义全局的 CompositionLocal 注入对象
 * 使用 staticCompositionLocalOf 因为缩放倍率在 Activity 生命周期内极少变动
 */
val LocalAppDimens = staticCompositionLocalOf<AppDimensRepo> {
    AppDimensRepo(scaleFactor = 1.0f)
}

/**
 * Provide adaptive dimens
 * 适配方案注入器：建议在 App 主题内最外层调用
 * @param designWidthDp 设计稿的标准宽度，通常为 360dp
 * @param maxScale 最大缩放倍率限制，防止在平板或大屏手机上 UI 变得过于巨大
 * @param content 子组合项
 */
@Composable
fun ProvideAdaptiveDimens(
    designWidthDp: Int = 375,
    maxScale: Float = 1.25f,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val repo = remember(configuration.screenWidthDp) {
        val scale = (configuration.screenWidthDp.toFloat() / designWidthDp).coerceIn(1.0f, maxScale)
        AppDimensRepo(scaleFactor = scale)
    }

    CompositionLocalProvider(LocalAppDimens provides repo) {
        content()
    }
}

// --- 扩展属性群 ---

/** 整数走缓存：只有第一次计算，后续 O(1) */
val Int.adp: Dp
    @Composable @ReadOnlyComposable get() = LocalAppDimens.current.getDp(this)
val Int.asp: TextUnit
    @Composable @ReadOnlyComposable get() = LocalAppDimens.current.getSp(this)

/** 小数走实时计算：灵活且无损 */
val Double.adp: Dp
    @Composable @ReadOnlyComposable get() = (this.toFloat() * LocalAppDimens.current.scaleFactor).dp
val Float.adp: Dp
    @Composable @ReadOnlyComposable get() = (this * LocalAppDimens.current.scaleFactor).dp
val Double.asp: TextUnit
    @Composable @ReadOnlyComposable get() = (this.toFloat() * LocalAppDimens.current.scaleFactor).sp