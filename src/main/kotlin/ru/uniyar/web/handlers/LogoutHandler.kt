package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.invalidateCookie
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes

class LogoutHandler : StateReadingHandler {
    override fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response {
        val response = Response(Status.FOUND).header("Location", "/")
        return response.invalidateCookie("auth")
    }
}
