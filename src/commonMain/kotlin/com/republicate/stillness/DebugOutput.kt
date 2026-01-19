package com.republicate.stillness

/**
 * Debug output handler for scraping operations.
 * Generates HTML showing where matching succeeded/failed.
 */
class DebugOutput {
    private val buffer = StringBuilder()
    private var matchCount = 0

    /**
     * Record a successful match
     */
    fun matched(text: String, position: Int, length: Int) {
        matchCount++
        buffer.append("""<span class="matched" title="Match #$matchCount at $position">""")
        buffer.append(escapeHtml(text.take(100)))
        if (text.length > 100) buffer.append("...")
        buffer.append("</span>")
    }

    /**
     * Record skipped text (between matches)
     */
    fun skipped(text: String) {
        if (text.isNotEmpty()) {
            buffer.append("""<span class="skipped">""")
            buffer.append(escapeHtml(text.take(200)))
            if (text.length > 200) buffer.append("...")
            buffer.append("</span>")
        }
    }

    /**
     * Record a failed match
     */
    fun failed(expected: String, found: String, position: Int) {
        buffer.append("""<div class="error">""")
        buffer.append("Match failed at position $position<br>")
        buffer.append("Expected: <code>${escapeHtml(expected.take(50))}</code><br>")
        buffer.append("Found: <code>${escapeHtml(found.take(50))}</code>")
        buffer.append("</div>")
    }

    /**
     * Record entering a directive
     */
    fun enterDirective(name: String) {
        buffer.append("""<!-- #$name -->""")
    }

    /**
     * Record exiting a directive
     */
    fun exitDirective(name: String) {
        buffer.append("""<!-- /#$name -->""")
    }

    /**
     * Get the full debug HTML output
     */
    fun toHtml(): String {
        return """
<!DOCTYPE html>
<html>
<head>
<style>
body { font-family: monospace; white-space: pre-wrap; }
.matched { background-color: #90EE90; }
.skipped { background-color: #FFFFE0; }
.error { background-color: #FFB6C1; padding: 10px; margin: 10px 0; }
code { background-color: #f0f0f0; padding: 2px 4px; }
</style>
</head>
<body>
<h3>Stillness Debug Output ($matchCount matches)</h3>
$buffer
</body>
</html>
        """.trimIndent()
    }

    companion object {
        fun escapeHtml(text: String): String {
            return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "↵\n")
        }
    }
}
