package com.republicate.stillness.node;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.republicate.stillness.*;
import com.republicate.stillness.directive.MatchDirective;
import com.republicate.stillness.directive.RegexDirective;
import com.republicate.stillness.directive.FollowDirective;
import com.republicate.stillness.directive.OptionalDirective;

/**
 * RASTDirective is responsible for handling all directive call.
 * <br>
 * Refer to the Stillness documentation for more detail on
 * the Stillness' directives and the new foreach behaviour.
 *
 * @author Claude Brisson
 */
public class RASTDirective extends RNode {

    protected static Logger logger = LoggerFactory.getLogger("stillness");
    
	public RASTDirective() { }

    /**
     * Handle all directives. Use the scrape method for Stillness
     * directives and the render one for the Velocity's
     */
    public void scrape(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException{
      String value = null;
        try {
            directiveName = ((ASTDirective)astNode).getDirectiveName();
            // stillness directives
            if (isStillnessDirective()) {
                if (directiveName.compareTo("match") == 0) {
                    new MatchDirective().scrape(source, context, scrapeContext, this);
                } else if (directiveName.compareTo("regexp") == 0) {
                    new RegexDirective().scrape(source, context, scrapeContext, this);
                } else if (directiveName.compareTo("follow") == 0) {
                    new FollowDirective().scrape(source, context, scrapeContext, this);
                } else if (directiveName.compareTo("optional") == 0) {
                    new OptionalDirective().scrape(source, context, scrapeContext, this);
                } else
                    throw new ScrapeException("Unknown stillness Directive : "+ astNode.toString());
            } else if (scrapeContext.getMacro(directiveName) != null) {
                // calling a macro, special case
                handleMacro(source, context, scrapeContext);
            } else {
                // velocity directives...

				// new behavior for the foreach directive
				if (directiveName.compareTo("foreach") == 0) {
					handleForeachDirective(source, context, scrapeContext);
				} else if (directiveName.compareTo("macro") == 0) {
                    // we must save the macro name and its RASTBlock node to find them when the macro is called
                    // no scrape/render here, we are in the macro definition and Macro.render() does nothing
                    scrapeContext.putMacro(astNode.jjtGetChild(0).toString(), this);
                } else {
	                CharArrayWriter w = new CharArrayWriter();
                    // todo : test all the directives
    	            ((ASTDirective)astNode).render(new InternalContextAdapterImpl(context), w);
            	    // now synchronize the output
                	value = w.toString();
                	startIndex = scrapeContext.match(source, value);
                	if (startIndex == -1) throw new ScrapeException("RASTDirective error : synchronization failed for "+ directiveName +" directive ("+ astNode.toString()+")");
                            if (scrapeContext.getReference() != null) {
                                scrapeContext.getReference().setValue(source, context, startIndex, scrapeContext);
                                scrapeContext.setReference(null);
                                scrapeContext.getDebugOutput().logValue(astNode.toString(), value);
                            }
                        scrapeContext.setSynchronized(true);
	                }
				}
        } catch (Exception e) {
            if (e instanceof ScrapeException) throw (ScrapeException)e;
            //else throw new ScrapeException("RASTDirective error : "+ e.getMessage() +" ("+ astNode.toString()+")");
            else throw new ScrapeException("RASTDirective error : "+ e.getMessage() +" ("+ value+")", e);
        }
    }

    /**
     * This method deals whith the foreach directive. It create an ArrayList that will
     * contain all scraped elements and put it in the context. For more details on the
     * foreach directive see Stillness documentation.
     */
	protected void handleForeachDirective(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {
    List list;
    String listName = ((ASTReference)astNode.jjtGetChild(2)).getRootString();
	  Object prevSlotValue = context.get(listName);
	  if (prevSlotValue == null) list = new ArrayList();
	  else if (prevSlotValue instanceof List) list = (List)prevSlotValue;
	  else throw new ScrapeException("Cannot fill non-list object in #foreach");
        int count = 0;

        WrappedContext ch = new WrappedContext(context);
		try {
			while (true) {
				if (scrapeContext.isDebugEnabled()) {
                	scrapeContext.getDebugOutput().logCode("Loop #"+ count + "\n", true);
				}
                scrapeContext.save(); // save the current ScrapeContext
                ch.commitChange(); // save Context
				ch.put("loopCount", new Integer(count));
				((RNode)children.get(3)).scrape(source, ch, scrapeContext);
                // put in the list the values retrieved during the scrape and remove them from the context
				list.add(ch.remove(((ASTReference)astNode.jjtGetChild(0)).getRootString()));
                scrapeContext.remove(); // remove deprecated context from the stack
				count++;
			}
		} catch (ScrapeException Se) {
            // roll back to the last available Context and ScrapeContext
logger.debug("Restoring context");
            scrapeContext.restore();
            context = ch.restoreContext();

			// nothing to do
			if (scrapeContext.isDebugEnabled()) {
                scrapeContext.getDebugOutput().logEndOfLoop(/*...*/);
			}
		}

		context.put(listName, list);
	}

    // handle a macro call
    // This Directive's node contains one child for each macro parameter
    // The given rootNode contains one child for each parameters plus one more for the macro name
    // and another one for the macro block.
    protected void handleMacro(String source, Context context, ScrapeContext scrapeContext) {
        RNode macro = scrapeContext.getMacro(directiveName);
        WrappedContext ch = new WrappedContext(context);
        try {
            scrapeContext.save();
            ch = storeParameters(ch, macro);
            RNode block = getBlockNode(macro);
            if (block == null) {
                logger.error("No Block found for macro "+ directiveName);
                return;
            }
//ch.dumpContext();
            block.scrape(source, ch, scrapeContext);
            scrapeContext.remove();
            ch.commitChange();
            context = ch.restoreContext();
        } catch (Exception e) {
            logger.error("An exception occurred during macro : "+ directiveName, e);
            scrapeContext.restore();
            context = ch.restoreContext();
        }
    }

    protected WrappedContext storeParameters(WrappedContext context, RNode root) throws Exception {
        int nbChild = astNode.jjtGetNumChildren();

        if (nbChild != (root.getAstNode().jjtGetNumChildren()-2))
            throw new Exception("Error : wrong number of parameters in "+directiveName+" call");

        for (int i=0; i<nbChild; i++) {
            Object value = astNode.jjtGetChild(i).value(new InternalContextAdapterImpl(context));
            Node n = root.getAstNode().jjtGetChild(i+1);
            String key;
            if (n instanceof ASTReference) key = ((ASTReference)n).getRootString();
            else key = n.toString();
            context.localPut(key, value);
        }

        return context;
    }

    protected RNode getBlockNode(RNode root) {
        List child = root.getListChildrens();
        for (int i=0; i<child.size(); i++) {
            if (child.get(i) instanceof RASTBlock)
                return (RNode)child.get(i);
        }
        return null;
    }

    /**
     * Test if the current directive is stillness specific
     */
    protected boolean isStillnessDirective() {
        logger.debug("DirectiveName : "+ directiveName);
        int nbStillnessDirective = StillnessConstants._stillnessDirectives.length;

        for (int i=0; i<nbStillnessDirective; i++)
            if (directiveName.compareTo(StillnessConstants._stillnessDirectives[i]) == 0)
                return true;

        return false;
    }

    protected String directiveName = null;
}
