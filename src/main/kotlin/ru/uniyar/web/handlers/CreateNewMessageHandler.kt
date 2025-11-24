package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.SharedState
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.domain.addNewMessage
import ru.uniyar.domain.fetchThemeByNumber
import ru.uniyar.web.models.NewMessageDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class CreateNewMessageHandler(
    val lens: ContextAwareViewRender,
    val sharedStateLens: RequestContextLens<SharedState?>,
) : StatefulHandler {
    override fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult {
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val themeAndMessages =
            fetchThemeByNumber(themes, themeId)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val form = messageFormLens(request)
        if (isListNotEmpty(form.errors)) {
            val failures = formFailureInfoList(form.errors)
            val model = NewMessageDataVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val user = sharedStateLens(request) ?: return createResult(Response(BAD_REQUEST))
        val authorId = user.userId
        val text = messageTextField(form)
        val (updatedThemes, newMessage) = addNewMessage(themeAndMessages, themes, authorId, text)
        return createResultWithThemes(
            Response(FOUND).header(
                "Location",
                "/themes/theme/$themeId/message/${newMessage.id}",
            ),
            updatedThemes,
        )
    }
}
