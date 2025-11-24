package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.domain.editMessage
import ru.uniyar.domain.fetchMessageByNumber
import ru.uniyar.domain.fetchThemeByNumber
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
        val themeAndMessages = fetchThemeByNumber(themes, themeId) ?: return notFoundResponse
        val messageId = lensOrNull(messageIdLens, request) ?: return notFoundResponse
        val message = fetchMessageByNumber(themeAndMessages.messages, messageId) ?: return notFoundResponse
        val form = messageFormLens(request)
        if (isListNotEmpty(form.errors)) {
            val failures = formFailureInfoList(form.errors)
            val model = EditMessageDataVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val text = messageTextField(form)
        val updatedThemes = editMessage(themes, themeAndMessages, message, text)
        return createResultWithThemes(
            Response(FOUND).header(
                "Location",
                "/themes/theme/$themeId/message/$messageId",
            ),
            updatedThemes,
        )
    }
}
