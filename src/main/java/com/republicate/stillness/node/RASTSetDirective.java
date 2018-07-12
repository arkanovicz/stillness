package com.republicate.stillness.node;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Reader;
import java.util.Iterator;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * @author Claude Brisson
 */

public class RASTSetDirective extends RNode {

	public RASTSetDirective() { }

    /**
     * Call the ASTSetDirective's render method.
     */
    public void scrape(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException{
        try {
            // default velocity behaviour
            astNode.render(new InternalContextAdapterImpl(context), null);
        } catch (Exception e) {
            throw new ScrapeException("RASTText match : "+e.getMessage() +" ("+ astNode.literal()+")");
        }
    }

}
