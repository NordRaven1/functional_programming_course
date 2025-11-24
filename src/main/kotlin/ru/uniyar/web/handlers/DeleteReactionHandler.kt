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
import ru.uniyar.domain.deleteReaction
import ru.uniyar.domain.fetchMessageByNumber
import ru.uniyar.domain.fetchThemeByNumber
import ru.uniyar.domain.findReactionInList
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
        val themeAndMessages = fetchThemeByNumber(themes, themeId) ?: return notFoundResponse
        val messageId = lensOrNull(messageIdLens, request) ?: return notFoundResponse
        val message = fetchMessageByNumber(themeAndMessages.messages, messageId) ?: return notFoundResponse
        val reactionNum = lensOrNull(reactionNumberLens, request)
        if (reactionNum == null || reactionNum > message.listOfReactions.lastIndex || reactionNum <= -1) {
            return notFoundResponse
        }
        val form = deleteLens(request)
        val agreement = form.fields["agreement"]
        return if (agreement != null && agreement.size > 0) {
            val updatedThemes = deleteReaction(themes, themeAndMessages, message, reactionNum)
            createResultWithThemes(
                Response(FOUND).header(
                    "Location",
                    "/themes/theme/$themeId/message/$messageId",
                ),
                updatedThemes,
            )
        } else {
            val reaction = findReactionInList(message, reactionNum)
            val reactionAuthor = findFirstUserById(users, reaction.author)
            val reactionStruct = AuthorStructure(reaction, reactionAuthor.userName)
            val model = DeleteReactionDataVM(reactionStruct, true)
            createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
    }
}
