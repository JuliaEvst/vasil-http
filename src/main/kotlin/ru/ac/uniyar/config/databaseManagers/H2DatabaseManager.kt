package ru.ac.uniyar.config.databaseManagers

import org.h2.tools.Server
import kotlin.concurrent.thread

class H2DatabaseManager {
    private var tcpServer: Server? = null
    private var webServer: Server? = null
    private val shutdownThread = thread(start = false, name = "") {
        println("Stopping server")
        stopServers()
    }

    fun initialize(
        webPort: Int
    ): H2DatabaseManager {
        startServers(webPort)
        registerShutdownHook()
        return this
    }

    private fun startServers(
        webPort: Int
    ) {
        tcpServer = Server.createTcpServer(
            "-tcpPort", "9092",
            "-baseDir", ".",
            "-ifNotExists",
        ).start()
        webServer = Server.createWebServer(
            "-webPort", webPort.toString(),
            "-baseDir", ".",
            "-ifNotExists",
        ).start()
    }

    private fun registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(shutdownThread)
    }

    fun stopServers() {
        tcpServer?.stop()
        tcpServer = null
        webServer?.stop()
        webServer = null
    }
}
