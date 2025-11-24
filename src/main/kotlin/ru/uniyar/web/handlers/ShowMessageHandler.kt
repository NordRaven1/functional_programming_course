package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.findFirstUserById
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Themes
import ru.uniyar.domain.fetchMessageByNumber
import ru.uniyar.domain.fetchThemeByNumber
import ru.uniyar.web.models.MessagePageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowMessageHandler(
    val lens: ContextAwareViewRender,
) : StateReadingHandler {
    override fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response {
        val notFoundResponse = Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeId = lensOrNull(themeIdLens, request) ?: return notFoundResponse
        val theme = fetchThemeByNumber(themes, themeId) ?: return notFoundResponse
        val messageId = lensOrNull(messageIdLens, request) ?: return notFoundResponse
        val message = fetchMessageByNumber(theme.messages, messageId) ?: return notFoundResponse
        val author = findFirstUserById(users, message.author)
        val messageStruct = AuthorStructure(message, author.userName)
        val reactions = formReactionList(users, message)
        val model = MessagePageVM(messageStruct, reactions)
        return Response(OK).with(lens(request) of model)
    }
}
