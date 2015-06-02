package stillness;

import stillness.node.RASTReference;
import stillness.node.RNode;

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

    public boolean isSynchonized() { return _synchronized; }
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
        _synchronized = inContext.isSynchonized();
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
}
