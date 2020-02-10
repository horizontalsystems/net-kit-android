package io.horizontalsystems.tor.utils

import io.horizontalsystems.tor.core.TorConstants
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

object FileUtils {
    fun setExecutable(fileBin: File) {
        fileBin.setReadable(true)
        fileBin.setExecutable(true)
        fileBin.setWritable(false)
        fileBin.setWritable(true, true)
    }
}

object ProcessUtils {

    @Throws(IOException::class)
    fun findProcessId(command: String?): Int {
        val procPs: Process = Runtime.getRuntime().exec(TorConstants.SHELL_CMD_PS)
        val reader = BufferedReader(InputStreamReader(procPs.inputStream))
        var line: String
        while (reader.readLine().also { line = it } != null) {
            if (!line.contains("PID") && line.contains(command!!)) {
                val lineParts = line.split("\\s+").toTypedArray()
                return try {
                    lineParts[1].toInt() //for most devices it is the second
                } catch (e: NumberFormatException) {
                    lineParts[0].toInt() //but for samsungs it is the first
                } finally {
                    try {
                        procPs.destroy()
                    } catch (e: Exception) {
                    }
                }
            }
        }
        return -1
    }

    @Throws(Exception::class)
    fun killProcess(fileProcBin: File) {
        killProcess(fileProcBin, "-9") // this is -KILL
    }

    @Throws(Exception::class)
    fun killProcess(fileProcBin: File, signal: String) {
        var procId = -1
        var killAttempts = 0
        while (findProcessId(fileProcBin.canonicalPath).also { procId = it } != -1) {
            killAttempts++
            val pidString = procId.toString()
            try {
                Runtime.getRuntime().exec("busybox killall " + signal + " " + fileProcBin.name)
            } catch (ioe: IOException) {
            }
            killProcess(pidString, signal)
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) { // ignored
            }
            if (killAttempts > 4) throw Exception("Cannot kill: " + fileProcBin.absolutePath)
        }
    }

    @Throws(Exception::class)
    fun killProcess(pidString: String, signal: String) {
        try {
            Runtime.getRuntime().exec("toolbox kill $signal $pidString")
        } catch (ioe: IOException) {
        }
        try {
            Runtime.getRuntime().exec("busybox kill $signal $pidString")
        } catch (ioe: IOException) {
        }
        try {
            Runtime.getRuntime().exec("kill $signal $pidString")
        } catch (ioe: IOException) {
        }
    }

}