package io.horizontalsystems.tor

import android.app.Application
import android.content.Context
import io.horizontalsystems.tor.core.TorConstants
import java.io.File

enum class EntityState(val processId: Int) {
    STARTING(-1),
    RUNNING(1),
    STOPPED(0);

    companion object {

        fun getByProcessId(procId: Int): EntityState {
            return values()
                .find { it.processId == procId } ?: RUNNING
        }
    }

    override fun toString(): String {
        return this.name
    }
}

enum class ConnectionStatus {

    UNDEFINED,
    IDLE,
    CLOSED,
    CONNECTING,
    CONNECTED,
    FAILED;

    companion object {

        fun getByName(typName: String): ConnectionStatus {
            return values()
                .find { it.name.contentEquals(typName.toUpperCase()) } ?: CLOSED
        }
    }

    override fun toString(): String {
        return this.name
    }
}

object Tor {

    class Info(var connection: Connection) {

        var processId: Int
            get() = connection.processId
            set(value) {
                connection.processId = value
            }

        var isInstalled: Boolean = false

        var state: EntityState
            get() = EntityState.getByProcessId(processId)
            set(value) {
                processId = value.processId

                if(value == EntityState.STOPPED)
                    connection.isBootstrapped = false
            }

        val isStarted: Boolean
            get() = connection.processId > 0
    }

    class Connection(processIdArg: Int = -1) {

        var processId: Int = processIdArg
        var proxyHost = TorConstants.IP_LOCALHOST
        var proxySocksPort = TorConstants.SOCKS_PROXY_PORT_DEFAULT
        var proxyHttpPort = TorConstants.HTTP_PROXY_PORT_DEFAULT
        var isBootstrapped: Boolean = false
        var status: ConnectionStatus
            get() {
                return if (processId > 0) {

                    if (isBootstrapped)
                        ConnectionStatus.CONNECTED
                    else
                        ConnectionStatus.CONNECTING
                } else {
                    ConnectionStatus.CLOSED
                }
            }
        set(value) {
            if(value == ConnectionStatus.CONNECTED)
                isBootstrapped = true
            else if(value == ConnectionStatus.FAILED)
                isBootstrapped = false
        }
    }

    class Settings(var context: Context) {
        var appFilesDir: File = context.filesDir
        var appDataDir: File = context.getDir(TorConstants.DIRECTORY_TOR_DATA, Application.MODE_PRIVATE)
        var appNativeDir: File = File(context.applicationInfo.nativeLibraryDir)
        var appSourceDir: File = File(context.applicationInfo.sourceDir)
        var useBridges: Boolean = false
    }

    interface Listener {
        fun onProcessStatusUpdate(torInfo: Info?, message: String)
        fun onConnStatusUpdate(torConnInfo: Connection?, message: String)
    }
}
