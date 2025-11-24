package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.MainPageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowAppInfoHandler(val lens: ContextAwareViewRender) : StateReadingHandler {
    override fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response {
        val model = MainPageVM()
        return Response(OK).with(lens(request) of model)
    }
}
