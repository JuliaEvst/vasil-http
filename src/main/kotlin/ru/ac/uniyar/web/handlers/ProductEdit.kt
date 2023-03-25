package ru.ac.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.ProductsOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.ProductEditVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

private val feedbackFormBody = Body.webForm(
    Validator.Feedback,
    priceField,
    countField,
).toLens()

fun editProductForm(
    productsOperation: ProductsOperation,
    view: ContextAwareViewRender,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canEditProduct).then(
        showEditProductForm(view, productsOperation, permissionLens)
    ),
    "/" bind Method.POST to permissionFilter(permissionLens, Permission::canEditProduct).then(
        editProduct(view, productsOperation, permissionLens)
    )
)

private fun showEditProductForm(
    view: ContextAwareViewRender,
    productsOperation: ProductsOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditProduct) {
        return@handler Response(Status.FORBIDDEN)
    }

    val index = request.path("number").orEmpty().toIntOrNull() ?: return@handler Response(Status.BAD_REQUEST)
    val product = productsOperation.fetch(index) ?: return@handler Response(Status.BAD_REQUEST)
    val viewModel = ProductEditVM(product)
    Response(Status.OK).with(view(request) of viewModel)
}

private fun editProduct(
    view: ContextAwareViewRender,
    productsOperation: ProductsOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditProduct) {
        return@handler Response(Status.FORBIDDEN)
    }

    val webForm = feedbackFormBody(request)
    val index = request.path("number").orEmpty().toIntOrNull() ?: return@handler Response(Status.BAD_REQUEST)
    val product = productsOperation.fetch(index) ?: return@handler Response(Status.BAD_REQUEST)

    if (webForm.errors.isEmpty()) {
        productsOperation.edit(product, webForm)
        Response(Status.FOUND).header("Location", "/product/$index")
    } else {
        Response(Status.OK).with(view(request) of ProductEditVM(product, webForm))
    }
}
