package net.zhuruoling.frpmgr.daemon.frps

import net.zhuruoling.frpmgr.daemon.jobServerThread
import net.zhuruoling.frpmgr.daemon.network.JobResponse
import net.zhuruoling.frpmgr.daemon.network.ResponseType
import net.zhuruoling.frpmgr.process.FrpCommandBuilder
import net.zhuruoling.frpmgr.process.FrpProcess
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("FrpsProcess")

class FrpsProcess(frpCommandBuilder: FrpCommandBuilder, workingDir: String) : FrpProcess(frpCommandBuilder, workingDir, {
    logger.info(it)
    jobServerThread.sendToAllConnections(JobResponse(true,ResponseType.PROCESS_OUTPUT, it, mutableMapOf()))
})