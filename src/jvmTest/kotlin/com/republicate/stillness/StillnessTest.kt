package com.republicate.stillness

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StillnessTest {

    @Test
    fun testSimpleExtraction() {
        val source = "Hello John!"
        val template = "Hello \$name!"

        val stillness = Stillness()
        val result = stillness.scrape(source, template)

        assertEquals("John", result.getString("name"))
    }

    @Test
    fun testNestedProperty() {
        val source = "<div>Smith</div>"
        val template = "<div>\${user.lastName}</div>"

        val stillness = Stillness()
        val result = stillness.scrape(source, template)

        val user = result.getObject("user")
        assertNotNull(user)
        assertEquals("Smith", user.getString("lastName"))
    }

    @Test
    fun testMatchDirective() {
        val source = "prefix:value:suffix"
        val template = "#match('prefix:')\$value#match(':suffix')"

        val stillness = Stillness()
        val result = stillness.scrape(source, template)

        assertEquals("value", result.getString("value"))
    }

    @Test
    fun testRegexDirective() {
        val source = "Price: $29.99 USD"
        val template = "#regex('Price: \\$(\\d+\\.\\d+)', \$price) USD"

        val stillness = Stillness()
        val result = stillness.scrape(source, template)

        assertEquals("29.99", result.getString("price"))
    }

    @Test
    fun testDebugOutput() {
        val source = "Hello World!"
        val template = "Hello \$name!"

        val stillness = Stillness(debug = true)
        val (result, debugHtml) = stillness.scrapeDebug(source, template)

        assertNotNull(result)
        assertNotNull(debugHtml)
        assertEquals("World", result.getString("name"))
    }
}
