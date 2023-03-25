package ru.ac.uniyar.config

import org.http4k.cloudnative.env.Environment
import ru.ac.uniyar.config.DatabaseConfig.Companion.databaseConfigDefaultEnv
import ru.ac.uniyar.config.WebConfig.Companion.webConfigDefaultEnv

data class AppConfig(
    val webConfig: WebConfig,
    val databaseConfig: DatabaseConfig
) {
    companion object {
        private val appEnv = Environment.fromResource("/ru/ac/uniyar/config/app.properties") overrides
            Environment.JVM_PROPERTIES overrides
            Environment.ENV overrides
            webConfigDefaultEnv overrides
            databaseConfigDefaultEnv

        fun readConfiguration(): AppConfig = AppConfig(
            WebConfig.createWebConfig(appEnv),
            DatabaseConfig.createDatabaseConfig(appEnv)
        )
    }

    val salt = "zeUzqVtTaZC1GQaGSYKMVYoN4a7noZNINFKcEog6AAwUNYbFngk309ifVnUWq1QvvxHxApO6Bhl4sIJt8kzMsuBAjoRKUyOQv8jco"
}
