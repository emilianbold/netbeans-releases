/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ClassBreakpointImpl extends ClassBasedBreakpoint {

    private ClassLoadUnloadBreakpoint breakpoint;
    
    
    public ClassBreakpointImpl (
        ClassLoadUnloadBreakpoint breakpoint, 
        JPDADebuggerImpl debugger,
        Session session
    ) {
        super (breakpoint, debugger, session);
        this.breakpoint = breakpoint;
        set ();
    }
    
    protected void setRequests () {
        setClassRequests (
            breakpoint.getClassFilters (), 
            breakpoint.getClassExclusionFilters (), 
            breakpoint.getBreakpointType ()
        );
    }

    public boolean exec (Event event) {
        if (event instanceof ClassPrepareEvent)
            try {
                return perform (
                    null,
                    ((ClassPrepareEvent) event).thread (),
                    ((ClassPrepareEvent) event).referenceType (),
                    ((ClassPrepareEvent) event).referenceType ().classObject ()
                );
            } catch (UnsupportedOperationException ex) {
                // PATCH for KVM. They does not support 
                // ReferenceType.classObject ()
                return perform (
                    null,
                    ((ClassPrepareEvent) event).thread (),
                    ((ClassPrepareEvent) event).referenceType (),
                    null
                );
            }
        else
            return perform (
                null,
                null,
                null,
                null
            );
    }
}

