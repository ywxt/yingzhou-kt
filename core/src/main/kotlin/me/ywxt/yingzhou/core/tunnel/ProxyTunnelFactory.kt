package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import io.ktor.network.sockets.*
import me.ywxt.yingzhou.core.SocketError

object ProxyTunnelFactory {
    suspend fun bind(socket: Socket): Either<SocketError, ProxyTunnel> = TODO("Not yet implemented. $socket")
}
