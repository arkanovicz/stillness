package stillness.node;

import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.context.Context;

import stillness.ScrapeException;
import stillness.ScrapeContext;

public class RASTIntegerLiteral extends RNode {


    public boolean match(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {
        try {
            // same as RASTText...
            String value = (String)astNode.value(new InternalContextAdapterImpl(context));
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
        } catch (Exception e) {
            if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logFailure(astNode.literal());
            throw new ScrapeException("RASTStringLiteral match : "+e.getMessage() +" ("+ astNode.literal()+")");
        }
        if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logFailure(astNode.literal());
        return false;
    }

}
