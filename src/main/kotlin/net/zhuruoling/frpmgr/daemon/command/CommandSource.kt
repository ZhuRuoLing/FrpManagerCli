package net.zhuruoling.frpmgr.daemon.command

import org.slf4j.LoggerFactory

class CommandSource {
    private val logger = LoggerFactory.getLogger("CommandSource")
    val results = mutableListOf<String>()
    fun sendFeedback(content: String) {
        logger.info(content)
        results += content
    }

    fun sendError(format: String) {
        logger.error(format)
        results += "E: $format"
    }

}