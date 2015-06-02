package stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;
import java.util.Iterator;

import stillness.ScrapeContext;
import stillness.ScrapeException;

/**
 * Scraping not allowed for RASTAndNode. This node is essentially used
 * in conditionnal so only the match method is available
 *
 * @author Claude Brisson
 */
public class RASTAndNode extends RNode {

	public RASTAndNode() { }

    /**
     * Try to match all childrens and returns false if one of them doesn't match
     */
    public boolean match(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException{
        return ( ((RNode)children.get(0)).match(source, context, scrapingContext)
                && ((RNode)children.get(1)).match(source, context, scrapingContext) );
    }
}
