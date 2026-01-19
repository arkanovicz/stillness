package com.republicate.stillness

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import java.io.File

/**
 * Tests for Google Play scraping using headless browser.
 *
 * Note: These tests require Playwright browsers to be installed.
 * First run will auto-download Chromium.
 */
class GooglePlayTest {

    /**
     * Fetch and inspect a Google Play page structure.
     * This is a development/exploration test, not a regression test.
     */
    @Test
    fun inspectGooglePlayPage() {
        val browser = Browser()
        try {
            // Use a well-known app
            val url = "https://play.google.com/store/apps/details?id=com.whatsapp&hl=en"
            val html = browser.fetch(url, waitSelector = "[itemprop='starRating']", timeout = 60000)

            // Save for inspection
            File("build/google-play-sample.html").writeText(html)

            // Basic sanity checks
            assertTrue(html.contains("WhatsApp"), "Page should contain app name")
            assertTrue(html.isNotEmpty(), "Page should not be empty")

            println("=== Google Play Page Analysis ===")
            println("Page length: ${html.length} chars")

            // Find key patterns
            println("itemprop=starRating: ${html.contains("itemprop=\"starRating\"")}")
            println("class=ClM7O (downloads): ${html.contains("class=\"ClM7O\"")}")
            println("class=g1rdde (labels): ${html.contains("class=\"g1rdde\"")}")

        } finally {
            browser.close()
        }
    }

    /**
     * Test scraping app metrics with regex directives.
     */
    @Test
    fun scrapeAppMetrics() {
        val browser = Browser()
        try {
            val url = "https://play.google.com/store/apps/details?id=com.whatsapp&hl=en"
            val html = browser.fetch(url, waitSelector = "[itemprop='starRating']", timeout = 60000)

            val stillness = Stillness()

            // Extract rating: <div itemprop="starRating"><div class="TT9eCd"...>4.3</div>
            val ratingTemplate = """#regex('itemprop="starRating"><div[^>]*>([0-9.]+)<', ${'$'}rating)"""
            val ratingResult = stillness.scrape(html, ratingTemplate)
            val rating = ratingResult.getString("rating")
            println("Rating: $rating")
            assertNotNull(rating, "Should extract rating")

            // Extract downloads: <div class="ClM7O">10B+</div>
            val downloadsTemplate = """#regex('"ClM7O">(\d+[BMK]?\+?)</div>', ${'$'}downloads)"""
            val downloadsResult = stillness.scrape(html, downloadsTemplate)
            val downloads = downloadsResult.getString("downloads")
            println("Downloads: $downloads")
            assertNotNull(downloads, "Should extract downloads")

        } finally {
            browser.close()
        }
    }

    /**
     * Test scraping from cached HTML (no browser needed).
     */
    @Test
    fun scrapeFromCachedHtml() {
        val htmlFile = File("build/google-play-sample.html")
        if (!htmlFile.exists()) {
            println("Skipping: no cached HTML file. Run inspectGooglePlayPage first.")
            return
        }

        val html = htmlFile.readText()
        val stillness = Stillness(debug = true)

        // Test Kotlin regex directly first
        val testPattern = """"ClM7O">([0-9]+[BMK]?\+?)</div><div class="g1rdde">Downloads"""
        val testMatch = Regex(testPattern).find(html)
        println("Direct regex match: ${testMatch?.groupValues?.getOrNull(1)}")

        // Extract each field independently (order in HTML varies)
        val ratingTemplate = """#regex('itemprop="starRating"><div[^>]*>([0-9.]+)<', ${'$'}rating)"""
        val ratingResult = stillness.scrape(html, ratingTemplate)
        println("Rating: ${ratingResult.getString("rating")}")

        // Simpler downloads pattern without special chars
        val downloadsTemplate = """#regex('"ClM7O">(\d+[BMK]?\+?)</div>', ${'$'}downloads)"""
        val downloadsResult = stillness.scrape(html, downloadsTemplate)

        println("=== Scraped Metrics ===")
        println("Rating: ${ratingResult.getString("rating")}")
        println("Downloads: ${downloadsResult.getString("downloads")}")

        assertNotNull(ratingResult.getString("rating"))
        assertNotNull(downloadsResult.getString("downloads"))
    }
}
