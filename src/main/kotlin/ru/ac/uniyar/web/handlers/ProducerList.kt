package ru.ac.uniyar.web.handlers

import lensOrDefault
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.RequestContextLens
import org.http4k.lens.int
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.ListProducerOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.ProducerListVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender
import kotlin.math.ceil

private val pageLens = Query.int().defaulted("page", 1)
private const val PAGE_SIZE = 3

fun producerList(
    view: ContextAwareViewRender,
    listProducerOperation: ListProducerOperation,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canSeeProducer).then(
        showProducerList(view, listProducerOperation, permissionLens)
    )
)

private fun showProducerList(
    view: ContextAwareViewRender,
    listProducerOperation: ListProducerOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canSeeProducer) {
        return@handler Response(Status.FORBIDDEN)
    }

    var pageLens = lensOrDefault(pageLens, request, 1)
    val lastPage = (ceil(listProducerOperation.count() / PAGE_SIZE.toDouble())).toInt()

    if (pageLens > lastPage || pageLens < 1) {
        pageLens = 1
    }

    val list = listProducerOperation.subList(pageLens, PAGE_SIZE)

    val viewModel = ProducerListVM(list, request.uri.path, pageLens, lastPage)
    Response(Status.OK).with(view(request) of viewModel)
}
