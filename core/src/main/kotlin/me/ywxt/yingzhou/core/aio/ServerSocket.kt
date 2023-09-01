package me.ywxt.yingzhou.core.aio

import arrow.core.Either
import kotlinx.coroutines.suspendCancellableCoroutine
import me.ywxt.yingzhou.core.SocketError
import me.ywxt.yingzhou.core.getStackTrace
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.resume

class ServerSocket(private val serverSocketChannel: AsynchronousServerSocketChannel) : AutoCloseable {

    fun bind(address: SocketAddress, port: UShort) = Either.catch {
        serverSocketChannel.bind(InetSocketAddress.createUnresolved(address.toString(), port.toInt()))
    }.mapLeft { SocketError(it.message ?: "", it.stackTrace.toList()) }

    suspend fun accept(): Either<SocketError, AsynchronousSocketChannel> = suspendCancellableCoroutine {
        serverSocketChannel.accept(Unit, object : CompletionHandler<AsynchronousSocketChannel, Unit> {
            override fun completed(result: AsynchronousSocketChannel?, attachment: Unit?) {
                if (result == null) {
                    it.resume(Either.Left(SocketError("socket is null", getStackTrace())))
                } else {
                    it.resume(Either.Right(result))
                }
            }

            override fun failed(exc: Throwable?, attachment: Unit?) {
                it.resume(Either.Left(SocketError(exc?.message ?: "", exc?.stackTrace?.toList() ?: getStackTrace())))
            }
        })
    }

    override fun close() {
        serverSocketChannel.close()
    }
}

