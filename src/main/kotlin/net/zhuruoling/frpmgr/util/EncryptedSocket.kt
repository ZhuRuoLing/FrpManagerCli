package net.zhuruoling.frpmgr.util

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
class EncryptedSocket constructor(private val `in`: BufferedReader, private val out: PrintWriter, key: String) {
    private val key: ByteArray
    private val logger = LoggerFactory.getLogger("EncryptedSocket")

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
    fun println(content: String) {
        send(content)
    }

    @Throws(
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        InvalidKeyException::class
    )
    fun send(content: String) {
        val data = encryptECB(content.toByteArray(StandardCharsets.UTF_8), key)
        logger.debug("Sending:$content")
        out.println(String(data, StandardCharsets.UTF_8))
        out.flush()
    }

    @Throws(
        IOException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        InvalidKeyException::class
    )
    fun readLine(): String? {
        val line = `in`.readLine() ?: return null
        logger.debug("Received:$line")
        val data = decryptECB(line.toByteArray(StandardCharsets.UTF_8), key)
        return String(data, StandardCharsets.UTF_8)
    }
}