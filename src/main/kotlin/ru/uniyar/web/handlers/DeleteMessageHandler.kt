package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Reaction
import ru.uniyar.domain.Themes
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
            themes.fetchThemeByNumber(themeId)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val messageId =
            lensOrNull(messageIdLens, request)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val message =
            themeAndMessages.messages.fetchMessageByNumber(messageId)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val form = deleteLens(request)
        return if (form.fields["agreement"]?.isNotEmpty() == true) {
            val updatedMessages = themeAndMessages.messages.removeMessage(messageId)
            val updatedThemeAndMessages = themeAndMessages.copy(messages = updatedMessages)
            val updatedThemes = themes.replaceTheme(themeId, updatedThemeAndMessages)
            createResultWithThemes(
                Response(FOUND).header("Location", "/themes/theme/$themeId"),
                updatedThemes,
            )
        } else {
            val author = users.usersList.first { it.userId == message.author }
            val messageStruct = AuthorStructure(message, author.userName)
            val reactions = mutableListOf<AuthorStructure<Reaction>>()
            for (reaction in message.listOfReactions) {
                val reactionAuthor = users.usersList.first { it.userId == reaction.author }
                reactions.add(AuthorStructure(reaction, reactionAuthor.userName))
            }
            val model = DeleteMessageDataVM(messageStruct, reactions, true)
            createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
    }
}
