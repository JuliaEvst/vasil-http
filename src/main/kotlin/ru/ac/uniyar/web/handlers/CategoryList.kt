package ru.ac.uniyar.web.handlers

import lensOrDefault
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.string
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.ListCategoryOperation
import ru.ac.uniyar.web.models.CategoryPageVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender
import kotlin.math.ceil

private const val PAGE_SIZE = 3
private val pageLens = Query.int().defaulted("page", 1)
private val parentCategoryLens = Query.string().defaulted("parent", "")

fun categoryList(
    view: ContextAwareViewRender,
    listCategoryOperation: ListCategoryOperation,
) = routes(
    "/" bind Method.GET to showCategoryList(view, listCategoryOperation),
)

private fun showCategoryList(
    view: ContextAwareViewRender,
    listCategoryOperation: ListCategoryOperation,
): HttpHandler = {
    var parentCategoryFilter = ""
    var pageLens = lensOrDefault(pageLens, it, 1)
    val parentCategoryLens = lensOrDefault(parentCategoryLens, it, "")
    val lastPage = (ceil(listCategoryOperation.countWithFilter(parentCategoryLens) / PAGE_SIZE.toDouble())).toInt()

    if (pageLens > lastPage || pageLens < 1) {
        pageLens = 1
    }
    if (parentCategoryLens != "") {
        parentCategoryFilter = "&parent=$parentCategoryLens"
    }

    val list = listCategoryOperation.subListWithParentFilter(parentCategoryLens, pageLens, PAGE_SIZE)

    val viewModel = CategoryPageVM(list, it.uri.path, pageLens, lastPage, parentCategoryFilter)
    Response(Status.OK).with(view(it) of viewModel)
}
