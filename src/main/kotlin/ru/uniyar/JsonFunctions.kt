package ru.uniyar

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.uniyar.authorization.User
import ru.uniyar.authorization.Users
import ru.uniyar.domain.ThemeAndMessages
import ru.uniyar.domain.Themes
import java.io.File

private const val THEMES_PATH = "data/data.json"
private const val USERS_PATH = "data/users.json"

fun serializeThemes(themes: List<ThemeAndMessages>): String {
    val mapper = jacksonObjectMapper()
    val writer: ObjectWriter = mapper.writer(DefaultPrettyPrinter())
    return writer.writeValueAsString(themes)
}

fun deserializeThemes(json: String): List<ThemeAndMessages> {
    val mapper = jacksonObjectMapper()
    return mapper.readValue(
        json,
        jacksonObjectMapper().typeFactory.constructCollectionType(List::class.java, ThemeAndMessages::class.java),
    )
}

fun serializeUsers(users: List<User>): String {
    val mapper = jacksonObjectMapper()
    val writer: ObjectWriter = mapper.writer(DefaultPrettyPrinter())
    return writer.writeValueAsString(users)
}

fun deserializeUsers(json: String): List<User> {
    val mapper = jacksonObjectMapper()
    return mapper.readValue(
        json,
        jacksonObjectMapper().typeFactory.constructCollectionType(List::class.java, User::class.java),
    )
}

fun readFileContent(filePath: String): String {
    return File(filePath).readText(Charsets.UTF_8)
}

fun writeFileContent(
    filePath: String,
    content: String,
) {
    File(filePath).writeText(content, Charsets.UTF_8)
}

fun saveToFileThemes(themes: Themes) {
    try {
        val jsonContent = serializeThemes(themes.themesList)
        writeFileContent(THEMES_PATH, jsonContent)
    } catch (e: Exception) {
        println("Something went wrong during json serialization")
        println(e.message)
        e.printStackTrace()
    }
}

fun loadFromFileThemes(): List<ThemeAndMessages> {
    return try {
        val jsonString = readFileContent(THEMES_PATH)
        deserializeThemes(jsonString)
    } catch (e: Exception) {
        println("Something went wrong during json deserialization")
        println(e.message)
        emptyList()
    }
}

fun saveToFileUsers(users: Users) {
    try {
        val jsonContent = serializeUsers(users.usersList)
        writeFileContent(USERS_PATH, jsonContent)
    } catch (e: Exception) {
        println("Something went wrong during json serialization")
        println(e.message)
        e.printStackTrace()
    }
}

fun loadFromFileUsers(): List<User> {
    return try {
        val jsonString = readFileContent(USERS_PATH)
        deserializeUsers(jsonString)
    } catch (e: Exception) {
        println("Something went wrong during json deserialization")
        println(e.message)
        emptyList()
    }
}
