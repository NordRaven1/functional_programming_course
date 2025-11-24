package ru.uniyar.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Uri
import ru.uniyar.Paginator
import ru.uniyar.authorization.Users
import ru.uniyar.authorization.findFirstUserById
import ru.uniyar.createPaginator
import ru.uniyar.formTodaysDate
import ru.uniyar.itemsByPageNumber
import ru.uniyar.pageAmount
import ru.uniyar.safeDateInFormat
import ru.uniyar.safeDateInMillis
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Messages(
    @JsonProperty("listOfMessage")
    val messagesList: List<Message>,
)

fun addNewMessage(
    themeAndMessages: ThemeAndMessages,
    themes: Themes,
    authorId: String,
    text: String,
): Pair<Themes, Message> {
    val newMessage = createMessage(themeAndMessages.theme, authorId, text)
    val updatedMessages = addMessageToTheme(themeAndMessages, newMessage)
    val updatedThemeAndMessages = themeAndMessages.copy(messages = updatedMessages)
    val updatedThemes = replaceThemeInList(themes, updatedThemeAndMessages)
    return updatedThemes to newMessage
}

fun editMessage(
    themes: Themes,
    themeAndMessages: ThemeAndMessages,
    message: Message,
    text: String,
): Themes {
    val updatedMessages = updateMessageInList(themeAndMessages.messages, message, text)
    val updatedThemeAndMessages = themeAndMessages.copy(messages = updatedMessages)
    return replaceThemeInList(themes, updatedThemeAndMessages)
}

fun deleteMessage(
    themes: Themes,
    themeAndMessages: ThemeAndMessages,
    message: Message,
): Themes {
    val updatedMessages = removeMessageFromList(themeAndMessages.messages, message)
    val updatedThemeAndMessages = themeAndMessages.copy(messages = updatedMessages)
    return replaceThemeInList(themes, updatedThemeAndMessages)
}

fun updateMessageInList(
    messages: Messages,
    message: Message,
    text: String,
): Messages {
    val updatedMessage =
        message.copy(
            text = text,
            updateDate = formTodaysDate(),
            revisions = message.revisions + 1,
        )
    return messages.copy(
        messagesList =
            messages.messagesList.map {
                if (it.id == updatedMessage.id) {
                    updatedMessage
                } else {
                    it
                }
            },
    )
}

fun replaceMessageInList(
    messages: Messages,
    updatedMessage: Message,
): Messages {
    return messages.copy(
        messagesList =
            messages.messagesList.map {
                if (it.id == updatedMessage.id) {
                    updatedMessage
                } else {
                    it
                }
            },
    )
}

fun addMessageToTheme(
    themeAndMessages: ThemeAndMessages,
    message: Message,
): Messages {
    val messages = themeAndMessages.messages
    return messages.copy(messagesList = messages.messagesList + message)
}

fun removeMessageFromList(
    messages: Messages,
    message: Message,
): Messages {
    return messages.copy(messagesList = messages.messagesList.filter { it.id != message.id })
}

fun fetchMessageByNumber(
    messages: Messages,
    id: String,
): Message? {
    return messages.messagesList.find { it.id == id }
}

fun messagesByUserParameters(
    messages: Messages,
    minD: LocalDateTime?,
    maxD: LocalDateTime?,
): List<Message> {
    var filteredList =
        messages.messagesList.sortedBy { safeDateInMillis(safeDateInFormat(it.addDate)) }
    if (minD != null) {
        filteredList =
            filteredList.filter {
                safeDateInFormat(it.addDate).isAfter(minD) ||
                    safeDateInFormat(it.addDate).isEqual(minD)
            }
    }
    if (maxD != null) {
        filteredList =
            filteredList.filter {
                safeDateInFormat(it.addDate).isBefore(maxD) ||
                    safeDateInFormat(it.addDate).isEqual(maxD)
            }
    }
    return filteredList
}

fun getMessagesPerPage(
    messages: Messages,
    users: Users,
    mindate: LocalDateTime?,
    maxdate: LocalDateTime?,
    pageNum: Int,
    uri: Uri,
): Paginator<AuthorStructure<Message>> {
    val filteredList = messagesByUserParameters(messages, mindate, maxdate)
    val pageAmount = pageAmount(filteredList)
    val pagedList = itemsByPageNumber(pageNum, filteredList)
    val messagesPerPage =
        pagedList.map { message ->
            val messageAuthor = findFirstUserById(users, message.author)
            AuthorStructure(message, messageAuthor.userName)
        }
    return createPaginator(messagesPerPage, uri, pageNum, pageAmount)
}
