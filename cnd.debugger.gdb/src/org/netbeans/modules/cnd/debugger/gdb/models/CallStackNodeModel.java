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

    /** 
     * Gets Call Stack Frame name.
     * Logic scheme: 
     * By default return function name and filename:line.
     * If function name is not available, return address and filename:line.
     * If function name and address are not available, return filename.
     *
     * @param s Session
     * @param csf Call Stack Frame
     * @param l A boolean flag to define filename format (l=true means fullname)
     * @return Call Stack Frame name.
     */
    public static String getCSFName(Session s, CallStackFrame csf, boolean useFullName) {
        final String DOUBLE_QUESTION = "??"; // NOI18N
        
        String csfName = "";
        String functionName = csf.getFunctionName();
        if (functionName != null && !functionName.equals(DOUBLE_QUESTION)) {
            // By default use function name
            csfName = functionName;
        }
        else if (csf.getAddr() != null) {  
            //If function name is not available, use address
            csfName = NbBundle.getMessage(CallStackNodeModel.class,
			"CTL_CallstackModel_Msg_Format", csf.getAddr()); // NOI18N
	}        
        // add filename:line, if no functionName available use full path name.
        int ln = csf.getLineNumber();
        if (csfName.length() == 0)
        {
            String fileName = useFullName ? csf.getFullname() : csf.getFileName();
            if (ln<0) {
                if (fileName == null)
                    csfName = DOUBLE_QUESTION;
                else
                    csfName = fileName;
            }
        }
        else
        {
            String fileName = csf.getFileName();
            if (fileName != null && ln>=0)
                csfName += "; " + fileName + ":" + ln;
	}
        return csfName; // NOI18N
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
