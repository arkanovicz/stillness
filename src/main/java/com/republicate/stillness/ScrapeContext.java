package com.republicate.stillness;

import com.republicate.stillness.node.RASTReference;
import com.republicate.stillness.node.RNode;
import org.apache.velocity.context.Context;

import java.util.Stack;
import java.util.Hashtable;

/**
 * Context of scraping
 *
 * @author Claude Brisson
 */
public class ScrapeContext {

    /**
     * Index in the source at whitch scraping must start
     */
    protected int _start;

    /**
     * Indicate if the next node must match the source directly at the index or not
     */
    protected boolean _synchronized = false;

    /**
     * Indicate if a RASTReference is waiting for a value or not (null)
     */
    protected RASTReference _reference = null;

    /**
     * Normalization of the text
     */
    protected boolean _normalize = false;

    /**
     * If this member is null, no debug is available
     */
    protected DebugOutput _debugOutput = null;

    protected Stack _contextStack = new Stack();

    protected Hashtable _macroList = new Hashtable();

    public ScrapeContext(int start) {
        _start = start;
    }

    public ScrapeContext(int start, boolean synch) {
        this(start);
        _synchronized = synch;
    }

    public ScrapeContext(int start, boolean sync, RASTReference ref) {
        this(start, sync);
        _reference = ref;
    }

    public ScrapeContext(ScrapeContext inContext) {
        copyValue(inContext);
    }

    // Getters/Setters

    public int getStart() { return _start; }
    public void setStart(int i) { _start = i; }
    public void incrStart(int i) { _start += i; }

    public boolean isSynchronized() { return _synchronized; }
    public void setSynchronized(boolean b) { _synchronized = b; }

    public RASTReference getReference() { return _reference; }
    public void setReference(RASTReference ref) { _reference = ref; }

    public boolean isNormalized() { return _normalize; }
    public void setNormalization(boolean b) { _normalize = b; }

    public boolean isDebugEnabled() { return (_debugOutput != null); }
    public DebugOutput getDebugOutput() { return _debugOutput; }
    public void setDebugOutput(DebugOutput d) { _debugOutput = d; }

    public Object putMacro(String name, RNode rootNode) { return _macroList.put(name, rootNode); }
    public RNode getMacro(String name) { return (RNode)_macroList.get(name); }
    public Hashtable getMacroList() { return _macroList; }

    private void copyValue(ScrapeContext inContext) {
        _start = inContext.getStart();
        _synchronized = inContext.isSynchronized();
        _reference = inContext.getReference();
        _normalize = inContext.isNormalized();
        _debugOutput = inContext.getDebugOutput();
        _macroList = new Hashtable(inContext.getMacroList());
    }

    public void save() {
        _contextStack.push(new ScrapeContext(this));
    }

    public void restore() {
        copyValue((ScrapeContext)_contextStack.pop());
    }

    public void remove() {
        _contextStack.pop();
    }

    public int match(String source, String value) throws ScrapeException
    {
        return match(source, value, true);
    }

    public int match(String source, String value, boolean log) throws ScrapeException
    {
        int pos = -1;
        value = value.trim();
        if (isNormalized()) value = StillnessUtil.normalize(value);
        if (value.length() == 0) return _start;
        try
        {
            if (isSynchronized())
            {
                // skip whitespaces
                while (_start < source.length() && Character.isWhitespace(source.charAt(_start))) ++_start;
                if (source.startsWith(value, _start))
                {
                    pos = _start;
                    _start += value.length();
                    if (isDebugEnabled() && log) getDebugOutput().logText(value, false);
                }
            }
            else
            {
                pos = source.indexOf(value, _start);
                if (pos != -1)
                {
                    _start = pos + value.length();
                    if (isDebugEnabled() && log) getDebugOutput().logText(value, false);
                }
            }
        }
        catch (Exception e)
        {
            if (isDebugEnabled() && log) getDebugOutput().logFailure(value);
            throw new ScrapeException("match : " + e.getMessage() + " (" + value + ")");
        }
        if (pos == -1 && isDebugEnabled() && log) getDebugOutput().logFailure(value);
        return pos;
    }
}
