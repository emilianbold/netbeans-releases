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

package org.netbeans.modules.debugger.jpda;

import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDADebugger;

import com.sun.jdi.event.Event;
import com.sun.jdi.ThreadReference; 
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;


public class JPDAStepImpl extends JPDAStep implements Executor {
    
    public JPDAStepImpl(JPDADebugger debugger, int size, int depth) {
        super(debugger, size, depth);
    }
    
    public void addStep(JPDAThread tr) {
        JPDADebuggerImpl debuggerImpl = (JPDADebuggerImpl)debugger;
        JPDAThreadImpl trImpl = (JPDAThreadImpl)tr;
        EventRequestManager erm = debuggerImpl.getVirtualMachine().eventRequestManager();
        //Remove all step requests -- TODO: Do we want it?
        erm.deleteEventRequests(erm.stepRequests());
        StepRequest stepRequest = debuggerImpl.getVirtualMachine().
        eventRequestManager().createStepRequest(
            trImpl.getThreadReference(),
            getSize(),
            getDepth()
        );
        stepRequest.addCountFilter(1);
        debuggerImpl.getOperator().register(stepRequest, this);
        stepRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        
        stepRequest.enable ();
    }
    
    public boolean exec (Event event) {
        JPDADebuggerImpl debuggerImpl = (JPDADebuggerImpl)debugger;
        JPDAThreadImpl trImpl = (JPDAThreadImpl)debuggerImpl.getCurrentThread();
        EventRequestManager erm = debuggerImpl.getVirtualMachine().eventRequestManager();
        erm.deleteEventRequest(event.request());
        firePropertyChange(PROP_STATE_EXEC, null, null);
        if (! getHidden())
            debuggerImpl.setStoppedState(trImpl.getThreadReference());
        return getHidden();
    }
}
