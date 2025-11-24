package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes

interface StatefulHandler {
    fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult
}

interface StateReadingHandler {
    fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response
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

fun wrapStateReadingHandler(
    handler: StateReadingHandler,
    themes: MutableRef<Themes>,
    users: MutableRef<Users>,
): HttpHandler =
    { request ->
        handler.invokeWithContext(request, themes.value, users.value)
    }

class MutableRef<T>(var value: T)
