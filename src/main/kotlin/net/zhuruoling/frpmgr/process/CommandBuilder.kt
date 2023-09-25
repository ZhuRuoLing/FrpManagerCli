package net.zhuruoling.frpmgr.process

interface CommandBuilder {
    fun build(): Array<String>
}

open class FrpCommandBuilder(private val executablePath: String, private val configPath:String) : CommandBuilder{
    override fun build(): Array<String> {
        return arrayOf(executablePath, "-c", configPath)
    }

}
