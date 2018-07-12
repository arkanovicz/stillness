package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * @author Claude Brisson
 */

public class RASTTrue extends RNode {

	public RASTTrue() { }

    /**
     * Scrape not allowed on this node. By default it throws a ScrapeException 
     */
    public void scrape(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        throw new ScrapeException("RASTTrue error : scraping not allowed");
    }
}
