package com.republicate.stillness.directive;

import org.apache.velocity.runtime.directive.InputBase;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Writer;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.net.URL;
import java.net.MalformedURLException;

import com.republicate.stillness.ScrapeException;
import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.node.RNode;
import com.republicate.stillness.StillnessUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Handle the scraping for the follow directives.
 *
 * @author Claude Brisson
 */
public class FollowDirective extends InputBase {
    public String getName() {
        return "follow";
    }

    public int getType() {
        return BLOCK;
    }

    /**
     * Not used
     */
    public boolean render(InternalContextAdapter context, Writer writer, Node node) {
        throw new RuntimeException("FollowDirective.render is not implemented.");
    }

    /**
     * Load the file or url and scrape it using the reverse template contained in its body.
     */
    public void scrape(String source, Context context, ScrapeContext scrapeContext, RNode node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

		// check if we have 2 childrens (path and body)
			int numArgs = node.getListChildrens().size();
		if (numArgs < 2)
			 throw new ScrapeException("#follow: expecting at least two arguments: url/path and body, plus an optional selector");

		String sourceArg = "";

        // get the path or url
        Object value = node.getAstNode().jjtGetChild(0).value(new InternalContextAdapterImpl(context));
        if ( value == null) {
            throw new ScrapeException("#follow() argument has no value");
        }
        sourceArg = value.toString();

        String selector = null;
        if (numArgs == 3)
        {
          // get the path or url
          Object o = node.getAstNode().jjtGetChild(1).value(new InternalContextAdapterImpl(context));
          if ( value != null) {
            selector = o.toString();
          }
        }

		BufferedReader reader = null;
        // reading the data
        try {
			if (sourceArg.indexOf("://") != -1) {
				reader = new BufferedReader(new InputStreamReader(new URL(sourceArg).openStream()));
			} else {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceArg)));
			}
		} catch (MalformedURLException MUe) {
			if (scrapeContext.isDebugEnabled())
				scrapeContext.getDebugOutput().logText("#follow(\"<font color='red'>"+ value +"</font>\")", true);
			throw new ScrapeException("#follow("+sourceArg+"): Bad URL: "+ MUe.getMessage());
		} catch (IOException IOe) {
			if (scrapeContext.isDebugEnabled())
				scrapeContext.getDebugOutput().logText("#follow(\"<font color='red'>"+ value +"</font>\")", true);
			throw new ScrapeException("#follow("+sourceArg+"): I/O exception: "+ IOe.getMessage());
        } catch (Exception e) {
			if (scrapeContext.isDebugEnabled())
				scrapeContext.getDebugOutput().logText("#follow(\"<font color='red'>"+ value +"</font>\")", true);
			throw new ScrapeException("#follow("+sourceArg+"): Exception occurred: "+ e.getMessage() + " ("+ sourceArg+ ")");
        }

		if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logCode("#follow(\""+ sourceArg + (selector == null?"":", '"+selector+"'") + "\")\n", true);

        if ( reader == null ) throw new ScrapeException("#follow("+sourceArg+"): Unable to load resource!");

		String buffer = "";
		String l;
		while((l=reader.readLine())!= null) {
			buffer +=l;
		}
		reader.close();

		// apply selector
      if (selector != null)
      {
        buffer = applySelector(selector, buffer);
      }

		// source normalization
		if (scrapeContext.isNormalized())
			buffer = StillnessUtil.normalize(buffer);

        // we scrape the resource found using the reverse template found in the body (second child)
        // we use a new ScrapeContext - TODO: why? it's certainly wrong, we cannot reuse previous values!
		ScrapeContext sc = new ScrapeContext(0);
		sc.setNormalization(scrapeContext.isNormalized());
		sc.setDebugOutput(scrapeContext.getDebugOutput());
		((RNode)node.getListChildrens().get(numArgs - 1)).scrape(buffer, context, sc);
        // TODO synchronize = false en sortie de follow ?
		if (scrapeContext.isDebugEnabled()) scrapeContext.getDebugOutput().logText("#end", true);
    }

    protected String applySelector(String selector, String content)
    {
      {
        if (selector != null && selector.length() > 0)
        {
          Document doc = Jsoup.parse(content);
          Elements selected = doc.select(selector);
          content = selected.outerHtml();
        }
        return content;
      }

    }
}
