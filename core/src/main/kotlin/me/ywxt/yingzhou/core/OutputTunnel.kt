package me.ywxt.yingzhou.core

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import java.nio.ByteBuffer

sealed interface OutputTunnel : Closeable {
    suspend fun write(channel: ByteReadChannel)
    suspend fun write(bytes: ByteArray)
    suspend fun write(buffer: ByteBuffer)
}
