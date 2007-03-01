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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.AbstractAction;
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

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;


/**
 * @author   Jan Jancura
 */
public class CallStackActionsProvider implements NodeActionsProvider {
    
    private final Action MAKE_CURRENT_ACTION = Models.createAction (
        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_MakeCurrent_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                // TODO: Check whether is not current - API change necessary
                return true;
            }
            public void perform (Object[] nodes) {
                makeCurrent ((CallStackFrame) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
//    private final Action COPY_TO_CLBD_ACTION = Models.createAction (
//        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_Copy2CLBD_Label"),
//        new Models.ActionPerformer () {
//            public boolean isEnabled (Object node) {
//                return true;
//            }
//            public void perform (Object[] nodes) {
//                stackToCLBD (nodes[0]);
//            }
//        },
//        Models.MULTISELECTION_TYPE_ANY
//    );
        
    private final Action COPY_TO_CLBD_ACTION = new AbstractAction (
        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_Copy2CLBD_Label")) {
        public void actionPerformed (ActionEvent e) {
            stackToCLBD ();
        }
    };
        
    private static final Action POP_TO_HERE_ACTION = Models.createAction (
        NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_PopToHere_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                // TODO: Check whether this frame is deeper then the top-most
                return true;
            }
            public void perform (final Object[] nodes) {
                // Do not do expensive actions in AWT,
                // It can also block if it can not procceed for some reason
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        popToHere ((CallStackFrame) nodes [0]);
                    }
                });
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
        if (node == TreeModel.ROOT) {
            return new Action [] { COPY_TO_CLBD_ACTION };
        }
        
        if (!(node instanceof CallStackFrame))
            throw new UnknownTypeException (node);
        
        boolean popToHere = debugger.canPopFrames ();
        if (popToHere)
            return new Action [] { MAKE_CURRENT_ACTION, POP_TO_HERE_ACTION, COPY_TO_CLBD_ACTION };
        else
            return new Action [] { MAKE_CURRENT_ACTION, COPY_TO_CLBD_ACTION };
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
            if (k < 2) return ;
            for (i = 0; i < k; i++)
                if (stack [i].equals (frame)) {
                    if (i > 0) {
                        stack [i - 1].popFrame ();
                    }
                    return;
                }
        } catch (AbsentInformationException ex) {
        }
    }
    
    private void stackToCLBD() {
        try {
            JPDAThread t = debugger.getCurrentThread();;
            
//            if (frame instanceof CallStackFrame )
//                t = ((CallStackFrame)frame).getThread ();
//            else 
//                t = debugger.getCurrentThread();
            
            CallStackFrame[] stack = t.getCallStack ();
            int i, k = stack.length;
            StringBuffer frameStr = new StringBuffer(50);
            
            for (i = 0; i < k; i++) {
                int index = stack[i].getClassName().lastIndexOf('.');
                frameStr.append(stack[i].getClassName().substring(index + 1));
         
                frameStr.append("." + stack[i].getMethodName() +
                        " line: " + stack[i].getLineNumber("java"));
                if (i != k - 1) frameStr.append('\n');
            }    
            Clipboard systemClipboard = 
                    Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferableText =
                    new StringSelection(new String(frameStr));
            systemClipboard.setContents(
                    transferableText,
                    null);
            
        } catch (AbsentInformationException ex) {
            ErrorManager.getDefault().notify(ex);
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
