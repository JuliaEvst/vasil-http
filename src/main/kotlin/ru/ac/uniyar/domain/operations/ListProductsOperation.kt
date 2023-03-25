package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.asc
import org.ktorm.dsl.count
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.storage.Product
import ru.ac.uniyar.domain.tables.ProductTable

class ListProductsOperation(private val database: Database) {
    fun countWithFilter(categoryFilter: String): Double {
        if (categoryFilter != "") {
            return database
                .from(ProductTable)
                .select(count())
                .where { ProductTable.category eq categoryFilter }
                .map { row -> row.getInt(1) }
                .first()
                .toDouble()
        } else {
            return database
                .from(ProductTable)
                .select(count())
                .map { row -> row.getInt(1) }
                .first()
                .toDouble()
        }
    }

    fun list(): List<Product> =
        database
            .from(ProductTable)
            .select()
            .orderBy(ProductTable.time.asc(), ProductTable.name.asc())
            .mapNotNull { row ->
                try {
                    Product(
                        row[ProductTable.productId]!!,
                        row[ProductTable.category]!!,
                        row[ProductTable.name]!!,
                        row[ProductTable.description]!!,
                        row[ProductTable.price]!!,
                        row[ProductTable.count]!!,
                        row[ProductTable.producer]!!,
                        row[ProductTable.country]!!,
                        row[ProductTable.time]!!
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }

    fun subListWithCategoryFilter(
        categoryFilter: String,
        sortFilter: String,
        left: Int,
        pageSize: Int
    ): List<Product> {
        return if (categoryFilter != "") {
            if (sortFilter == "time") {
                subListWhereCategoryIsNotEmptyTime(categoryFilter, left, pageSize)
            } else {
                subListWhereCategoryIsNotEmptyName(categoryFilter, left, pageSize)
            }
        } else {
            if (sortFilter == "time") {
                subListWhereCategoryIsEmptyTime(left, pageSize)
            } else {
                subListWhereCategoryIsEmptyName(left, pageSize)
            }
        }
    }

    private fun subListWhereCategoryIsNotEmptyTime(
        categoryFilter: String,
        left: Int,
        pageSize: Int
    ): List<Product> {
        return database
            .from(ProductTable)
            .select()
            .orderBy(ProductTable.time.asc(), ProductTable.name.asc())
            .where { ProductTable.category eq categoryFilter }
            .mapNotNull { row ->
                try {
                    Product(
                        row[ProductTable.productId]!!,
                        row[ProductTable.category]!!,
                        row[ProductTable.name]!!,
                        row[ProductTable.description]!!,
                        row[ProductTable.price]!!,
                        row[ProductTable.count]!!,
                        row[ProductTable.producer]!!,
                        row[ProductTable.country]!!,
                        row[ProductTable.time]!!
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
    }

    private fun subListWhereCategoryIsNotEmptyName(
        categoryFilter: String,
        left: Int,
        pageSize: Int
    ): List<Product> {
        return database
            .from(ProductTable)
            .select()
            .orderBy(ProductTable.name.asc(), ProductTable.time.asc())
            .where { ProductTable.category eq categoryFilter }
            .mapNotNull { row ->
                try {
                    Product(
                        row[ProductTable.productId]!!,
                        row[ProductTable.category]!!,
                        row[ProductTable.name]!!,
                        row[ProductTable.description]!!,
                        row[ProductTable.price]!!,
                        row[ProductTable.count]!!,
                        row[ProductTable.producer]!!,
                        row[ProductTable.country]!!,
                        row[ProductTable.time]!!
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
    }

    private fun subListWhereCategoryIsEmptyTime(
        left: Int,
        pageSize: Int
    ): List<Product> {
        return database
            .from(ProductTable)
            .select()
            .orderBy(ProductTable.time.asc(), ProductTable.name.asc())
            .mapNotNull { row ->
                try {
                    Product(
                        row[ProductTable.productId]!!,
                        row[ProductTable.category]!!,
                        row[ProductTable.name]!!,
                        row[ProductTable.description]!!,
                        row[ProductTable.price]!!,
                        row[ProductTable.count]!!,
                        row[ProductTable.producer]!!,
                        row[ProductTable.country]!!,
                        row[ProductTable.time]!!
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
    }

    private fun subListWhereCategoryIsEmptyName(
        left: Int,
        pageSize: Int
    ): List<Product> {
        return database
            .from(ProductTable)
            .select()
            .orderBy(ProductTable.name.asc(), ProductTable.time.asc())
            .mapNotNull { row ->
                try {
                    Product(
                        row[ProductTable.productId]!!,
                        row[ProductTable.category]!!,
                        row[ProductTable.name]!!,
                        row[ProductTable.description]!!,
                        row[ProductTable.price]!!,
                        row[ProductTable.count]!!,
                        row[ProductTable.producer]!!,
                        row[ProductTable.country]!!,
                        row[ProductTable.time]!!
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
    }
}
