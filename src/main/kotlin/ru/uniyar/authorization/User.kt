package ru.uniyar.authorization

import ru.uniyar.generateId

data class User(
    val userName: String,
    val password: String,
    val role: Permissions,
    val userId: String,
)

fun createUser(
    userName: String,
    password: String,
    role: Permissions,
): User {
    return User(userName, password, role, generateId())
}
