package com.republicate.stillness.node;

import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.parser.node.ASTText;

import java.io.Reader;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;
import com.republicate.stillness.StillnessUtil;

/**
 * @author Claude Brisson
 */
public class RASTText extends RNode {

	public RASTText() { }

    public boolean match(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {

        String value =((ASTText)astNode).getCtext();
        startIndex = scrapeContext.match(source, value, !_isScrape);
        return startIndex != -1;
    }

    public void scrape(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {
        // we don't want to log twice so we indicate that the debug will be done in the scrape method
		_isScrape = true;
		if (!match(source, context, scrapeContext))
		{
			if (scrapeContext.isDebugEnabled())
			{
				scrapeContext.getDebugOutput().logFailure(((ASTText)astNode).getCtext());
			}	
			_isScrape = false;
			throw new ScrapeException("RASTText error : Synchronization failed for "+ ((ASTText)astNode).getCtext());
        // ok all went good so far, now simply synchronize a reference if needed
		}
		else if (scrapeContext.getReference() != null)
		{
			scrapeContext.getReference().setValue(source, context, startIndex, scrapeContext);
			scrapeContext.setReference(null);
		}

		_isScrape = false;
		if (scrapeContext.isDebugEnabled())
		{
			scrapeContext.getDebugOutput().logText(((ASTText)astNode).getCtext(), false);
		}
		scrapeContext.setSynchronized(true);
	}

	boolean _isScrape = false; // used for debug purpose
}
