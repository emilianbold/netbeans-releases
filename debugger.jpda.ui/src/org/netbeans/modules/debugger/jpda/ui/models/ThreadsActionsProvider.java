/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.Action;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.ui.Context;
import org.netbeans.modules.debugger.jpda.ui.EngineContext;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;


/**
 * @author   Jan Jancura
 */
public class ThreadsActionsProvider implements NodeActionsProvider,
Models.ActionPerformer {

    private JPDADebugger debugger;
    
    
    public ThreadsActionsProvider (LookupProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.
            lookupFirst (JPDADebugger.class);
    }
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [0];
        if (node instanceof JPDAThreadGroup) {
            return new Action [] {
                Models.createAction ("Resume", node, this),
                Models.createAction ("Suspend", node, this),
                Models.createAction ("Properties", node, this)
            };
        } else
        if (node instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) node;
            boolean suspended = t.isSuspended ();
            Action a = null;
            if (suspended)
                a = Models.createAction ("Resume", node, this);
            else
                a = Models.createAction ("Suspend", node, this);
            return new Action [] {
                Models.createAction (
                    "Make Current", 
                    node, 
                    this, 
                    debugger.getCurrentThread () != t
                ),
                a,
                Models.createAction (
                    "Go to Source", 
                    node, 
                    this,
                    isGoToSourceSupported (t)
                ),
                Models.createAction ("Properties", node, this)
            };
        } else
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof JPDAThread)
            ((JPDAThread) node).makeCurrent ();
        else
        if (node instanceof JPDAThreadGroup) 
            return;
        else
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addTreeModelListener (TreeModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    public void perform (String action, Object node) {
        String language = DebuggerManager.getDebuggerManager ().
            getCurrentSession ().getCurrentLanguage ();
        if ("Make Current".equals (action)) {
            ((JPDAThread) node).makeCurrent ();
        } else
        if ("Resume".equals (action)) {
            if (node instanceof JPDAThread)
                ((JPDAThread) node).resume ();
            else
                ((JPDAThreadGroup) node).resume ();
        } else
        if ("Suspend".equals (action)) {
            if (node instanceof JPDAThread)
                ((JPDAThread) node).suspend ();
            else
                ((JPDAThreadGroup) node).suspend ();
        } else
        if ("Go to Source".equals (action)) {
            String className = ((JPDAThread) node).getClassName ();
            EngineContext ectx = (EngineContext) DebuggerManager.
                getDebuggerManager ().getCurrentSession ().lookupFirst 
                (EngineContext.class);
            ectx.showSource ((JPDAThread) node, language);
        } else
        if ("Properties".equals (action)) {
        }
    }    

    private boolean isGoToSourceSupported (JPDAThread t) {
        String language = DebuggerManager.getDebuggerManager ().
            getCurrentSession ().getCurrentLanguage ();
        if (!t.isSuspended ())
            return false;
        String className = t.getClassName ();
        EngineContext ectx = (EngineContext) DebuggerManager.
            getDebuggerManager ().getCurrentSession ().lookupFirst 
            (EngineContext.class);
        return ectx.sourceAvailable (t, language);
    }

}
