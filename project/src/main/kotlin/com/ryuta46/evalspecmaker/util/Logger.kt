package com.ryuta46.evalspecmaker.util

class Logger(val tag: String) {
    companion object {
        val LOG_LEVEL_NONE = 0
        val LOG_LEVEL_ERROR = 1
        val LOG_LEVEL_WARN = 2
        val LOG_LEVEL_INFO = 3
        val LOG_LEVEL_DEBUG = 4
        val LOG_LEVEL_VERBOSE = 5

        var level = LOG_LEVEL_NONE
    }

    fun e(message: String) {
        if (level >= LOG_LEVEL_ERROR) println("|ERR|$tag|$message")
    }

    fun w(message: String) {
        if (level >= LOG_LEVEL_WARN) println("|WRN|$tag|$message")
    }

    fun i(message: String) {
        if (level >= LOG_LEVEL_INFO) println("|INF|$tag|$message")
    }

    fun d(message: String) {
        if (level >= LOG_LEVEL_DEBUG) println("|DBG|$tag|$message")
    }

    fun v(message: String) {
        if (level >= LOG_LEVEL_VERBOSE) println("|VRB|$tag|$message")
    }

    inline fun <T> trace(body: () -> T): T {
        val callerName = if (level >= LOG_LEVEL_DEBUG) {
            Throwable().stackTrace[0].methodName
        } else {
            null
        }

        try {
            callerName?.let {
                d("$callerName start")
            }
            return body()
        }
        finally {
            callerName?.let {
                d("$callerName end")
            }
        }
    }

}

