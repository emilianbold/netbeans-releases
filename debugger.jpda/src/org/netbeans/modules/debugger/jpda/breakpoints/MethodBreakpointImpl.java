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

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.netbeans.modules.debugger.jpda.util.Executor;


/**
* Implementation of method breakpoint.
*
* @author   Jan Jancura
*/
public class MethodBreakpointImpl extends BreakpointImpl implements Executor {

    private MethodBreakpoint breakpoint;
    
    
    public MethodBreakpointImpl (MethodBreakpoint breakpoint, JPDADebuggerImpl debugger, Session session) {
        super (breakpoint, debugger, session);
        this.breakpoint = breakpoint;
        set ();
    }
    
    protected void setRequests () {
        try {
            if ((breakpoint.getBreakpointType () & MethodBreakpoint.TYPE_METHOD_ENTRY) != 0
            ) {
                MethodEntryRequest mer = getEventRequestManager ().
                    createMethodEntryRequest ();
                String[] cnf = breakpoint.getClassFilters ();
                int i, k = cnf.length;
                for (i = 0; i < k; i++)
                    mer.addClassFilter (cnf [i]);
                cnf = breakpoint.getClassExclusionFilters ();
                k = cnf.length;
                for (i = 0; i < k; i++)
                    mer.addClassExclusionFilter (cnf [i]);
                addEventRequest (mer);
            }
            if ((breakpoint.getBreakpointType () & MethodBreakpoint.TYPE_METHOD_EXIT) != 0
            ) {
                MethodExitRequest mxr = getEventRequestManager ().
                    createMethodExitRequest ();
                String[] cnf = breakpoint.getClassFilters ();
                int i, k = cnf.length;
                for (i = 0; i < k; i++)
                    mxr.addClassFilter (cnf [i]);
                cnf = breakpoint.getClassExclusionFilters ();
                k = cnf.length;
                for (i = 0; i < k; i++)
                    mxr.addClassExclusionFilter (cnf [i]);
                addEventRequest (mxr);
            }
        } catch (VMDisconnectedException e) {
        }
    }

    public boolean exec (Event event) {
        Method m = event instanceof MethodEntryEvent ?
            ((MethodEntryEvent) event).method () :
            ((MethodExitEvent) event).method ();
        ThreadReference thread = event instanceof MethodEntryEvent ?
            ((MethodEntryEvent) event).thread () :
            ((MethodExitEvent) event).thread ();
            
        // check if the name is OK
        if (breakpoint.getMethodName ().length () > 0) {
            if ("<init>".equals (m.name ())) {
                // stopped in constructor
                try {
                if (thread.frameCount () > 0) {
                    String cn = thread.frame (0).location ().
                        declaringType ().name ();
                    int i = cn.lastIndexOf ('.');
                    if (i > 0) cn = cn.substring (i + 1);
                    if ( (!breakpoint.getMethodName ().equals (cn)) &&
                         (!breakpoint.getMethodName ().equals ("<init>"))
                    )
                        return true; //resume
                }
                } catch (IncompatibleThreadStateException ex) {
                    ex.printStackTrace ();
                }
            } else
            if (!breakpoint.getMethodName ().equals (m.name ()))
                // stopped in normal method
                return true; //resume
            
        }
            
        // perform breakpoint 
        return event instanceof MethodEntryEvent ?
            perform (
                breakpoint.getCondition (),
                thread,
                ((MethodEntryEvent) event).location ().declaringType (),
                null
            ) :
            perform (
                breakpoint.getCondition (),
                thread,
                ((MethodExitEvent) event).location ().declaringType (),
                null
            );
    }
}

