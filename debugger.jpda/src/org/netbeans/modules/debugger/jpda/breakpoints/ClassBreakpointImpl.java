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

package org.netbeans.modules.debugger.jpda.breakpoints;


import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.netbeans.modules.debugger.jpda.util.Executor;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ClassBreakpointImpl extends BreakpointImpl 
implements Executor {

    private ClassLoadUnloadBreakpoint breakpoint;
    
    
    public ClassBreakpointImpl (
        ClassLoadUnloadBreakpoint breakpoint, 
        JPDADebuggerImpl debugger
    ) {
        super (breakpoint, debugger);
        this.breakpoint = breakpoint;
        set ();
    }
    
    protected void setRequests () {
        setRequests (
            breakpoint.getClassNameFilter (), 
            breakpoint.getBreakpointType (),
            breakpoint.isExclusionFilter ()
        );
    }
    
    protected void setRequests (
        String className,
        int breakpointType,
        boolean isExclusion
    ) {
        try {
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED) != 0
            ) {
                ClassPrepareRequest cpr = getEventRequestManager ().
                    createClassPrepareRequest ();
                if (isExclusion)
                    cpr.addClassExclusionFilter (className);
                else
                    cpr.addClassFilter (className);
                addEventRequest (cpr);
            }
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED) != 0
            ) {
                ClassUnloadRequest cur = getEventRequestManager ().
                    createClassUnloadRequest ();
                if (isExclusion)
                    cur.addClassExclusionFilter (className);
                else
                    cur.addClassFilter (className);
                addEventRequest (cur);
            }
        } catch (VMDisconnectedException e) {
        }
    }

    public boolean exec (Event event) {
        return event instanceof ClassPrepareEvent ?
            perform (
                null,
                ((ClassPrepareEvent) event).thread (),
                ((ClassPrepareEvent) event).referenceType ()
            ) :
            perform (
                null,
                null,
                null
            );
    }
}

