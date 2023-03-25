package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.domain.tables.PermissionsTable

class PermissionOperation(private val database: Database) {
    fun getPermissionByUserRole(userRole: String): Permission {
        return database
            .from(PermissionsTable)
            .select()
            .where { PermissionsTable.role eq userRole }
            .mapNotNull { row ->
                try {
                    Permission(
                        userRole,
                        row[PermissionsTable.canAddProductInCart]!!,
                        row[PermissionsTable.canDeleteProductFromCart]!!,
                        row[PermissionsTable.canSeeCart]!!,
                        row[PermissionsTable.canOrder]!!,
                        row[PermissionsTable.canAddNewProduct]!!,
                        row[PermissionsTable.canEditProduct]!!,
                        row[PermissionsTable.canDeleteProduct]!!,
                        row[PermissionsTable.canEditCategories]!!,
                        row[PermissionsTable.canSeeProducer]!!,
                        row[PermissionsTable.canAddNewProducer]!!
                    )
                } catch (nullPointerEx: java.lang.NullPointerException) {
                    println(nullPointerEx)
                    null
                }
            }
            .first()
    }
}
