package com.seven.file.manager.screenRoute

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.seven.file.manager.ui.MainScreen

// 定义全局配置容器
val LocalScreenConfig = compositionLocalOf<MutableState<ScreenRoute>> {
    mutableStateOf(ScreenRoute.Main)
}

/**
 * Screen 极简扩展函数：在官方 composable 基础上增加自动上报功能
 *
 * @param T
 * @param enterTransition
 * @param exitTransition
 * @param popEnterTransition
 * @param popExitTransition
 * @param content
 */
inline fun <reified T : Any> NavGraphBuilder.screen(
    noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    noinline content: @Composable (NavBackStackEntry) -> Unit
) {
    composable<T>(
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition
    ) { backStackEntry ->
        val configState = LocalScreenConfig.current

        // 核心：利用 toRoute<T>() 拿到当前真实的对象实例
        val route = backStackEntry.toRoute<T>()

        // 只要进入这个页面，就自动更新全局状态
        if (route is ScreenRoute && configState.value != route) {
            configState.value = route
        }
        content(backStackEntry)
    }
}

/**
 * Register all screens 注册所有页面
 *
 * @param navController 导航
 */
fun NavGraphBuilder.registerAllScreens(navController: NavHostController) {
    //辅助函数，用来触发编译器的穷举检查
    val sentinel = { route: ScreenRoute ->
        when (route) {
            ScreenRoute.Main -> {}
        }
    }

    screen<ScreenRoute.Main> {
        MainScreen(navController)
    }
}