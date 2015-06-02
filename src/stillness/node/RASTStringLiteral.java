package stillness.node;

import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.parser.node.ASTText;
import org.apache.velocity.runtime.parser.node.ASTStringLiteral;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Reader;
import java.util.Iterator;

import stillness.ScrapeContext;
import stillness.ScrapeException;
import stillness.Logger;

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

            // don't know when it happens but in velocity source ASTStringLiteral can have childrens
        	if (children != null && children.size() > 0) {
	            for (Iterator it = children.iterator();it.hasNext();) {
                    ((RNode)it.next()).match(source, context, scrapeContext);
                }
            	return true;
        	} else { // same as RASTText...
	        	String value = (String)astNode.value(new InternalContextAdapterImpl(context));

/*Logger.debug("----> dump scrapeContext ");
Logger.debug("Start index  : "+scrapeContext._start);
Logger.debug("Synchronized : "+scrapeContext._synchronized);
Logger.debug("Reference    : "+scrapeContext._reference);
Logger.debug("----> other debug info");
Logger.debug("source.length : "+ source.length());
Logger.debug("astNode.literal : '"+ astNode.literal()+"'");
Logger.debug(source.substring(scrapeContext._start, (scrapeContext._start+100 > source.length() ? source.length() :
				 scrapeContext._start+100 )));
Logger.debug("Valeur -> "+ value);				
Logger.debug("");*/
				
            	if (scrapeContext.isSynchonized()) {
                	if (source.startsWith(value, scrapeContext.getStart())) {
                    	startIndex = scrapeContext.getStart();
                    	// update the starting index in the context
                    	scrapeContext.incrStart(value.length());
						if (scrapeContext.isDebugEnabled())
							scrapeContext.getDebugOutput().logValue(astNode.literal(), value);
                    	return true;
                	}
            	} else {
                	startIndex = source.indexOf(value, scrapeContext.getStart());
                	if (startIndex != -1) {
                    	// update the starting index in the context
                    	scrapeContext.setStart(startIndex+value.length());
						if (scrapeContext.isDebugEnabled())
							scrapeContext.getDebugOutput().logValue(astNode.literal(), value);
                    	return true;
                	}
            	}
        	}
		} catch (Exception e) {
			if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logFailure(astNode.literal());
			throw new ScrapeException("RASTStringLiteral match : "+e.getMessage() +" ("+ astNode.literal()+")");
		}
		if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logFailure(astNode.literal());
        return false;
    }

    /**
     * See scrape documentation in RNode.
     */
    // For now, I only saw ASTStringLiteral in conditionals
    // and set statements so this method is not used.
    public void scrape(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException{
        boolean saveSync = scrapeContext.isSynchonized();
        // don't know when it happens but in velocity source ASTStringLiteral can have childrens
        if (children != null && children.size() > 0) {
            for (int i=0; i<children.size();i++) {
                // synchronization needed for all childrens after the first
                if (i==1) scrapeContext.setSynchronized(true);
                ((RNode)children.get(i)).scrape(source, context, scrapeContext);
            }
            scrapeContext.setSynchronized(saveSync); // restore synchronization
        } else if (!match(source, context, scrapeContext))
            throw new ScrapeException("RASTStringLiteral error : synchronization failed for "+ astNode.literal() +" in scrape method");
    }

}
