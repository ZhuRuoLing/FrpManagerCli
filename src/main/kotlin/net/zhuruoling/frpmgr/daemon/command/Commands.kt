package net.zhuruoling.frpmgr.daemon.command

import com.mojang.brigadier.CommandDispatcher
import io.ktor.util.*
import net.zhuruoling.frpmgr.daemon.data.NodeData
import net.zhuruoling.frpmgr.daemon.data.NodeDataStorage
import net.zhuruoling.frpmgr.daemon.data.Protocol
import net.zhuruoling.frpmgr.daemon.data.TransportData

val dispatcher = CommandDispatcher<CommandSource>().apply {
    register(addCommand.node)
    register(listCommand.node)
    register(removeCommand.node)
}

val addCommand = LiteralCommand("add") {
    literal("node") {
        wordArgument("name") {
            execute {
                val name = getStringArgument("name")
                if (name !in NodeData) {
                    NodeData.addNodeData(NodeDataStorage(name, ArrayList()))
                    sendFeedback("Created node $name")
                } else {
                    sendError("Node $name already exists")
                }
                1
            }
        }
    }
    literal("tunnel") {
        wordArgument("node") {
            wordArgument("name") {
                integerArgument("hostPort", 1, 65535) {
                    integerArgument("remotePort", 1, 65535) {
                        wordArgument("protocol") {
                            execute {
                                val node = getStringArgument("node")
                                return@execute if (node in NodeData) {
                                    val name = getStringArgument("name")
                                    val hostPort = getIntegerArgument("hostPort")
                                    val remotePort = getIntegerArgument("remotePort")
                                    val protocol =
                                        enumValueOf<Protocol>(getStringArgument("protocol").toUpperCasePreservingASCIIRules())
                                    val tunnel = TransportData(name, hostPort, remotePort, protocol)
                                    NodeData.getNodeData(node)!!.transports += tunnel
                                    NodeData.save()
                                    sendFeedback("Added tunnel $tunnel to $node")
                                    1
                                } else {
                                    sendError("Node $node not found.")
                                    0
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val listCommand = LiteralCommand("List") {
    execute {
        NodeData.data.forEach { _, node ->
            sendFeedback("Node: ${node.nodeName}")
            node.transports.forEach {
                sendFeedback("    - Tunnel: [${it.protocol}] ${it.name}")
                sendFeedback("        localPort: ${it.localPort}")
                sendFeedback("        remotePort: ${it.remotePort}")
            }
        }
        1
    }
}

val removeCommand = LiteralCommand("remove") {
    literal("node") {
        wordArgument("name") {
            execute {
                val node = getStringArgument("node")
                return@execute if (node in NodeData) {
                    NodeData.data.remove(node)
                    NodeData.save()
                    sendFeedback("Removed node $node")
                    1
                } else {
                    sendError("Node $node not found.")
                    0
                }
            }
        }
    }
    literal("tunnel") {
        wordArgument("node") {
            wordArgument("name") {
                execute {
                    val node = getStringArgument("node")
                    return@execute if (node in NodeData) {
                        val name = getStringArgument("name")
                        if (NodeData[node]!!.transports.any { it.name == name }) {
                            NodeData[node]!!.transports.removeIf { it.name == name }
                            sendFeedback("Removed tunnel $name from $node")
                            NodeData.save()
                            1
                        } else {
                            sendError("Tunnel $name not found.")
                            0
                        }
                    } else {
                        sendError("Node $node not found.")
                        0
                    }
                }
            }
        }
    }
}