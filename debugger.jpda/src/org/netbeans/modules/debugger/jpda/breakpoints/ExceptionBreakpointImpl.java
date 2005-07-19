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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.ExceptionRequest;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;

import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ExceptionBreakpointImpl extends ClassBasedBreakpoint {

    
    private ExceptionBreakpoint breakpoint;
    
    
    public ExceptionBreakpointImpl (ExceptionBreakpoint breakpoint, JPDADebuggerImpl debugger, Session session) {
        super (breakpoint, debugger, session);
        this.breakpoint = breakpoint;
        set ();
    }
    
    protected void setRequests () {
        setClassRequests (
            new String[] {breakpoint.getExceptionClassName ()},
            new String[0],
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
        );
        checkLoadedClasses (breakpoint.getExceptionClassName (), true);
    }
    
    protected void classLoaded (ReferenceType referenceType) {
        try {
            ExceptionRequest er = getEventRequestManager ().
                createExceptionRequest (
                    referenceType, 
                    (breakpoint.getCatchType () & 
                        ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED) != 0, 
                    (breakpoint.getCatchType () & 
                        ExceptionBreakpoint.TYPE_EXCEPTION_UNCATCHED) != 0
                );
            addEventRequest (er);
        } catch (VMDisconnectedException e) {
        }
    }

    public boolean exec (Event event) {
        if (event instanceof ExceptionEvent)
            return perform (
                breakpoint.getCondition (),
                ((ExceptionEvent) event).thread (),
                ((ExceptionEvent) event).location().declaringType(),
                ((ExceptionEvent) event).exception ()
            );
        return super.exec (event);
    }
}

