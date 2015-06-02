package stillness.directive;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Writer;
import java.io.IOException;
import java.util.Vector;

import stillness.ScrapeException;
import stillness.ScrapeContext;
import stillness.StillnessConstants;
import stillness.Logger;
import stillness.node.RNode;

/**
 * Handle the scraping for the follow directives.
 * If the match is successfull, set $match in context whith the matching text as value. 
 *
 * @author Claude Brisson
 */
public class MatchDirective extends Directive {

    public String getName() {
        return "match";
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
     * Synchronize the text in the match directive whith the source. If it is successfull,
     * put it's value in the context.
     */
    public void scrape(String source, Context context, ScrapeContext scrapeContext, RNode node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException, ScrapeException {

		// first condition before anything else : does the source match the pattern ?
        // if not it is useless to continue and we throw a ScrapeException
        if (!((RNode)node.getListChildrens().get(0)).match(source, context, scrapeContext)) {
			if (scrapeContext.isDebugEnabled()) {
				// todo : precision du debug à augmenter (parties matchée/non matchée)
				String value = ""+((RNode)node.getListChildrens().get(0)).getAstNode().value(new InternalContextAdapterImpl(context));
				int longerMatchIndex = getSubMatch(value, source.substring(scrapeContext.getStart()));
				String subMatch = (longerMatchIndex != -1) ? 
					value.substring(0, subMatchLength) : null;
				String error = (longerMatchIndex != -1) ? 
					source.substring(scrapeContext.getStart()+longerMatchIndex+subMatchLength).substring(0, 10) : value;
				scrapeContext.getDebugOutput().logMismatch(node.getAstNode().literal(), subMatch, error);
			}
            throw new ScrapeException("Match directive : matching failed");
		}

        // ok matching was successfull, now get the value and modify the context.
        Node n = node.getAstNode().jjtGetChild(0);

        // test if we have a valid children
        if (n == null) {
            rsvc.error( "#match() error :  null argument" );
            throw new ScrapeException("Null argument in #match() directive");
        }

        // the pattern must have a value
        Object value = n.value(new InternalContextAdapterImpl(context));
        if ( value == null) {
            rsvc.error("#match() error :  null argument");
            throw new ScrapeException("Unable to get argument's value in #match() directive");
        }

        // we put the new $match in context
        Vector v = new Vector();
        v.add(""+value);
        context.put("match", v);

		if (scrapeContext.isDebugEnabled()) {
			scrapeContext.getDebugOutput().logValue(node.getAstNode().literal(),value.toString());
		}
    }

	// todo : get a better algotithm
	protected int getSubMatch(String pattern, String source) {
		int bestSubMatchIndex = -1;
		int index = 0;
		int nextStep;

		while (index+pattern.length() < source.length()) {
			int i = source.indexOf(pattern.charAt(0), index);
			if (i == -1) return bestSubMatchIndex;
			nextStep = 0;
			for (int l=1; l<pattern.length(); l++) {
				// save the index of the first character that is equals to the
                // first one of the pattern.
				if (pattern.charAt(0) == source.charAt(i+l) && nextStep == 0) nextStep = i+l;
				if (pattern.charAt(l) != source.charAt(i+l)) {
					if (l > subMatchLength) {
						subMatchLength = l;
						bestSubMatchIndex = i;
					}
					index = i+(nextStep != 0 ? nextStep : l);
				}
			}
		}
		
		return bestSubMatchIndex;
	}

	int subMatchLength = 0;
}
