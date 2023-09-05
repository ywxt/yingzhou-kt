package me.ywxt.yingzhou.core

import arrow.core.Either
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.SocketAddress

sealed interface Tunnel {
    suspend fun close(): Either<SocketError, Unit>
}

interface TcpTunnel : Tunnel {
    val localPort: Int
    val remotePort: Int
    val remoteHost: String

    suspend fun write(buffer: Buffer): Either<SocketError, Unit>
    suspend fun read(): Either<SocketError, Buffer>
}

interface UdpTunnel : Tunnel {
    val localPort: Int

    suspend fun write(buffer: Buffer, dist: SocketAddress): Either<SocketError, Unit>

    suspend fun read(): Either<SocketError, Pair<Buffer, SocketAddress>>
}

sealed interface TunnelFactory<T : Tunnel> {
    suspend fun create(socketAddress: SocketAddress): Either<SocketError, T>
}
