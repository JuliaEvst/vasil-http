package ru.ac.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.lens.RequestContextLens
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.CategoryOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter

fun categoryDelete(
    categoryOperation: CategoryOperation,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canEditCategories).then(
        deleteCategory(categoryOperation, permissionLens)
    )
)

private fun deleteCategory(
    categoryOperation: CategoryOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditCategories) {
        return@handler Response(Status.FORBIDDEN)
    }

    val categoryName = request.path("categoryName") ?: return@handler Response(Status.BAD_REQUEST)
    categoryOperation.deleteCategory(categoryName)
    Response(Status.FOUND).header("Location", "/category")
}
