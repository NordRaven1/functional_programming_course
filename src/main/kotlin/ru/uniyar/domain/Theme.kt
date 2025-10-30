package ru.uniyar.domain

import ru.uniyar.formTodaysDate
import ru.uniyar.generateId

data class Theme(
    val title: String,
    val author: String,
    val addDate: String,
    val id: String,
    val addPossibility: Boolean = true,
)

fun createTheme(
    title: String,
    author: String,
    addPossibility: Boolean = true,
): Theme {
    return Theme(title, author, formTodaysDate(), generateId(), addPossibility)
}
