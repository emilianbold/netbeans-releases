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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.VirtualMachine;
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
        JPDADebuggerImpl debuggerImpl = (JPDADebuggerImpl) debugger;
        JPDAThreadImpl trImpl = (JPDAThreadImpl) tr;
        VirtualMachine vm = debuggerImpl.getVirtualMachine();
        if (vm == null) {
            return ; // The session has finished
        }
        EventRequestManager erm = vm.eventRequestManager();
        //Remove all step requests -- TODO: Do we want it?
        erm.deleteEventRequests(erm.stepRequests());
        StepRequest stepRequest = vm.eventRequestManager().createStepRequest(
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
        VirtualMachine vm = debuggerImpl.getVirtualMachine();
        if (vm == null) {
            return false; // The session has finished
        }
        EventRequestManager erm = vm.eventRequestManager();
        erm.deleteEventRequest(event.request());
        firePropertyChange(PROP_STATE_EXEC, null, null);
        if (! getHidden())
            debuggerImpl.setStoppedState(trImpl.getThreadReference());
        return getHidden();
    }
}
