package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.count
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.forEach
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.storage.Category
import ru.ac.uniyar.domain.tables.CategoryTable

class CategoryOperation(private val database: Database) {
    fun deleteCategory(categoryName: String) {
        database.delete(CategoryTable) { it.categoryName eq categoryName }
    }

    fun addCategory(
        categoryName: String,
        parentCategoryName: String
    ) {
        database.insert(CategoryTable) {
            set(it.categoryName, categoryName)
            set(it.parentCategoryName, parentCategoryName)
        }
    }

    fun fetch(
        categoryName: String
    ): Category? {
        return database
            .from(CategoryTable)
            .select()
            .where { CategoryTable.categoryName eq categoryName }
            .mapNotNull { row ->
                try {
                    Category(
                        row[CategoryTable.categoryName]!!,
                        row[CategoryTable.parentCategoryName]!!
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }
            .firstOrNull()
    }

    fun edit(
        categoryName: String,
        newCategoryName: String,
        newParentCategoryName: String
    ) {
        database.update(CategoryTable) {
            set(it.categoryName, newCategoryName)
            set(it.parentCategoryName, newParentCategoryName)
            where { it.categoryName eq categoryName }
        }
    }

    fun isNotNewCategoryName(newCategoryName: String): Boolean {
        var bool = false
        database
            .from(CategoryTable)
            .select(count())
            .where { CategoryTable.categoryName eq newCategoryName }
            .forEach {
                if (it.getInt(1) > 0) {
                    bool = true
                }
            }
        return bool
    }
}
