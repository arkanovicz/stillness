package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * Not used in Stillness for now
 * Matching or scraping are not allowed
 * 
 * @author Claude Brisson
 */
public class RASTSubtractNode extends RNode {

	public RASTSubtractNode() { }

    /**
     * Scrape not allowed on this node. By default it throws a ScrapeException
     */
    public void scrape(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        throw new ScrapeException("RASTSubstractNode error : scraping not allowed ("+ astNode.literal()+")");
    }
}
