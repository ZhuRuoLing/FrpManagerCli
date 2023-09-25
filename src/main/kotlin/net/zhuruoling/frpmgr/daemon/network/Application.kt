package net.zhuruoling.frpmgr.daemon.network

import io.ktor.server.application.*
import net.zhuruoling.frpmgr.daemon.network.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureRouting()
}
