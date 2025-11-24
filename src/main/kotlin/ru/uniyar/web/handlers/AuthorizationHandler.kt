package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.with
import ru.uniyar.authorization.JwtTools
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.authUser
import ru.uniyar.authorization.findUserByName
import ru.uniyar.defaultAuthCookieExpiry
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.AuthPageVM
import ru.uniyar.web.templates.ContextAwareViewRender

class AuthorizationHandler(
    val jwtTools: JwtTools,
    val lens: ContextAwareViewRender,
) : StateReadingHandler {
    override fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response {
        val form = authFormLens(request)
        if (isListNotEmpty(form.errors)) {
            val failures = formFailureInfoList(form.errors)
            val model = AuthPageVM(form, failures)
            return Response(Status.BAD_REQUEST).with(lens(request) of model)
        }
        val username = userNameField(form)
        val pass = passField(form)
        if (!authUser(users, username, pass)) {
            val failures = formFailureInfoList(form.errors)
            addFailureInList(failures, "Неверная информация в полях авторизации")
            val model = AuthPageVM(form, failures)
            return Response(Status.BAD_REQUEST).with(lens(request) of model)
        }
        val user = findUserByName(users, username) ?: return Response(Status.OK)
        if (user.role.name == "BANNED") {
            val failures = formFailureInfoList(form.errors)
            addFailureInList(failures, "Данный пользователь находится в чёрном списке")
            val model = AuthPageVM(form, failures)
            return Response(Status.BAD_REQUEST).with(lens(request) of model)
        }
        val token = jwtTools.createJWT(user.userId)
        if (token == null) {
            val failures = formFailureInfoList(form.errors)
            addFailureInList(failures, "Произошла ошибка при авторизации. Попробуйте ещё раз")
            val model = AuthPageVM(form, failures)
            return Response(Status.BAD_REQUEST).with(lens(request) of model)
        }
        val response = Response(Status.FOUND).header("Location", "/")
        return response.cookie(
            Cookie(
                "auth",
                token,
                expires = defaultAuthCookieExpiry(),
                httpOnly = true,
            ),
        )
    }
}
