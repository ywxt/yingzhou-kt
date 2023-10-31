package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import me.ywxt.yingzhou.core.SocketError
import java.nio.ByteBuffer

sealed interface OutgoingChannel : Closeable {
    suspend fun write(channel: ByteReadChannel): Either<SocketError, Long>
    suspend fun write(bytes: ByteArray): Either<SocketError, Long>
    suspend fun write(buffer: ByteBuffer): Either<SocketError, Long>
}
