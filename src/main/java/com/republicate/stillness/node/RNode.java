package com.republicate.stillness.node;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
        throw new ScrapeException("Error : scraping not allowed here! ("+ astNode.toString()+")");
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
        throw new ScrapeException(this.getClass().getName() + " : match call not allowed for "+ astNode.toString());
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
        for (int i=0;i<source.jjtGetNumChildren();i++) {
            ret.children.add(reverse(source.jjtGetChild(i)));
        }
        return ret;
    }

    protected Node astNode;
    protected List children = null;
    public int startIndex = -1;
    public int endIndex = -1;

    public String toString(String prefix)
    {
        return prefix + "_" + toString();
    }

    public final void dump(String prefix)
    {
        dump(prefix, System.out);
    }

    /**
     * <p>Dumps nodes tree on System.out.</p>
     * <p>Override {@link #dump(String, PrintWriter)} if you want to customize
     * how the node dumps out its children.
     *
     * @param prefix
     */
    public final void dump(String prefix, PrintStream out)
    {
        dump(prefix, new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
    }
    
    /**
     * <p>Dumps nodes tree on System.out.</p>
     * <p>Override this method if you want to customize how the node dumps
     * out its children.</p>
     *
     * @param prefix line prefix
     * @param out output
     */
    public void dump(String prefix, PrintWriter out)
    {
        out.println(toString());
        if (children != null)
        {
            for (int i = 0; i < children.size(); ++i)
            {
                RNode n = (RNode) children.get(i);
                out.print(prefix + " |_");
                if (n != null)
                {
                    n.dump(prefix + ( i == children.size() - 1 ? "   " : " | " ), out);
                }
            }
        }
    }

    public String toString()
    {
        return getClass().getSimpleName();
    }    
}
