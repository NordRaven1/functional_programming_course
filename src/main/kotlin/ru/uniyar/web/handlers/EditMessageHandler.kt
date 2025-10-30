package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.domain.createMessage
import ru.uniyar.web.models.EditMessageDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class EditMessageHandler(
    val lens: ContextAwareViewRender,
) : StatefulHandler {
    override fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult {
        val notFoundResponse = createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val themeId = lensOrNull(themeIdLens, request) ?: return notFoundResponse
        val themeAndMessages = themes.fetchThemeByNumber(themeId) ?: return notFoundResponse
        val messageId = lensOrNull(messageIdLens, request) ?: return notFoundResponse
        val message = themeAndMessages.messages.fetchMessageByNumber(messageId) ?: return notFoundResponse
        val form = messageFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = EditMessageDataVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val text = messageTextField(form)
        val newMessage = createMessage(themeAndMessages.theme, message.author, text, message.listOfReactions)
        val updatedMessages = themeAndMessages.messages.replaceMessage(messageId, newMessage)
        val updatedThemeAndMessages = themeAndMessages.copy(messages = updatedMessages)
        val updatedThemes = themes.replaceTheme(themeId, updatedThemeAndMessages)
        return createResultWithThemes(
            Response(FOUND).header(
                "Location",
                "/themes/theme/$themeId/message/$messageId",
            ),
            updatedThemes,
        )
    }
}
