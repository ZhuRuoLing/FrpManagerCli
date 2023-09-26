package net.zhuruoling.frpmgr.daemon

import io.ktor.server.sessions.*
import net.zhuruoling.frpmgr.daemon.command.CommandSource
import net.zhuruoling.frpmgr.daemon.command.dispatcher
import net.zhuruoling.frpmgr.daemon.data.Config
import net.zhuruoling.frpmgr.daemon.data.NodeData
import net.zhuruoling.frpmgr.daemon.frps.FrpsProcess
import net.zhuruoling.frpmgr.daemon.frps.prepareFrpsLaunch
import net.zhuruoling.frpmgr.daemon.network.SocketJobServer
import net.zhuruoling.frpmgr.daemon.network.applicationMain
import org.slf4j.LoggerFactory
import java.util.Stack
import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val jobs = Stack<Job>()
private val logger = LoggerFactory.getLogger("Main")
val frpsAccessToken = generateSessionId()

lateinit var jobServerThread: SocketJobServer
private lateinit var frpProcess: FrpsProcess
private lateinit var applicationThread: Thread



fun main(args: Array<String>) {
    Config.obtainFromArgs(args)
    NodeData.load()
    logger.info("Using Frps Token: $frpsAccessToken")
    logger.info("Using JobServer Token: ${Config.jobKey}")
    applicationThread = thread(start = false) {
        applicationMain(args)
    }
    jobServerThread = SocketJobServer()
    applicationThread.start()
    jobServerThread.start()
    frpProcess = prepareFrpsLaunch()
    frpProcess.start()

    while (true) {
        while (!jobs.empty()) {
            synchronized(jobs) {
                try {
                    val job = jobs.pop()
                    logger.info("Handling job \"${job.command}\" from ${job.fromDesc}")
                    val src = CommandSource()
                    try{
                        val result = dispatcher.execute(job.command, src)
                        job.callback(src, result)
                    }catch (e:Exception){
                        e.stackTraceToString().split("\n").forEach(src::sendError)
                        job.callback(src, -1)
                    }
                } catch (e: Exception) {
                    logger.error("Error occurred while executing jobs.", e)
                }
            }
        }
        LockSupport.parkNanos(1000)
    }
}

fun scheduleJob(job: Job) {
    synchronized(jobs) {
        jobs.push(job)
    }
}

fun stop() {
    applicationThread.interrupt()
    jobServerThread.interrupt()
    frpProcess.end()
    exitProcess(0)
}