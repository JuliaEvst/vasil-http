package ru.ac.uniyar.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.int
import org.http4k.lens.nonEmptyString

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String
) {
    companion object {
        val hostLens = EnvironmentKey.nonEmptyString().required("db.host")
        val portLens = EnvironmentKey.int().required("db.port")
        val databaseLens = EnvironmentKey.nonEmptyString().required("db.database")

        fun createDatabaseConfig(environment: Environment): DatabaseConfig = DatabaseConfig(
            environment.let(hostLens), environment.let(portLens), environment.let(databaseLens)
        )

        val databaseConfigDefaultEnv = Environment.defaults(
            hostLens of "localhost",
            portLens of 8082,
            databaseLens of "database.h2"
        )
    }

    val jdbcConnection = "jdbc:h2:tcp://$host/$database"
}
