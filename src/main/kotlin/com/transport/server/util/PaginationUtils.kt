package com.transport.server.util

import kotlinx.serialization.Serializable

@Serializable
data class Page<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)

object PaginationUtils {
    fun <T> paginate(items: List<T>, page: Int = 1, pageSize: Int = 20): Page<T> {
        val p    = page.coerceAtLeast(1)
        val from = ((p - 1) * pageSize).coerceIn(0, items.size)
        val to   = (from + pageSize).coerceIn(0, items.size)
        return Page(
            items    = items.subList(from, to),
            total    = items.size,
            page     = p,
            pageSize = pageSize,
            hasNext  = to < items.size,
            hasPrev  = p > 1
        )
    }
}
