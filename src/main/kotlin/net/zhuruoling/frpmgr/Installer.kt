package net.zhuruoling.frpmgr

import io.ktor.http.*
import net.zhuruoling.frpmgr.util.toPath
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.*
import kotlin.system.exitProcess

class Installer {
    private val logger: Logger = LoggerFactory.getLogger("Installer")
    @OptIn(ExperimentalPathApi::class)
    fun install(){
        val scriptStream = this.javaClass.classLoader.getResourceAsStream("frpmgr")
        if (scriptStream == null){
            logger.error("Cannot find startup script in jar resources.")
            exitProcess(1)
        }
        val installDir = Path("/etc/frpmgr")
        val jarPath = installDir.resolve("frpmgr.jar")
        val binDir = Path("/usr/bin/frpmgr")
        binDir.deleteIfExists()
        binDir.createFile(PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxwx")))
        scriptStream.use {
            while (it.available() != 0){
                binDir.writer().write(it.read())
            }
        }
        val srcPath = Installer::class.java.protectionDomain.codeSource.location.path.decodeURLPart().toPath()
        srcPath.copyToRecursively(jarPath.createParentDirectories(), followLinks = true)
    }
}

