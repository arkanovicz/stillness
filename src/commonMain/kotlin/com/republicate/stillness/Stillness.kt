package com.republicate.stillness

import com.republicate.kson.Json
import com.republicate.stillness.parser.StillnessLexer
import com.republicate.stillness.parser.StillnessParser
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream

/**
 * Stillness reverse templating engine.
 *
 * Extracts data from text by matching it against a template pattern.
 *
 * Example:
 * ```
 * val stillness = Stillness()
 * val template = "<div class=\"name\">\$name</div>"
 * val source = "<div class=\"name\">John Doe</div>"
 * val result = stillness.scrape(source, template)
 * // result["name"] == "John Doe"
 * ```
 */
class Stillness(
    /** Whether to normalize whitespace during matching */
    var normalize: Boolean = true,
    /** Enable debug output */
    var debug: Boolean = false
) {

    /**
     * Scrape data from source text using the given template.
     *
     * @param source The text to scrape from
     * @param template The template pattern to match
     * @return Extracted data as a MutableObject
     * @throws ScrapeException if matching fails
     */
    fun scrape(source: String, template: String): Json.MutableObject {
        val output = Json.MutableObject()
        scrape(source, template, output)
        return output
    }

    /**
     * Scrape data from source text into an existing context.
     *
     * @param source The text to scrape from
     * @param template The template pattern to match
     * @param output The context to populate with extracted values
     * @throws ScrapeException if matching fails
     */
    fun scrape(source: String, template: String, output: Json.MutableObject) {
        // Parse the template
        val parseTree = parseTemplate(template)

        // Create scrape context
        val debugOutput = if (debug) DebugOutput() else null
        val ctx = ScrapeContext(
            source = source,
            output = output,
            normalize = normalize,
            debugOutput = debugOutput
        )

        // Execute scraping
        val scraper = Scraper(ctx)
        try {
            scraper.visit(parseTree)
        } catch (e: ScrapeException) {
            if (debugOutput != null) {
                debugOutput.failed(
                    expected = e.expected ?: "unknown",
                    found = e.found ?: ctx.remaining().take(50),
                    position = e.position.takeIf { it >= 0 } ?: ctx.position
                )
            }
            throw e
        }
    }

    /**
     * Scrape with debug output returned.
     *
     * @return Pair of (result, debugHtml) - debugHtml is null if scraping succeeded without debug enabled
     */
    fun scrapeDebug(source: String, template: String): Pair<Json.MutableObject?, String?> {
        val wasDebug = debug
        debug = true
        val output = Json.MutableObject()

        val debugOutput = DebugOutput()
        val ctx = ScrapeContext(
            source = source,
            output = output,
            normalize = normalize,
            debugOutput = debugOutput
        )

        val parseTree = parseTemplate(template)
        val scraper = Scraper(ctx)

        return try {
            scraper.visit(parseTree)
            debug = wasDebug
            Pair(output, debugOutput.toHtml())
        } catch (e: ScrapeException) {
            debugOutput.failed(
                expected = e.expected ?: "unknown",
                found = e.found ?: ctx.remaining().take(50),
                position = e.position.takeIf { it >= 0 } ?: ctx.position
            )
            debug = wasDebug
            Pair(null, debugOutput.toHtml())
        }
    }

    /**
     * Parse a template string into an AST.
     */
    private fun parseTemplate(template: String): StillnessParser.TemplateContext {
        val charStream = CharStreams.fromString(template)
        val lexer = StillnessLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = StillnessParser(tokenStream)

        // TODO: Add error listener for better error messages

        return parser.template()
    }

    companion object {
        /**
         * Quick scrape utility method.
         */
        fun scrape(source: String, template: String): Json.MutableObject {
            return Stillness().scrape(source, template)
        }
    }
}
