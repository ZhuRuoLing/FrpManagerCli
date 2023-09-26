package net.zhuruoling.frpmgr.process

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("FrpProcess")

open class FrpProcess(
    frpCommandBuilder: FrpCommandBuilder, workingDir: String, onLogOutput: (String) -> Unit = {
        logger.info(it)
    }
) : Process(
    "FrpProcess",
    frpCommandBuilder,
    workingDir,
    "",
    onLogOutput,
    { it },
    { true },
    {

    }) {
    fun end() {
        this.process?.destroyForcibly()
    }
}