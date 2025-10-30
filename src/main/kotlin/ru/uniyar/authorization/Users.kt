package ru.uniyar.authorization

data class Users(val usersList: List<User>) {
    fun add(user: User): Users {
        return Users(usersList + user)
    }

    fun findUserByName(username: String): User? {
        return usersList.find { it.userName == username }
    }

    fun findUserById(id: String): User? {
        return usersList.find { it.userId == id }
    }

    fun editUser(
        id: String,
        user: User,
    ): Users {
        val updatedUser = user.copy(userId = id)
        return Users(usersList.map { if (it.userId == id) updatedUser else it })
    }
}
