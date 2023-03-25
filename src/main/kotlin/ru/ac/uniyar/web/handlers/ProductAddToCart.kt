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
import ru.ac.uniyar.domain.operations.ShoppingCartProductsOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.domain.storage.User
import ru.ac.uniyar.web.filters.permissionFilter

fun productAddToCart(
    productsOperation: ProductsOperation,
    shoppingCartProductsOperation: ShoppingCartProductsOperation,
    currentUserLens: RequestContextLens<User?>,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canAddProductInCart).then(
        addToShoppingCart(shoppingCartProductsOperation, productsOperation, currentUserLens, permissionLens)
    )
)

private fun addToShoppingCart(
    shoppingCartProductsOperation: ShoppingCartProductsOperation,
    productsOperation: ProductsOperation,
    currentUserLens: RequestContextLens<User?>,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canAddProductInCart) {
        return@handler Response(Status.FORBIDDEN)
    }

    val index = request.path("number").orEmpty().toIntOrNull() ?: return@handler Response(Status.BAD_REQUEST)
    val countToCart = request.path("countToCart").orEmpty().toIntOrNull() ?: return@handler Response(Status.BAD_REQUEST)
    val currentUser = currentUserLens(request) ?: return@handler Response(Status.UNAUTHORIZED)
    val product = productsOperation.fetch(index) ?: return@handler Response(Status.BAD_REQUEST)

    shoppingCartProductsOperation.add(product, currentUser.id, countToCart)
    Response(Status.FOUND).header("Location", "/product/$index")
}
