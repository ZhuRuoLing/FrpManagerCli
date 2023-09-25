package net.zhuruoling.frpmgr

import net.zhuruoling.frpmgr.util.BuildProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("Main")
fun main(args: Array<String>) {
    logger.info("Hello World!")
    val os = ManagementFactory.getOperatingSystemMXBean()
    val runtime = ManagementFactory.getRuntimeMXBean()
    val version = BuildProperties["version"]
    val buildTimeMillis = BuildProperties["buildTime"]?.toLong() ?: 0L
    val buildTime = Date(buildTimeMillis)
    val versionInfoString = "FrpManagerCli $version (${BuildProperties["branch"]}:${
        BuildProperties["commitId"]?.substring(0, 7)
    } $buildTime)"
    logger.info("$versionInfoString is running on ${os.name} ${os.arch} ${os.version} at pid ${runtime.pid}")
}