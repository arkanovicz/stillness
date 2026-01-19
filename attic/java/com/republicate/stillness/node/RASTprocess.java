package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;
import java.util.Iterator;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * This node is the root of the tree of RNode
 *
 * @author Claude Brisson
 */
public class RASTprocess extends RNode {

	public RASTprocess() { }

    public void scrape(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException{
        for (Iterator it = children.iterator();it.hasNext();) {
            ((RNode)it.next()).scrape(source, context, scrapingContext);
        }
    }
}
