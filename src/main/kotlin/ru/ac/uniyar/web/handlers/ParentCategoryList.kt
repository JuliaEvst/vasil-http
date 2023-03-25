package ru.ac.uniyar.web.handlers

import lensOrDefault
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.ListParentCategoryOperation
import ru.ac.uniyar.web.models.ParentCategoryListVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender
import kotlin.math.ceil

private val pageLens = Query.int().defaulted("page", 1)
private const val PAGE_SIZE = 3

fun parentCategoryList(
    view: ContextAwareViewRender,
    listParentCategoryOperation: ListParentCategoryOperation,
) = routes(
    "/" bind Method.GET to showParentCategoryList(view, listParentCategoryOperation),
)

private fun showParentCategoryList(
    view: ContextAwareViewRender,
    listParentCategoryOperation: ListParentCategoryOperation,
): HttpHandler = {
    var pageLens = lensOrDefault(pageLens, it, 1)
    val lastPage = (ceil(listParentCategoryOperation.count() / PAGE_SIZE.toDouble())).toInt()

    if (pageLens > lastPage || pageLens < 1) {
        pageLens = 1
    }

    val list = listParentCategoryOperation.subList(pageLens, PAGE_SIZE)

    val viewModel = ParentCategoryListVM(list, it.uri.path, pageLens, lastPage)
    Response(Status.OK).with(view(it) of viewModel)
}
