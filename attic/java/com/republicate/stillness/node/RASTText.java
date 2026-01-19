package com.republicate.stillness.node;

import com.republicate.stillness.DebugOutput;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.parser.node.ASTText;

import java.io.Reader;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * @author Claude Brisson
 */
public class RASTText extends RNode {

	public RASTText() { }

    public boolean match(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {

        String value = getValue();
        startIndex = scrapeContext.match(source, value, !_isScrape);
        return startIndex != -1;
    }

	public void scrape(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException {
		// we don't want to log twice so we indicate that the debug will be done in the scrape method
		_isScrape = true;

		String value = getValue();

		int prevStart = scrapeContext.getStart();
		if (!match(source, context, scrapeContext))
		{
			if (scrapeContext.isDebugEnabled())
			{
				if (scrapeContext.isSynchronized())
				{
					int pos = scrapeContext.getStart();
					if (scrapeContext.isNormalized())
					{
						while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) ++pos;
					}
					int rtlPos = 0;
					if (scrapeContext.isNormalized())
					{
						while (rtlPos < value.length() && Character.isWhitespace(value.charAt(rtlPos))) ++rtlPos;
					}
					scrapeContext.getDebugOutput().logMismatch(value.substring(rtlPos), source.substring(pos));
				}
				else
				{
					int pos = scrapeContext.getStart();
					if (scrapeContext.isNormalized())
					{
						while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) ++pos;
					}
					int rtlPos = 0;
					if (scrapeContext.isNormalized())
					{
						while (rtlPos < value.length() && Character.isWhitespace(value.charAt(rtlPos))) ++rtlPos;
					}
					value = value.substring(rtlPos);
					int len = value.length();
					int found = -1;
					while (len > 4)
					{
						found = source.indexOf(value.substring(0, len), pos);
						if (found != -1) break;
						len /= 2;
					}
					if (found != -1)
					{
						scrapeContext.getDebugOutput().logMismatch(value.substring(0, len * 2), source.substring(pos, found + len * 2));
					}
				}
				scrapeContext.getDebugOutput().logFailure(getValue());
			}
			_isScrape = false;
			throw new ScrapeException("RASTText error : Synchronization failed, was expecting: "+ DebugOutput.textToHtml(getValue()));
        // ok all went good so far, now simply synchronize a reference if needed
		}
		_isScrape = false;
		if (scrapeContext.getStart() != prevStart)
		{
			scrapeContext.setSynchronized(true);
			if (scrapeContext.getReference() != null)
			{
				scrapeContext.getReference().setValue(source, context, startIndex, scrapeContext);
				scrapeContext.setReference(null);
			}
			if (scrapeContext.isDebugEnabled())
			{
				scrapeContext.getDebugOutput().logText(getValue(), false);
			}
		}
		else if (scrapeContext.isDebugEnabled())
		{
			scrapeContext.getDebugOutput().logText(getValue(), false);
		}

	}

	// concatenate successive ASTText nodes
	private String getValue()
	{
		if (sText == null) {
			synchronized (this) {
				if (sText == null) {
					StringBuilder builder = new StringBuilder();
					ASTText node = (ASTText)astNode;
					SimpleNode parent = (SimpleNode)node.jjtGetParent();
					boolean found = false;
					for (int i = 0; i  < parent.jjtGetNumChildren(); ++i) {
						if (found || parent.jjtGetChild(i) == node)
						{
							found = true;
							SimpleNode child = (SimpleNode)parent.jjtGetChild(i);
							if (!(child instanceof ASTText)) break;
							ASTText txt = (ASTText)child;
							builder.append(txt.getCtext());
							// set node text to "" to avoid a subsequent match
							if (txt != node) {
								txt.setCtext("");
							}
						}
					}
					sText = builder.toString();
				}
			}
		}
		return sText;
	}

	boolean _isScrape = false; // used for debug purpose

	String sText = null;
}
