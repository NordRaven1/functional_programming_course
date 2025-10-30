package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.EditUserDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class EditUserHandler(
    val lens: ContextAwareViewRender,
) : StatefulHandler {
    override fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult {
        val userId =
            lensOrNull(userIdLens, request)
                ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val user = users.findUserById(userId) ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val form = editRoleFormLens(request)
        if (form.errors.isNotEmpty()) {
            val failures = formFailureInfoList(form.errors)
            val model = EditUserDataVM(user, form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val role = roleField(form)
        val newPermissions = rolesList.find { it.name == role } ?: rolesList.get(1)
        val updatedUsers = users.editUser(userId, user.copy(role = newPermissions))
        return createResultWithUsers(
            Response(FOUND).header("Location", "/users"),
            updatedUsers,
        )
    }
}
