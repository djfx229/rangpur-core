package io.github.iamfacetheflames.rangpur.data

import java.io.Serializable

class Result<out T>(
    val value: Any?
) {

    companion object {
        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("waiting")
        fun <T> waiting(): Result<T> =
            Result(Waiting())

        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("success")
        fun <T> success(value: T): Result<T> =
            Result(value)

        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("failure")
        fun <T> failure(exception: Throwable): Result<T> =
            Result(
                Failure(exception)
            )
    }

    val isSuccess: Boolean get() = value !is Failure && value !is Waiting
    val isFailure: Boolean get() = value is Failure
    val isWaiting: Boolean get() = value is Waiting

    fun getOrNull(): T? =
        when {
            isFailure -> null
            else -> value as T
        }

    internal class Failure(
        @JvmField
        val exception: Throwable
    ) : Serializable {
        override fun equals(other: Any?): Boolean = other is Failure && exception == other.exception
        override fun hashCode(): Int = exception.hashCode()
        override fun toString(): String = "Failure($exception)"
    }

    internal class Waiting : Serializable {
        override fun equals(other: Any?): Boolean = other is Waiting
        override fun hashCode(): Int = super.hashCode()
        override fun toString(): String = "Waiting()"
    }

}