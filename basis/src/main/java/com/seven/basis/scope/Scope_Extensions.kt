package com.seven.basis.scope

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Basis default scope 全局模式默认协程
 */
val BasisDefaultScope = CoroutineScope(Dispatchers.Default + SupervisorJob())


/**
 * Basis io scope 全局模式IO协程
 */
val BasisIoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())