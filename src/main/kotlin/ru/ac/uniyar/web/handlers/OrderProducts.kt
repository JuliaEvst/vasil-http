package ru.ac.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.FormField
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.nonEmptyString
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.InformationOperation
import ru.ac.uniyar.domain.operations.ShoppingCartProductsOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.domain.storage.User
import ru.ac.uniyar.web.filters.permissionFilter
import ru.ac.uniyar.web.models.OrderProductsVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

private val addressField = FormField.nonEmptyString().required("address")
private val feedbackFormBody = Body.webForm(
    Validator.Feedback,
    addressField
).toLens()

fun orderProductsForm(
    view: ContextAwareViewRender,
    shoppingCartProductsOperation: ShoppingCartProductsOperation,
    informationOperation: InformationOperation,
    currentUserLens: RequestContextLens<User?>,
    permissionLens: RequestContextLens<Permission>
) = routes(
    "/" bind Method.GET to permissionFilter(permissionLens, Permission::canOrder).then(
        showOrderProductsForm(
            view,
            shoppingCartProductsOperation,
            informationOperation,
            currentUserLens,
            permissionLens
        )
    ),
    "/" bind Method.POST to permissionFilter(permissionLens, Permission::canOrder).then(
        orderProducts(view, shoppingCartProductsOperation, currentUserLens, permissionLens)
    )
)

private fun showOrderProductsForm(
    view: ContextAwareViewRender,
    shoppingCartProductsOperation: ShoppingCartProductsOperation,
    informationOperation: InformationOperation,
    currentUserLens: RequestContextLens<User?>,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canOrder) {
        return@handler Response(Status.FORBIDDEN)
    }

    val currentUser = currentUserLens(request) ?: return@handler Response(Status.UNAUTHORIZED)
    val infoList = informationOperation.getListForUserById(currentUser.id)

    if (infoList.isEmpty() && shoppingCartProductsOperation.orderListIsNotEmpty(currentUser.id)) {
        val viewModel = OrderProductsVM(currentUser)
        Response(Status.OK).with(view(request) of viewModel)
    } else {
        Response(Status.BAD_REQUEST)
    }
}

private fun orderProducts(
    view: ContextAwareViewRender,
    shoppingCartProductsOperation: ShoppingCartProductsOperation,
    currentUserLens: RequestContextLens<User?>,
    permissionLens: RequestContextLens<Permission>
): HttpHandler = handler@{ request ->
    val permission = permissionLens(request)
    if (!permission.canOrder) {
        return@handler Response(Status.FORBIDDEN)
    }

    val currentUser = currentUserLens(request) ?: return@handler Response(Status.UNAUTHORIZED)
    val webForm = feedbackFormBody(request)
    if (webForm.errors.isEmpty()) {
        val newAddress = addressField(webForm)
        shoppingCartProductsOperation.order(currentUser.id, newAddress)
        Response(Status.FOUND).header("Location", "/shopping_cart")
    } else {
        Response(Status.OK).with(view(request) of OrderProductsVM(currentUser, webForm))
    }
}
