package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import io.ktor.network.sockets.*
import me.ywxt.yingzhou.core.SocketError

class Socks5ProxyTunnel(private val socket: Socket) : ProxyTunnel {
    override suspend fun upgrade(): Either<SocketError, Socks5IncomingChannel> {
        TODO("Not yet implemented")
    }

    override fun close() {
        socket.close()
    }
}
