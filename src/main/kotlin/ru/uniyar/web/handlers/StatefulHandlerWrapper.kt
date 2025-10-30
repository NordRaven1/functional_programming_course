package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes

interface StatefulHandler {
    fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult
}

fun wrapStatefulHandler(
    handler: StatefulHandler,
    themes: MutableRef<Themes>,
    users: MutableRef<Users>,
): HttpHandler =
    { request ->
        val result = handler.invokeWithState(request, themes.value, users.value)

        result.newThemes?.let { themes.value = it }
        result.newUsers?.let { users.value = it }

        result.response
    }

class MutableRef<T>(var value: T)
