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

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * This class represents JPDA Debugger Implementation.
 *
 * @author Jan Jancura
 */
public class ThreadsTreeModel extends TranslatingTreeModel {

    private JPDADebuggerImpl    debugger;
    private ContextProvider      lookupProvider;
    
    
    public ThreadsTreeModel (ContextProvider lookupProvider) {
        super (new BasicThreadsTreeModel (lookupProvider));
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        this.lookupProvider = lookupProvider;
    }

    protected Object createTranslation (Object o) throws UnknownTypeException {
        if (o instanceof ThreadReference)
            return new JPDAThreadImpl ((ThreadReference) o, this);
        else
        if (o instanceof ThreadGroupReference)
            return new JPDAThreadGroupImpl ((ThreadGroupReference) o, this);
        else
        if (o.equals (BasicThreadsTreeModel.ROOT))
            return o;
        else
        throw new UnknownTypeException (o);
    }

    public Object translate (Object o) throws UnknownTypeException {
        return super.translate (o);
    }
    
    private CallStackTreeModel callStackTreeModel;
    
    CallStackTreeModel getCallStackTreeModel () {
        if (callStackTreeModel == null)
            callStackTreeModel = (CallStackTreeModel) lookupProvider.
                lookupFirst ("CallStackView", TreeModel.class);
        return callStackTreeModel;
    }
    
    private LocalsTreeModel localsTreeModel;

    LocalsTreeModel getLocalsTreeModel () {
        if (localsTreeModel == null)
            localsTreeModel = (LocalsTreeModel) lookupProvider.
                lookupFirst ("LocalsView", TreeModel.class);
        return localsTreeModel;
    }
    
    JPDADebuggerImpl getDebugger () {
        return debugger;
    }
}

