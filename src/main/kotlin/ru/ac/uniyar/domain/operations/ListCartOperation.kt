package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.storage.ShoppingCart
import ru.ac.uniyar.domain.storage.User
import ru.ac.uniyar.domain.tables.ShoppingCartTable

class ListCartOperation(private val database: Database) {
    fun list(): List<ShoppingCart> =
        database
            .from(ShoppingCartTable)
            .select()
            .orderBy(ShoppingCartTable.time.asc())
            .mapNotNull { row ->
                try {
                    ShoppingCart(
                        row[ShoppingCartTable.id]!!,
                        row[ShoppingCartTable.userId]!!,
                        row[ShoppingCartTable.productId]!!,
                        row[ShoppingCartTable.productName]!!,
                        row[ShoppingCartTable.productPrice]!!,
                        row[ShoppingCartTable.productCount]!!,
                        row[ShoppingCartTable.time]!!
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }

    fun subList(left: Int, pageSize: Int, currentUser: User): List<ShoppingCart> =
        database
            .from(ShoppingCartTable)
            .select()
            .where { ShoppingCartTable.userId eq currentUser.id }
            .orderBy(ShoppingCartTable.time.asc())
            .mapNotNull { row ->
                try {
                    ShoppingCart(
                        row[ShoppingCartTable.id]!!,
                        row[ShoppingCartTable.userId]!!,
                        row[ShoppingCartTable.productId]!!,
                        row[ShoppingCartTable.productName]!!,
                        row[ShoppingCartTable.productPrice]!!,
                        row[ShoppingCartTable.productCount]!!,
                        row[ShoppingCartTable.time]!!
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }
            .let {
                if (it.isNotEmpty()) {
                    if (left * pageSize - 1 <= it.size - 1) {
                        return@let it.subList((left - 1) * pageSize, left * pageSize)
                    }
                    if (left * pageSize - 2 <= it.size - 1) {
                        return@let it.subList((left - 1) * pageSize, left * pageSize - 1)
                    } else {
                        return@let it.subList((left - 1) * pageSize, left * pageSize - 2)
                    }
                } else {
                    return@let emptyList()
                }
            }

    fun count(currentUser: User?): Double {
        return if (currentUser != null) {
            val userId = currentUser.id
            database
                .from(ShoppingCartTable)
                .select(org.ktorm.dsl.count())
                .where { ShoppingCartTable.userId eq userId }
                .map { row -> row.getInt(1) }
                .first()
                .toDouble()
        } else {
            0.0
        }
    }
}
