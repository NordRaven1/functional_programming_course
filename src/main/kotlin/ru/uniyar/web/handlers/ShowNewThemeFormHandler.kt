package ru.uniyar.web.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import org.http4k.lens.WebForm
import ru.uniyar.authorization.Permissions
import ru.uniyar.authorization.Users
import ru.uniyar.domain.Themes
import ru.uniyar.web.models.NewThemeDataVM
import ru.uniyar.web.templates.ContextAwareViewRender

class ShowNewThemeFormHandler(
    val webForm: WebForm,
    val lens: ContextAwareViewRender,
    val permissionLens: RequestContextLens<Permissions>,
) : StateReadingHandler {
    override fun invokeWithContext(
        request: Request,
        themes: Themes,
        users: Users,
    ): Response {
        val role = permissionLens(request)
        if (!role.canAddTheme) {
            return Response(FORBIDDEN)
        }
        val model = NewThemeDataVM(webForm)
        return Response(OK).with(lens(request) of model)
    }
}
