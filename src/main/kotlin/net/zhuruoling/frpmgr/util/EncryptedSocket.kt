package net.zhuruoling.frpmgr.util

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

//AES/ECB/PKCS5Padding
class EncryptedSocket(
    private val socket: Socket,
    key: String
) {
    val isClosed get() = socket.isClosed
    private val key: ByteArray
    private val logger = LoggerFactory.getLogger("EncryptedSocket")
    private val inChannel = socket.openReadChannel()
    private val outChannel = socket.openWriteChannel(autoFlush = true)
    init {
        this.key = toPaddedAesKey(key).toByteArray(StandardCharsets.UTF_8)
    }

    @Throws(
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        InvalidKeyException::class
    )
    suspend fun println(content: String) {
        send(content)
    }

    @Throws(
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        InvalidKeyException::class
    )
    suspend fun send(content: String) {
        val data = encryptECB(content.toByteArray(StandardCharsets.UTF_8), key)
        logger.debug("Sending:$content")
        outChannel.writeStringUtf8(String(data, StandardCharsets.UTF_8) + "\n")
        outChannel.flush()
    }

    @Throws(
        IOException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        InvalidKeyException::class
    )
    suspend fun readLine(): String? {
        val line = inChannel.readUTF8Line() ?: return null
        logger.debug("Received:$line")
        val data = decryptECB(line.toByteArray(StandardCharsets.UTF_8), key)
        return String(data, StandardCharsets.UTF_8)
    }
}