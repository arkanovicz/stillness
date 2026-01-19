package com.republicate.stillness

/**
 * Exception thrown during scraping when a pattern fails to match.
 */
class ScrapeException(
    message: String,
    val position: Int = -1,
    val expected: String? = null,
    val found: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    override fun toString(): String {
        val sb = StringBuilder("ScrapeException: $message")
        if (position >= 0) sb.append(" at position $position")
        if (expected != null) sb.append("\n  Expected: $expected")
        if (found != null) sb.append("\n  Found: $found")
        return sb.toString()
    }
}
