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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Vector;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;


/**
 * @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CallStackNodeModel implements NodeModel {

    public static final String CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame"; // NOI18N
    public static final String CURRENT_CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame"; // NOI18N

    private GdbDebugger debugger;
    private Session session;
    private Vector listeners = new Vector();
    
    
    public CallStackNodeModel(ContextProvider lookupProvider) {
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        session = (Session) lookupProvider.lookupFirst(null, Session.class);
        new Listener(this, debugger);
    }
    
    public String getDisplayName(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(CallStackNodeModel.class).getString("CTL_CallstackModel_Column_Name_Name");
        } else if (o instanceof CallStackFrame) {
            CallStackFrame sf = (CallStackFrame) o;
            CallStackFrame ccsf = debugger.getCurrentCallStackFrame();
            if (ccsf != null && ccsf.equals(sf)) { 
                return BoldVariablesTableModelFilterFirst.toHTML(getCSFName(session, sf, false),
			true, false, null);
	    }
            return getCSFName(session, sf, false);
        } else if ("No current thread" == o) {
            return NbBundle.getMessage(CallStackNodeModel.class, "NoCurrentThread");
        } else if ("Thread is running" == o) {
            return NbBundle.getMessage(CallStackNodeModel.class, "ThreadIsRunning");
        } else {
	    throw new UnknownTypeException(o);
	}
    }
    
    public String getShortDescription(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(CallStackNodeModel.class).getString("CTL_CallstackModel_Column_Name_Desc");
        } else if (o instanceof CallStackFrame) {
            CallStackFrame sf = (CallStackFrame) o;
            return getCSFName(session, sf, true);
        } else if ("No current thread" == o) {
            return NbBundle.getMessage(CallStackNodeModel.class, "NoCurrentThread");
        } else if ("Thread is running" == o) {
            return NbBundle.getMessage(CallStackNodeModel.class, "ThreadIsRunning");
        } else {
	    throw new UnknownTypeException (o);
	}
    }
    
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof String) {
	    return null;
	}
        if (node instanceof CallStackFrame) {
            CallStackFrame ccsf = debugger.getCurrentCallStackFrame();
            if (ccsf != null && ccsf.equals(node)) {
		return CURRENT_CALL_STACK;
	    }
            return CALL_STACK;
        }
        throw new UnknownTypeException(node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    private void fireTreeChanged() {
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++) {
	    ((ModelListener) v.get(i)).modelChanged(null);
	}
    }
    
    public static String getCSFName(Session s, CallStackFrame csf, boolean l) {
        int ln = csf.getLineNumber();
        String fileName = l ? csf.getFullname() : csf.getFileName();
        if (ln < 0) {
	    if (fileName != null) {
		return fileName;
	    } else if (csf.getFunctionName() != null && !csf.getFunctionName().equals("??")) { // NOI18N
		return csf.getFunctionName();
	    } else if (csf.getAddr() != null) {
		return NbBundle.getMessage(CallStackNodeModel.class,
			"CTL_CallstackModel_Msg_Format", csf.getAddr()); // NOI18N
	    } else {
		return "??"; // NOI18N
	    }
	}
        return fileName + ":" + ln; // NOI18N
    }
            
    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE, and on 
     * currentTreeModel.
     */
    private static class Listener implements PropertyChangeListener {
        
        private WeakReference ref;
        private GdbDebugger debugger;
        
        private Listener(CallStackNodeModel rm, GdbDebugger debugger) {
            ref = new WeakReference(rm);
            this.debugger = debugger;
            debugger.addPropertyChangeListener(debugger.PROP_CURRENT_CALL_STACK_FRAME, this);
        }
        
        private CallStackNodeModel getModel() {
            CallStackNodeModel rm = (CallStackNodeModel) ref.get();
            if (rm == null) {
                debugger.removePropertyChangeListener(debugger.PROP_CURRENT_CALL_STACK_FRAME, this);
            }
            return rm;
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            CallStackNodeModel rm = getModel();
            if (rm == null) {
		return;
	    }
            rm.fireTreeChanged();
        }
    }
}
