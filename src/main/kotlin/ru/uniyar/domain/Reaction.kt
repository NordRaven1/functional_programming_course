package ru.uniyar.domain
import ru.uniyar.formTodaysDate

data class Reaction(
    val reactionType: Int,
    val author: String,
    val reactionDate: String,
)

fun createReaction(
    reactionType: Int,
    author: String,
): Reaction {
    return Reaction(reactionType, author, formTodaysDate())
}
