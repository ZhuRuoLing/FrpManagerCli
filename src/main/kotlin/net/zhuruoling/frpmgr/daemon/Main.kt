package net.zhuruoling.frpmgr.daemon

import net.zhuruoling.frpmgr.daemon.data.Config
import net.zhuruoling.frpmgr.daemon.network.SocketJobServer
import net.zhuruoling.frpmgr.daemon.network.applicationMain
import java.util.Stack
import kotlin.concurrent.thread

private val jobs = Stack<Job>()

private lateinit var applicationThread: Thread
private lateinit var jobServerThread:Thread
fun main(args: Array<String>) {
    Config.obtainFromArgs(args)
    applicationThread = thread(start = false) {
        applicationMain(args)
    }
    jobServerThread = SocketJobServer()
}