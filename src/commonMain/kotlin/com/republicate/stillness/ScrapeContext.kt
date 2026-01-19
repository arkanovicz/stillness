package com.republicate.stillness

import com.republicate.kson.Json

/**
 * Context for scraping operation.
 * Tracks position in source text and holds the output context.
 */
class ScrapeContext(
    /** The source text being scraped */
    val source: String,
    /** The output context where extracted values are stored */
    val output: Json.MutableObject = Json.MutableObject(),
    /** Starting position in source */
    var position: Int = 0,
    /** Whether next match must be at exact position (no skipping) */
    var synchronized: Boolean = false,
    /** Whether to normalize whitespace */
    var normalize: Boolean = true,
    /** Debug output handler, null if disabled */
    var debugOutput: DebugOutput? = null
) {
    /** Stack for nested scopes (foreach, macros) */
    private val scopeStack = mutableListOf<Json.MutableObject>()

    /** Current scope for variable assignment */
    var currentScope: Json.MutableObject = output
        private set

    /** Registered macros */
    val macros = mutableMapOf<String, Any>() // TODO: proper macro type

    /**
     * Push a new scope for nested contexts (foreach iterations, etc.)
     */
    fun pushScope(scope: Json.MutableObject) {
        scopeStack.add(currentScope)
        currentScope = scope
    }

    /**
     * Pop back to parent scope
     */
    fun popScope() {
        currentScope = scopeStack.removeLastOrNull() ?: output
    }

    /**
     * Get remaining source text from current position
     */
    fun remaining(): String = source.substring(position)

    /**
     * Get remaining length
     */
    fun remainingLength(): Int = source.length - position

    /**
     * Check if we've reached end of source
     */
    fun isAtEnd(): Boolean = position >= source.length

    /**
     * Advance position by n characters
     */
    fun advance(n: Int) {
        position += n
    }

    /**
     * Try to match literal text at current position.
     * If synchronized, must match at exact position.
     * Otherwise, searches forward for the match.
     *
     * @return The skipped text (empty if synchronized and matched), or null if no match
     */
    fun matchLiteral(text: String): String? {
        val searchText = if (normalize) normalizeWhitespace(text) else text
        val sourceText = if (normalize) normalizeWhitespace(remaining()) else remaining()

        if (synchronized) {
            // Must match at exact position
            if (sourceText.startsWith(searchText)) {
                advance(text.length)
                return ""
            }
            return null
        } else {
            // Search forward
            val idx = sourceText.indexOf(searchText)
            if (idx >= 0) {
                val skipped = source.substring(position, position + idx)
                advance(idx + text.length)
                return skipped
            }
            return null
        }
    }

    /**
     * Set a value in the current scope at the given path.
     * Creates nested objects as needed.
     *
     * @param path List of path segments (e.g., ["foo", "bar"] for $foo.bar)
     * @param value The value to set
     */
    fun setValue(path: List<String>, value: Any?) {
        if (path.isEmpty()) return

        var target: Json.MutableObject = currentScope

        // Navigate/create path except last segment
        for (i in 0 until path.size - 1) {
            val key = path[i]
            val existing = target[key]
            target = when (existing) {
                is Json.MutableObject -> existing
                null -> Json.MutableObject().also { target[key] = it }
                else -> throw ScrapeException("Cannot set property on non-object: $key")
            }
        }

        // Set the final value
        target[path.last()] = value
    }

    /**
     * Get a value from the current scope at the given path.
     */
    fun getValue(path: List<String>): Any? {
        if (path.isEmpty()) return null

        var current: Any? = currentScope
        for (key in path) {
            current = when (current) {
                is Json.Object -> current[key]
                is Json.MutableObject -> current[key]
                else -> return null
            }
        }
        return current
    }

    /**
     * Add a value to a list at the given path.
     * Creates the list if it doesn't exist.
     */
    fun addToList(path: List<String>, value: Any?) {
        if (path.isEmpty()) return

        var target: Json.MutableObject = currentScope

        // Navigate/create path except last segment
        for (i in 0 until path.size - 1) {
            val key = path[i]
            val existing = target[key]
            target = when (existing) {
                is Json.MutableObject -> existing
                null -> Json.MutableObject().also { target[key] = it }
                else -> throw ScrapeException("Cannot navigate through non-object: $key")
            }
        }

        // Get or create list
        val key = path.last()
        val existing = target[key]
        val list = when (existing) {
            is Json.MutableArray -> existing
            null -> Json.MutableArray().also { target[key] = it }
            else -> throw ScrapeException("Cannot add to non-list: $key")
        }

        list.add(value)
    }

    /**
     * Create a copy of this context with a new position (for backtracking)
     */
    fun copy(newPosition: Int = position): ScrapeContext {
        return ScrapeContext(
            source = source,
            output = output,
            position = newPosition,
            synchronized = synchronized,
            normalize = normalize,
            debugOutput = debugOutput
        ).also {
            it.scopeStack.addAll(this.scopeStack)
            // Note: currentScope reference is shared
        }
    }

    companion object {
        /**
         * Normalize whitespace: collapse sequences of whitespace to single space
         */
        fun normalizeWhitespace(text: String): String {
            return text.replace(Regex("\\s+"), " ")
        }
    }
}
