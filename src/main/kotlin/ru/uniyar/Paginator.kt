package ru.uniyar

import org.http4k.core.Uri
import org.http4k.core.query
import org.http4k.core.removeQuery

data class Paginator<T>(
    val elementList: List<T>,
    val targetPage: Int,
    val amountOfPages: Int,
    val previousPageUri: Uri,
    val previousPage: String,
    val nextPageUri: Uri,
    val nextPage: String,
    val firstPageUri: Uri,
    val lastPageUri: Uri,
)

data class PaginatorState(
    val targetPage: Int,
    val amountOfPages: Int,
    val previousPage: String,
    val nextPage: String,
)

fun calculatePaginatorState(
    pageNum: Int,
    totalPages: Int,
): PaginatorState {
    val amountOfPages = if (totalPages != 0) totalPages else 1
    val targetPage = pageNum.coerceIn(1, amountOfPages)
    val previousPage = if (targetPage == 1) "disabled" else ""
    val nextPage = if (targetPage == amountOfPages) "disabled" else ""

    return PaginatorState(targetPage, amountOfPages, previousPage, nextPage)
}

fun buildPageUri(
    baseUri: Uri,
    pageNum: Int,
): Uri {
    return baseUri.removeQuery("page").query("page", pageNum.toString())
}

fun <T> createPaginator(
    elementList: List<T>,
    baseUri: Uri,
    pageNum: Int,
    totalPages: Int,
): Paginator<T> {
    val state = calculatePaginatorState(pageNum, totalPages)

    return Paginator(
        elementList = elementList,
        targetPage = state.targetPage,
        amountOfPages = state.amountOfPages,
        previousPageUri = buildPageUri(baseUri, state.targetPage - 1),
        previousPage = state.previousPage,
        nextPageUri = buildPageUri(baseUri, state.targetPage + 1),
        nextPage = state.nextPage,
        firstPageUri = buildPageUri(baseUri, 1),
        lastPageUri = buildPageUri(baseUri, state.amountOfPages),
    )
}
