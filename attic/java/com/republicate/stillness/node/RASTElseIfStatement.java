package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * This node has 2 childrens, the first one is the condition and the second the
 * corresponding block of instructions. 
 *
 * @author Claude Brisson
 */

public class RASTElseIfStatement extends RNode {

	public RASTElseIfStatement() { }

    /**
     * Call the match method on the first child and returns the result
     */
    public boolean match(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        return ((RNode)children.get(0)).match(source, context, scrapingContext);
    }

    /**
     * Call the scrape method on the second child
     */
    public void scrape(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        ((RNode)children.get(1)).scrape(source, context, scrapingContext);
    }
}
