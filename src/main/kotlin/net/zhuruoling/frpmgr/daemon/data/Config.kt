package net.zhuruoling.frpmgr.daemon.data

import java.io.File

object Config {
    var jobKey: String = "DefaultJobKey"
    var jobPort = 7700
    var dataDirectory = File("/etc/frpmgr")
    var frpsPort = 7702
    var httpPort = 7701

    fun obtainFromArgs(args: Array<String>) {
        val jobKeyIndex = args.lastIndexOf("--jobKey") + 1
        val jobPortIndex = args.lastIndexOf("--jobPort") + 1
        val dataDirIndex = args.lastIndexOf("--dataDir") + 1
        val frpsPortIndex = args.lastIndexOf("--frpsPort") + 1
        val httpPortIndex = args.lastIndexOf("--httpPort") + 1
        if (jobKeyIndex != -1) {
            jobKey = args[jobKeyIndex]
        }
        if (jobPortIndex != -1) {
            jobPort = args[jobPortIndex].toInt()
        }
        if (dataDirIndex != -1) {
            dataDirectory = File(args[dataDirIndex])
        }
        if (frpsPortIndex != -1) {
            frpsPort = args[frpsPortIndex].toInt()
        }
        if (httpPortIndex != -1) {
            httpPort = args[httpPortIndex].toInt()
        }
    }
}