package com.republicate.stillness.node;

import com.republicate.stillness.NullWriter;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Reader;
import java.util.Iterator;
import java.util.Properties;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;
import org.apache.velocity.runtime.parser.node.ASTSetDirective;

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
            // if setting $foo.bar without foo, create a map
            ASTReference reference = (ASTReference)((ASTSetDirective)astNode).jjtGetChild(0);
            if (reference.jjtGetNumChildren() > 0)
            {
                String root = reference.getRootString();
                if (context.get(root) == null)
                {
                    Properties p = new Properties();
                    context.put(root, p);
                }
            }
            // default velocity behaviour
            astNode.render(new InternalContextAdapterImpl(context), new NullWriter());
        } catch (Exception e) {
            throw new ScrapeException("RASTText match : "+e.getMessage() +" ("+ astNode.toString()+")");
        }
    }

}
