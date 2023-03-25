package ru.ac.uniyar.domain.operations

import org.http4k.lens.WebForm
import org.ktorm.database.Database
import org.ktorm.database.asIterable
import org.ktorm.dsl.and
import org.ktorm.dsl.count
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.storage.User
import ru.ac.uniyar.domain.tables.UserTable
import ru.ac.uniyar.web.filters.JwtTools
import ru.ac.uniyar.web.handlers.userAddressField
import ru.ac.uniyar.web.handlers.userNameField
import ru.ac.uniyar.web.handlers.userPasswordField

class UserOperation(private val database: Database) {
    private fun setPasswordWithSql(webForm: WebForm, salt: String): String {
        val password = userPasswordField(webForm) + salt
        val sql = """
            CALL HASH('SHA3-256', '$password', 10)
        """.trimIndent()

        return database.useConnection { connection ->
            connection.prepareStatement(sql).use { statement ->
                return statement
                    .executeQuery()
                    .asIterable()
                    .map { row -> row.getString(1) }
                    .first()
            }
        }
    }

    private var countOfUsers = database
        .from(UserTable)
        .select(count())
        .map { row -> row.getInt(1) }
        .first()

    fun addUser(
        webForm: WebForm,
        salt: String
    ) {
        val password = setPasswordWithSql(webForm, salt).hashCode()
        countOfUsers++

        database.insert(UserTable) {
            set(it.id, countOfUsers)
            set(it.name, userNameField(webForm))
            set(it.address, userAddressField(webForm))
            set(it.userRole, "Авторизованный пользователь")
            set(it.password, password)
        }
    }

    fun getUser(
        webForm: WebForm,
        salt: String
    ): User? {
        val password = setPasswordWithSql(webForm, salt).hashCode()

        return database
            .from(UserTable)
            .select()
            .where {
                (UserTable.name eq userNameField(webForm)) and
                    (UserTable.password eq password)
            }
            .mapNotNull { row ->
                row[UserTable.id]?.let { id ->
                    row[UserTable.name]?.let { name ->
                        row[UserTable.address]?.let { address ->
                            row[UserTable.userRole]?.let { userRole ->
                                row[UserTable.password]?.let { pass ->
                                    User(id, name, address, userRole, pass)
                                }
                            }
                        }
                    }
                }
            }
            .firstOrNull()
    }

    fun getUserWithToken(
        token: String,
        jwtTools: JwtTools
    ): User? {
        val userId = jwtTools.subject(token)
        return if (userId != null) {
            database
                .from(UserTable)
                .select()
                .where { UserTable.id eq userId.toInt() }
                .mapNotNull { row ->
                    row[UserTable.id]?.let { id ->
                        row[UserTable.name]?.let { name ->
                            row[UserTable.address]?.let { address ->
                                row[UserTable.userRole]?.let { userRole ->
                                    row[UserTable.password]?.let { pass ->
                                        User(id, name, address, userRole, pass)
                                    }
                                }
                            }
                        }
                    }
                }
                .first()
        } else {
            null
        }
    }
}
