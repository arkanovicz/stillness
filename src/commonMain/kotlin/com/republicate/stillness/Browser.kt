package com.republicate.stillness

/**
 * Headless browser abstraction for fetching JS-rendered pages.
 */
expect class Browser() {
    /**
     * Fetch page content after JS rendering.
     *
     * @param url The URL to fetch
     * @param waitSelector Optional CSS selector to wait for before extracting content
     * @param timeout Timeout in milliseconds
     * @return The rendered page HTML
     */
    fun fetch(url: String, waitSelector: String? = null, timeout: Long = 30000): String

    /**
     * Close the browser and release resources.
     */
    fun close()
}

/**
 * Convenience extension to scrape a JS-rendered page.
 */
fun Stillness.scrapeUrl(
    url: String,
    template: String,
    waitSelector: String? = null,
    timeout: Long = 30000
): com.republicate.kson.Json.MutableObject {
    val browser = Browser()
    return try {
        val source = browser.fetch(url, waitSelector, timeout)
        scrape(source, template)
    } finally {
        browser.close()
    }
}
