package net.zhuruoling.frpmgr.process

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("FrpProcess")

class FrpProcess(frpCommandBuilder: FrpCommandBuilder, workingDir: String) : Process(
    "FrpProcess",
    frpCommandBuilder,
    workingDir,
    "",
    {
        logger.info(it)
    },
    { it },
    { true },
    {

    }){
    fun end(){
        this.process?.destroyForcibly()
    }
}