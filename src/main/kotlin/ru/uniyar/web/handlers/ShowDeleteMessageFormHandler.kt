package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.SharedState
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.findFirstUserById
import ru.uniyar.domain.AuthorStructure
import ru.uniyar.domain.Themes
import ru.uniyar.domain.fetchMessageByNumber
import ru.uniyar.domain.fetchThemeByNumber
import ru.uniyar.web.models.DeleteMessageDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowDeleteMessageFormHandler(
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
    val sharedStateLens: RequestContextLens<SharedState?>,
) : StateReadingHandler {
    override fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response {
        val role = permissionLens(request)
        val user = sharedStateLens(request) ?: return Response(FORBIDDEN)
        val notFoundResponse = Response(NOT_FOUND).with(lens(request) of errorModel)
        val themeId = lensOrNull(themeIdLens, request) ?: return notFoundResponse
        val themeAndMessages = fetchThemeByNumber(themes, themeId) ?: return notFoundResponse
        val messageId = lensOrNull(messageIdLens, request) ?: return notFoundResponse
        val message = fetchMessageByNumber(themeAndMessages.messages, messageId) ?: return notFoundResponse
        if (role.canDeleteMessage && (
                message.author == user.userId || role.name == "ADMIN" ||
                    role.name == "MODERATOR"
            )
        ) {
            val author = findFirstUserById(users, message.author)
            val messageStruct = AuthorStructure(message, author.userName)
            val reactions = formReactionList(users, message)
            val model = DeleteMessageDataVM(messageStruct, reactions)
            return Response(OK).with(lens(request) of model)
        }
        return Response(FORBIDDEN)
    }
}
