package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.queries
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.domain.fetchThemeByNumber
import ru.uniyar.domain.getMessagesPerPage
import ru.uniyar.unsafeDateInFormat
import ru.uniyar.web.models.MessageListPageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class MessageListHandler(
    val lens: ContextAwareViewRender,
) : StateReadingHandler {
    override fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response {
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val theme = fetchThemeByNumber(themes, themeId) ?: return Response(NOT_FOUND).with(lens(request) of errorModel)
        val queries = request.uri.queries()
        val mindate = parseQueryParam(
            queries = queries,
            paramName = "mindate",
            parser = { unsafeDateInFormat(it, "yyyy-MM-dd'T'HH:mm") },
            default = null
        )
        val maxdate = parseQueryParam(
            queries = queries,
            paramName = "maxdate",
            parser = { unsafeDateInFormat(it, "yyyy-MM-dd'T'HH:mm") },
            default = null
        )
        val pageNum = parseQueryParam(
            queries = queries,
            paramName = "page",
            parser = { it.toIntOrNull() },
            default = 1
        )
        val paginator = getMessagesPerPage(theme.messages, users, mindate, maxdate, pageNum, request.uri)
        val model = MessageListPageVM(paginator, mindate, maxdate, theme.theme.addPossibility, theme.theme)
        return Response(OK).with(lens(request) of model)
    }
}
