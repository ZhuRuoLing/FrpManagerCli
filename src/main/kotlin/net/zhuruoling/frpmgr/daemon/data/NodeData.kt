package net.zhuruoling.frpmgr.daemon.data

import net.zhuruoling.frpmgr.util.gson
import java.nio.file.Path
import kotlin.io.path.*

data class TransportData(val name:String,val localPort: Int, val remotePort: Int, val protocol: Protocol)
data class NodeDataStorage(val nodeName: String, val transports: ArrayList<TransportData>)

enum class Protocol {
    TCP, UDP
}

object NodeData {

    val data = mutableMapOf<String, NodeDataStorage>()
    private lateinit var dataFile: Path

    fun load() {
        dataFile = Config.dataDirectory.toPath().resolve("nodes.json")
        if (!dataFile.exists()) {
            dataFile.createFile()
            dataFile.writeText("[]")
        }
        val jsonData = dataFile.reader().use { gson.fromJson(it, Array<NodeDataStorage>::class.java).toList() }
        jsonData.forEach { data += it.nodeName to it }
    }

    fun getNodeData(nodeId: String): NodeDataStorage?{
        return data[nodeId]
    }



    fun addNodeData(nodeDataStorage: NodeDataStorage){
        data[nodeDataStorage.nodeName] = nodeDataStorage
    }

    fun save() {
        val jsonData = data.values.toList()
        dataFile.deleteIfExists()
        dataFile.createFile()
        dataFile.writeText(gson.toJson(jsonData))
    }

    operator fun contains(nodeId: String): Boolean {
        return nodeId in data
    }

    operator fun get(nodeId: String) = getNodeData(nodeId)
}

