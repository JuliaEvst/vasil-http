package ru.ac.uniyar.config.databaseManagers

import org.flywaydb.core.Flyway

fun performMigrations(
    jdbcConnection: String
) {
    val flyway = Flyway
        .configure()
        .locations("ru/ac/uniyar/db/migrations")
        .validateMigrationNaming(true)
        .dataSource(jdbcConnection, "sa", null)
        .load()
    flyway.migrate()
}
