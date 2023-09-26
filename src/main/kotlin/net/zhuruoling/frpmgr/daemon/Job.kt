package net.zhuruoling.frpmgr.daemon

import net.zhuruoling.frpmgr.daemon.command.CommandSource

data class Job(val fromDesc:String, val command:String, val callback:CommandSource.(Int) -> Unit)