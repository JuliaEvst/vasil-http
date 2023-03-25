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
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.ParentCategoryOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.ParentCategoryEditVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

private val parentCategoryNameField = FormField.nonEmptyString().required("parentCategoryName")
private val feedbackFormBody = Body.webForm(
    Validator.Feedback,
    parentCategoryNameField
).toLens()

fun editParentCategoryForm(
    view: ContextAwareViewRender,
    parentCategoryOperation: ParentCategoryOperation,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canEditCategories).then(
        showEditParentCategoryForm(view, permissionLens)
    ),
    "/" bind Method.POST to permissionFilter(permissionLens, Permission::canEditCategories).then(
        editParentCategory(view, parentCategoryOperation, permissionLens)
    )
)

private fun showEditParentCategoryForm(
    view: ContextAwareViewRender,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditCategories) {
        return@handler Response(Status.FORBIDDEN)
    }

    val parentCategoryName = request.path("parentCategoryName") ?: return@handler Response(Status.BAD_REQUEST)
    val viewModel = ParentCategoryEditVM(parentCategoryName)
    Response(Status.OK).with(view(request) of viewModel)
}

private fun editParentCategory(
    view: ContextAwareViewRender,
    parentCategoryOperation: ParentCategoryOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditCategories) {
        return@handler Response(Status.FORBIDDEN)
    }

    val webForm = feedbackFormBody(request)
    val parentCategoryName = request.path("parentCategoryName") ?: return@handler Response(Status.BAD_REQUEST)

    if (webForm.errors.isNotEmpty()) {
        return@handler Response(Status.OK).with(view(request) of ParentCategoryEditVM(parentCategoryName, webForm))
    }

    val newParentCategoryName = parentCategoryNameField(webForm)

    if (newParentCategoryName == parentCategoryName) {
        return@handler Response(Status.FOUND).header("Location", "/parent_category")
    }

    if (parentCategoryOperation.isNotNewParentCategoryName(newParentCategoryName)) {
        val newError = webForm.errors + Invalid(
            parentCategoryNameField.meta.copy(description = "уже существует родительская категория с таким именем.")
        )
        val newWebForm = webForm.copy(errors = newError)
        return@handler Response(Status.OK).with(view(request) of ParentCategoryEditVM(parentCategoryName, newWebForm))
    }

    parentCategoryOperation.editParentCategory(parentCategoryName, newParentCategoryName)
    Response(Status.FOUND).header("Location", "/parent_category")
}
