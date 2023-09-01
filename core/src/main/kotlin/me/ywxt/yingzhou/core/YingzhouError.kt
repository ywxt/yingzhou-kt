package me.ywxt.yingzhou.core

import arrow.core.Either

class Panic(val error: YingzhouError) : Error(error.toString())

inline fun <reified L : YingzhouError, reified R> Either<L, R>.unwrap(): R = when (this) {
    is Either.Left -> throw Panic(this.value)
    is Either.Right -> this.value
}

@Suppress("NOTHING_TO_INLINE")
inline fun getStackTrace(): List<StackTraceElement> = Thread.currentThread().stackTrace.toList()

sealed class YingzhouError(val message: String, val stackTrace: List<StackTraceElement>) {
    override fun toString(): String = "YingzhouError: $message\n${stackTrace.joinToString("\n")}"
}

class SocketError(message: String, stackTrace: List<StackTraceElement>) : YingzhouError(message, stackTrace)
