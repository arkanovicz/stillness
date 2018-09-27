package com.republicate.stillness.directive;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Writer;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Vector;

import com.republicate.stillness.ScrapeException;
import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.node.RNode;

/**
 * Handle the scraping for the regexp directives.
 * If the regexp is successfull set the $match in context. The
 * $match is a Vector containing all matching groups
 *
 * @author Claude Brisson
 */
public class RegexDirective extends Directive {

    public String getName() {
        return "regexp";
    }

    public int getType() {
        return LINE;
    }

    /**
     * Not used
     */
    public boolean render(InternalContextAdapter context, Writer writer, Node node) {
        return false;
    }

    /**
     * Tries to match the pattern whith the source. If it's successfull, set the list of groups
     * in the context.
     * This method use sun's package java.util.regex.
     */
    public void scrape(String source, Context context, ScrapeContext scrapeContext, RNode node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        // get the pattern
        Node n = node.getAstNode().jjtGetChild(0);
        String stringPattern = (String)n.value(new InternalContextAdapterImpl(context));

        Pattern pattern = Pattern.compile(stringPattern);
        // create the matcher and define the region of matching
		Matcher matcher = pattern.matcher(source).region(scrapeContext.getStart(), source.length());

		// tries to matche the pattern
		if (matcher.find()) {
/* Bizarre...
			// reduce again the region to use the matches method
			matcher = matcher.region(matcher.start(), matcher.end());
		
        	if (!matcher.matches()) {
				// handle error
	            throw new ScrapeException("Regexp directive : matching failed");
    	    }
*/
        scrapeContext.setStart(scrapeContext.getStart() + matcher.end());
        scrapeContext.setSynchronized(true);
		} else {
			if (scrapeContext.isDebugEnabled()) {
				// todo : would be fine to get the longest match for debug purpose...
				scrapeContext.getDebugOutput().logMismatch(node.getAstNode().toString(), null, stringPattern);
			}
			throw new ScrapeException("Regexp directive : pattern not found in the given source");
		}

		// get all the capturing groups and set the $match variable in context
        int nbGroup = matcher.groupCount();
        Vector v = new Vector();
        for (int i=0; i<=nbGroup; i++) {
            v.add(matcher.group(i));
        }

        context.put("match", v);

		if (scrapeContext.isDebugEnabled()) {
			scrapeContext.getDebugOutput().logRegexp(node.getAstNode().toString(), v);
		}
    }

}
