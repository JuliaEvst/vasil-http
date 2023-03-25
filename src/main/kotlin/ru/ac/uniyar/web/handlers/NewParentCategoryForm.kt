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
import ru.ac.uniyar.domain.operations.ParentCategoryOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.NewParentCategoryDataVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

private val parentCategoryNameField = FormField.nonEmptyString().required("parentCategoryName")
private val feedbackFormBody = Body.webForm(
    Validator.Feedback,
    parentCategoryNameField
).toLens()

fun parentCategoryForm(
    view: ContextAwareViewRender,
    parentCategoryOperation: ParentCategoryOperation,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canEditCategories).then(
        showNewParentCategoryForm(view, permissionLens)
    ),
    "/" bind Method.POST to permissionFilter(permissionLens, Permission::canEditCategories).then(
        addNewParentCategory(view, parentCategoryOperation, permissionLens)
    )
)

private fun showNewParentCategoryForm(
    view: ContextAwareViewRender,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditCategories) {
        return@handler Response(Status.FORBIDDEN)
    }

    val viewModel = NewParentCategoryDataVM()
    Response(Status.OK).with(view(request) of viewModel)
}

private fun addNewParentCategory(
    view: ContextAwareViewRender,
    parentCategoryOperation: ParentCategoryOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditCategories) {
        return@handler Response(Status.FORBIDDEN)
    }

    val webForm = feedbackFormBody(request)
    if (webForm.errors.isNotEmpty()) {
        return@handler Response(Status.OK).with(view(request) of NewParentCategoryDataVM(webForm))
    }

    val parentCategoryName = parentCategoryNameField(webForm)

    if (parentCategoryOperation.isNotNewParentCategoryName(parentCategoryName)) {
        val newError = webForm.errors + Invalid(
            parentCategoryNameField.meta.copy(description = "уже существует родительская категория с таким именем.")
        )
        val newWebForm = webForm.copy(errors = newError)
        return@handler Response(Status.OK).with(view(request) of NewParentCategoryDataVM(newWebForm))
    }

    parentCategoryOperation.addParentCategory(parentCategoryName)
    Response(Status.FOUND).header("Location", "/parent_category")
}
