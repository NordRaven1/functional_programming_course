package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.with
import ru.uniyar.authorization.UserEditAction
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.editUser
import ru.uniyar.authorization.findUserById
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.EditUserPasswordVM
import ru.uniyar.web.templates.ContextAwareViewRender

class EditPasswordHandler(
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
        val user = findUserById(users, userId) ?: return createResult(Response(NOT_FOUND).with(lens(request) of errorModel))
        val form = editPasswordFormLens(request)
        if (isListNotEmpty(form.errors)) {
            val failures = formFailureInfoList(form.errors)
            val model = EditUserPasswordVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val pass1 = passField(form)
        val pass2 = pass2Field(form)
        if (pass1 != pass2) {
            val failures = formFailureInfoList(form.errors)
            addFailureInList(failures, "Пароли не сходятся")
            val model = EditUserPasswordVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val updatedUsers = editUser(users, user.userId, UserEditAction.SetPassword(pass1))
        return createResultWithUsers(
            Response(FOUND).header("Location", "/users"),
            updatedUsers,
        )
    }
}
