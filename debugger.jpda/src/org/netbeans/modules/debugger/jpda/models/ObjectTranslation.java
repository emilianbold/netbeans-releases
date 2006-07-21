/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
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
            case LOCALS_ID:
                if (o instanceof ReferenceType) {
                    return new JPDAClassTypeImpl(debugger, (ReferenceType) o);
                }
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
    
    public static ObjectTranslation createLocalsTranslation(JPDADebuggerImpl debugger) {
        return new ObjectTranslation(debugger, LOCALS_ID);
    }
    
}
