package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.network.sockets.*
import me.ywxt.yingzhou.core.SocketError
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress

private const val IPV6_RADIX = 16

data class TunnelAddress(val host: TunnelHost, val port: Int)

fun SocketAddress.toTunnelAddress(): Either<SocketError, TunnelAddress> {
    val javaAddress = this.toJavaAddress()
    when (javaAddress) {
        is InetSocketAddress -> {
            when (val address = javaAddress.address) {
                is Inet4Address -> TunnelAddress(
                    TunnelIPv4Host(address.address.joinToString(".")),
                    javaAddress.port
                ).right()

                is Inet6Address -> TunnelAddress(
                    TunnelIPv6Host(address.address.joinToString(":") { it.toString(IPV6_RADIX) }),
                    javaAddress.port
                ).right()
            }
        }
    }
    return SocketError("Unsupported address type: ${javaAddress::class.java.name}").left()
}

sealed class TunnelHost

data class TunnelIPv4Host(val host: String) : TunnelHost()

data class TunnelIPv6Host(val host: String) : TunnelHost()

data class TunnelDomainHost(val host: String) : TunnelHost()
