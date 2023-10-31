package me.ywxt.yingzhou.core

import arrow.core.Either
import io.ktor.utils.io.core.*

interface InputTunnel : Closeable {
    suspend fun receivePacket(): Either<SocketError, TunnelPacket>
}

interface TunnelPacket {
    val srcAddress: TunnelAddress
    val dstAddress: TunnelAddress

    /**
     * Once the method is called, the packet will be discarded.
     */
    suspend fun copyTo(output: OutputTunnel): Either<SocketError, Long>
}
