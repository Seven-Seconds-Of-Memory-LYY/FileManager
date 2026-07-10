package com.seven.file.manager.screenRoute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController

/**
 * Pop back stack with result 返回，携带数据
 *
 * @param T 数据对象类型
 * @param key 键值
 * @param result 返回数据对象
 */
fun <T : Any> NavController.popBackStackWithResult(
    key: String,
    result: T
) {
    previousBackStackEntry?.savedStateHandle?.set(key, result)
    popBackStack()
}

/**
 * Pop back stack with result 返回，携带数据
 *
 * @param pairs 键值对
 */
fun NavController.popBackStackWithResult(vararg pairs: Pair<String, Any?>) {
    val handle = previousBackStackEntry?.savedStateHandle
    pairs.forEach { (key, value) ->
        handle?.set(key, value)
    }
    popBackStack()
}

/**
 * Observe nav result 监听NavController，返回当前页面携带数据
 *
 * @param T 数据类型
 * @param navController
 * @param key 键值
 * @param onResult 结果操作
 */
@Composable
fun <T> ObserveNavControllerResult(
    navController: NavController,
    key: String,
    onResult: (T) -> Unit
) {
    val navBackStackEntry = navController.currentBackStackEntry
    val resultFlow = navBackStackEntry?.savedStateHandle?.getStateFlow<T?>(key, null)
    val result by resultFlow?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(result) {
        result?.let {
            onResult(it)
            // 拿到结果后立即清理 Key，防止配置更改或重组时重复触发
            navBackStackEntry?.savedStateHandle?.remove<T>(key)
        }
    }
}