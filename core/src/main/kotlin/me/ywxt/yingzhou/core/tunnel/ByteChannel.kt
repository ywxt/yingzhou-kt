package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import io.ktor.utils.io.*
import me.ywxt.yingzhou.core.SocketError
import me.ywxt.yingzhou.core.stackTrace

suspend inline fun ByteReadChannel.readByteEither(): Either<SocketError, Byte> =
    Either.catch { readByte() }.mapLeft { e: Throwable ->
        (SocketError("The channel is closed unexpectedly", e.stackTrace()))
    }

suspend inline fun ByteWriteChannel.writeByteEither(byte: Byte): Either<SocketError, Unit> =
    Either.catch { writeByte(byte) }.mapLeft { e: Throwable ->
        (SocketError("The channel is closed unexpectedly", e.stackTrace()))
    }
