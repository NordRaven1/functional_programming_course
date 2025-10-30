package ru.uniyar.web.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.findSingle
import org.http4k.core.queries
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.unsafeDateInFormat
import ru.uniyar.web.models.ThemesListPageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ThemesListHandler(
    val lens: ContextAwareViewRender,
    val themes: Themes,
    val users: Users,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val mindate =
            unsafeDateInFormat(
                request.uri.queries().findSingle("mindate"),
                "yyyy-MM-dd'T'HH:mm",
            )
        val maxdate =
            unsafeDateInFormat(
                request.uri.queries().findSingle("maxdate"),
                "yyyy-MM-dd'T'HH:mm",
            )
        val themeSearch = request.uri.queries().findSingle("theme")
        val pageNum = request.uri.queries().findSingle("page")?.toIntOrNull() ?: 1

        val paginator = themes.getThemesPerPage(users, themeSearch, mindate, maxdate, pageNum, request.uri)
        val model = ThemesListPageVM(paginator, mindate, maxdate, themeSearch)
        return Response(OK).with(lens(request) of model)
    }
}
