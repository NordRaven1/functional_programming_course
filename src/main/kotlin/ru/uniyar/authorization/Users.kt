package ru.uniyar.authorization

import ru.uniyar.web.handlers.findRole

data class Users(val usersList: List<User>)

sealed interface UserEditAction {
    data class FindAndSetRole(val roleName: String) : UserEditAction

    data class SetRole(val role: Permissions) : UserEditAction

    data class SetPassword(val rawPassword: String) : UserEditAction
    data object ResetToDefaultRole : UserEditAction
}

fun addNewUser(
    user: User,
    users: Users,
): Users {
    return users.copy(usersList = users.usersList + user)
}

fun editUser(
    users: Users,
    id: String,
    action: UserEditAction,
): Users {
    val updatedList =
        users.usersList.map { user ->
            if (user.userId != id) return@map user

            when (action) {
                is UserEditAction.FindAndSetRole -> {
                    val role = findRole(action.roleName)
                    user.copy(role = role)
                }
                is UserEditAction.SetRole -> {
                    user.copy(role = action.role)
                }
                is UserEditAction.SetPassword -> {
                    val hashed = formHexPass(action.rawPassword)
                    user.copy(password = hashed)
                }
                UserEditAction.ResetToDefaultRole -> {
                    user.copy(role = findRole("BANNED"))
                }
            }
        }
    return users.copy(usersList = updatedList)
}

fun findUserBy(
    users: Users,
    value: String,
    selector: (User) -> String,
): User? {
    return users.usersList.find { selector(it) == value }
}

fun findUserByName(users: Users, username: String): User? {
    return findUserBy(users, username, selector = { it.userName })
}

fun findUserById(users: Users, id: String): User? {
    return findUserBy(users, id, selector = { it.userId })
}

fun findFirstUserById(
    users: Users,
    id: String,
): User {
    return users.usersList.first { it.userId == id }
}
