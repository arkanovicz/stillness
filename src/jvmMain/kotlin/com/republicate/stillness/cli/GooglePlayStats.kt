package com.republicate.stillness.cli

import com.republicate.stillness.Browser
import com.republicate.stillness.Stillness

/**
 * CLI tool to scrape Google Play app stats.
 */
object GooglePlayStats {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            System.err.println("Usage: gpstats <package-id>")
            System.err.println("Example: gpstats com.whatsapp")
            System.exit(1)
        }

        val packageId = args[0]
        val url = "https://play.google.com/store/apps/details?id=$packageId&hl=en"

        System.err.println("Fetching $url...")

        val browser = Browser()
        try {
            val html = browser.fetch(url, waitSelector = "[itemprop='starRating']", timeout = 60000)

            val stillness = Stillness()

            // Extract app name
            val nameTemplate = """#regex('itemprop="name">([^<]+)<', ${'$'}name)"""
            val name = try {
                stillness.scrape(html, nameTemplate).getString("name") ?: "Unknown"
            } catch (e: Exception) { "Unknown" }

            // Extract rating
            val ratingTemplate = """#regex('itemprop="starRating"><div[^>]*>([0-9.]+)<', ${'$'}rating)"""
            val rating = try {
                stillness.scrape(html, ratingTemplate).getString("rating") ?: "N/A"
            } catch (e: Exception) { "N/A" }

            // Extract downloads
            val downloadsTemplate = """#regex('"ClM7O">(\d+[BMK]?\+?)</div>', ${'$'}downloads)"""
            val downloads = try {
                stillness.scrape(html, downloadsTemplate).getString("downloads") ?: "N/A"
            } catch (e: Exception) { "N/A" }

            // Output as simple key=value
            println("package=$packageId")
            println("name=$name")
            println("rating=$rating")
            println("downloads=$downloads")

        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            System.exit(1)
        } finally {
            browser.close()
        }
    }
}
