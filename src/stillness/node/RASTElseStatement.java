package stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;
import java.util.Iterator;

import stillness.ScrapeContext;
import stillness.ScrapeException;

/**
 * @author Claude Brisson
 */

// the match method is defined only to be used by the RASTIfStatement.
// Because there is no condition it returns always true
public class RASTElseStatement extends RNode {

	public RASTElseStatement() { }

    /**
     * Always returns true
     */
    public boolean match(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        return true;
    }

    /**
     * Srape the node corresponding to the body of this else statement
     */
    public void scrape(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        for (Iterator it = children.iterator();it.hasNext();) {
            ((RNode)it.next()).scrape(source, context, scrapingContext);
        }
    }
}
