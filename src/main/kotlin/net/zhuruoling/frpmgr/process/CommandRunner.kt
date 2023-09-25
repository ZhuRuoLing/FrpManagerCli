package net.zhuruoling.frpmgr.process

import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.Process
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

open class Process(
    private val name: String,
    val commandBuilder: CommandBuilder,
    val workingDir: String,
    val description: String,
    private val onRunnerOutputLine: (String) -> Unit = {},
    private val processOutputLineFormatter: (String) -> String = { it },
    private val shouldRecordProcessOutputLine: (String) -> Boolean = { true },
    private val onProcessExitCallback: (Process) -> Unit = {}
) :
    Thread(name) {
    private val logger = LoggerFactory.getLogger("RunnerDaemon")
    private lateinit var out: OutputStream
    private lateinit var input: InputStream
    var processStarted = false
    private val queue = ArrayBlockingQueue<String>(1024)
    protected var process: Process? = null
    private var processOutputReader: ProcessOutputReader? = null
    var startFailReason: RuntimeException? = null

    private fun resolveCommand(command: String): Array<out String> {
        if (command.isEmpty()) throw IllegalArgumentException("Illegal command $command, to short or empty!")
        val stringTokenizer = StringTokenizer(command)
        val list = mutableListOf<String>()
        while (stringTokenizer.hasMoreTokens()) {
            list.add(stringTokenizer.nextToken())
        }
        return list.toTypedArray()
    }

    val reader get() = processOutputReader

    val processAlive: Boolean
        get() {
            ensureStarted()
            return process!!.isAlive
        }

    val returnCode: Int
        get() {
            ensureStopped()
            return process!!.exitValue()

        }

    private fun ensureStarted() {
        if (!processStarted) throw IllegalStateException("Runner process has not started.")
    }

    private fun ensureStopped() {
        if (processStarted and processAlive) throw IllegalStateException("Process is still running.")
    }

    fun waitForStart() {
        while (!processStarted) sleep(10)
    }

    fun waitForProcessStop() {
        ensureStarted()
        while (process!!.isAlive) sleep(10)
    }


    override fun run() {
        logger.info("Runner started.")
        try {
            process = Runtime.getRuntime().exec(commandBuilder.build(), null, File(workingDir))
            out = process!!.outputStream
            input = process!!.inputStream
            processStarted = true
        } catch (e: Exception) {
            startFailReason = RuntimeException("StartupFail", e)
            logger.error("Cannot start runner.", e)
            return
        }
        processOutputReader = ProcessOutputReader(
            process!!,
            name,
            onRunnerOutputLine,
            processOutputLineFormatter,
            shouldRecordProcessOutputLine
        )
        processOutputReader!!.start()
        val writer = out.writer(Charset.defaultCharset())
        while (process!!.isAlive) {
            try {
                if (queue.isNotEmpty()) {
                    synchronized(queue) {
                        while (queue.isNotEmpty()) {
                            val line = queue.poll()
                            logger.debug("input $line")
                            writer.write(line + "\n")
                            writer.flush()
                        }
                    }
                }
                sleep(10)
            } catch (e: ConcurrentModificationException) {
                e.printStackTrace()
            }
        }
        onProcessExitCallback(process!!)
        logger.info("Process exited with exit value ${process!!.exitValue()}")
    }

    fun input(str: String) {
        synchronized(queue) {
            queue.add(str)
        }
    }

    fun terminate() {
        if (processStarted) {
            if (processAlive) {
                process!!.destroyForcibly()
            } else {
                throw IllegalStateException("Process already exited.")
            }
        } else {
            throw IllegalStateException("Runner has not started.")
        }
    }

}


class ProcessOutputReader(
    private val process: Process,
    name: String,
    val onRunnerOutputLine: (String) -> Unit = {},
    val processOutputLineFormatter: (String) -> String = { it },
    val shouldRecordProcessOutputLine: (String) -> Boolean
) :
    Thread(name) {
    private val logger = LoggerFactory.getLogger(name)
    private lateinit var input: InputStream
    private var lineIndex = 0
    private val outputLines = mutableListOf<String>()

    override fun run() {
        logger.info("Process output reader started.")
        try {
            input = process.inputStream
            val reader = input.bufferedReader(Charset.forName("utf-8"))
            while (process.isAlive) {
                sleep(10)
                while (input.available() > 0) {
                    val li = reader.readLine()
                    if (li != null) {
                        if (shouldRecordProcessOutputLine(li)) {
                            val formatted = processOutputLineFormatter(li)
                            onRunnerOutputLine(formatted)
                            synchronized(outputLines) {
                                outputLines.add(formatted)
                            }
                        }
                    }
                }
            }
        } catch (ignored: InterruptedException) {
        }
    }

    fun nextLine(): String {
        synchronized(outputLines) {
            return if (hasNextLine()) {
                throw IndexOutOfBoundsException("Has no next line.")
            } else {
                lineIndex++
                outputLines[lineIndex + 1]
            }
        }
    }

    fun hasNextLine() = outputLines.size - 1 == lineIndex

    fun getAllLines() = outputLines

    fun getAllLinesAfterLineIndex(): MutableList<String> {
        return if (hasNextLine()) {
            val t = outputLines.subList(lineIndex, outputLines.size - 1)
            lineIndex = outputLines.size - 1
            t
        } else {
            mutableListOf()
        }
    }

}