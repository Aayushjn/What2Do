package com.aayush.what2do.util.logging

import android.util.Log
import timber.log.Timber.Tree

class ProdLogTree: Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (isLoggable(tag, priority)) {
            e(t, message)
        }
    }

    override fun isLoggable(tag: String?, priority: Int): Boolean = when(priority) {
        Log.ERROR -> true
        else -> false
    }
}