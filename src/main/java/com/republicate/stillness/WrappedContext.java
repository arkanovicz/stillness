package com.republicate.stillness;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ArrayList;

import org.apache.velocity.context.Context;
import org.apache.velocity.VelocityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Demo
 * Date: 20 oct. 2006
 * Time: 10:35:38
 * To change this template use File | Settings | File Templates.
 */
public class WrappedContext implements Context {

    protected static Logger logger = LoggerFactory.getLogger("stillness");
    
    protected Context _savedContext = null; // original context whithout any modification
    protected Context _wrappedContext = null; // Context used for put/get methods call

    protected Hashtable _localData = new Hashtable(); // used in macro to store parameters (avoid name conflict on reference name)
    protected ArrayList _removedList = new ArrayList(); // list of removed elements to be removed from _savedContext on next commit

    public WrappedContext(Context c) {
        _savedContext = c;
        _wrappedContext = new VelocityContext();
        saveContext(_savedContext, _wrappedContext);
    }

    // commit modification from the _wrappedContext to the _savedContext
    public void commitChange() {
        saveContext(_wrappedContext, _savedContext);
    }

    protected void saveContext(Context src, Context dest) {
        while(_removedList.size()!=0)
            dest.remove((String)_removedList.remove(0));

        Object[] listKeys = src.getKeys();
        for (int i=0;i<listKeys.length; i++) {
            dest.put(""+listKeys[i], src.get(""+listKeys[i]));
        }
    }

    public Context restoreContext() {
        return _savedContext;
    }

    // used to store macro parameters
    public Object localPut(String s, Object o) {
        return _localData.put(s, o);
    }

    public Object put(String s, Object o) {
        return _wrappedContext.put(s, o);
    }

    // check in the localData first
    public Object get(String s) {
        Object o = _localData.get(s);
        if (o != null) return o;
        return _wrappedContext.get(s);
    }

    public boolean containsKey(String o) {
        return _localData.containsKey(o) || _wrappedContext.containsKey(o);
    }

    // todo : a key can be in the context and the localData !!!
    public String[] getKeys() {
        String[] wrapped = _wrappedContext.getKeys();
        String[] keys = new String[_localData.size()+wrapped.length];
        int i = 0;
        // put the localData keys
        for (Enumeration e = _localData.keys(); e.hasMoreElements() && i < _localData.size();)
            keys[i++] = (String)e.nextElement();
        // put the wrappedContext keys
        for (int j=0; j<wrapped.length && (i+j)<keys.length; j++)
            keys[i++] = wrapped[j];

        return keys;
    }

    public Object remove(String s) {
        _removedList.add(s); // save the removed keys for future commit
        return _wrappedContext.remove(s);
    }

    // debug method
    public void dumpContext() {
        logger.debug("_savedContext contains :");
        Object []sctxt = _savedContext.getKeys();
        for (int i=0; i<sctxt.length; i++) {
            logger.debug(sctxt[i] + " -> '"+ _savedContext.get(""+sctxt[i]) +"'");
        }
        logger.debug("_wrappedContext contains :");
        Object []wctxt = _wrappedContext.getKeys();
        for (int i=0; i<wctxt.length; i++) {
            logger.debug(wctxt[i] + " -> '"+ _wrappedContext.get(""+wctxt[i]) +"'");
        }
        logger.debug("_localData contains :");
        for (Enumeration e = _localData.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            logger.debug(key + " -> '"+ _localData.get(key) +"'");
        }
    }
}
