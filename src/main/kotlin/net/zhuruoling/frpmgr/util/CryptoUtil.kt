package net.zhuruoling.frpmgr.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec


fun toPaddedAesKey(src: String): String {
    var key = src
    key = if (key.length <= 16) {
        val keyBuilder = StringBuilder(key)
        while (keyBuilder.length < 16) keyBuilder.append("0")
        keyBuilder.toString()
    } else {
        if (key.length <= 32) {
            val keyBuilder = StringBuilder(key)
            while (keyBuilder.length < 32) keyBuilder.append("0")
            keyBuilder.toString()
        } else {
            throw RuntimeException()
        }
    }
    return key
}

@Throws(
    NoSuchPaddingException::class,
    IllegalBlockSizeException::class,
    NoSuchAlgorithmException::class,
    BadPaddingException::class,
    InvalidKeyException::class
)
fun aesEncryptToB64String(data: String, key: String): String {
    return String(
        encryptECB(
            data.toByteArray(StandardCharsets.UTF_8),
            toPaddedAesKey(key).toByteArray(StandardCharsets.UTF_8)
        )
    )
}

@Throws(Exception::class)
fun aesDecryptFromB64String(data: String, key: String): String {
    return String(
        decryptECB(
            data.toByteArray(StandardCharsets.UTF_8), toPaddedAesKey(key).toByteArray(
                StandardCharsets.UTF_8
            )
        )
    )
}

@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun encryptECB(data: ByteArray, key: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
    val result = cipher.doFinal(data)
    return Base64.getEncoder().encode(result)
}


@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun decryptECB(data: ByteArray?, key: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
    val base64 = Base64.getDecoder().decode(data)
    return cipher.doFinal(base64)
}

fun gzipCompress(source: String): String {
    val out = ByteArrayOutputStream()
    val gzip: GZIPOutputStream
    try {
        gzip = GZIPOutputStream(out)
        gzip.write(source.toByteArray(StandardCharsets.UTF_8))
        gzip.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return base64Encode(out.toString())
}

fun base64Encode(content: String): String {
    return Base64.getEncoder().encodeToString(content.toByteArray(StandardCharsets.UTF_8))
}

fun base64Decode(content: String): String {
    return Base64.getDecoder().decode(content.toByteArray(StandardCharsets.UTF_8)).decodeToString()
}


fun gzipDecompress(source: String): String? {
    val `in` = ByteArrayInputStream(Base64.getDecoder().decode(source.toByteArray(StandardCharsets.UTF_8)))
    try {
        GZIPInputStream(`in`).use { stream -> return String(stream.readAllBytes()) }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}
