package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.DeleteThemeDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class DeleteThemeHandler(
    val lens: ContextAwareViewRender,
) : StatefulHandler {
    override fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult {
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val theme =
            themes.fetchThemeByNumber(themeId)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val form = deleteLens(request)
        return if (form.fields["agreement"]?.isNotEmpty() == true) {
            val updatedThemes = themes.removeTheme(themeId)
            createResultWithThemes(
                Response(FOUND).header("Location", "/themes"),
                updatedThemes,
            )
        } else {
            val themeAuthor = users.usersList.first { it.userId == theme.theme.author }
            val themeStruct = AuthorStructure(theme, themeAuthor.userName)
            val model = DeleteThemeDataVM(themeStruct, true)
            createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
    }
}
