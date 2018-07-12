package com.republicate.stillness.node;

import org.apache.velocity.context.Context;

import java.io.Reader;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * ASTIdentifier nodes are not directly used in Stillness. All is done via ASTReference
 *
 * @author Claude Brisson
 */
public class RASTIdentifier extends RNode {

	public RASTIdentifier() { }
}
