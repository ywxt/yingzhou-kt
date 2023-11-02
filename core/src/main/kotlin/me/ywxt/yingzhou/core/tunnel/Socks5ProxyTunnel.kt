package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import me.ywxt.yingzhou.core.SocketError

private const val SOCKS5_VERSION: Byte = 0x05
private const val SOCKS5_AUTH_METHOD_NO_AUTHENTICATION_REQUIRED: Byte = 0x00
private const val SOCKS5_AUTH_METHOD_NO_ACCEPTABLE_METHODS: Byte = -0x7F

private const val SOCKS5_CMD_CONNECT: Byte = 0x01

private const val SOCKS5_ADDRESS_TYPE_IPV4: Byte = 0x01
private const val SOCKS5_ADDRESS_TYPE_DOMAIN_NAME: Byte = 0x03
private const val SOCKS5_ADDRESS_TYPE_IPV6: Byte = 0x04

private const val SOCKS5_RESPONSE_SUCCESS: Byte = 0x00
private val SOCKS5_RESPONSE_ADDRESS: ByteArray = byteArrayOf(0x00, 0x00, 0x00, 0x00)
private const val SOCKS5_RESERVED: Byte = 0x00
private const val SOCKS5_RESPONSE_PORT: Short = 0x00

private const val SOCKS5_IPV6_LENGTH = 16
private const val SOCKS5_IPV4_LENGTH = 4
private const val SOCKS5_IPV6_RADIX = 16

class Socks5ProxyTunnel(private val socket: Socket) : ProxyTunnel {
    override suspend fun upgrade(): Either<SocketError, Socks5IncomingChannel> = either {
        val readChannel = socket.openReadChannel()
        val writeChannel = socket.openWriteChannel(autoFlush = true)
        val methods = clientGreet(readChannel).bind()
        if (!methods.contains(SOCKS5_AUTH_METHOD_NO_AUTHENTICATION_REQUIRED)) {
            serverChoose(writeChannel, SOCKS5_AUTH_METHOD_NO_ACCEPTABLE_METHODS).bind()
            raise(SocketError("SOCKS5 authentication method is not supported. Methods: $methods"))
        }
        serverChoose(writeChannel, SOCKS5_AUTH_METHOD_NO_AUTHENTICATION_REQUIRED).bind()
        val address = clientRequest(readChannel).bind()
        serverResponse(writeChannel).bind()
        Socks5IncomingChannel(socket, socket.remoteAddress.toTunnelAddress().bind(), address)
    }

    private suspend fun clientGreet(readChannel: ByteReadChannel): Either<SocketError, Set<Byte>> = either {
        readSocks5Version(readChannel).bind()
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

    private suspend fun clientRequest(readChannel: ByteReadChannel): Either<SocketError, TunnelAddress> = either {
        readSocks5Version(readChannel).bind()
        val command = readChannel.readByteEither().bind()
        if (command != SOCKS5_CMD_CONNECT) {
            raise(SocketError("SOCKS5 command is not supported. Command: $command"))
        }
        val reserved = readChannel.readByteEither().bind()
        if (reserved != SOCKS5_RESERVED) {
            raise(SocketError("SOCKS5 reserved is not 0x00. Reserved: $reserved"))
        }
        val address = when (val addressType = readChannel.readByteEither().bind()) {
            SOCKS5_ADDRESS_TYPE_IPV4 -> {
                val ipv4 = readChannel.readBytesEither(SOCKS5_IPV4_LENGTH).bind()
                val port = readChannel.readShortEither().bind()
                TunnelAddress(TunnelIPv4Host(ipv4.array().joinToString(".")), port.toInt())
            }

            SOCKS5_ADDRESS_TYPE_DOMAIN_NAME -> {
                val domainNameLength = readChannel.readByteEither().bind()
                val domainName = readChannel.readBytesEither(domainNameLength.toInt()).bind()
                val port = readChannel.readShortEither().bind()
                TunnelAddress(TunnelDomainHost(domainName.array().toString(Charsets.US_ASCII)), port.toInt())
            }

            SOCKS5_ADDRESS_TYPE_IPV6 -> {
                val ipv6 = readChannel.readBytesEither(SOCKS5_IPV6_LENGTH).bind()
                val port = readChannel.readShortEither().bind()
                TunnelAddress(
                    TunnelIPv6Host(ipv6.array().joinToString(":") { it.toString(SOCKS5_IPV6_RADIX) }),
                    port.toInt()
                )
            }

            else -> {
                raise(SocketError("SOCKS5 address type is not supported. Address type: $addressType"))
            }
        }
        address
    }

    private suspend fun serverResponse(writeChannel: ByteWriteChannel): Either<SocketError, Unit> = either {
        writeChannel.writeByteEither(SOCKS5_VERSION).bind()
        writeChannel.writeByteEither(SOCKS5_RESPONSE_SUCCESS).bind() // succeeded
        writeChannel.writeByteEither(SOCKS5_RESERVED) // reserved
        writeChannel.writeByteEither(SOCKS5_ADDRESS_TYPE_IPV4).bind()
        writeChannel.writeBytesEither(SOCKS5_RESPONSE_ADDRESS).bind()
        writeChannel.writeShortEither(SOCKS5_RESPONSE_PORT)
    }

    override fun close() {
        socket.close()
    }

    private suspend fun readSocks5Version(readChannel: ByteReadChannel): Either<SocketError, Unit> =
        readChannel.readByteEither().flatMap { version ->
            if (version != SOCKS5_VERSION) {
                SocketError("SOCKS5 version is not 5").left()
            } else {
                Unit.right()
            }
        }
}
