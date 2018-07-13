package com.republicate.stillness.node;

import java.io.Reader;
import java.util.List;
import java.util.ArrayList;

import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.republicate.stillness.ScrapeContext;
import com.republicate.stillness.ScrapeException;

/**
 * Base class for all stillness node. Each RNode contains a reference
 * to a velocity Node and an accessible list of childrens.
 *
 * @author Claude Brisson
 */
public abstract class RNode {

    protected static Logger logger = LoggerFactory.getLogger("stillness");

    public void setAstNode(Node s) {
        astNode = s;
    }

    public Node getAstNode() {
        return astNode;
    }

    /**
     * Returns the list of this node's childrens
     */
    public List getListChildrens() { return children; }

    /**
     * Scrape not allowed on this node. By default it throws a ScrapeException
     */
    public void scrape(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        throw new ScrapeException("Error : scraping not allowed here! ("+ astNode.literal()+")");
    }

    /**
     * See evaluate in org.apache.velocity.runtime.parser.node.Node's documentation
     */
    public boolean evaluate(InternalContextAdapter context) throws MethodInvocationException {
        return astNode.evaluate(context);
    }

    /**
     * See value in org.apache.velocity.runtime.parser.node.Node's documentation
     */
    public Object value(InternalContextAdapter context) throws MethodInvocationException {
        return astNode.value(context);
    }

    /**
     * Used in conditionnal to test if the value of this RNode match the source.
     * This method must be implemented in subclasses, by default match throws a ScrapeException.
     *
     * @param source data (of a file or URL) from which we want to get informations
     * @param context velocity context in which we will store all retrieved informations
     * @param scrapingContext stillness context used to modify the behaviour of the scraping in the next Node
     * @return true if the value of the node match the source, else false.
     * @throws ScrapeException
     */
    public boolean match(String source, Context context, ScrapeContext scrapingContext) throws ScrapeException {
        throw new ScrapeException(this.getClass().getName() + " : match call not allowed for "+ astNode.literal());
    }

    /**
     * Creates an RNode from a Node
     * This method is recursive and will reverse the whole tree
     *
     * @param source the Node to transform
     * @return the root node of the RNode tree
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static RNode reverse(Node source) throws ClassNotFoundException,InstantiationException,IllegalAccessException {
        String nodeType = source.getClass().getName();
        nodeType = nodeType.substring(nodeType.lastIndexOf('.')+1);
        Class cls = Class.forName(RNode.class.getPackage().getName() + ".R" + nodeType);
        RNode ret = (RNode)cls.newInstance();
        ret.setAstNode(source);
        ret.children = new ArrayList();
logger.debug(indent+ret.getClass().getName());
        for (int i=0;i<source.jjtGetNumChildren();i++) {
indent+="|";
            ret.children.add(reverse(source.jjtGetChild(i)));
indent=indent.substring(1);
        }
        return ret;
    }

    protected Node astNode;
    protected List children = null;
    public int startIndex = -1;
    public int endIndex = -1;

protected static String indent = "|";
}
