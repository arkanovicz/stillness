package stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;
import java.util.Iterator;

import stillness.ScrapeContext;
import stillness.ScrapeException;

/**
 * @author Claude Brisson
 */
public class RASTBlock extends RNode {

	public RASTBlock() { }

    public void scrape(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        for (Iterator it = children.iterator();it.hasNext();) {
            ((RNode)it.next()).scrape(source, context, scrapingContext);
        }
    }
}
