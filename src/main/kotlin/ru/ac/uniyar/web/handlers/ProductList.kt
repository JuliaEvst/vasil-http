package ru.ac.uniyar.web.handlers

import lensOrDefault
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.string
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.ListProductsOperation
import ru.ac.uniyar.domain.operations.ProductsOperation
import ru.ac.uniyar.web.models.ProductsListVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender
import kotlin.math.ceil

private const val PAGE_SIZE = 3
private val pageLens = Query.int().defaulted("page", 1)
private val categoryLens = Query.string().defaulted("category", "")
private val sortLens = Query.string().defaulted("sort", "time")

fun productList(
    view: ContextAwareViewRender,
    listProductsOperation: ListProductsOperation,
    productsOperation: ProductsOperation,
) = routes(
    "/" bind GET to showProductsList(listProductsOperation, view, productsOperation)
)

private fun showProductsList(
    listProductsOperation: ListProductsOperation,
    view: ContextAwareViewRender,
    productsOperation: ProductsOperation,
): HttpHandler = {
    var categoryFilter = ""
    var parentCategoryFilter: String? = null
    var pageLens = lensOrDefault(pageLens, it, 1)
    val categoryLens = lensOrDefault(categoryLens, it, "")
    var sortLens = lensOrDefault(sortLens, it, "time")

    if (sortLens != "time" && sortLens != "name") {
        sortLens = "time"
    }
    val sortFilter = "&sort=$sortLens"

    val lastPage = (ceil(listProductsOperation.countWithFilter(categoryLens) / PAGE_SIZE.toDouble())).toInt()

    if (pageLens > lastPage || pageLens < 1) {
        pageLens = 1
    }
    if (categoryLens != "") {
        categoryFilter = "&category=$categoryLens"
        parentCategoryFilter = productsOperation.fetchParentCategory(categoryLens)
    }

    val list = listProductsOperation.subListWithCategoryFilter(categoryLens, sortLens, pageLens, PAGE_SIZE)

    val viewModel = ProductsListVM(
        list,
        it.uri.path,
        pageLens,
        lastPage,
        categoryFilter,
        sortFilter,
        parentCategoryFilter
    )
    Response(Status.OK).with(view(it) of viewModel)
}
