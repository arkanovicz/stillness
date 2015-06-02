package stillness.node;

import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.parser.node.ASTText;

import java.io.Reader;

import stillness.ScrapeContext;
import stillness.ScrapeException;
import stillness.StillnessUtil;
import stillness.Logger;

/**
 * @author Claude Brisson
 */
public class RASTText extends RNode {

	public RASTText() { }

    public boolean match(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {

/*Logger.debug("----> dump scrapeContext ");
Logger.debug("Start index  : "+scrapeContext._start);
Logger.debug("Synchronized : "+scrapeContext._synchronized);
Logger.debug("Reference    : "+scrapeContext._reference);
Logger.debug("----> other debug info");
Logger.debug("source.length : "+ source.length());
Logger.debug("astNode.literal : '"+ astNode.literal()+"'");
Logger.debug(source.substring(scrapeContext._start, (scrapeContext._start+100 > source.length()
                ? source.length() : scrapeContext._start+100 )));*/

        String value = scrapeContext.isNormalized() ? StillnessUtil.normalize(((ASTText)astNode).literal()) : ((ASTText)astNode).literal();
//Logger.debug("Normalized value : '"+ value+"'");
//Logger.debug("");
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
