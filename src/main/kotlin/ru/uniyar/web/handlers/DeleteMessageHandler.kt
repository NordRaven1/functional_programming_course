package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.findFirstUserById
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Themes
import ru.uniyar.domain.deleteMessage
import ru.uniyar.domain.fetchMessageByNumber
import ru.uniyar.domain.fetchThemeByNumber
import ru.uniyar.web.models.DeleteMessageDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class DeleteMessageHandler(
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
        val themeAndMessages =
            fetchThemeByNumber(themes, themeId)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val messageId =
            lensOrNull(messageIdLens, request)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val message =
            fetchMessageByNumber(themeAndMessages.messages, messageId)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val form = deleteLens(request)
        val agreement = form.fields["agreement"]
        return if (agreement != null && agreement.size > 0) {
            val updatedThemes = deleteMessage(themes, themeAndMessages, message)
            createResultWithThemes(
                Response(FOUND).header("Location", "/themes/theme/$themeId"),
                updatedThemes,
            )
        } else {
            val author = findFirstUserById(users, message.author)
            val messageStruct = AuthorStructure(message, author.userName)
            val reactions = formReactionList(users, message)
            val model = DeleteMessageDataVM(messageStruct, reactions, true)
            createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
    }
}
