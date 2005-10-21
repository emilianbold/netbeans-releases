/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 * Helps to translate one tree to another.
 *
 * Used just for ThreadsTreeModel
 *
 * @author   Jan Jancura
 */
public final class ObjectTranslation {
    
    private static final int THREAD_ID = 0;
    private static final int STACK_ID = 1;
    private static final int LOCALS_ID = 2;
    
    private JPDADebuggerImpl debugger;
    private int translationID;
    
    /* original Object to a new one.*/
    private WeakHashMap cache = new WeakHashMap ();
    
    
    /**
     * Creates a new instance of translating tree model for given 
     * tree model.
     *
     * @param model a tree model to be translated
     */
    private ObjectTranslation (JPDADebuggerImpl debugger, int translationID) {
        this.debugger = debugger;
        this.translationID = translationID;
    }
    
    /**
     * Creates a new translated node for given original one.
     *
     * @param o a node to be translated
     * @return a new translated node
     */
    private Object createTranslation (Object o) {
        switch (translationID) {
            case THREAD_ID:
                if (o instanceof ThreadReference) {
                    return new JPDAThreadImpl ((ThreadReference) o, debugger);
                } else if (o instanceof ThreadGroupReference) {
                    return new JPDAThreadGroupImpl ((ThreadGroupReference) o, debugger);
                } else {
                    return null;
                }
            case STACK_ID:
                if (o instanceof StackFrame) {
                    return new CallStackFrameImpl ((StackFrame) o, debugger);
                }
            case LOCALS_ID:
                /*
                if (o instanceof LocalVariable) {
                    return new Local(debugger, null, null, (LocalVariable) o, null);
                }
                 */
            default:
                throw new IllegalStateException(""+o);
        }
    }
    
    private Object createTranslation (Object o, Object v) {
        switch (translationID) {
            case LOCALS_ID:
                if (o instanceof LocalVariable && (v == null || v instanceof Value)) {
                    LocalVariable lv = (LocalVariable) o;
                    Local local;
                    if (v instanceof ObjectReference) {
                        local = new ObjectLocalVariable (
                            debugger, 
                            (Value) v, 
                            null, 
                            lv, 
                            JPDADebuggerImpl.getGenericSignature (lv), 
                            null
                        );
                    } else {
                        local = new Local (debugger, (Value) v, null, lv, null);
                    }
                    return local;
                }
            default:
                throw new IllegalStateException(""+o);
        }
    }
    
    /**
     * Translates a debuggee Mirror to a wrapper object.
     *
     * @param o the Mirror object in the debuggee
     * @return translated object or <code>null</code> when the argument
     *         is not possible to translate.
     */
    public Object translate (Mirror o) {
        if (o instanceof StackFrame) {
            // StackFrame is not cachable - hashCode() and equals()
            // throws exceptions as soon as the StackFrame becomes invalid
            return createTranslation(o);
        }
        Object r = null;
        synchronized (cache) {
            WeakReference wr = (WeakReference) cache.get (o);
            if (wr != null)
                r = wr.get ();
            if (r == null) {
                r = createTranslation (o);
                cache.put (o, new WeakReference (r));
            }
        }
        return r;
    }
    
    /**
     * Translates a debuggee Mirror to a wrapper object.
     *
     * @param o the Mirror object in the debuggee
     * @param v an additional argument used for the translation
     * @return translated object or <code>null</code> when the argument
     *         is not possible to translate.
     */
    public Object translate (Mirror o, Object v) {
        Object r = null;
        WeakReference wr = (WeakReference) cache.get (o);
        if (wr != null)
            r = wr.get ();
        if (r == null) {
            r = createTranslation (o, v);
            cache.put (o, new WeakReference (r));
        }
        return r;
    }
    
    public static ObjectTranslation createThreadTranslation(JPDADebuggerImpl debugger) {
        return new ObjectTranslation(debugger, THREAD_ID);
    }
    
    public static ObjectTranslation createStackTranslation(JPDADebuggerImpl debugger) {
        return new ObjectTranslation(debugger, STACK_ID);
    }
    
    public static ObjectTranslation createLocalsTranslation(JPDADebuggerImpl debugger) {
        return new ObjectTranslation(debugger, LOCALS_ID);
    }
    
}
