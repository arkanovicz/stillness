package com.republicate.stillness.cli

import com.republicate.stillness.Browser
import com.republicate.stillness.Stillness
import java.io.File

/**
 * CLI tool to search Google Play and list apps with stats.
 *
 * Usage: gpsearch <query> [--limit N]
 * Example: gpsearch yahtzee --limit 5
 */
object GooglePlaySearch {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            System.err.println("Usage: gpsearch <query> [--limit N]")
            System.err.println("Example: gpsearch yahtzee --limit 5")
            System.exit(1)
        }

        // Parse args
        var limit = 10
        val queryParts = mutableListOf<String>()
        var i = 0
        while (i < args.size) {
            if (args[i] == "--limit" && i + 1 < args.size) {
                limit = args[i + 1].toIntOrNull() ?: 10
                i += 2
            } else {
                queryParts.add(args[i])
                i++
            }
        }
        val query = queryParts.joinToString(" ")

        val searchUrl = "https://play.google.com/store/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&c=apps&hl=en"
        System.err.println("Searching: $query")

        val browser = Browser()
        val stillness = Stillness()

        try {
            // Fetch search results
            val searchHtml = browser.fetch(searchUrl, waitSelector = "a[href*='/store/apps/details']", timeout = 60000)

            // Extract package IDs from search results
            val linkPattern = Regex("""href="/store/apps/details\?id=([^"&]+)""")
            val packages = linkPattern.findAll(searchHtml)
                .map { it.groupValues[1] }
                .distinct()
                .take(limit)
                .toList()

            System.err.println("Found ${packages.size} apps, fetching stats...\n")

            // Header
            println("%-45s %6s %10s".format("PACKAGE", "RATING", "DOWNLOADS"))
            println("-".repeat(65))

            // Fetch stats for each app
            for (pkg in packages) {
                try {
                    val appUrl = "https://play.google.com/store/apps/details?id=$pkg&hl=en"
                    val appHtml = browser.fetch(appUrl, waitSelector = "[itemprop='starRating']", timeout = 30000)

                    val rating = try {
                        val tpl = """#regex('itemprop="starRating"><div[^>]*>([0-9.]+)<', ${'$'}v)"""
                        stillness.scrape(appHtml, tpl).getString("v") ?: "N/A"
                    } catch (e: Exception) { "N/A" }

                    val downloads = try {
                        val tpl = """#regex('"ClM7O">(\d+[BMK]?\+?)</div>', ${'$'}v)"""
                        stillness.scrape(appHtml, tpl).getString("v") ?: "N/A"
                    } catch (e: Exception) { "N/A" }

                    println("%-45s %6s %10s".format(pkg.take(45), rating, downloads))
                } catch (e: Exception) {
                    println("%-45s %6s %10s".format(pkg.take(45), "ERR", "ERR"))
                }
            }

        } catch (e: Exception) {
            System.err.println("Error: ${e.message}")
            System.exit(1)
        } finally {
            browser.close()
        }
    }
}
