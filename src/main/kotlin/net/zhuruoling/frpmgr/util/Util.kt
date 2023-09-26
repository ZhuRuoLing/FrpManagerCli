package net.zhuruoling.frpmgr.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path

val gson: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()

fun Any?.toJson(): String = gson.toJson(this)

fun <T> String.toObject(clazz: Class<T>): T = gson.fromJson(this, clazz)

fun String.toPath(): Path = Path(this)

fun generateRandomString(len: Int): String {
    return generateRandomString(len, true, true)
}

fun generateRandomString(len: Int, hasInteger: Boolean, hasUpperLetter: Boolean): String {
    val ch =
        "abcdefghijklmnopqrstuvwxyz" + (if (hasUpperLetter) "ABCDEFGHIGKLMNOPQRSTUVWXYZ" else "") + if (hasInteger) "0123456789" else ""
    val stringBuffer = StringBuilder()
    for (i in 0 until len) {
        val random = Random(System.nanoTime())
        val num = random.nextInt(ch.length - 1)
        stringBuffer.append(ch[num])
    }
    return stringBuffer.toString()
}