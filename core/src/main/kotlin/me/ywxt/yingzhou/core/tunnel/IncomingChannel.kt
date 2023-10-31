package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import me.ywxt.yingzhou.core.SocketError

interface IncomingChannel : Closeable {
    suspend fun receivePackets(): Flow<Either<SocketError, ChannelPacket>>
}

interface ChannelPacket {
    val srcAddress: TunnelAddress
    val dstAddress: TunnelAddress

    /**
     * Once the method is called, the packet will be discarded.
     */
    suspend fun copyTo(output: OutgoingChannel): Either<SocketError, Long>
}
