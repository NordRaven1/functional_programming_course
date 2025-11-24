package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.getDisplayableUsers
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.UserListVM
import ru.uniyar.web.templates.ContextAwareViewRender

class UserListHandler(
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
) : StateReadingHandler {
    override fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response {
        val role = permissionLens(request)
        if (role.name == "ADMIN" || role.name == "MODERATOR") {
            val model = UserListVM(getDisplayableUsers(users))
            return Response(OK).with(lens(request) of model)
        }
        return Response(FORBIDDEN)
    }
}
