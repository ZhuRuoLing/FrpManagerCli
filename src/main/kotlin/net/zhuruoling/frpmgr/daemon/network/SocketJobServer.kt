package net.zhuruoling.frpmgr.daemon.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.zhuruoling.frpmgr.daemon.Job
import net.zhuruoling.frpmgr.daemon.data.Config
import net.zhuruoling.frpmgr.daemon.scheduleJob
import net.zhuruoling.frpmgr.util.EncryptedSocket
import net.zhuruoling.frpmgr.util.toJson
import net.zhuruoling.frpmgr.util.toObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter
import java.net.ServerSocket

class SocketJobServer : Thread("SocketJobServer") {
    private val logger: Logger = LoggerFactory.getLogger("SessionLoginServer")
    val selectorManager = ActorSelectorManager(Dispatchers.IO)
    private val connections = mutableListOf<EncryptedSocket>()
    override fun run() {
        try {
            runBlocking {
                val serverSocket = aSocket(selectorManager).tcp().bind(port = Config.jobPort)
                while (true) {
                    val socket = serverSocket.accept()
                    logger.info("$socket connected.")
                    launch {
                        val encryptedSocket = EncryptedSocket(socket, Config.jobKey)
                        connections += encryptedSocket
                        try {
                            while (true) {
                                val line = encryptedSocket.readLine()
                                if (line == null) {
                                    socket.close()
                                    break
                                }
                                val result = handleRequest(line.toObject(JobRequest::class.java), encryptedSocket);
                                encryptedSocket.send(result.toJson())
                            }
                        } catch (e: Throwable) {
                            socket.close()
                        }
                        connections -= encryptedSocket
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun handleRequest(request: JobRequest, encryptedSocket: EncryptedSocket): JobResponse {
        return when (request.type) {
            RequestType.RUN_COMMAND -> {
                val jobCommand = request.args["command"]
                if (jobCommand == null) {
                    JobResponse(false, ResponseType.JOB_RESPONSE, "Missing command", mutableMapOf())
                }
                scheduleJob(Job("SocketJobServer", jobCommand!!) {
                    runBlocking {
                        launch {
                            encryptedSocket.send(
                                JobResponse(
                                    it == -1,
                                    ResponseType.MESSAGE,
                                    results.toJson().encodeBase64(),
                                    mutableMapOf()
                                ).toJson()
                            )
                        }
                    }
                })
                JobResponse(true, ResponseType.JOB_RESPONSE, "Job scheduled.", mutableMapOf())
            }
        }
    }


    fun sendToAllConnections(response: JobResponse) {
        runBlocking {
            for (connection in connections) {
                connection.send(response.toJson())
            }
        }
    }

}

enum class RequestType {
    RUN_COMMAND
}

enum class ResponseType {
    PROCESS_OUTPUT, MESSAGE, JOB_RESPONSE
}

data class JobRequest(val type: RequestType, val args: Map<String, String>)

data class JobResponse(val status: Boolean, val type: ResponseType, val message: String, val args: Map<String, String>)