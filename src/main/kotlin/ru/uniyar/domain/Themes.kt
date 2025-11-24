package ru.uniyar.domain

import org.http4k.core.Uri
import ru.uniyar.Paginator
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.findFirstUserById
import ru.uniyar.createPaginator
import ru.uniyar.itemsByPageNumber
import ru.uniyar.pageAmount
import ru.uniyar.safeDateInFormat
import java.time.LocalDateTime

data class Themes(val themesList: List<ThemeAndMessages>)

fun addNewTheme(
    themes: Themes,
    title: String,
    author: String,
): Themes {
    val newTheme = createTheme(title, author)
    val newThemeAndMessages = ThemeAndMessages(newTheme, Messages(emptyList()))
    return addThemeToList(themes, newThemeAndMessages)
}

fun editTheme(
    themes: Themes,
    themeAndMessages: ThemeAndMessages,
    title: String,
    adding: Boolean,
): Themes {
    val updatedTheme =
        themeAndMessages.theme.copy(
            title = title,
            addPossibility = adding,
        )
    val updatedThemeAndMessages = themeAndMessages.copy(theme = updatedTheme)
    return replaceThemeInList(themes, updatedThemeAndMessages)
}

fun removeTheme(
    themes: Themes,
    themeToRemove: ThemeAndMessages,
): Themes {
    return themes.copy(themesList = themes.themesList.filter { it.theme.id != themeToRemove.theme.id })
}

fun addThemeToList(
    themes: Themes,
    themeAndMessages: ThemeAndMessages,
): Themes {
    return themes.copy(themesList = themes.themesList + themeAndMessages)
}

fun replaceThemeInList(
    themes: Themes,
    updatedThemeAndMessages: ThemeAndMessages,
): Themes {
    val updatedMessages =
        updatedThemeAndMessages.messages.copy(
            messagesList =
                updatedThemeAndMessages.messages.messagesList.map { message ->
                    message.copy(theme = updatedThemeAndMessages.theme)
                },
        )
    val finalTheme = updatedThemeAndMessages.copy(messages = updatedMessages)

    return themes.copy(
        themesList =
            themes.themesList.map {
                if (it.theme.id == finalTheme.theme.id) {
                    finalTheme
                } else {
                    it
                }
            },
    )
}

fun findThemeByNormalizedTitle(
    themes: Themes,
    title: String,
): ThemeAndMessages? {
    return themes.themesList.find { allThemes ->
        allThemes.theme.title.replace(" ", "")
            .equals(title.replace(" ", ""), true)
    }
}

fun fetchThemeByNumber(
    themes: Themes,
    id: String,
): ThemeAndMessages? {
    return themes.themesList.find { it.theme.id == id }
}

fun themesByUserParameters(
    themes: Themes,
    minD: LocalDateTime?,
    maxD: LocalDateTime?,
    themeSearch: String?,
): List<ThemeAndMessages> {
    return themes.themesList
        .let { list ->
            themeSearch?.let { text ->
                list.filter { it.theme.title.contains(text, true) }
            } ?: list
        }
        .let { list ->
            minD?.let { min ->
                list.filter {
                    safeDateInFormat(it.theme.addDate).isAfter(min) ||
                        safeDateInFormat(it.theme.addDate).isEqual(min)
                }
            } ?: list
        }
        .let { list ->
            maxD?.let { max ->
                list.filter {
                    safeDateInFormat(it.theme.addDate).isBefore(max) ||
                        safeDateInFormat(it.theme.addDate).isEqual(max)
                }
            } ?: list
        }
}

fun getThemesPerPage(
    themes: Themes,
    users: Users,
    themeSearch: String?,
    mindate: LocalDateTime?,
    maxdate: LocalDateTime?,
    pageNum: Int,
    uri: Uri,
): Paginator<AuthorStructure<ThemeAndMessages>> {
    val filteredList = themesByUserParameters(themes, mindate, maxdate, themeSearch)
    val pageAmount = pageAmount(filteredList)
    val pagedList = itemsByPageNumber(pageNum, filteredList)
    val themesPerPage =
        pagedList.map { themeAndMessages ->
            val themeAuthor = findFirstUserById(users, themeAndMessages.theme.author)
            AuthorStructure(themeAndMessages, themeAuthor.userName)
        }
    return createPaginator(themesPerPage, uri, pageNum, pageAmount)
}
