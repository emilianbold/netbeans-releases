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

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
* Representation of a debugging session.
*
* @author  Gordon Prieur (copied from Jan Jancura's JPDA implementation)
*/
public class MakeCallerCurrentActionProvider extends ActionsProviderSupport implements PropertyChangeListener {
    
    private ContextProvider lookupProvider;
    private GdbDebugger debugger;
    
    public MakeCallerCurrentActionProvider(ContextProvider lookupProvider) {
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        this.lookupProvider = lookupProvider;
        debugger.addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_MAKE_CALLER_CURRENT);
    }

    public void doAction(Object action) {
        int i = getCurrentCallStackFrameIndex(debugger);
        if (i < (debugger.getStackDepth() - 1)) {
	    setCurrentCallStackFrameIndex(debugger, ++i);
	}
    }
    
    protected void checkEnabled(String debuggerState) {
        if (debuggerState == debugger.STATE_STOPPED) {
	    int i = getCurrentCallStackFrameIndex(debugger);
	    setEnabled(ActionsManager.ACTION_MAKE_CALLER_CURRENT, i < (debugger.getStackDepth() - 1));
        } else {
	    setEnabled(ActionsManager.ACTION_MAKE_CALLER_CURRENT, false);
	}
    }
    
    static int getCurrentCallStackFrameIndex(GdbDebugger debugger) {
	CallStackFrame csf = debugger.getCurrentCallStackFrame();
	if (csf != null) {
	    ArrayList callstack = debugger.getCallStack();
	    return callstack.indexOf(csf);
	} else {
	    return -1;
	}
    }
    
    static void setCurrentCallStackFrameIndex(GdbDebugger debugger, int index) {
	if (index < debugger.getStackDepth()) {
	    CallStackFrame csf = debugger.getCallStackFrames(index, index + 1)[0];
	    csf.makeCurrent();
	}
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
	checkEnabled(debugger.getState());
    }
}

