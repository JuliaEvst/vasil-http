package ru.ac.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.int
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.ProductsOperation
import ru.ac.uniyar.web.models.ProductVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

private val countToCartField = FormField.int().required("count")
private val feedbackFormBody = Body.webForm(Validator.Feedback, countToCartField).toLens()

fun productDetail(
    view: ContextAwareViewRender,
    productsOperation: ProductsOperation
) = routes(
    "/" bind Method.GET to showProduct(productsOperation, view),
    "/" bind Method.POST to addToShoppingCart()
)

private fun showProduct(
    productsOperation: ProductsOperation,
    view: ContextAwareViewRender,
): HttpHandler = handler@{
    val index = it.path("number").orEmpty().toIntOrNull() ?: return@handler Response(Status.BAD_REQUEST)
    val product = productsOperation.fetch(index) ?: return@handler Response(Status.BAD_REQUEST)
    val viewModel = ProductVM(
        product.productId,
        product.time,
        product.category,
        product.name,
        product.description,
        product.price,
        product.count,
        product.producer,
        product.country,
        productsOperation.fetchParentCategory(product.category)
    )
    Response(Status.OK).with(view(it) of viewModel)
}

private fun addToShoppingCart(): HttpHandler = handler@{ request ->
    val index = request.path("number").orEmpty().toIntOrNull() ?: return@handler Response(Status.BAD_REQUEST)
    val countToCart = countToCartField(feedbackFormBody(request))
    Response(Status.FOUND).header("Location", "/product/$index/toCart/$countToCart")
}
