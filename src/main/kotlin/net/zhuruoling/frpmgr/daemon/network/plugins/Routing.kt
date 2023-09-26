package net.zhuruoling.frpmgr.daemon.network.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import net.zhuruoling.frpmgr.daemon.data.Config
import net.zhuruoling.frpmgr.daemon.data.NodeData
import net.zhuruoling.frpmgr.daemon.frpsAccessToken
import net.zhuruoling.frpmgr.daemon.network.HttpResponse
import net.zhuruoling.frpmgr.util.gson
import net.zhuruoling.frpmgr.util.toJson

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("FrpManagerCli")
        }
        get("/config/{nodeId?}") {
            val nodeId = call.parameters["nodeId"] ?: return@get call.respondText(
                HttpResponse(false, buildMap {
                    this["message"] = "Missing nodeId"
                }).toJson(),
                status = HttpStatusCode.BadRequest
            )
            if (nodeId in NodeData) {
                call.respondText(
                    text = HttpResponse(false, buildMap {
                        this += "nodeData" to NodeData[nodeId].toJson().encodeBase64()
                        this += "accessToken" to frpsAccessToken
                        this += "port" to Config.frpsPort.toString()
                    }).toJson(),
                    status = HttpStatusCode.OK
                )
            }
        }
    }
}
