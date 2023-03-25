package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.count
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.forEach
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.storage.Information
import ru.ac.uniyar.domain.storage.Product
import ru.ac.uniyar.domain.tables.InformationTable
import ru.ac.uniyar.domain.tables.ShoppingCartTable

class InformationOperation(private val database: Database) {
    fun addInformationAboutDelete(
        product: Product
    ) {
        val description = "В магазине был удален товар '${product.name}'."
        database
            .from(ShoppingCartTable)
            .select(ShoppingCartTable.userId)
            .where { ShoppingCartTable.productId eq product.productId }
            .forEach { row ->
                val userId = row.getInt(1)
                database.insert(InformationTable) {
                    set(it.userId, userId)
                    set(it.description, description)
                }
            }
    }

    fun addInformationAboutEdit(
        product: Product
    ) {
        val description = "Товар '${product.name}' изменил свою стоимость и/или изменилось количество товара."
        database
            .from(ShoppingCartTable)
            .select(ShoppingCartTable.userId)
            .where { ShoppingCartTable.productId eq product.productId }
            .forEach { row ->
                val userId = row.getInt(1)
                if (checkIsNewInfo(userId, description)) {
                    database.insert(InformationTable) {
                        set(it.userId, userId)
                        set(it.description, description)
                    }
                }
            }
    }

    fun addInformationAboutOrder(
        userId: Int,
        address: String
    ) {
        val description = "Ожидайте заказ по адресу: $address."
        if (checkIsNewInfo(userId, description)) {
            database.insert(InformationTable) {
                set(it.userId, userId)
                set(it.description, description)
            }
        }
    }

    fun addInformationAboutCount(
        userId: Int,
        description: String
    ) {
        if (checkIsNewInfo(userId, description)) {
            database.insert(InformationTable) {
                set(it.userId, userId)
                set(it.description, description)
            }
        }
    }

    fun getListForUserById(userId: Int): List<Information> {
        return database
            .from(InformationTable)
            .select()
            .where { InformationTable.userId eq userId }
            .mapNotNull { row ->
                try {
                    Information(
                        row[InformationTable.userId]!!,
                        row[InformationTable.description]!!,
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }
    }

    fun clearListForUser(userId: Int) {
        database.delete(InformationTable) { it.userId eq userId }
    }

    private fun checkIsNewInfo(
        userId: Int,
        description: String
    ): Boolean {
        return database
            .from(InformationTable)
            .select(count())
            .where {
                (InformationTable.userId eq userId) and (InformationTable.description eq description)
            }
            .map { row -> row.getInt(1) }
            .first()
            .let { return@let it == 0 }
    }
}
