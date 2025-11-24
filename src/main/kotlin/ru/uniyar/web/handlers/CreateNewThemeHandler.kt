package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.SharedState
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.domain.addNewTheme
import ru.uniyar.domain.findThemeByNormalizedTitle
import ru.uniyar.web.models.NewThemeDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class CreateNewThemeHandler(
    val lens: ContextAwareViewRender,
    val sharedStateLens: RequestContextLens<SharedState?>,
) : StatefulHandler {
    override fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult {
        val form = themeFormLens(request)
        if (isListNotEmpty(form.errors)) {
            val failures = formFailureInfoList(form.errors)
            val model = NewThemeDataVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val title = themeTitleField(form)
        val themeCheck = findThemeByNormalizedTitle(themes, title)
        if (themeCheck != null) {
            val failures = formFailureInfoList(form.errors)
            addFailureInList(failures, "Такая тема уже существует!")
            val model = NewThemeDataVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val user = sharedStateLens(request) ?: return createResult(Response(BAD_REQUEST))
        val authorId = user.userId
        val updatedThemes = addNewTheme(themes, title, authorId)
        return createResultWithThemes(
            Response(FOUND).header("Location", "/themes"),
            updatedThemes,
        )
    }
}
