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
import ru.uniyar.domain.addReactionToMessage
import ru.uniyar.domain.createReaction
import ru.uniyar.web.models.NewReactionDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class CreateNewReactionHandler(
    val lens: ContextAwareViewRender,
    val sharedStateLens: RequestContextLens<SharedState?>,
) : StatefulHandler {
    override fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult {
        val notFoundResponse = createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val themeId =
            lensOrNull(themeIdLens, request)
                ?: return notFoundResponse
        val themeAndMessages =
            themes.fetchThemeByNumber(themeId)
                ?: return notFoundResponse
        val messageId =
            lensOrNull(messageIdLens, request)
                ?: return notFoundResponse
        val message =
            themeAndMessages.messages.fetchMessageByNumber(messageId)
                ?: return notFoundResponse
        val form = reactionFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = NewReactionDataVM(form, reactionsList, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val user = sharedStateLens(request) ?: return createResult(Response(BAD_REQUEST))
        val authorId = user.userId
        val reactionType = reactionTypeField(form)
        val reaction = reactionsList.find { reaction -> reaction == reactionType } ?: 10067
        val newReaction = createReaction(reaction, authorId)
        val updatedReactions = addReactionToMessage(message, newReaction)
        val updatedMessages = themeAndMessages.messages.updateMessageReactions(messageId, updatedReactions)
        val updatedThemeAndMessages = themeAndMessages.copy(messages = updatedMessages)
        val updatedThemes = themes.replaceTheme(themeId, updatedThemeAndMessages)
        return createResultWithThemes(
            Response(FOUND).header(
                "Location",
                "/themes/theme/$themeId/message/${message.id}",
            ),
            updatedThemes,
        )
    }
}
