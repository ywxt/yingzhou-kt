package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import arrow.core.right
import io.ktor.network.sockets.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.ywxt.yingzhou.core.SocketError

/**
 * TCP only. UDP is not supported.
 */
class Socks5IncomingChannel(
    private val socket: Socket,
    private val srcAddress: TunnelAddress,
    private val dstAddress: TunnelAddress
) : IncomingChannel {
    override suspend fun receivePackets(): Flow<Either<SocketError, ChannelPacket>> = flow {
        emit(
            Socks5SocketPacket(
                srcAddress,
                dstAddress,
                socket
            ).right()
        )
    }

    override fun close() {
        socket.close()
    }
}

data class Socks5SocketPacket(
    override val srcAddress: TunnelAddress,
    override val dstAddress: TunnelAddress,
    private val socket: Socket
) : ChannelPacket {
    override suspend fun copyTo(output: OutgoingChannel): Either<SocketError, Long> {
        val read = socket.openReadChannel()
        return output.write(read)
    }
}
