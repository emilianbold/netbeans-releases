/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;


/**
 * Listens on GdbDebugger.PROP_STATE and DebuggerManager.PROP_BREAKPOINTS, and
 * and creates XXXBreakpointImpl classes for all GdbBreakpoints.
 *
 * @author   Gordon Prieur (Copied from Jan Jancura's JPDA implementation)
 */
public class BreakpointsEngineListener extends LazyActionsManagerListener 
		implements PropertyChangeListener, DebuggerManagerListener {
    
    private GdbDebugger	    debugger;
    private Session                 session;
    private BreakpointsReader       breakpointsReader;


    public BreakpointsEngineListener(ContextProvider lookupProvider) {
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        session = (Session) lookupProvider.lookupFirst(null, Session.class);
        debugger.addPropertyChangeListener(GdbDebugger.PROP_STATE, this);
        breakpointsReader = PersistenceManager.findBreakpointsReader();
    }
    
    protected void destroy() {
        debugger.removePropertyChangeListener(GdbDebugger.PROP_STATE, this);
        DebuggerManager.getDebuggerManager().removeDebuggerListener(
		    DebuggerManager.PROP_BREAKPOINTS, this);
        removeBreakpointImpls();
	unvalidateBreakpoints();
    }
    
    public String[] getProperties() {
        return new String[] {"asd"}; // NOI18N
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() == GdbDebugger.STATE_LOADING) {
	    createBreakpointImpls();
	    DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
            debugger.setReady();
        }
    }
    
    public void actionPerformed(Object action) {
//        if (action == ActionsManager.ACTION_FIX)
//            fixBreakpointImpls ();
    }

    public void breakpointAdded(Breakpoint breakpoint) {
        createBreakpointImpl(breakpoint);
    }    

    public void breakpointRemoved(Breakpoint breakpoint) {
        removeBreakpointImpl(breakpoint);
    }
    

    public Breakpoint[] initBreakpoints() {return new Breakpoint[0];}
    public void initWatches() {}
    public void sessionAdded(Session session) {}
    public void sessionRemoved(Session session) {}
    public void watchAdded(Watch watch) {}
    public void watchRemoved(Watch watch) {}
    public void engineAdded(DebuggerEngine engine) {
        System.err.println("BEL.engineAdded: "); // NOI18N
    }
    public void engineRemoved(DebuggerEngine engine) {
        System.err.println("BEL.engineRemoved: "); // NOI18N
    }


    // helper methods ..........................................................
    
    private HashMap breakpointToImpl = new HashMap();
    
    
    private void createBreakpointImpls() {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
        int i, k = bs.length;
	
	if (k > 0) {
	    for (i = 0; i < k; i++) {
		if (bs[i] instanceof GdbBreakpoint) {
		    createBreakpointImpl(bs[i]);
		}
	    }
	} else {
	    debugger.setRunning(); // set state to running because no breakoints to set
	}
    }
    
    private void removeBreakpointImpls() {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
        int i, k = bs.length;
        for (i = 0; i < k; i++) {
	    if (bs[i] instanceof GdbBreakpoint) {
		removeBreakpointImpl(bs [i]);
	    }
	}
    }
    
    /**
     *  Set breakpoint state to UNVALIDATED
     */
    private void unvalidateBreakpoints() {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
        int i, k = bs.length;
        for (i = 0; i < k; i++) {
	    if (bs[i] instanceof GdbBreakpoint) {
		((GdbBreakpoint) bs[i]).setState(GdbBreakpoint.UNVALIDATED);
	    }
	}
    }
    
    public void fixBreakpointImpls() {
        Iterator i = breakpointToImpl.values().iterator();
        while (i.hasNext()) {
            ((BreakpointImpl) i.next()).fixed();
        }
    }

    private void createBreakpointImpl(Breakpoint b) {
        if (breakpointToImpl.containsKey(b)) {
	    return;
	}
        if (b instanceof LineBreakpoint) {
            breakpointToImpl.put(b, new LineBreakpointImpl((LineBreakpoint) b,
			breakpointsReader, debugger, session));
        } else if (b instanceof FunctionBreakpoint) {
            breakpointToImpl.put(b, new FunctionBreakpointImpl(
			(FunctionBreakpoint) b, breakpointsReader, debugger, session));
        }
    }

    private void removeBreakpointImpl(Breakpoint b) {
        BreakpointImpl impl = (BreakpointImpl) breakpointToImpl.get(b);
        if (impl != null) {
            impl.remove();
            breakpointToImpl.remove(b);
	}
    }
}
