package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import me.ywxt.yingzhou.core.SocketError

private const val SOCKS5_VERSION: Byte = 0x05
private const val SOCKS5_AUTH_METHOD_NO_AUTHENTICATION_REQUIRED: Byte = 0x00
private const val SOCKS5_AUTH_METHOD_NO_ACCEPTABLE_METHODS: Byte = -0x7F

class Socks5ProxyTunnel(private val socket: Socket) : ProxyTunnel {
    override suspend fun upgrade(): Either<SocketError, Socks5IncomingChannel> = either {
        val readChannel = socket.openReadChannel()
        val writeChannel = socket.openWriteChannel(autoFlush = true)
        val methods = clientGreet(readChannel).bind()
        if (!methods.contains(SOCKS5_AUTH_METHOD_NO_AUTHENTICATION_REQUIRED)) {
            serverChoose(writeChannel, SOCKS5_AUTH_METHOD_NO_ACCEPTABLE_METHODS).bind()
            raise(SocketError("SOCKS5 authentication method is not supported"))
        }
        serverChoose(writeChannel, SOCKS5_AUTH_METHOD_NO_AUTHENTICATION_REQUIRED).bind()

        Socks5IncomingChannel(socket, TunnelAddress("", "", 0), TunnelAddress("", "", 0))
    }

    private suspend fun clientGreet(readChannel: ByteReadChannel): Either<SocketError, Set<Byte>> = either {
        val version = readChannel.readByteEither().bind()
        if (version != SOCKS5_VERSION) {
            raise(SocketError("SOCKS5 version is not 5"))
        }
        val methodCount = readChannel.readByteEither().bind()
        val methods = mutableSetOf<Byte>()
        repeat(methodCount.toInt()) {
            methods.add(readChannel.readByteEither().bind())
        }
        methods
    }

    private suspend fun serverChoose(writeChannel: ByteWriteChannel, method: Byte): Either<SocketError, Unit> =
        writeChannel.writeByteEither(SOCKS5_VERSION).flatMap {
            writeChannel.writeByteEither(method)
        }

    override fun close() {
        socket.close()
    }
}
