package ru.uniyar.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Uri
import ru.uniyar.Paginator
import ru.uniyar.authorization.Users
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
) {
    fun add(message: Message): Messages {
        return Messages(messagesList + message)
    }

    fun fetchMessageByNumber(id: String): Message? {
        return messagesList.find { it.id == id }
    }

    fun replaceMessage(
        id: String,
        newMessage: Message,
    ): Messages {
        val oldMessage = messagesList.first { it.id == id }
        val updatedMessage =
            newMessage.copy(
                id = oldMessage.id,
                addDate = oldMessage.addDate,
                updateDate = formTodaysDate(),
                revisions = oldMessage.revisions + 1,
            )
        return Messages(messagesList.map { if (it.id == id) updatedMessage else it })
    }

    fun updateMessageReactions(
        id: String,
        newReactions: List<Reaction>,
    ): Messages {
        val oldMessage = messagesList.first { it.id == id }
        val updatedMessage =
            oldMessage.copy(
                reactions = newReactions,
            )
        return Messages(messagesList.map { if (it.id == id) updatedMessage else it })
    }

    fun removeMessage(id: String): Messages {
        return Messages(messagesList.filter { it.id != id })
    }

    fun messagesByUserParameters(
        minD: LocalDateTime?,
        maxD: LocalDateTime?,
    ): List<Message> {
        var filteredList =
            messagesList.sortedBy { safeDateInMillis(safeDateInFormat(it.addDate)) }
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
        users: Users,
        mindate: LocalDateTime?,
        maxdate: LocalDateTime?,
        pageNum: Int,
        uri: Uri,
    ): Paginator<AuthorStructure<Message>> {
        val filteredList = messagesByUserParameters(mindate, maxdate)
        val pageAmount = pageAmount(filteredList)
        val pagedList = itemsByPageNumber(pageNum, filteredList)
        val messages =
            pagedList.map { message ->
                val messageAuthor = users.usersList.first { it.userId == message.author }
                AuthorStructure(message, messageAuthor.userName)
            }
        return createPaginator(messages, uri, pageNum, pageAmount)
    }
}
