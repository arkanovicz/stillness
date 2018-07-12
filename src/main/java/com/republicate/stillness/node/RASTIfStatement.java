package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;
import com.republicate.stillness.StillnessConstants;

/**
 * No match method call allowed for if statement.
 *
 * The scrape occurs in 2 steps :
 * <ul>
 *   <li> call the match method on the conditionnal node
 *   <li> if the condition is true, scrape the corresponding body
 * </ul>
 *
 * @author Claude Brisson
 */

// The first children is the condition and the second is the block of instruction for
// a true condition. If needed we have one more children per elseif or else statement.
public class RASTIfStatement extends RNode {

	public RASTIfStatement() { }

    public void scrape(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {
        // size = 2 + n where n is the number of elseif/else clauses
        int size = children.size();

		if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logText("#if ( ", false);

        // if the condition (first child) is matched scrape the second child then returns,
		if (((RNode)children.get(0)).match(source, context, scrapeContext)) {
			if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logText(" )", false);
			((RNode)children.get(1)).scrape(source, context, scrapeContext);
			if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logText("#end", true);
            return;
        } else if (scrapeContext.isDebugEnabled()) {
			scrapeContext.getDebugOutput().logText(" )", false);
		}

        // if condition is false, now use elseif or else clauses
        for (int i=2; i<size; i++) {
            RNode n = (RNode)children.get(i); // RASTElseIfStatement or RASTElseStatement
			
			if (scrapeContext.isDebugEnabled()) {
				if (n.getAstNode().jjtGetNumChildren() == 2)
					scrapeContext.getDebugOutput().logText("#else if ( ", true);
				else scrapeContext.getDebugOutput().logText("#else", true);
			}
    
			if (n.match(source, context, scrapeContext)) {
				if (scrapeContext.isDebugEnabled() && n.getAstNode().jjtGetNumChildren() == 2)
					scrapeContext.getDebugOutput().logText(" )", false);
                n.scrape(source, context, scrapeContext);
				if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logText("#end", true);
                return;
            } else if (scrapeContext.isDebugEnabled() && n.getAstNode().jjtGetNumChildren() == 2) {
				scrapeContext.getDebugOutput().logText(" )", false);
			}
        }
		if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logText("#end", true);

    }
}
