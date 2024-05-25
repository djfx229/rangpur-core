package io.github.iamfacetheflames.rangpur.core.common.domain

import java.lang.Exception

abstract class Logger {

    enum class Level {
        debug, warning, error
    }

    abstract fun log(level: Level, parentObject: Any, message: String)

    fun d(parentObject: Any, message: String) = log(Level.debug, parentObject, message)

    fun w(parentObject: Any, message: String) = log(Level.warning, parentObject, message)

    fun e(parentObject: Any, message: String) = log(Level.error, parentObject, message)

    fun e(parentObject: Any, exception: Exception) {
        exception.printStackTrace()
        log(Level.error, parentObject, exception.message ?: exception.toString())
    }

}