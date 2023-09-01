package me.ywxt.yingzhou.core.aio

import arrow.core.None
import arrow.core.Option
import arrow.core.Some


sealed class SocketAddress(val bytes: ByteArray) {
    abstract override fun toString(): String

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SocketAddress) return false

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    class IPv4 internal constructor(bytes: ByteArray) : SocketAddress(bytes) {
        override fun toString(): String {
            return "IPv4(${bytes.joinToString(".")})"
        }
    }

    class IPv6 internal constructor(bytes: ByteArray) : SocketAddress(bytes) {
        override fun toString(): String {
            return "IPv6(${bytes.joinToString(":")})"
        }
    }

    companion object {
        private val ipv4Regex = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
        private val ipv6Regex = Regex("""^(\d|[a-fA-F]){1,4}(:(\d|[a-fA-F]){1,4}){7}$""")
        fun IPv4(a: Byte, b: Byte, c: Byte, d: Byte): IPv4 =
            IPv4(byteArrayOf(a, b, c, d))

        fun IPv6(a: Byte, b: Byte, c: Byte, d: Byte, e: Byte, f: Byte, g: Byte, h: Byte): IPv6 = IPv6(
            byteArrayOf(a, b, c, d, e, f, g, h)
        )

        /**
         * Convert a string to IPv4 or IPv6
         */
        fun fromString(address: String): Option<SocketAddress> {
            return when {
                ipv4Regex.matches(address) -> {
                    val bytes = address.split(".").map { it.toInt().toByte() }.toByteArray()
                    Some(IPv4(bytes))
                }

                ipv6Regex.matches(address) -> {
                    val bytes = address.split(":").map { it.toInt(16).toByte() }.toByteArray()
                    Some(IPv6(bytes))
                }

                else -> None
            }
        }

    }
}