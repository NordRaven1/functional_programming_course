package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.SharedState
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Messages
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import ru.uniyar.domain.createTheme
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
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = NewThemeDataVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val title = themeTitleField(form)
        val themeCheck =
            themes.themesList.find { allThemes ->
                allThemes.theme.title.replace(" ", "")
                    .equals(title.replace(" ", ""), true)
            }
        if (themeCheck != null) {
            val failures = formFailureInfoList(form.errors)
            failures.add("Такая тема уже существует!")
            val model = NewThemeDataVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val user = sharedStateLens(request) ?: return createResult(Response(BAD_REQUEST))
        val authorId = user.userId
        val newTheme = createTheme(title, authorId)
        val newThemeAndMessages = ThemeAndMessages(newTheme, Messages(emptyList()))
        val updatedThemes = themes.add(newThemeAndMessages)
        return createResultWithThemes(
            Response(FOUND).header("Location", "/themes"),
            updatedThemes,
        )
    }
}
