package ru.uniyar.domain
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import ru.uniyar.formTodaysDate
import ru.uniyar.generateId

@JsonIgnoreProperties(ignoreUnknown = true)
data class Message(
    val theme: Theme,
    val author: String,
    val text: String,
    @JsonProperty("reactions")
    val reactions: List<Reaction> = listOf(),
    val addDate: String,
    val updateDate: String,
    val id: String,
    val revisions: Int = 1,
) {
    @get:JsonIgnore
    val listOfReactions: List<Reaction> = reactions
}

fun findReactionInList(
    message: Message,
    reactionNum: Int,
): Reaction {
    return message.listOfReactions[reactionNum]
}

fun createMessage(
    theme: Theme,
    author: String,
    text: String,
    reactions: List<Reaction> = listOf(),
): Message {
    val currentDate = formTodaysDate()
    return Message(
        theme = theme,
        author = author,
        text = text,
        reactions = reactions,
        addDate = currentDate,
        updateDate = currentDate,
        id = generateId(),
        revisions = 1,
    )
}

fun addReactionToMessage(reaction: Reaction): (Message) -> Message =
    { message -> message.copy(reactions = message.reactions + reaction) }

fun removeReactionFromMessage(index: Int): (Message) -> Message =
    { message ->
        val updatedReactions = message.reactions.filterIndexed { i, _ -> i != index }
        message.copy(reactions = updatedReactions)
    }

fun updateMessage(
    themes: Themes,
    themeAndMessages: ThemeAndMessages,
    message: Message,
    update: (Message) -> Message
): Themes {
    val updatedMessage = update(message)
    val updatedMessages = replaceMessageInList(themeAndMessages.messages, updatedMessage)
    val updatedThemeAndMessages = themeAndMessages.copy(messages = updatedMessages)
    return replaceThemeInList(themes, updatedThemeAndMessages)
}

fun addNewReaction(
    themes: Themes,
    themeAndMessages: ThemeAndMessages,
    messageToUpdate: Message,
    reactionType: Int,
    author: String,
): Themes {
    val newReaction = createReaction(reactionType, author)
    return updateMessage(themes, themeAndMessages, messageToUpdate, addReactionToMessage(newReaction) )
}

fun deleteReaction(
    themes: Themes,
    themeAndMessages: ThemeAndMessages,
    messageToUpdate: Message,
    reactionNum: Int,
): Themes {
    return updateMessage(themes, themeAndMessages, messageToUpdate, removeReactionFromMessage(reactionNum) )
}
