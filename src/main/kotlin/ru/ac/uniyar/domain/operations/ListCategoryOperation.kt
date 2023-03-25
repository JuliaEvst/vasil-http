package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.asc
import org.ktorm.dsl.associate
import org.ktorm.dsl.count
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.storage.Category
import ru.ac.uniyar.domain.storage.ParentCategory
import ru.ac.uniyar.domain.tables.CategoryTable
import ru.ac.uniyar.domain.tables.ParentCategoryTable

class ListCategoryOperation(
    private val database: Database,
    private val countProductsByCategory: CountProductsByCategory
) {
    fun listForStartPage(): Map<ParentCategory, List<Category>> =
        database
            .from(ParentCategoryTable)
            .select()
            .orderBy(ParentCategoryTable.parentCategoryName.asc())
            .associate { row ->
                ParentCategory(row[ParentCategoryTable.parentCategoryName]!!) to
                    database
                        .from(CategoryTable)
                        .select()
                        .orderBy(CategoryTable.categoryName.asc())
                        .where { CategoryTable.parentCategoryName eq row[ParentCategoryTable.parentCategoryName]!! }
                        .mapNotNull { row ->
                            try {
                                Category(
                                    row[CategoryTable.categoryName]!!,
                                    row[CategoryTable.parentCategoryName]!!,
                                    countProductsByCategory.start(row[CategoryTable.categoryName]!!)
                                )
                            } catch (nullPointerEx: java.lang.NullPointerException) {
                                println(nullPointerEx)
                                null
                            }
                        }
            }

    fun list(): List<Category> =
        database
            .from(CategoryTable)
            .select()
            .orderBy(CategoryTable.parentCategoryName.asc(), CategoryTable.categoryName.asc())
            .mapNotNull { row ->
                try {
                    Category(
                        row[CategoryTable.categoryName]!!,
                        row[CategoryTable.parentCategoryName]!!,
                        countProductsByCategory.start(row[CategoryTable.categoryName]!!)
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }

    fun subListWithParentFilter(
        parentFilter: String,
        left: Int,
        pageSize: Int
    ): List<Category> {
        return if (parentFilter != "") {
            subListWhereParentFilterIsNotEmpty(parentFilter, left, pageSize)
        } else {
            subListWhereParentFilterIsEmpty(left, pageSize)
        }
    }

    fun countWithFilter(parentCategoryFilter: String): Double {
        if (parentCategoryFilter != "") {
            return database
                .from(CategoryTable)
                .select(count())
                .where { CategoryTable.parentCategoryName eq parentCategoryFilter }
                .map { row -> row.getInt(1) }
                .first()
                .toDouble()
        } else {
            return database
                .from(CategoryTable)
                .select(count())
                .map { row -> row.getInt(1) }
                .first()
                .toDouble()
        }
    }

    private fun subListWhereParentFilterIsNotEmpty(
        parentFilter: String,
        left: Int,
        pageSize: Int
    ): List<Category> {
        return database
            .from(CategoryTable)
            .select()
            .where { CategoryTable.parentCategoryName eq parentFilter }
            .orderBy(CategoryTable.categoryName.asc())
            .mapNotNull { row ->
                try {
                    Category(
                        row[CategoryTable.categoryName]!!,
                        row[CategoryTable.parentCategoryName]!!,
                        countProductsByCategory.start(row[CategoryTable.categoryName]!!)
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

    private fun subListWhereParentFilterIsEmpty(
        left: Int,
        pageSize: Int
    ): List<Category> {
        return database
            .from(CategoryTable)
            .select()
            .orderBy(CategoryTable.categoryName.asc())
            .mapNotNull { row ->
                try {
                    Category(
                        row[CategoryTable.categoryName]!!,
                        row[CategoryTable.parentCategoryName]!!,
                        countProductsByCategory.start(row[CategoryTable.categoryName]!!)
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
