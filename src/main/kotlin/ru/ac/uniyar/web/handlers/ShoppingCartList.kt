package ru.ac.uniyar.web.handlers

import lensOrDefault
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.FormField
import org.http4k.lens.Query
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.int
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.InformationOperation
import ru.ac.uniyar.domain.operations.ListCartOperation
import ru.ac.uniyar.domain.operations.ShoppingCartProductsOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.domain.storage.User
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.ShoppingCartListVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender
import kotlin.math.ceil

private val pageLens = Query.int().defaulted("page", 1)
private const val PAGE_SIZE = 3

fun shoppingCartList(
    view: ContextAwareViewRender,
    listCartOperation: ListCartOperation,
    shoppingCartProductsOperation: ShoppingCartProductsOperation,
    informationOperation: InformationOperation,
    currentUserLens: RequestContextLens<User?>,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canSeeCart).then(
        showShoppingCartList(view, listCartOperation, informationOperation, currentUserLens, permissionLens)
    ),
    "/" bind Method.POST to permissionFilter(permissionLens, Permission::canSeeCart).then(
        deleteProductFromCart(shoppingCartProductsOperation, permissionLens)
    ),
    "/clearInfo" bind Method.GET to permissionFilter(permissionLens, Permission::canSeeCart).then(
        clearInfo(currentUserLens, informationOperation, permissionLens)
    ),
)

private fun showShoppingCartList(
    view: ContextAwareViewRender,
    listCartOperation: ListCartOperation,
    informationOperation: InformationOperation,
    currentUserLens: RequestContextLens<User?>,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canSeeCart) {
        return@handler Response(Status.FORBIDDEN)
    }

    var pageLens = lensOrDefault(pageLens, request, 1)
    val currentUser = currentUserLens(request) ?: return@handler Response(Status.BAD_REQUEST)
    val lastPage = (ceil(listCartOperation.count(currentUser) / PAGE_SIZE.toDouble())).toInt()

    if (pageLens > lastPage || pageLens < 1) {
        pageLens = 1
    }

    val list = listCartOperation.subList(pageLens, PAGE_SIZE, currentUser)
    val infoList = informationOperation.getListForUserById(currentUser.id)

    val viewModel = ShoppingCartListVM(list, request.uri.path, pageLens, lastPage, infoList, currentUser.address)
    Response(Status.OK).with(view(request) of viewModel)
}

private fun deleteProductFromCart(
    shoppingCartProductsOperation: ShoppingCartProductsOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canSeeCart) {
        return@handler Response(Status.FORBIDDEN)
    }

    val idLens = FormField.int().required("id")
    val feedback = Body.webForm(Validator.Feedback, idLens).toLens()
    val id = feedback(request).fields["id"].orEmpty().toString().replace("[", "").replace("]", "").toInt()

    shoppingCartProductsOperation.deleteByUser(id)

    Response(Status.FOUND).header("Location", "/shopping_cart")
}

private fun clearInfo(
    currentUserLens: RequestContextLens<User?>,
    informationOperation: InformationOperation,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canSeeCart) {
        return@handler Response(Status.FORBIDDEN)
    }

    val user = currentUserLens(request) ?: return@handler Response(Status.UNAUTHORIZED)

    informationOperation.clearListForUser(user.id)
    Response(Status.FOUND).header("Location", "/shopping_cart")
}
