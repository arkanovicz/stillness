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

        String value = scrapeContext.isNormalized() ? StillnessUtil.normalize(((ASTText)astNode).getCtext()) : ((ASTText)astNode).getCtext();
        try {
            if (scrapeContext.isSynchonized()) {
                if (source.startsWith(value, scrapeContext.getStart())) {
                    startIndex = scrapeContext.getStart();
                    // update the starting index in the context
                    scrapeContext.incrStart(value.length());
					if (scrapeContext.isDebugEnabled() && !_isScrape)
						scrapeContext.getDebugOutput().logText(value, false);
                    return true;
                }
            } else {
                startIndex = source.indexOf(value, scrapeContext.getStart());
                if (startIndex != -1) {
                    // update the starting index in the context
                    scrapeContext.setStart(startIndex + value.length());
					if (scrapeContext.isDebugEnabled() && !_isScrape)
						scrapeContext.getDebugOutput().logText(value, false);
                    return true;
                }
            }
        } catch (Exception e) {
			if (scrapeContext.isDebugEnabled() && !_isScrape) scrapeContext.getDebugOutput().logFailure(value);
            throw new ScrapeException("RASTText match : "+e.getMessage() +" ("+ astNode.literal()+")");
        }

		if (scrapeContext.isDebugEnabled() && !_isScrape) scrapeContext.getDebugOutput().logFailure(value);
        return false;
    }

    public void scrape(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {
        // we don't want to log twice so we indicate that the debug will be done in the scrape method
		_isScrape = true;
		if (!match(source, context, scrapeContext)) {
			if (scrapeContext.isDebugEnabled()) {
				scrapeContext.getDebugOutput().logFailure(astNode.literal());
			}	
			_isScrape = false;
            throw new ScrapeException("RASTText error : Synchronization failed for "+ astNode.literal());
        // ok all went good so far, now simply synchronize a reference if needed
		} else if (scrapeContext.getReference() != null) {
            scrapeContext.getReference().setValue(source, context, startIndex, scrapeContext);
			scrapeContext.setReference(null);
        }

		_isScrape = false;
		if (scrapeContext.isDebugEnabled()) {
			scrapeContext.getDebugOutput().logText(astNode.literal(), false);
		}
    }

	boolean _isScrape = false; // used for debug purpose
}
