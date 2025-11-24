package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.UserEditAction
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.editUser
import ru.uniyar.authorization.findUserById
import ru.uniyar.domain.Themes
import ru.uniyar.web.templates.ContextAwareViewRender

class BanUserHandler(
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
) : StatefulHandler {
    override fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult {
        val role = permissionLens(request)
        if (role.name != "ADMIN" && role.name != "MODERATOR") return createResult(Response(FORBIDDEN))
        val userId =
            lensOrNull(userIdLens, request)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val user = findUserById(users, userId) ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val updatedUsers = editUser(users, user.userId, UserEditAction.ResetToDefaultRole)
        return createResultWithUsers(
            Response(FOUND).header(
                "Location",
                "/users",
            ),
            updatedUsers,
        )
    }
}
