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
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;


/**
 * @author   Jan Jancura
 */
public class ThreadsActionsProvider implements NodeActionsProvider {

    private Action MAKE_CURRENT_ACTION = Models.createAction (
        "Make Current", new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return debugger.getCurrentThread () != node;
            }
            
            public void perform (Object[] nodes) {
                ((JPDAThread) nodes [0]).makeCurrent ();
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    private static Action GO_TO_SOURCE_ACTION = Models.createAction (
        "Go To Source", new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return isGoToSourceSupported ((JPDAThread) node);
            }
            
            public void perform (Object[] nodes) {
                String language = DebuggerManager.getDebuggerManager ().
                    getCurrentSession ().getCurrentLanguage ();
                String className = ((JPDAThread) nodes [0]).getClassName ();
                SourcePath sp = (SourcePath) DebuggerManager.
                    getDebuggerManager ().getCurrentEngine ().lookupFirst 
                    (null, SourcePath.class);
                sp.showSource ((JPDAThread) nodes [0], language);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    private Action SUSPEND_ACTION = Models.createAction (
        "Suspend", new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                if (node instanceof JPDAThread)
                    return !((JPDAThread) node).isSuspended ();
                else
                    return true;
            }
            
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    if (nodes [i] instanceof JPDAThread)
                        ((JPDAThread) nodes [i]).suspend ();
                    else
                        ((JPDAThreadGroup) nodes [i]).suspend ();
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );

    private Action RESUME_ACTION = Models.createAction (
        "Resume", new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                if (node instanceof JPDAThread)
                    return ((JPDAThread) node).isSuspended ();
                else
                    return true;
            }
            
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    if (nodes [i] instanceof JPDAThread)
                        ((JPDAThread) nodes [i]).resume ();
                    else
                        ((JPDAThreadGroup) nodes [i]).resume ();
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );
        
    private JPDADebugger debugger;
    
    
    public ThreadsActionsProvider (ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [0];
        if (node instanceof JPDAThreadGroup) {
            return new Action [] {
                RESUME_ACTION,
                SUSPEND_ACTION,
            };
        } else
        if (node instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) node;
            boolean suspended = t.isSuspended ();
            Action a = null;
            if (suspended)
                a = RESUME_ACTION;
            else
                a = SUSPEND_ACTION;
            return new Action [] {
                MAKE_CURRENT_ACTION,
                a,
                GO_TO_SOURCE_ACTION,
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

    private static boolean isGoToSourceSupported (JPDAThread t) {
        String language = DebuggerManager.getDebuggerManager ().
            getCurrentSession ().getCurrentLanguage ();
        if (!t.isSuspended ())
            return false;
        String className = t.getClassName ();
        SourcePath sp = (SourcePath) DebuggerManager.
            getDebuggerManager ().getCurrentEngine ().lookupFirst 
            (null, SourcePath.class);
        return sp.sourceAvailable (t, language);
    }
}
