package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.queries
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.domain.getThemesPerPage
import ru.uniyar.unsafeDateInFormat
import ru.uniyar.web.models.ThemesListPageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ThemesListHandler(
    val lens: ContextAwareViewRender,
) : StateReadingHandler {
    override fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response {
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
        val themeSearch = parseQueryParam(
            queries = queries,
            paramName = "theme",
            parser = { it },
            default = null
        )
        val pageNum = parseQueryParam(
            queries = queries,
            paramName = "page",
            parser = { it.toIntOrNull() },
            default = 1
        )
        val paginator = getThemesPerPage(themes, users, themeSearch, mindate, maxdate, pageNum, request.uri)
        val model = ThemesListPageVM(paginator, mindate, maxdate, themeSearch)
        return Response(OK).with(lens(request) of model)
    }
}
