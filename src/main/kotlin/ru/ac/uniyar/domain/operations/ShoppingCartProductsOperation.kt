package ru.ac.uniyar.domain.operations

import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.count
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.forEach
import org.ktorm.dsl.from
import org.ktorm.dsl.gt
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.minus
import org.ktorm.dsl.plus
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.storage.Product
import ru.ac.uniyar.domain.tables.ProductTable
import ru.ac.uniyar.domain.tables.ShoppingCartTable
import java.text.SimpleDateFormat
import java.util.Date

class ShoppingCartProductsOperation(
    private val database: Database,
    private val informationOperation: InformationOperation
) {
    private var countOfProductsAtCart =
        database
            .from(ShoppingCartTable)
            .select(count())
            .map { row -> row.getInt(1) }
            .first()

    fun add(
        product: Product,
        userId: Int,
        countToCart: Int
    ) {
        if (isNewProductAtCart(product.productId, userId)) {
            countOfProductsAtCart++
            database.insert(ShoppingCartTable) {
                set(it.id, countOfProductsAtCart)
                set(it.userId, userId)
                set(it.productId, product.productId)
                set(it.productName, product.name)
                set(it.productPrice, product.price)
                set(it.productCount, countToCart)
                set(it.time, SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date()))
            }
        } else {
            database.update(ShoppingCartTable) {
                set(it.productCount, it.productCount + countToCart)
                where {
                    (it.productId eq product.productId) and (it.userId eq userId)
                }
            }
        }
    }

    fun deleteByUser(id: Int): Boolean {
        var check = false
        database.delete(ShoppingCartTable) { it.id eq id }
        database
            .from(ShoppingCartTable)
            .select(count())
            .where { ShoppingCartTable.id gt id }
            .forEach {
                if (it.getInt(1) > 0) {
                    check = true
                }
            }
        database.update(ShoppingCartTable) {
            set(it.id, it.id - 1)
            where { it.id gt id }
        }
        countOfProductsAtCart--
        return check
    }

    fun deleteByWorker(
        productId: Int
    ) {
        var counter = 0
        database
            .from(ShoppingCartTable)
            .select(ShoppingCartTable.id)
            .where { ShoppingCartTable.productId eq productId }
            .forEach { row ->
                val id = row.getInt(1) - counter
                deleteByUser(id)
                counter++
            }
    }

    private fun isNewProductAtCart(
        productId: Int,
        userId: Int
    ): Boolean = database
        .from(ShoppingCartTable)
        .select(count())
        .where {
            (ShoppingCartTable.productId eq productId) and (ShoppingCartTable.userId eq userId)
        }
        .map { row -> row.getInt(1) }
        .first()
        .let { return@let it == 0 }

    fun order(
        userId: Int,
        address: String
    ) {
        val listWithCountError = listWithCountError(userId)
        if (listWithCountError.isNotEmpty()) {
            for (productName in listWithCountError) {
                val description = "Количество товара '$productName' в корзине превышает количество в магазине. " +
                    "Удалите товар и добавьте его снова."
                informationOperation.addInformationAboutCount(userId, description)
            }
        } else {
            orderWhereCartIsNotEmpty(userId, address)
        }
    }

    private fun orderWhereCartIsNotEmpty(
        userId: Int,
        address: String
    ) {
        val list = database
            .from(ShoppingCartTable)
            .select(ShoppingCartTable.id)
            .where { ShoppingCartTable.userId eq userId }
            .map { it.getInt(1) }

        var counter = 0
        for (idAtCart in list) {
            val id = idAtCart - counter
            database
                .from(ShoppingCartTable)
                .select(ShoppingCartTable.productId)
                .where { ShoppingCartTable.id eq id }
                .forEach { row ->
                    val productId = row.getInt(1)
                    removeSomeCountOfProduct(id, productId)
                    if (deleteByUser(id)) {
                        counter++
                    }
                }
        }
        informationOperation.addInformationAboutOrder(userId, address)
    }

    private fun listWithCountError(userId: Int): List<String> {
        val listWithCountError: MutableList<String> = mutableListOf()
        database
            .from(ShoppingCartTable)
            .select(ShoppingCartTable.productId, ShoppingCartTable.productCount)
            .where { ShoppingCartTable.userId eq userId }
            .forEach {
                val productId = it.getInt(1)
                val countAtCart = it.getInt(2)
                database
                    .from(ProductTable)
                    .select(ProductTable.count, ProductTable.name)
                    .where { ProductTable.productId eq productId }
                    .map { row ->
                        row.getString(1) + "|" + row.getString(2)
                    }
                    .first()
                    .let { string ->
                        val listOfString = string.split("|")
                        val countOfProduct = listOfString[0].toInt()
                        var productName = listOfString[1]
                        for (index in 2 until listOfString.size) {
                            productName += "|" + listOfString[index]
                        }
                        if (countAtCart > countOfProduct) {
                            listWithCountError.add(productName)
                        }
                    }
            }
        return listWithCountError.toList()
    }

    private fun removeSomeCountOfProduct(
        idAtCart: Int,
        productId: Int
    ) {
        val countAtCart = database
            .from(ShoppingCartTable)
            .select(ShoppingCartTable.productCount)
            .where { ShoppingCartTable.id eq idAtCart }
            .map { it.getInt(1) }
            .first()

        database.update(ProductTable) {
            set(it.count, it.count - countAtCart)
            where { it.productId eq productId }
        }
    }

    fun orderListIsNotEmpty(userId: Int): Boolean {
        var bool = false
        database
            .from(ShoppingCartTable)
            .select(count())
            .where { ShoppingCartTable.userId eq userId }
            .forEach {
                if (it.getInt(1) > 0) {
                    bool = true
                }
            }
        return bool
    }
}
