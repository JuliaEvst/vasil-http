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
import ru.ac.uniyar.domain.operations.ProductsOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.web.filters.permissionFilter

fun productDelete(
    productsOperation: ProductsOperation,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canDeleteProduct).then(
        deleteProduct(productsOperation, permissionLens)
    )
)

private fun deleteProduct(
    productsOperation: ProductsOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canDeleteProduct) {
        return@handler Response(Status.FORBIDDEN)
    }

    val index = request.path("number").orEmpty().toIntOrNull() ?: return@handler Response(Status.BAD_REQUEST)
    val product = productsOperation.fetch(index) ?: return@handler Response(Status.BAD_REQUEST)

    productsOperation.delete(product)
    Response(Status.FOUND).header("Location", "/product")
}
