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
import ru.ac.uniyar.domain.operations.CategoryOperation
import ru.ac.uniyar.domain.operations.ListParentCategoryOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.CategoryEditVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

private var categoryNameField = FormField.nonEmptyString().required("categoryName")
private val parentCategoryNameField = FormField.nonEmptyString().required("parentCategoryName")
private val feedbackFormBody = Body.webForm(
    Validator.Feedback,
    categoryNameField,
    parentCategoryNameField
).toLens()

fun editCategoryForm(
    view: ContextAwareViewRender,
    listParentCategory: ListParentCategoryOperation,
    categoryOperation: CategoryOperation,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canEditCategories).then(
        showEditCategoryForm(view, listParentCategory, categoryOperation, permissionLens)
    ),
    "/" bind Method.POST to permissionFilter(permissionLens, Permission::canEditCategories).then(
        editCategory(view, listParentCategory, categoryOperation, permissionLens)
    )
)

private fun showEditCategoryForm(
    view: ContextAwareViewRender,
    listParentCategory: ListParentCategoryOperation,
    categoryOperation: CategoryOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditCategories) {
        return@handler Response(Status.FORBIDDEN)
    }

    val parentCategoryList = listParentCategory.list()
    val categoryName = request.path("categoryName") ?: return@handler Response(Status.BAD_REQUEST)
    val category = categoryOperation.fetch(categoryName) ?: return@handler Response(Status.BAD_REQUEST)
    val viewModel = CategoryEditVM(category, parentCategoryList)
    Response(Status.OK).with(view(request) of viewModel)
}

private fun editCategory(
    view: ContextAwareViewRender,
    listParentCategory: ListParentCategoryOperation,
    categoryOperation: CategoryOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditCategories) {
        return@handler Response(Status.FORBIDDEN)
    }

    val webForm = feedbackFormBody(request)
    val categoryName = request.path("categoryName") ?: return@handler Response(Status.BAD_REQUEST)

    if (webForm.errors.isNotEmpty()) {
        val parentCategoryList = listParentCategory.list()
        val category = categoryOperation.fetch(categoryName) ?: return@handler Response(Status.BAD_REQUEST)
        return@handler Response(Status.OK).with(view(request) of CategoryEditVM(category, parentCategoryList, webForm))
    }

    val newCategoryName = categoryNameField(webForm)

    if (newCategoryName == categoryName) {
        return@handler Response(Status.FOUND).header("Location", "/category")
    }

    if (categoryOperation.isNotNewCategoryName(newCategoryName)) {
        val newError = webForm.errors + Invalid(
            categoryNameField.meta.copy(description = "уже существует категория с таким именем.")
        )
        val newWebForm = webForm.copy(errors = newError)
        val parentCategoryList = listParentCategory.list()
        val category = categoryOperation.fetch(categoryName) ?: return@handler Response(Status.BAD_REQUEST)
        return@handler Response(Status.OK)
            .with(view(request) of CategoryEditVM(category, parentCategoryList, newWebForm))
    }

    val newParentCategoryName = parentCategoryNameField(webForm)
    categoryOperation.edit(categoryName, newCategoryName, newParentCategoryName)
    Response(Status.FOUND).header("Location", "/category")
}
