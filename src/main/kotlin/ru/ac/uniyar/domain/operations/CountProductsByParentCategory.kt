package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.count
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.tables.CategoryTable
import ru.ac.uniyar.domain.tables.ParentCategoryTable
import ru.ac.uniyar.domain.tables.ProductTable

class CountProductsByParentCategory(private val database: Database) {
    fun start(parentCategoryNames: String): Int =
        database
            .from(ProductTable)
            .leftJoin(
                CategoryTable,
                on = ProductTable.category eq CategoryTable.categoryName
            )
            .leftJoin(
                ParentCategoryTable,
                on = CategoryTable.parentCategoryName eq ParentCategoryTable.parentCategoryName
            )
            .select(count())
            .where {
                (ParentCategoryTable.parentCategoryName eq CategoryTable.parentCategoryName) and
                    (ParentCategoryTable.parentCategoryName eq parentCategoryNames)
            }
            .map { row -> row.getInt(1) }
            .first()
}
