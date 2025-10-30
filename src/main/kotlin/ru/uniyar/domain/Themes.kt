package ru.uniyar.domain

import org.http4k.core.Uri
import ru.uniyar.Paginator
import ru.uniyar.authorization.Users
import ru.uniyar.createPaginator
import ru.uniyar.itemsByPageNumber
import ru.uniyar.pageAmount
import ru.uniyar.safeDateInFormat
import java.time.LocalDateTime

data class Themes(val themesList: List<ThemeAndMessages>) {
    fun add(themeAndMessages: ThemeAndMessages): Themes {
        return Themes(themesList + themeAndMessages)
    }

    fun fetchThemeByNumber(id: String): ThemeAndMessages? {
        return themesList.find { it.theme.id == id }
    }

    fun replaceTheme(
        id: String,
        newTheme: ThemeAndMessages,
    ): Themes {
        val oldTheme = themesList.first { it.theme.id == id }
        val updatedTheme =
            newTheme.copy(
                theme =
                    newTheme.theme.copy(
                        id = oldTheme.theme.id,
                        addDate = oldTheme.theme.addDate,
                    ),
            )
        val updatedMessages =
            updatedTheme.messages.copy(
                messagesList =
                    updatedTheme.messages.messagesList.map { message ->
                        message.copy(theme = updatedTheme.theme)
                    },
            )
        val finalTheme = updatedTheme.copy(messages = updatedMessages)

        return Themes(themesList.map { if (it.theme.id == id) finalTheme else it })
    }

    fun removeTheme(id: String): Themes {
        return Themes(themesList.filter { it.theme.id != id })
    }

    fun themesByUserParameters(
        minD: LocalDateTime?,
        maxD: LocalDateTime?,
        themeSearch: String?,
    ): List<ThemeAndMessages> {
        return themesList
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
        users: Users,
        themeSearch: String?,
        mindate: LocalDateTime?,
        maxdate: LocalDateTime?,
        pageNum: Int,
        uri: Uri,
    ): Paginator<AuthorStructure<ThemeAndMessages>> {
        val filteredList = themesByUserParameters(mindate, maxdate, themeSearch)
        val pageAmount = pageAmount(filteredList)
        val pagedList = itemsByPageNumber(pageNum, filteredList)
        val themes =
            pagedList.map { themeAndMessages ->
                val themeAuthor = users.usersList.first { it.userId == themeAndMessages.theme.author }
                AuthorStructure(themeAndMessages, themeAuthor.userName)
            }
        return createPaginator(themes, uri, pageNum, pageAmount)
    }
}
