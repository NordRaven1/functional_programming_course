package ru.uniyar.web.handlers

import org.http4k.core.Response
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes

data class HandlerResult(
    val response: Response,
    val newThemes: Themes? = null,
    val newUsers: Users? = null,
)

fun createResult(response: Response): HandlerResult = HandlerResult(response)

fun createResultWithThemes(
    response: Response,
    themes: Themes,
): HandlerResult = HandlerResult(response, newThemes = themes)

fun createResultWithUsers(
    response: Response,
    users: Users,
): HandlerResult = HandlerResult(response, newUsers = users)
