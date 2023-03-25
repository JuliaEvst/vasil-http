package ru.ac.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.FormField
import org.http4k.lens.Invalid
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.nonEmptyString
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.ProducerOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.NewProducerDataVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

val producerNameField = FormField.nonEmptyString().required("producerName")
private val feedbackFormBody = Body.webForm(Validator.Feedback, producerNameField).toLens()

fun producerForm(
    producerOperation: ProducerOperation,
    view: ContextAwareViewRender,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canAddNewProducer).then(
        showNewProducerForm(view, permissionLens)
    ),
    "/" bind Method.POST to permissionFilter(permissionLens, Permission::canAddNewProducer).then(
        createNewProducer(producerOperation, view, permissionLens)
    )
)

private fun showNewProducerForm(
    view: ContextAwareViewRender,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canAddNewProducer) {
        return@handler Response(Status.FORBIDDEN)
    }

    val viewModel = NewProducerDataVM()
    Response(Status.OK).with(view(request) of viewModel)
}

private fun createNewProducer(
    producerOperation: ProducerOperation,
    view: ContextAwareViewRender,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canAddNewProducer) {
        return@handler Response(Status.FORBIDDEN)
    }

    val webForm = feedbackFormBody(request)
    if (webForm.errors.isNotEmpty()) {
        return@handler Response(Status.BAD_REQUEST).with(view(request) of NewProducerDataVM(webForm))
    }
    if (producerOperation.isNewProducer(webForm)) {
        val newError = webForm.errors + Invalid(
            producerNameField.meta.copy(description = "уже существует производитель с таким именем.")
        )
        val newWebForm = webForm.copy(errors = newError)
        return@handler Response(Status.BAD_REQUEST).with(view(request) of NewProducerDataVM(newWebForm))
    }
    producerOperation.add(webForm)
    return@handler Response(Status.FOUND).header("Location", "/producer")
}
