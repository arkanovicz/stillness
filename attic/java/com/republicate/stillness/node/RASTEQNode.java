package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;
import org.apache.velocity.context.InternalContextAdapterImpl;

/**
 *
 * @author Claude Brisson
 */
public class RASTEQNode extends RNode {

	public RASTEQNode() { }

	// CB TODO - have all comparison operators inherit from RASTComparisonNode, and behave the same
	public boolean match(String source, Context context, ScrapeContext scrapeContext) throws ScrapeException
	{
		return astNode.evaluate(new InternalContextAdapterImpl(context));
	}

}
