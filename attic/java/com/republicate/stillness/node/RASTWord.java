package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.util.Iterator;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * @author Claude Brisson
 */
public class RASTWord extends RNode {

	public RASTWord() { }

    public void scrape(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException{
        for (Iterator it = children.iterator();it.hasNext();) {
            ((RNode)it.next()).scrape(source, context, scrapingContext);
        }
    }
}
