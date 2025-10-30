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
import ru.uniyar.domain.removeReactionFromMessage
import ru.uniyar.web.models.DeleteReactionDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class DeleteReactionHandler(
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
        val reactuinNum = lensOrNull(reactionNumberLens, request)
        if (reactuinNum == null || reactuinNum > message.listOfReactions.lastIndex || reactuinNum <= -1) {
            return notFoundResponse
        }
        val form = deleteLens(request)
        return if (form.fields["agreement"]?.isNotEmpty() == true) {
            val updatedReactions = removeReactionFromMessage(message, reactuinNum)
            val updatedMessages = themeAndMessages.messages.updateMessageReactions(messageId, updatedReactions)
            val updatedThemeAndMessages = themeAndMessages.copy(messages = updatedMessages)
            val updatedThemes = themes.replaceTheme(themeId, updatedThemeAndMessages)
            createResultWithThemes(
                Response(FOUND).header(
                    "Location",
                    "/themes/theme/$themeId/message/$messageId",
                ),
                updatedThemes,
            )
        } else {
            val reaction = message.listOfReactions[reactuinNum]
            val reactionAuthor = users.usersList.first { it.userId == reaction.author }
            val reactionStruct = AuthorStructure(reaction, reactionAuthor.userName)
            val model = DeleteReactionDataVM(reactionStruct, true)
            createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
    }
}
