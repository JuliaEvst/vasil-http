package ru.ac.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.FormField
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.int
import org.http4k.lens.nonEmptyString
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.OperationHolder
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.NewProductDataVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

val categoryField = FormField.nonEmptyString().required("category")
val nameField = FormField.nonEmptyString().required("name")
val descriptionField = FormField.nonEmptyString().required("description")
val priceField = FormField.int().required("price")
val countField = FormField.int().required("count")
val producerField = FormField.nonEmptyString().required("producer")
val countryField = FormField.nonEmptyString().required("country")

private val feedbackFormBody = Body.webForm(
    Validator.Feedback,
    categoryField,
    nameField,
    descriptionField,
    priceField,
    countField,
    producerField,
    countryField
).toLens()

fun productForm(
    view: ContextAwareViewRender,
    operationHolder: OperationHolder,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canAddNewProduct).then(
        showNewProductForm(view, operationHolder, permissionLens)
    ),
    "/" bind Method.POST to permissionFilter(permissionLens, Permission::canAddNewProduct).then(
        createNewProductWithLens(view, operationHolder, permissionLens)
    )
)

private fun showNewProductForm(
    view: ContextAwareViewRender,
    operationHolder: OperationHolder,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canAddNewProduct) {
        return@handler Response(Status.FORBIDDEN)
    }
    val listProducer = operationHolder.listProducerOperation.list()
    val listCategory = operationHolder.listCategoryOperation.list()
    val viewModel = NewProductDataVM(WebForm(), listProducer, listCategory)
    Response(OK).with(view(request) of viewModel)
}

private fun createNewProductWithLens(
    view: ContextAwareViewRender,
    operationHolder: OperationHolder,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canAddNewProduct) {
        return@handler Response(Status.FORBIDDEN)
    }

    val webForm = feedbackFormBody(request)
    val productsOperation = operationHolder.productsOperation
    val listProducer = operationHolder.listProducerOperation.list()
    val listCategory = operationHolder.listCategoryOperation.list()

    if (webForm.errors.isEmpty()) {
        productsOperation.add(webForm)
        val number = productsOperation.countOfProducts
        Response(FOUND).header("Location", "/product/$number")
    } else {
        Response(OK).with(view(request) of NewProductDataVM(webForm, listProducer, listCategory))
    }
}
