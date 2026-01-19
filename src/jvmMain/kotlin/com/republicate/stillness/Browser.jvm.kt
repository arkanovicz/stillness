package com.republicate.stillness

import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState

/**
 * JVM implementation using Playwright.
 */
actual class Browser {
    private val playwright: Playwright = Playwright.create()
    private val browser: com.microsoft.playwright.Browser = playwright.chromium().launch()

    actual fun fetch(url: String, waitSelector: String?, timeout: Long): String {
        val page: Page = browser.newPage()
        try {
            // Navigate and wait for network idle
            page.navigate(url, Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))

            // Optionally wait for a specific element
            if (waitSelector != null) {
                page.waitForSelector(waitSelector, Page.WaitForSelectorOptions().setTimeout(timeout.toDouble()))
            }

            // Return the rendered HTML
            return page.content()
        } finally {
            page.close()
        }
    }

    actual fun close() {
        browser.close()
        playwright.close()
    }
}
