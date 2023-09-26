package net.zhuruoling.frpmgr.node;

import io.ktor.util.*
import net.zhuruoling.frpmgr.daemon.data.NodeDataStorage
import net.zhuruoling.frpmgr.daemon.frps.paths
import net.zhuruoling.frpmgr.process.FrpCommandBuilder
import net.zhuruoling.frpmgr.process.FrpProcess
import net.zhuruoling.frpmgr.util.toObject
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import kotlin.io.path.*
import kotlin.system.exitProcess

lateinit var frpProcess: FrpProcess
private val logger = LoggerFactory.getLogger("NodeMain")


fun generateFrpcConfig(node: NodeDataStorage, accessToken: String, serverIp: String, serverPort: String):String {
    var s = """
        [common]
        server_addr = $serverIp
        server_port = $serverPort
        token = $accessToken
        user = ${node.nodeName}
        
    """.trimIndent()
    node.transports.forEach {
        s += """
            [${it.name}]
            type = ${it.protocol.name.toLowerCasePreservingASCIIRules()}
            local_ip = 127.0.0.1
            local_port = ${it.localPort}
            remote_port = ${it.remotePort}
        """.trimIndent()
    }
    return s
}

fun findFrpcExecutable(): String {
    for (s in paths) {
        val exec = "$s/frpc"
        if (Path(exec).exists()) {
            return exec
        }
    }
    throw RuntimeException("Cannot find frpc executable.")
}

fun prepareFrpcLaunch(config:String, nodeId:String): FrpProcess {
    val exec = findFrpcExecutable()
    val frpcConfig = dataDirectory.resolve("frpc-$nodeId.ini").apply {
        deleteIfExists()
        createParentDirectories()
        createFile()
        writeText(config)
    }
    return FrpProcess(FrpCommandBuilder(exec.toString(), frpcConfig.toString()), dataDirectory.toString()){
        println(it)
    }
}
fun httpGet(url: String): Pair<Int, String> {
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString(Charset.defaultCharset()))
    return response.statusCode() to response.body()
}

var dataDirectory = Path("/etc/frpmgr")

fun main(args: Array<String>) {
    val daemonAddress = args[args.indexOf("--daemonAddress") + 1]
    val daemonPort = args[args.indexOf("--daemonPort") + 1]
    val nodeId = args[args.indexOf("--nodeId") + 1]
    val dataDirIndex = args.indexOf("--nodeId")
    if (dataDirIndex != -1){
        dataDirectory = Path(args[dataDirIndex + 1])
    }
    val (status,response) = httpGet("http://$daemonAddress:$daemonPort/config/$nodeId")
    val httpResponse = response.toObject(net.zhuruoling.frpmgr.daemon.network.HttpResponse::class.java)
    if (status != 200){
        logger.error("Remote daemon returned status $status")
        logger.error("Remote: ${httpResponse.messages["message"]}")
        exitProcess(1)
    }
    logger.info("Retrieved frpc config.")
    val accessToken = httpResponse.messages["accessToken"]!!
    val nodeData = httpResponse.messages["nodeData"]!!.decodeBase64String().toObject(NodeDataStorage::class.java)
    val port = httpResponse.messages["port"]!!
    logger.info("Token: $accessToken")
    logger.info("Port: $port")
    val config = generateFrpcConfig(nodeData, accessToken, daemonAddress, port)
    frpProcess = prepareFrpcLaunch(config, nodeId)
    frpProcess.start()
}