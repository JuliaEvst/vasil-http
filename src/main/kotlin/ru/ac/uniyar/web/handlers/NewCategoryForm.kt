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
import ru.ac.uniyar.domain.operations.CategoryOperation
import ru.ac.uniyar.domain.operations.ListParentCategoryOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.NewCategoryDataVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

private val categoryNameField = FormField.nonEmptyString().required("categoryName")
private val parentCategoryNameField = FormField.nonEmptyString().required("parentCategoryName")
private val feedbackFormBody = Body.webForm(
    Validator.Feedback,
    categoryNameField,
    parentCategoryNameField
).toLens()

fun categoryForm(
    view: ContextAwareViewRender,
    listParentCategory: ListParentCategoryOperation,
    categoryOperation: CategoryOperation,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canEditCategories).then(
        showNewCategoryForm(view, listParentCategory, permissionLens)
    ),
    "/" bind Method.POST to permissionFilter(permissionLens, Permission::canEditCategories).then(
        addNewCategory(view, listParentCategory, categoryOperation, permissionLens)
    )
)

private fun showNewCategoryForm(
    view: ContextAwareViewRender,
    listParentCategory: ListParentCategoryOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditCategories) {
        return@handler Response(Status.FORBIDDEN)
    }

    val listParentCategory = listParentCategory.list()
    val viewModel = NewCategoryDataVM(listParentCategory)
    Response(Status.OK).with(view(request) of viewModel)
}

private fun addNewCategory(
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
    if (webForm.errors.isNotEmpty()) {
        val listParentCategory = listParentCategory.list()
        return@handler Response(Status.OK).with(view(request) of NewCategoryDataVM(listParentCategory, webForm))
    }

    val categoryName = categoryNameField(webForm)

    if (categoryOperation.isNotNewCategoryName(categoryName)) {
        val newError = webForm.errors + Invalid(
            categoryNameField.meta.copy(description = "уже существует категория с таким именем.")
        )
        val newWebForm = webForm.copy(errors = newError)
        val listParentCategory = listParentCategory.list()
        return@handler Response(Status.OK).with(view(request) of NewCategoryDataVM(listParentCategory, newWebForm))
    }

    val parentCategoryName = parentCategoryNameField(webForm)
    categoryOperation.addCategory(categoryName, parentCategoryName)
    Response(Status.FOUND).header("Location", "/category")
}
