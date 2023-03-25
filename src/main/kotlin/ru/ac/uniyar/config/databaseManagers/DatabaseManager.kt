package ru.ac.uniyar.config.databaseManagers

import org.ktorm.database.Database
import org.ktorm.support.mysql.MySqlDialect

fun connectToDatabase(
    jdbcConnection: String
) = Database.connect(
    url = jdbcConnection,
    driver = "org.h2.Driver",
    user = "sa",
    dialect = MySqlDialect(),
)
