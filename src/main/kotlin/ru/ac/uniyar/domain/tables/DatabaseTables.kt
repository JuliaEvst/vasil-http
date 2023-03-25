package ru.ac.uniyar.domain.tables

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object ProductTable : Table<Nothing>("PRODUCT") {
    val productId = int("PRODUCT_ID").primaryKey()
    val category = varchar("CATEGORY")
    val name = varchar("NAME")
    val description = varchar("DESCRIPTION")
    val price = int("PRICE")
    val count = int("COUNT")
    val producer = varchar("PRODUCER")
    val country = varchar("COUNTRY")
    val time = varchar("TIME")
}

object ParentCategoryTable : Table<Nothing>("PARENT_CATEGORY") {
    val parentCategoryName = varchar("PARENT_CATEGORY_NAME").primaryKey()
}

object CategoryTable : Table<Nothing>("CATEGORY") {
    val categoryName = varchar("CATEGORY_NAME").primaryKey()
    val parentCategoryName = varchar("PARENT_CATEGORY_NAME")
}

object ProducerTable : Table<Nothing>("PRODUCER") {
    val producerName = varchar("PRODUCER_NAME").primaryKey()
    val time = varchar("TIME")
}

object ShoppingCartTable : Table<Nothing>("SHOPPING_CART") {
    val id = int("ID").primaryKey()
    val userId = int("USER_ID")
    val productId = int("PRODUCT_ID")
    val productName = varchar("PRODUCT_NAME")
    val productPrice = int("PRODUCT_PRICE")
    val productCount = int("PRODUCT_COUNT")
    val time = varchar("TIME")
}

object PermissionsTable : Table<Nothing>("PERMISSIONS") {
    val role = varchar("ROLE")
    val canAddProductInCart = boolean("CAN_ADD_PRODUCT_IN_CART")
    val canDeleteProductFromCart = boolean("CAN_DELETE_PRODUCT_FROM_CART")
    val canSeeCart = boolean("CAN_SEE_CART")
    val canOrder = boolean("CAN_ORDER")
    val canAddNewProduct = boolean("CAN_ADD_NEW_PRODUCT")
    val canEditProduct = boolean("CAN_EDIT_PRODUCT")
    val canDeleteProduct = boolean("CAN_DELETE_PRODUCT")
    val canEditCategories = boolean("CAN_EDIT_CATEGORIES")
    val canSeeProducer = boolean("CAN_SEE_PRODUCER")
    val canAddNewProducer = boolean("CAN_ADD_NEW_PRODUCER")
}

object UserTable : Table<Nothing>("USERS") {
    val id = int("USER_ID").primaryKey()
    val name = varchar("NAME")
    val address = varchar("ADDRESS")
    val userRole = varchar("USER_ROLE")
    val password = int("PASSWORD")
}

object InformationTable : Table<Nothing>("INFORMATION") {
    val userId = int("USER_ID")
    val description = varchar("DESCRIPTION")
}
