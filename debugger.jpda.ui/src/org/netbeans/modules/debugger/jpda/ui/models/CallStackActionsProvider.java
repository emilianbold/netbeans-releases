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

import com.sun.jdi.AbsentInformationException;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;

import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class CallStackActionsProvider implements NodeActionsProvider {
    
    private final Action MAKE_CURRENT_ACTION = Models.createAction (
        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_MakeCurrent_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                makeCurrent ((CallStackFrame) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    private static final Action POP_TO_HERE_ACTION = Models.createAction (
        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_PopToHere_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                popToHere ((CallStackFrame) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
    private JPDADebugger    debugger;
    private ContextProvider  lookupProvider;


    public CallStackActionsProvider (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (JPDADebugger) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT)
            return new Action [0];
        if (!(node instanceof CallStackFrame))
            throw new UnknownTypeException (node);
        
        CallStackFrame csf = (CallStackFrame) node;
        JPDAThread t = csf.getThread ();
        boolean popToHere = debugger.canPopFrames () && 
                            (t.getStackDepth () > 0);
        if (popToHere)
            try {
                popToHere = ! debugger.getCurrentThread ().getCallStack (0, 1) [0].
                    equals (csf);
            } catch (AbsentInformationException ex) {
                popToHere = false;
            }

        if (popToHere)
            return new Action [] { MAKE_CURRENT_ACTION, POP_TO_HERE_ACTION };
        else
            return new Action [] { MAKE_CURRENT_ACTION };
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof CallStackFrame) {
            makeCurrent ((CallStackFrame) node);
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }

    private static void popToHere (final CallStackFrame frame) {
        try {
            JPDAThread t = frame.getThread ();
            CallStackFrame[] stack = t.getCallStack ();
            int i, k = stack.length;
            for (i = 0; i < k; i++)
                if (stack [i].equals (frame)) {
                    stack [i - 1].popFrame ();
                    return;
                }
        } catch (AbsentInformationException ex) {
        }
    }
    
    private void makeCurrent (final CallStackFrame frame) {
        if (debugger.getCurrentCallStackFrame () != frame)
            frame.makeCurrent ();
        else
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    String language = DebuggerManager.getDebuggerManager ().
                        getCurrentSession ().getCurrentLanguage ();
                    SourcePath sp = (SourcePath) DebuggerManager.
                        getDebuggerManager ().getCurrentEngine ().lookupFirst 
                        (null, SourcePath.class);
                    sp.showSource (frame, language);
                }
            });
    }
}
