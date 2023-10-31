package me.ywxt.yingzhou.core.tunnel

import arrow.core.Either
import io.ktor.utils.io.core.*
import me.ywxt.yingzhou.core.SocketError

interface ProxyTunnel : Closeable {
    suspend fun upgrade(): Either<SocketError, IncomingChannel>
}
