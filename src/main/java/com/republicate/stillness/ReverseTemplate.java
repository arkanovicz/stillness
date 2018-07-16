package com.republicate.stillness;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.parser.node.Node;
import com.republicate.stillness.node.RNode;

import java.io.*;

/**
 * @author Claude Brisson
 */
public class ReverseTemplate {

    public ReverseTemplate(Template source)  throws ClassNotFoundException,InstantiationException,IllegalAccessException {
        Node r = (Node)source.getData();
        root = RNode.reverse(r);
    }

    public void scrape(String source,Context context,boolean normalize) throws IOException,ScrapeException {
        scrape(source,context,normalize,null);
    }


    public void scrape(String source,Context context,boolean normalize,PrintWriter debug) throws IOException,ScrapeException {
        ScrapeContext scrapeContext = new ScrapeContext(0);
        DebugOutput debugOutput = null;
        if (debug != null) {
            debugOutput = new DebugOutput(debug);
            scrapeContext._debugOutput = debugOutput;
        }
        try {
            scrapeContext._normalize = normalize;
            root.scrape(source,context, scrapeContext);
        } catch (ScrapeException se) {
            scrapeContext._debugOutput.logError(se);
            throw se;
        } finally {
            if (debugOutput != null) debugOutput.endDebug();
        }
    }

    public RNode getData() {
        return root;
    }
    
    protected RNode root = null;
}
