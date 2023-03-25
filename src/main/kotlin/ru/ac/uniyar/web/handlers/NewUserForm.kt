package ru.ac.uniyar.web.handlers

import lensOrNull
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.FormField
import org.http4k.lens.Invalid
import org.http4k.lens.Validator
import org.http4k.lens.nonEmptyString
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.UserOperation
import ru.ac.uniyar.web.models.NewUserDataVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

val userNameField = FormField.nonEmptyString().required("name")
val userAddressField = FormField.nonEmptyString().required("address")
val userPasswordField = FormField.nonEmptyString().required("password")
private val userPasswordTwoField = FormField.nonEmptyString().required("passwordTwo")

private val feedbackFormBody = Body.webForm(
    Validator.Feedback,
    userNameField,
    userAddressField,
    userPasswordField,
    userPasswordTwoField
).toLens()

fun signUpForm(
    view: ContextAwareViewRender,
    userOperation: UserOperation,
    salt: String,
) = routes(
    "/" bind Method.GET to showNewUserForm(view),
    "/" bind Method.POST to createNewUserWithLens(view, userOperation, salt)
)

private fun showNewUserForm(
    view: ContextAwareViewRender,
): HttpHandler = {
    val viewModel = NewUserDataVM()
    Response(Status.OK).with(view(it) of viewModel)
}

private fun createNewUserWithLens(
    view: ContextAwareViewRender,
    userOperation: UserOperation,
    salt: String,
): HttpHandler = { request ->
    var webForm = feedbackFormBody(request)

    val firstPassword = lensOrNull(userPasswordField, webForm)
    val secondPassword = lensOrNull(userPasswordTwoField, webForm)
    if (firstPassword != null && firstPassword != secondPassword) {
        val newError = webForm.errors + Invalid(userPasswordField.meta.copy(description = "разные пароли."))
        webForm = webForm.copy(errors = newError)
    }

    if (webForm.errors.isEmpty()) {
        userOperation.addUser(webForm, salt)
        Response(Status.FOUND).header("Location", "/")
    } else {
        Response(Status.BAD_REQUEST).with(view(request) of NewUserDataVM(webForm))
    }
}
