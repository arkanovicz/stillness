package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;
import java.util.Iterator;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * Scraping not allowed for RASTNotNode. This node is essentially used
 * in conditionnal so only the match method is available
 *
 * @author Claude Brisson
 */
public class RASTNotNode extends RNode {

	public RASTNotNode() { }

    public boolean match(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        return !((RNode)children.get(0)).match(source, context, scrapingContext);
    }

}
