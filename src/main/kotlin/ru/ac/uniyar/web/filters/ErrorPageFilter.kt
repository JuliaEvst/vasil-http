package ru.ac.uniyar.web.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.with
import ru.ac.uniyar.web.models.ErrorPage
import ru.ac.uniyar.web.models.templates.ContextAwareViewRender

fun errorFilter(
    view: ContextAwareViewRender,
) = Filter { next: HttpHandler ->
    { request: Request ->
        val response = next(request) // тут ошибка
        if (response.status.successful && response.headers.isNotEmpty()) {
            response
        } else {
            response.status(response.status).with(view(request) of ErrorPage())
        }
    }
}
