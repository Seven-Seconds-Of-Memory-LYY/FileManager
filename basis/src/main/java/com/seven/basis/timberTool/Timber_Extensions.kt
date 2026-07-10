package com.seven.basis.timberTool

import timber.log.Timber


fun Timber.Tree.dArgs(vararg args: Any?) {
    d(message = args.joinToString("||"))
}

fun Timber.Tree.iArgs(vararg args: Any?) {
    i(message = args.joinToString("||"))
}

fun Timber.Tree.vArgs(vararg args: Any?) {
    v(message = args.joinToString("||"))
}

fun Timber.Tree.wArgs(vararg args: Any?) {
    w(message = args.joinToString("||"))
}

fun Timber.Tree.eArgs(vararg args: Any?) {
    e(message = args.joinToString("||"))
}