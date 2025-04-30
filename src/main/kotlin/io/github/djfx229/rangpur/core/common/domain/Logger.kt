package io.github.djfx229.rangpur.core.common.domain

import java.lang.Exception

abstract class Logger {

    enum class Level {
        Debug,
        Warning,
        Error,
    }

    abstract fun log(level: Level, parentObject: Any, message: String)

    fun d(parentObject: Any, message: String) = log(Level.Debug, parentObject, message)

    fun w(parentObject: Any, message: String) = log(Level.Warning, parentObject, message)

    fun e(parentObject: Any, message: String) = log(Level.Error, parentObject, message)

    fun e(parentObject: Any, exception: Exception) {
        exception.printStackTrace()
        log(Level.Error, parentObject, exception.message ?: exception.toString())
    }

}