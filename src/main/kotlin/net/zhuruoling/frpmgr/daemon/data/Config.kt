package net.zhuruoling.frpmgr.daemon.data

import java.io.File
import kotlin.properties.Delegates

object Config {
    var jobKey:String = "DefaultJobKey"
    var jobPort = 7788
    var dataDirectory = File("/etc/frpmgr")
    fun obtainFromArgs(args:Array<String>){
        val jobKeyIndex = args.lastIndexOf("--jobKey") + 1
        val jobPortIndex = args.lastIndexOf("--jobPort") + 1
        val dataDirIndex = args.lastIndexOf("--dataDir") + 1
        jobKey = args[jobKeyIndex]
        jobPort = args[jobPortIndex].toInt()
        dataDirectory = File(args[dataDirIndex])
    }
}