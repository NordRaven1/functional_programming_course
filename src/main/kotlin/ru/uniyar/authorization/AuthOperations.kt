package ru.uniyar.authorization

import ru.uniyar.config.readAuthSaltFromConfiguration
import java.security.MessageDigest
import java.util.HexFormat
import kotlin.system.exitProcess

fun addUser(
    users: Users,
    username: String,
    hashedPassword: String,
    role: Permissions,
): Users {
    val newUser = createUser(username, hashedPassword, role)
    return users.add(newUser)
}

fun hashPasswordWithSalt(
    password: String,
    salt: String,
): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val passAndSalt = password + salt
    val hexPass = passAndSalt.toByteArray(Charsets.UTF_8)
    val digest = messageDigest.digest(hexPass)
    return HexFormat.of().formatHex(digest)
}

fun formHexPass(password: String): String {
    val saltConfig = readAuthSaltFromConfiguration() ?: exitProcess(0)
    return hashPasswordWithSalt(password, saltConfig.salt)
}

fun authUser(
    users: Users,
    username: String,
    password: String,
): Boolean {
    val user = users.findUserByName(username) ?: return false
    return user.password == formHexPass(password)
}

fun formSharedState(
    users: Users,
    userId: String,
): SharedState? {
    val user = users.findUserById(userId) ?: return null
    return SharedState(user.userId, user.userName)
}
