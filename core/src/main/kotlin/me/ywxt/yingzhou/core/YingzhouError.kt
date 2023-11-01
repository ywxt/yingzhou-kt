package me.ywxt.yingzhou.core

import arrow.core.Either

class Panic(val error: YingzhouError) : Error(error.toString())

inline fun <reified L : YingzhouError, reified R> Either<L, R>.unwrap(): R = when (this) {
    is Either.Left -> throw Panic(this.value)
    is Either.Right -> this.value
}

@Suppress("NOTHING_TO_INLINE")
inline fun getStackTrace(): List<StackTraceElement> = Thread.currentThread().stackTrace.drop(1)

@Suppress("NOTHING_TO_INLINE")
inline fun Throwable?.stackTrace(): List<StackTraceElement> = this?.stackTrace?.toList() ?: getStackTrace()

inline fun Throwable?.message(f: () -> String): String = this?.message ?: f()

sealed class YingzhouError(val message: String, val stackTrace: List<StackTraceElement>) {
    override fun toString(): String = "YingzhouError: $message\n${stackTrace.joinToString("\n")}"
}

class SocketError(message: String, stackTrace: List<StackTraceElement>) : YingzhouError(message, stackTrace) {
    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(message: String): SocketError = SocketError(message, getStackTrace())
    }
}

class IoError(message: String, stackTrace: List<StackTraceElement>) : YingzhouError(message, stackTrace) {
    companion object {

        @Suppress("NOTHING_TO_INLINE")
        inline operator fun invoke(message: String): IoError = IoError(message, getStackTrace())
    }
}
