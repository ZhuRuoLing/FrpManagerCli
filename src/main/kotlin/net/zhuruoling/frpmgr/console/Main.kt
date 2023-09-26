package net.zhuruoling.frpmgr.console

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.zhuruoling.frpmgr.daemon.network.JobRequest
import net.zhuruoling.frpmgr.daemon.network.JobResponse
import net.zhuruoling.frpmgr.daemon.network.RequestType
import net.zhuruoling.frpmgr.daemon.network.ResponseType
import net.zhuruoling.frpmgr.util.EncryptedSocket
import net.zhuruoling.frpmgr.util.toJson
import net.zhuruoling.frpmgr.util.toObject
import java.io.InputStream
import java.util.*
import kotlin.system.exitProcess

val stdin: InputStream = System.`in`
private val selectorManager = ActorSelectorManager(Dispatchers.IO)
private fun InputStream.lines() = Scanner(this).lines()

private fun Scanner.lines() = sequence {
    while (hasNext()) {
        yield(readlnOrNull())
    }
}

fun main(args: Array<String>) {
    val jobPort = args[args.indexOf("--port") + 1].toInt()
    val jobHost = args[args.indexOf("--host") + 1]
    val token = args[args.indexOf("--token") + 1]
    runBlocking {
        val socket = aSocket(selectorManager).tcp().connect(jobHost, port = jobPort)
        val encryptedSocket = EncryptedSocket(socket, token)

        launch(Dispatchers.IO) {
            while (true) {
                val line = encryptedSocket.readLine()
                if (line == null){
                    println("SOCKET: Read null")
                    println("Exiting.")
                    socket.close()
                    exitProcess(1)
                    return@launch
                }
                val response = line.toObject(JobResponse::class.java)
                when(response.type){
                    ResponseType.PROCESS_OUTPUT -> {
                        println("[frpc] ${response.message}")
                    }
                    ResponseType.MESSAGE -> {
                        val messages = response.message.decodeBase64String().toObject(Array<String>::class.java)
                        messages.forEach {
                            println(it)
                        }
                    }
                    ResponseType.JOB_RESPONSE -> {
                        if (response.status) {
                            println("[JOB] ${response.message}")
                        }else{
                            println("[JOB] E: ${response.message}")
                        }
                    }
                }
            }
        }
        println("--> ")
        for (line in stdin.lines()) {
            if (line == null) exitProcess(1)
            val request = JobRequest(RequestType.RUN_COMMAND, buildMap {
                this["command"] = line
            })
            encryptedSocket.send(request.toJson())
        }
    }
}