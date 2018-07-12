package com.republicate.stillness.directive;

import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.directive.InputBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;
import com.republicate.stillness.WrappedContext;
import com.republicate.stillness.node.RNode;

/**
 * Created by IntelliJ IDEA.
 * User: Demo
 * Date: 17 oct. 2006
 * Time: 17:00:50
 * To change this template use File | Settings | File Templates.
 */
public class OptionalDirective extends InputBase {

    protected static Logger logger = LoggerFactory.getLogger("stillness");
    
    public String getName() {
        return "optional";
    }

    public int getType() {
        return BLOCK;
    }

    /**
     * Not used
     */
    public boolean render(InternalContextAdapter context, Writer writer, Node node) {
        return false;
    }

    public void scrape(String source, Context context, ScrapeContext scrapeContext, RNode node)
        throws ScrapeException {

        // check if we have 2 childrens (path and body)
        if (node.getListChildrens().size() == 0)
             throw new ScrapeException("Too few element in #optional directive");

        scrapeContext.save();
        WrappedContext cw = new WrappedContext(context);
        try {
            ((RNode)node.getListChildrens().get(0)).scrape(source, context, scrapeContext);
            scrapeContext.remove(); // remove deprecated context from the stack
        } catch (Exception e) {
            logger.debug("Exception in #optional");
            Object []ctxt = context.getKeys();
            logger.debug("@@@ Contenu du context ds le catch : ");
            for (int i=0; i<ctxt.length; i++) {
                logger.debug(ctxt[i] + " -> '"+ context.get(""+ctxt[i]) +"'");
            }

            scrapeContext.restore();
            context = cw.restoreContext();
        }

    }

}
