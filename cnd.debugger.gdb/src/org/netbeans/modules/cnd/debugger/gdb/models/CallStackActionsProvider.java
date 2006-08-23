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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.util.NbBundle;


/**
 * @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CallStackActionsProvider implements NodeActionsProvider {
    
    private final Action MAKE_CURRENT_ACTION = Models.createAction(
        NbBundle.getBundle(CallStackActionsProvider.class).getString("CTL_CallstackAction_MakeCurrent_Label"), // NOI18N
        new Models.ActionPerformer() {
            public boolean isEnabled(Object node) {
                // TODO: Check whether is not current - API change necessary
                return true;
            }
            public void perform(Object[] nodes) {
                makeCurrent((CallStackFrame) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE);
	
    private final Action POP_TO_HERE_ACTION = Models.createAction(
        NbBundle.getBundle(CallStackActionsProvider.class).getString("CTL_CallstackAction_PopToHere_Label"),
        new Models.ActionPerformer() {
            public boolean isEnabled(Object node) {
                // TODO: Check whether this frame is deeper then the top-most
                return true;
            }
            public void perform(Object[] nodes) {
                popToHere((CallStackFrame) nodes[0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
    private GdbDebugger    debugger;
    private ContextProvider  lookupProvider;


    public CallStackActionsProvider(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
    }
    
    public Action[] getActions(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
	    return new Action[0];
	}
        if (!(node instanceof CallStackFrame)) {
	    throw new UnknownTypeException(node);
	}
        
        boolean popToHere = debugger.canPopFrames();
        if (popToHere) {
            return new Action[] { MAKE_CURRENT_ACTION, POP_TO_HERE_ACTION };
	} else {
	    return new Action[] { MAKE_CURRENT_ACTION };
	}
    }
    
    public void performDefaultAction(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
	    return;
	}
        if (node instanceof CallStackFrame) {
            makeCurrent((CallStackFrame) node);
            return;
        }
        throw new UnknownTypeException(node);
    }

    public void addModelListener(ModelListener l) {
    }

    public void removeModelListener(ModelListener l) {
    }

    private void popToHere(final CallStackFrame frame) {
	ArrayList stack = debugger.getCallStack();
	int i, k = stack.size();
	if (k < 2) {
	    return;
	}
	for (i = 0; i < k; i++) {
	    if (stack.get(i).equals(frame)) {
		if (i > 0) {
		    ((CallStackFrame) stack.get(i - 1)).popFrame();
		}
		return;
	    }
	}
    }
    
    private void makeCurrent(final CallStackFrame frame) {
        if (debugger.getCurrentCallStackFrame() != frame) {
	    frame.makeCurrent();
	} else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
		    EditorContextBridge.showSource(frame);
                }
            });
	}
    }
}
