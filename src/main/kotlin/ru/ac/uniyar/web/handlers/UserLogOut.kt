package ru.ac.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.invalidateCookie
import org.http4k.routing.bind
import org.http4k.routing.routes

fun logOutUser() = routes(
    "/" bind Method.GET to doLogOutUser()
)

private fun doLogOutUser(): HttpHandler = {
    Response(Status.FOUND).header("Location", "/").invalidateCookie("auth_token")
}
