package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import arrow.core.raise.either
import io.ktor.utils.io.*
import me.ywxt.yingzhou.core.SocketError
import me.ywxt.yingzhou.core.message
import me.ywxt.yingzhou.core.stackTrace
import java.nio.ByteBuffer

suspend inline fun ByteReadChannel.readByteEither(): Either<SocketError, Byte> =
    Either.catch { readByte() }.mapLeft { e: Throwable ->
        (SocketError(e.message("The channel is closed unexpectedly"), e.stackTrace()))
    }

suspend inline fun ByteReadChannel.readBytesEither(
    count: Int
): Either<SocketError, ByteBuffer> = either {
    if (count <= 0) raise(SocketError("The count reading socket must be greater than 0"))
    val buffer = ByteBuffer.allocate(count)
    val length = Either.catch { readFully(buffer) }.mapLeft { e: Throwable ->
        (SocketError(e.message("The channel is closed unexpectedly"), e.stackTrace()))
    }.bind()
    if (length != count) raise(SocketError("The channel is closed unexpectedly"))
    buffer.flip()
}

suspend inline fun ByteReadChannel.readShortEither(): Either<SocketError, Short> =
    Either.catch { readShort() }.mapLeft { e: Throwable ->
        (SocketError(e.message("The channel is closed unexpectedly"), e.stackTrace()))
    }

suspend inline fun ByteWriteChannel.writeByteEither(byte: Byte): Either<SocketError, Unit> =
    Either.catch { writeByte(byte) }.mapLeft { e: Throwable ->
        (SocketError(e.message("The channel is closed unexpectedly"), e.stackTrace()))
    }

suspend inline fun ByteWriteChannel.writeBytesEither(bytes: ByteArray): Either<SocketError, Unit> =
    Either.catch { writeFully(bytes) }.mapLeft { e: Throwable ->
        (SocketError(e.message("The channel is closed unexpectedly"), e.stackTrace()))
    }

suspend inline fun ByteWriteChannel.writeShortEither(short: Short): Either<SocketError, Unit> =
    Either.catch { writeShort(short) }.mapLeft { e: Throwable ->
        (SocketError(e.message("The channel is closed unexpectedly"), e.stackTrace()))
    }
