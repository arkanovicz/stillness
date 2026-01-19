package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;
import java.util.Iterator;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * Expressions are used in if statement or set directive, so there is no scrape method available
 *
 * @author Claude Brisson
 */

// in #set ($foo = 1+1), '1+1' is an expression (no need to scrape or match it, we use default velocity behaviour)
// in #if("blabla$fooblibli"), "blabla$fooblibli" is an expression (we need match for this one)
// so only match is needed
public class RASTExpression extends RNode {

	public RASTExpression() { }

    public boolean match(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        for (Iterator it = children.iterator();it.hasNext();) {
            if (!((RNode)it.next()).match(source, context, scrapingContext))
                return false;
        }
        return true;
    }
}
