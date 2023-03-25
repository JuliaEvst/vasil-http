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
import ru.ac.uniyar.domain.operations.ParentCategoryOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter

fun parentCategoryDelete(
    parentCategoryOperation: ParentCategoryOperation,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canEditCategories).then(
        deleteParent(parentCategoryOperation, permissionLens)
    )
)

private fun deleteParent(
    parentCategoryOperation: ParentCategoryOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canEditCategories) {
        return@handler Response(Status.FORBIDDEN)
    }

    val parentCategoryName = request.path("parentCategoryName") ?: return@handler Response(Status.BAD_REQUEST)
    parentCategoryOperation.deleteParentCategory(parentCategoryName)
    Response(Status.FOUND).header("Location", "/parent_category")
}
