package net.zhuruoling.frpmgr.daemon.frps

import net.zhuruoling.frpmgr.daemon.data.Config
import net.zhuruoling.frpmgr.daemon.frpsAccessToken
import net.zhuruoling.frpmgr.process.FrpCommandBuilder
import kotlin.io.path.*

val paths = listOf("/usr/bin","/bin","/lib", "/opt", Config.dataDirectory.toString())

fun generateFrpsConfig():String{
    return """
        [common]
        bind_port = ${Config.frpsPort}
        bind_addr = 8080
        token = $frpsAccessToken
    """.trimIndent()
}

fun findFrpsExecutable():String{
    for (s in paths) {
        val exec = "$s/frps"
        if (Path(exec).exists()){
            return exec
        }
    }
    throw RuntimeException("Cannot find frps executable.")
}

fun prepareFrpsLaunch():FrpsProcess{
    val config = generateFrpsConfig()
    val configPath = Config.dataDirectory.resolve("config.ini").toPath().apply {
        deleteIfExists()
        createFile()
        writeText(config)
    }
    val executablePath = findFrpsExecutable()
    return FrpsProcess(FrpCommandBuilder(executablePath, configPath.toString()), Config.dataDirectory.toString())
}