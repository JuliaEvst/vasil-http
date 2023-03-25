package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.asc
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import ru.ac.uniyar.domain.storage.ParentCategory
import ru.ac.uniyar.domain.tables.ParentCategoryTable

class ListParentCategoryOperation(
    private val database: Database,
    private val countProductsByParentCategory: CountProductsByParentCategory
) {
    fun list(): List<ParentCategory> =
        database
            .from(ParentCategoryTable)
            .select()
            .orderBy(ParentCategoryTable.parentCategoryName.asc())
            .mapNotNull { row ->
                try {
                    ParentCategory(
                        row[ParentCategoryTable.parentCategoryName]!!,
                        countProductsByParentCategory.start(row[ParentCategoryTable.parentCategoryName]!!)
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }

    fun subList(left: Int, pageSize: Int): List<ParentCategory> =
        database
            .from(ParentCategoryTable)
            .select()
            .orderBy(ParentCategoryTable.parentCategoryName.asc())
            .mapNotNull { row ->
                try {
                    ParentCategory(
                        row[ParentCategoryTable.parentCategoryName]!!,
                        countProductsByParentCategory.start(row[ParentCategoryTable.parentCategoryName]!!)
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

    fun count(): Double {
        return database
            .from(ParentCategoryTable)
            .select(org.ktorm.dsl.count())
            .map { row -> row.getInt(1) }
            .first()
            .toDouble()
    }
}
