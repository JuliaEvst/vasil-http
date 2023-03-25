package ru.ac.uniyar.web.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import ru.ac.uniyar.domain.storage.Permission

fun permissionFilter(
    permissionLens: RequestContextLens<Permission>,
    canUse: (Permission) -> Boolean
): Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val response = next(request)
        val permission = permissionLens(request)
        if (permission.let(canUse)) {
            response
        } else {
            // Response(Status.FORBIDDEN)
            response.status(Status.FORBIDDEN)
        }
    }
}
