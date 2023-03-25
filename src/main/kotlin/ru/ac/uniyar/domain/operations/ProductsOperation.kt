package ru.ac.uniyar.domain.operations

import org.http4k.lens.WebForm
import org.ktorm.database.Database
import org.ktorm.dsl.count
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.gt
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.minus
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import ru.ac.uniyar.domain.storage.Product
import ru.ac.uniyar.domain.tables.CategoryTable
import ru.ac.uniyar.domain.tables.ProductTable
import ru.ac.uniyar.domain.tables.ShoppingCartTable
import ru.ac.uniyar.web.handlers.categoryField
import ru.ac.uniyar.web.handlers.countField
import ru.ac.uniyar.web.handlers.countryField
import ru.ac.uniyar.web.handlers.descriptionField
import ru.ac.uniyar.web.handlers.nameField
import ru.ac.uniyar.web.handlers.priceField
import ru.ac.uniyar.web.handlers.producerField
import java.text.SimpleDateFormat
import java.util.Date

class ProductsOperation(
    private val database: Database,
    private val shoppingCartProductsOperation: ShoppingCartProductsOperation,
    private val informationOperation: InformationOperation
) {
    var countOfProducts =
        database
            .from(ProductTable)
            .select(count())
            .map { row -> row.getInt(1) }
            .first()

    fun add(webForm: WebForm) {
        countOfProducts++
        database.insert(ProductTable) {
            set(it.productId, countOfProducts)
            set(it.category, categoryField(webForm))
            set(it.name, nameField(webForm))
            set(it.description, descriptionField(webForm))
            set(it.price, priceField(webForm))
            set(it.count, countField(webForm))
            set(it.producer, producerField(webForm))
            set(it.country, countryField(webForm))
            set(it.time, SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date()))
        }
    }

    fun fetch(productId: Int): Product? =
        database
            .from(ProductTable)
            .select()
            .where { ProductTable.productId eq productId }
            .mapNotNull { row ->
                row[ProductTable.productId]?.let { id ->
                    row[ProductTable.category]?.let { category ->
                        row[ProductTable.name]?.let { name ->
                            row[ProductTable.description]?.let { description ->
                                row[ProductTable.price]?.let { price ->
                                    row[ProductTable.count]?.let { count ->
                                        row[ProductTable.producer]?.let { producer ->
                                            row[ProductTable.country]?.let { country ->
                                                row[ProductTable.time]?.let { time ->
                                                    Product(
                                                        id,
                                                        category,
                                                        name,
                                                        description,
                                                        price,
                                                        count,
                                                        producer,
                                                        country,
                                                        time
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .firstOrNull()

    fun fetchParentCategory(category: String): String =
        database
            .from(CategoryTable)
            .select(CategoryTable.parentCategoryName)
            .where { CategoryTable.categoryName eq category }
            .map { row -> row[CategoryTable.parentCategoryName]!! }
            .first()

    fun delete(
        product: Product
    ) {
        val productId = product.productId
        informationOperation.addInformationAboutDelete(product)
        shoppingCartProductsOperation.deleteByWorker(productId)
        database.delete(ProductTable) { it.productId eq productId }
        database.update(ProductTable) {
            set(it.productId, it.productId - 1)
            where { it.productId gt productId }
        }
        countOfProducts--
    }

    fun edit(
        product: Product,
        webForm: WebForm
    ) {
        informationOperation.addInformationAboutEdit(product)
        val productId = product.productId
        database.update(ProductTable) {
            set(it.count, countField(webForm))
            set(it.price, priceField(webForm))
            where { it.productId eq productId }
        }
        database.update(ShoppingCartTable) {
            set(it.productPrice, priceField(webForm))
            where { it.productId eq productId }
        }
    }
}
