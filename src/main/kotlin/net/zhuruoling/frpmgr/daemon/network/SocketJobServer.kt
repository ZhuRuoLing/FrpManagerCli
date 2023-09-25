package net.zhuruoling.frpmgr.daemon.network

import net.zhuruoling.frpmgr.daemon.data.Config
import net.zhuruoling.frpmgr.util.EncryptedSocket
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter
import java.net.ServerSocket

class SocketJobServer() : Thread("SocketJobServer") {
    private val logger: Logger = LoggerFactory.getLogger("SessionLoginServer")
    override fun run() {
        try {
            ServerSocket(Config.jobPort).use {
                logger.info("Started SocketJobServer at ${Config.jobPort}.")
                while (true) {
                    val socket = it.accept()
                    logger.debug(socket.getKeepAlive().toString())
                    socket.setKeepAlive(true)
                    JobSession(
                        EncryptedSocket(
                            socket.getInputStream().bufferedReader(),
                            PrintWriter(socket.getOutputStream().writer()),
                            Config.jobKey
                        )
                    ).start()
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}