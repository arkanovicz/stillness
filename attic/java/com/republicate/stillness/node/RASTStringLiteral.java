package com.republicate.stillness.node;

import com.republicate.stillness.DebugOutput;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.parser.node.ASTText;
import org.apache.velocity.runtime.parser.node.ASTStringLiteral;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Reader;
import java.util.Iterator;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * @author Claude Brisson
 */
public class RASTStringLiteral extends RNode {

	public RASTStringLiteral() { }

    /**
     * See match documentation in RNode
     */
	public boolean match(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {

		try {
//Logger.debug("Valeur de children : "+ children);
//Logger.debug("Nb children de notre astNode : "+ astNode.jjtGetNumChildren());

            // for interpolated strings
        	if (children != null && children.size() > 0) {
	            for (Iterator it = children.iterator();it.hasNext();) {
                    ((RNode)it.next()).match(source, context, scrapeContext);
                }
            	return true;
        	} else {
        		// same as RASTText...
	        	String value = (String)astNode.value(new InternalContextAdapterImpl(context));
						startIndex = scrapeContext.match(source, value);
						return startIndex != -1;

        	}
		} catch (Exception e) {
			if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logFailure(astNode.toString());
			throw new ScrapeException("RASTStringLiteral match : "+e.getMessage() +" ("+ astNode.toString()+")");
		}
    }

    /**
     * See scrape documentation in RNode.
     */
    // For now, I only saw ASTStringLiteral in conditionals
    // and set statements so this method is not used.
    public void scrape(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException{
        boolean saveSync = scrapeContext.isSynchronized();
        // don't know when it happens but in velocity source ASTStringLiteral can have childrens
        if (children != null && children.size() > 0)
        {
            for (int i=0; i<children.size();i++)
            {
                // synchronization needed for all childrens after the first
                if (i==1) scrapeContext.setSynchronized(true);
                ((RNode)children.get(i)).scrape(source, context, scrapeContext);
            }
            scrapeContext.setSynchronized(saveSync); // restore synchronization
        }
        else if (!match(source, context, scrapeContext))
        {
          throw new ScrapeException("RASTStringLiteral error : synchronization failed, was expecting: " + DebugOutput.textToHtml(astNode.toString()));
        }
        else if (scrapeContext.getReference() != null)
        {
          scrapeContext.getReference().setValue(source, context, startIndex, scrapeContext);
          scrapeContext.setReference(null);
        }
      scrapeContext.setSynchronized(true);
    }

}
