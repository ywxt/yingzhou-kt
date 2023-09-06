package me.ywxt.yingzhou.core

import arrow.core.Either
import arrow.core.right
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import io.vertx.kotlin.coroutines.toReceiveChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ReadStreamChannel(private val channel: ReceiveChannel<Buffer>) : ReceiveChannel<Buffer> by channel {

    private val mutex = Mutex()
    private var buffer: Buffer? = null

    @Volatile
    private var bufferOffset = 0

    override suspend fun receive(): Buffer = mutex.withLock {
        checkBuffer()
        val offset = bufferOffset
        bufferOffset = buffer!!.length()
        return buffer!!.slice(offset, buffer!!.length())
    }

    private suspend fun readByte0(): Byte {
        checkBuffer()
        val offset = bufferOffset
        bufferOffset = offset + 1
        return buffer!!.getByte(offset)
    }

    suspend fun readByte(): Byte = mutex.withLock {
        readByte0()
    }

    private suspend fun readShort0(): Short {
        return (readByte0().toInt() shl Byte.SIZE_BITS or readByte0().toInt()).toShort()
    }

    suspend fun readShort(): Short = mutex.withLock {
        readShort0()
    }

    private suspend fun readInt0(): Int {
        return (readShort0().toInt() shl Short.SIZE_BITS or readShort0().toInt())
    }

    suspend fun readInt(): Int = mutex.withLock {
        readInt0()
    }

    private suspend fun readLong0(): Long {
        return (readInt0().toLong() shl Int.SIZE_BITS or readInt0().toLong())
    }

    suspend fun readLong(): Long = mutex.withLock {
        readLong0()
    }

    suspend fun readBytes(length: Int): Either<IoError, ByteArray> = mutex.withLock {
        if (length < 0) return Either.Left(IoError("length must be positive"))
        val bytes = ByteArray(length)
        var readLength = 0
        while (readLength < length) {
            checkBuffer()
            val bufferLength = buffer!!.length()
            val read = minOf(length - readLength, bufferLength - bufferOffset)
            buffer!!.getBytes(bufferOffset, bufferOffset + read, bytes, readLength)
            readLength += read
            bufferOffset += read
        }
        return bytes.right()
    }

    private suspend fun checkBuffer() {
        if (buffer == null || bufferOffset >= buffer!!.length()) {
            buffer = channel.receive()
            bufferOffset = 0
        }
    }
}

fun ReadStream<Buffer>.toReadStreamChannel(vertx: Vertx): ReadStreamChannel {
    return ReadStreamChannel(this.toReceiveChannel(vertx))
}

fun ReadStream<Buffer>.toReadStreamChannel(context: Context): ReadStreamChannel {
    return ReadStreamChannel(this.toReceiveChannel(context))
}
