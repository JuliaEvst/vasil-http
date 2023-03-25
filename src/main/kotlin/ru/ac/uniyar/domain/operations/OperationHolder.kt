package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database

class OperationHolder(database: Database) {
    val informationOperation = InformationOperation(database)
    val permissionOperation = PermissionOperation(database)
    val shoppingCartProductsOperation = ShoppingCartProductsOperation(database, informationOperation)
    val productsOperation = ProductsOperation(database, shoppingCartProductsOperation, informationOperation)
    val producerOperation = ProducerOperation(database)
    val userOperation = UserOperation(database)
    val categoryOperation = CategoryOperation(database)
    val parentCategoryOperation = ParentCategoryOperation(database)

    private val countProductsByCategory = CountProductsByCategory(database)
    private val countProductsByParentCategory = CountProductsByParentCategory(database)

    val listParentCategoryOperation = ListParentCategoryOperation(database, countProductsByParentCategory)
    val listCategoryOperation = ListCategoryOperation(database, countProductsByCategory)
    val listProducerOperation = ListProducerOperation(database)
    val listCartOperation = ListCartOperation(database)
    val listProductsOperation = ListProductsOperation(database)
}
