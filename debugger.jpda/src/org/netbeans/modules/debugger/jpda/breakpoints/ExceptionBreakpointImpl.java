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

