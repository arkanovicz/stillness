package com.republicate.stillness

import com.republicate.kson.Json
import com.republicate.stillness.parser.StillnessParser
import com.republicate.stillness.parser.StillnessParser.*

/**
 * Scraper walks the parsed template AST and matches it against the source text,
 * extracting values into the context.
 */
class Scraper(private val ctx: ScrapeContext) {

    /**
     * Visit the root template node.
     */
    fun visit(template: TemplateContext) {
        for (part in template.templatePart()) {
            visitTemplatePart(part)
        }
    }

    private fun visitTemplatePart(part: TemplatePartContext) {
        when (part) {
            is TextPartContext -> visitText(part.text())
            is SimpleRefPartContext -> visitSimpleReference(part.simpleReference())
            is ComplexRefPartContext -> visitComplexReference(part.complexReference())
            is DirectivePartContext -> visitDirective(part.directive())
            else -> throw ScrapeException("Unknown template part type: ${part::class.simpleName}")
        }
    }

    /**
     * Match literal text in the source.
     */
    private fun visitText(text: TextContext) {
        val literal = text.TEXT()?.text ?: return

        val startPos = ctx.position

        // If there's a pending capture, we need to find the literal and capture everything before it
        if (pendingCapture != null) {
            val remaining = ctx.remaining()
            val normalizedRemaining = if (ctx.normalize) ScrapeContext.normalizeWhitespace(remaining) else remaining
            val normalizedLiteral = if (ctx.normalize) ScrapeContext.normalizeWhitespace(literal) else literal

            val idx = normalizedRemaining.indexOf(normalizedLiteral)
            if (idx < 0) {
                throw ScrapeException(
                    message = "Text pattern not found (while capturing \$${pendingCapture!!.joinToString(".")})",
                    position = startPos,
                    expected = literal.take(50),
                    found = remaining.take(50)
                )
            }

            // Capture the text before the pattern
            val captured = remaining.substring(0, idx)
            completePendingCapture(captured)

            // Advance past the captured text and the matched literal
            ctx.advance(idx + literal.length)

            ctx.debugOutput?.let {
                it.matched(literal, startPos + idx, literal.length)
            }
        } else {
            // No pending capture - just match the literal
            val skipped = ctx.matchLiteral(literal)

            if (skipped == null) {
                throw ScrapeException(
                    message = "Text pattern not found",
                    position = startPos,
                    expected = literal.take(50),
                    found = ctx.remaining().take(50)
                )
            }

            ctx.debugOutput?.let {
                if (skipped.isNotEmpty()) it.skipped(skipped)
                it.matched(literal, startPos, literal.length)
            }
        }
    }

    /**
     * Handle simple reference like $foo
     * In scraping mode, this captures text until the next pattern match.
     */
    private fun visitSimpleReference(ref: SimpleReferenceContext) {
        val name = ref.REF_LABEL()?.text ?: return
        val path = listOf(name)

        // Mark that we're capturing into this reference
        // The actual capture happens when we match the next text pattern
        captureUntilNextMatch(path)
    }

    /**
     * Handle complex reference like ${foo.bar}
     */
    private fun visitComplexReference(ref: ComplexReferenceContext) {
        val expressions = ref.expression()
        if (expressions.isEmpty()) return

        val mainExpr = expressions[0]
        val path = extractPath(mainExpr)

        if (path != null) {
            // Check for alternate value: ${foo|default}
            if (expressions.size > 1) {
                // TODO: Handle alternate values
            }
            captureUntilNextMatch(path)
        }
    }

    /**
     * Extract a path from an expression (e.g., foo.bar.baz -> ["foo", "bar", "baz"])
     */
    private fun extractPath(expr: ExpressionContext): List<String>? {
        return when (expr) {
            is PrimaryExprContext -> {
                val primary = expr.primary()
                when (primary) {
                    is IdentifierPrimaryContext -> listOf(primary.EXPR_LABEL()?.text ?: return null)
                    else -> null
                }
            }
            is PropertyExprContext -> {
                val basePath = extractPath(expr.expression()) ?: return null
                val prop = expr.EXPR_LABEL()?.text ?: return null
                basePath + prop
            }
            else -> null
        }
    }

    /**
     * Capture text from current position until the next pattern matches.
     * This is the "reverse" part of reverse templating.
     */
    private fun captureUntilNextMatch(path: List<String>) {
        // For now, we need to look ahead to find what comes next
        // and capture everything up to that point.
        // This is a simplified version - the full implementation needs
        // to handle the case where the reference is followed by more template parts.

        // Mark the start position
        val startPos = ctx.position

        // Store the path for later - the next text match will use it
        pendingCapture = path
        pendingCaptureStart = startPos
    }

    // State for pending captures
    private var pendingCapture: List<String>? = null
    private var pendingCaptureStart: Int = 0

    /**
     * Complete a pending capture with the given value.
     */
    private fun completePendingCapture(value: String) {
        pendingCapture?.let { path ->
            ctx.setValue(path, value.trim())
            ctx.debugOutput?.matched("[\$${path.joinToString(".")}=$value]", pendingCaptureStart, value.length)
        }
        pendingCapture = null
    }

    /**
     * Handle directives (#if, #foreach, etc.)
     */
    private fun visitDirective(directive: DirectiveContext) {
        val dir = directive.getChild(0) ?: return

        ctx.debugOutput?.enterDirective(dir::class.simpleName ?: "directive")

        when (dir) {
            is IfDirectiveContext -> visitIf(dir)
            is ElseifDirectiveContext -> visitElseif(dir)
            is ElseDirectiveContext -> visitElse(dir)
            is EndDirectiveContext -> visitEnd(dir)
            is ForeachDirectiveContext -> visitForeach(dir)
            is SetDirectiveContext -> visitSet(dir)
            is MatchDirectiveContext -> visitMatch(dir)
            is RegexDirectiveContext -> visitRegex(dir)
            is OptionalDirectiveContext -> visitOptional(dir)
            is FollowDirectiveContext -> visitFollow(dir)
            is IncludeDirectiveContext -> visitInclude(dir)
            is ParseDirectiveContext -> visitParse(dir)
            is MacroDefDirectiveContext -> visitMacroDef(dir)
            is DefineDirectiveContext -> visitDefine(dir)
            is MacroCallDirectiveContext -> visitMacroCall(dir)
            else -> throw ScrapeException("Unknown directive: ${dir::class.simpleName}")
        }

        ctx.debugOutput?.exitDirective(dir::class.simpleName ?: "directive")
    }

    // Directive implementations (stubs for now)

    private fun visitIf(dir: IfDirectiveContext) {
        // TODO: Evaluate condition and conditionally process content
        val expr = dir.expression()
        // For scraping, #if usually tests if a pattern is present
    }

    private fun visitElseif(dir: ElseifDirectiveContext) {
        // TODO: Handle elseif
    }

    private fun visitElse(dir: ElseDirectiveContext) {
        // TODO: Handle else
    }

    private fun visitEnd(dir: EndDirectiveContext) {
        // End marker - usually handled by parent context
    }

    private fun visitForeach(dir: ForeachDirectiveContext) {
        // TODO: Handle foreach - iterate while pattern matches
        val loopVar = dir.loopVar?.text
        val listRef = dir.listRef()
        // In scraping mode, foreach repeats until the pattern stops matching
    }

    private fun visitSet(dir: SetDirectiveContext) {
        // TODO: Handle #set - evaluate expression and assign
    }

    private fun visitMatch(dir: MatchDirectiveContext) {
        val content = dir.matchContent()
        val pattern = extractMatchPattern(content)

        if (pattern != null) {
            val skipped = ctx.matchLiteral(pattern)
            if (skipped == null) {
                throw ScrapeException(
                    message = "#match pattern not found",
                    position = ctx.position,
                    expected = pattern,
                    found = ctx.remaining().take(50)
                )
            }
            // Complete any pending capture with the skipped text
            if (skipped.isNotEmpty()) {
                completePendingCapture(skipped)
            }
        }
    }

    private fun extractMatchPattern(content: MatchContentContext): String? {
        val str = content.MATCH_STRING()?.text
        if (str != null) {
            // Remove quotes
            return str.substring(1, str.length - 1)
        }
        // Could also be a reference - TODO
        return null
    }

    private fun visitRegex(dir: RegexDirectiveContext) {
        val pattern = dir.REGEX_STRING()?.text?.let { it.substring(1, it.length - 1) } ?: return
        val captures = dir.regexCapture()

        val regex = Regex(pattern)
        val remaining = ctx.remaining()
        val match = regex.find(remaining)

        if (match == null) {
            throw ScrapeException(
                message = "#regex pattern not found",
                position = ctx.position,
                expected = pattern,
                found = remaining.take(50)
            )
        }

        // Complete pending capture with text before match
        if (match.range.first > 0) {
            completePendingCapture(remaining.substring(0, match.range.first))
        }

        // Extract capture groups
        for ((i, capture) in captures.withIndex()) {
            if (i + 1 < match.groupValues.size) {
                val path = extractRegexCapturePath(capture)
                if (path != null) {
                    ctx.setValue(path, match.groupValues[i + 1])
                }
            }
        }

        ctx.advance(match.range.last + 1)
    }

    private fun extractRegexCapturePath(capture: RegexCaptureContext): List<String>? {
        val parts = mutableListOf<String>()
        capture.REGEX_LABEL().forEach { parts.add(it.text) }
        return parts.takeIf { it.isNotEmpty() }
    }

    private fun visitOptional(dir: OptionalDirectiveContext) {
        val content = dir.matchContent()
        val pattern = extractMatchPattern(content)

        if (pattern != null) {
            // Try to match, but don't fail if not found
            val skipped = ctx.matchLiteral(pattern)
            if (skipped != null && skipped.isNotEmpty()) {
                completePendingCapture(skipped)
            }
        }
    }

    private fun visitFollow(dir: FollowDirectiveContext) {
        // TODO: Fetch URL and scrape its content
        throw ScrapeException("#follow not yet implemented")
    }

    private fun visitInclude(dir: IncludeDirectiveContext) {
        // TODO: Include raw file content
        throw ScrapeException("#include not yet implemented")
    }

    private fun visitParse(dir: ParseDirectiveContext) {
        // TODO: Parse and process another template file
        throw ScrapeException("#parse not yet implemented")
    }

    private fun visitMacroDef(dir: MacroDefDirectiveContext) {
        // TODO: Define a macro
        val name = dir.macroName?.text ?: return
        // Store macro definition for later use
    }

    private fun visitDefine(dir: DefineDirectiveContext) {
        // TODO: Define a variable
    }

    private fun visitMacroCall(dir: MacroCallDirectiveContext) {
        // TODO: Call a macro
        val name = dir.DIR_LABEL()?.text
        throw ScrapeException("#$name macro call not yet implemented")
    }
}
