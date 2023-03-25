package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.count
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.forEach
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.tables.ParentCategoryTable

class ParentCategoryOperation(private val database: Database) {
    fun deleteParentCategory(parentCategoryName: String) {
        database.delete(ParentCategoryTable) { it.parentCategoryName eq parentCategoryName }
    }

    fun addParentCategory(parentCategoryName: String) {
        database.insert(ParentCategoryTable) {
            set(it.parentCategoryName, parentCategoryName)
        }
    }

    fun editParentCategory(parentCategoryName: String, newParentCategoryName: String) {
        database.update(ParentCategoryTable) {
            set(it.parentCategoryName, newParentCategoryName)
            where { it.parentCategoryName eq parentCategoryName }
        }
    }

    fun isNotNewParentCategoryName(newParentCategoryName: String): Boolean {
        var bool = false
        database
            .from(ParentCategoryTable)
            .select(count())
            .where { ParentCategoryTable.parentCategoryName eq newParentCategoryName }
            .forEach {
                if (it.getInt(1) > 0) {
                    bool = true
                }
            }
        return bool
    }
}
