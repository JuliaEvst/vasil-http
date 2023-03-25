package ru.ac.uniyar.web.handlers

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import org.http4k.core.cookie.cookie
import org.http4k.core.with
import org.http4k.lens.Invalid
import org.http4k.lens.Validator
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.domain.operations.UserOperation
import ru.ac.uniyar.web.filters.JwtTools
import ru.ac.uniyar.web.models.SignInVM
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

private val feedbackFormBody = Body.webForm(
    Validator.Feedback,
    userNameField,
    userPasswordField,
).toLens()

fun signInForm(
    view: ContextAwareViewRender,
    userOperation: UserOperation,
    salt: String,
    jwtTools: JwtTools,
) = routes(
    "/" bind Method.GET to showSignInForm(view),
    "/" bind Method.POST to doSignInForm(view, userOperation, salt, jwtTools)
)

private fun showSignInForm(
    view: ContextAwareViewRender,
): HttpHandler = {
    val viewModel = SignInVM()
    Response(Status.OK).with(view(it) of viewModel)
}

private fun doSignInForm(
    view: ContextAwareViewRender,
    userOperation: UserOperation,
    salt: String,
    jwtTools: JwtTools,
): HttpHandler = handler@{ request ->
    var webForm = feedbackFormBody(request)

    if (webForm.errors.isNotEmpty()) {
        return@handler Response(Status.BAD_REQUEST).with(view(request) of SignInVM(webForm))
    }

    val user = userOperation.getUser(webForm, salt)
    if (user == null) {
        val newError = webForm.errors + Invalid(
            userPasswordField.meta.copy(description = "логин или пароль неверны.")
        )
        val newWebForm = webForm.copy(errors = newError)
        return@handler Response(Status.BAD_REQUEST).with(view(request) of SignInVM(newWebForm))
    }

    val token = jwtTools.create(user.id.toString()) ?: return@handler Response(Status.INTERNAL_SERVER_ERROR)
    val authCookie = Cookie("auth_token", token, httpOnly = true, sameSite = SameSite.Strict)
    Response(Status.FOUND).header("Location", "/").cookie(authCookie)
}
