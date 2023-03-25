package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.count
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.tables.ProductTable

class CountProductsByCategory(private val database: Database) {
    fun start(categoryNames: String): Int =
        database
            .from(ProductTable)
            .select(count())
            .where { ProductTable.category eq categoryNames }
            .map { row -> row.getInt(1) }
            .first()
}
