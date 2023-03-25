package ru.ac.uniyar.web.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.ac.uniyar.domain.operations.PermissionOperation
import ru.ac.uniyar.domain.storage.Permission
import ru.ac.uniyar.domain.storage.User

fun authorizationFilter(
    currentUser: RequestContextLens<User?>,
    permissionLens: RequestContextLens<Permission>,
    permissionOperation: PermissionOperation
): Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val permission = currentUser(request)?.let {
            permissionOperation.getPermissionByUserRole(it.userRole)
        } ?: Permission("Гость")
        val authorizedRequest = request.with(permissionLens of permission)
        next(authorizedRequest)
    }
}
