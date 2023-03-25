package ru.ac.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.ListCategoryOperation
import ru.ac.uniyar.web.models.StartPageVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

fun startPage(
    view: ContextAwareViewRender,
    listCategoryOperation: ListCategoryOperation,
) = routes(
    "/" bind Method.GET to showStartPage(view, listCategoryOperation)
)
private fun showStartPage(
    view: ContextAwareViewRender,
    listCategoryOperation: ListCategoryOperation,
): HttpHandler = {
    val list = listCategoryOperation.listForStartPage()
    val viewModel = StartPageVM(list)
    Response(Status.OK).with(view(it) of viewModel)
}
