package ru.ac.uniyar.web.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.cookie.cookie
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.ac.uniyar.domain.operations.UserOperation
import ru.ac.uniyar.domain.storage.User

fun authenticationFilter(
    currentUser: RequestContextLens<User?>,
    userOperation: UserOperation,
    jwtTools: JwtTools
): Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val requestWithUser = request.cookie("auth_token")?.value?.let { token ->
            userOperation.getUserWithToken(token, jwtTools)
        }?.let { user ->
            request.with(currentUser of user)
        } ?: request
        next(requestWithUser)
    }
}
