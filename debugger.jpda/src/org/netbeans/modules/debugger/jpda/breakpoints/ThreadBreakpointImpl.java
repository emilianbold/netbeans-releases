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

package org.netbeans.modules.debugger.jpda.breakpoints;


import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.netbeans.modules.debugger.jpda.util.Executor;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ThreadBreakpointImpl extends BreakpointImpl implements Executor {

    // variables ...............................................................

    private ThreadBreakpoint              breakpoint;


    // init ....................................................................
    
    public ThreadBreakpointImpl (ThreadBreakpoint presenter, JPDADebuggerImpl debugger, Session session) {
        super (presenter, null, debugger, session);
        breakpoint = presenter;
        set ();
    }
            
       
    // Event impl ..............................................................

    protected void setRequests () {
        try {
            if ( (breakpoint.getBreakpointType () & 
                  breakpoint.TYPE_THREAD_STARTED ) != 0
            ) {
                ThreadStartRequest tsr = getEventRequestManager ().
                    createThreadStartRequest ();
                addEventRequest (tsr);
            }
            if ( (breakpoint.getBreakpointType () & 
                  breakpoint.TYPE_THREAD_DEATH) != 0
            ) {
                VirtualMachine vm = getVirtualMachine();
                if (vm != null) {
                    ThreadDeathRequest tdr = vm.eventRequestManager().
                        createThreadDeathRequest();
                    addEventRequest (tdr);
                }
            }
        } catch (VMDisconnectedException e) {
        }
    }

    public boolean exec (Event event) {
        ThreadReference thread = null;
        if (event instanceof ThreadStartEvent)
            thread = ((ThreadStartEvent) event).thread ();
        else
        if (event instanceof ThreadDeathEvent)
            thread = ((ThreadDeathEvent) event).thread ();

        return perform (
            null,
            thread,
            null,
            thread
        );
    }
}
