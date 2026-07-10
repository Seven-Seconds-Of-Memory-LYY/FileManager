package com.seven.file.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.seven.basis.dimens.ProvideAdaptiveDimens
import com.seven.file.manager.screenRoute.LocalScreenConfig
import com.seven.file.manager.screenRoute.ScreenRoute
import com.seven.file.manager.screenRoute.registerAllScreens
import com.seven.file.manager.toast.GlobalToastHost
import com.seven.file.manager.ui.components.KeepScreenOn
import com.seven.file.manager.ui.theme.FileManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { false }
        enableEdgeToEdge()
        setContent {
            ProvideAdaptiveDimens {
                KeepScreenOn()
                FileManagerTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainApp(ScreenRoute.Main)

                        GlobalToastHost()
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp(startScreen: ScreenRoute) {
    val navController = rememberNavController()

    // 初始化一个状态，默认为 Splash 或 Home
    val currentScreenState = remember { mutableStateOf(startScreen) }

    CompositionLocalProvider(LocalScreenConfig provides currentScreenState) {
        val screenAnimationSpec = 350
        NavHost(
            navController = navController,
            startDestination = startScreen,
            // 1. 新页面进入动画：从右侧推入
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(screenAnimationSpec)
                )
            },
            // 2. 当前页面离开动画：保持静止 (无位移)
            exitTransition = {
                ExitTransition.None
            },
            // 3. 弹栈（回退）时，新页面（原本在下层的页面）进入动画：保持静止
            popEnterTransition = {
                EnterTransition.None
            },
            // 4. 弹栈（回退）时，当前页面离开动画：向右侧切出
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(screenAnimationSpec)
                )
            }
        ) {
            registerAllScreens(navController = navController)
        }
    }
}
