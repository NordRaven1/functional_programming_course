package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.with
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.findUserByName
import ru.uniyar.authorization.formHexPass
import ru.uniyar.authorization.registerNewUser
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.NewUserPageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class RegisterNewUserHandler(
    val lens: ContextAwareViewRender,
) : StatefulHandler {
    override fun invokeWithState(
        request: Request,
        themes: Themes,
        users: Users,
    ): HandlerResult {
        val form = userFormLens(request)
        if (isListNotEmpty(form.errors)) {
            val failures = formFailureInfoList(form.errors)
            val model = NewUserPageVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val name = userNameField(form)
        val pass1 = passField(form)
        val pass2 = pass2Field(form)
        val roleName = roleField(form)
        val role = findRole(roleName)
        if (pass1 != pass2) {
            val failures = formFailureInfoList(form.errors)
            addFailureInList(failures, "Пароли не сходятся")
            val model = NewUserPageVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        if (findUserByName(users, name) != null) {
            val failures = formFailureInfoList(form.errors)
            addFailureInList(failures, "Невозможно зарегистрировать пользователя с таким именем")
            val model = NewUserPageVM(form, failures)
            return createResult(Response(BAD_REQUEST).with(lens(request) of model))
        }
        val hexPassInStr = formHexPass(pass1)
        val updatedUsers = registerNewUser(users, name, hexPassInStr, role)
        return createResultWithUsers(
            Response(FOUND).header("Location", "/"),
            updatedUsers,
        )
    }
}
