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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import java.util.Iterator;
import java.util.List;

import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public abstract class ClassBasedBreakpoint extends BreakpointImpl {

    public ClassBasedBreakpoint (
        JPDABreakpoint breakpoint, 
        JPDADebuggerImpl debugger
    ) {
        super (breakpoint, debugger);
    }
    
    protected void setClassRequests (
        String className
    ) {
        try {
            ClassPrepareRequest cpr = getEventRequestManager ().
                createClassPrepareRequest ();
            cpr.addClassFilter (className);
            addEventRequest (cpr);
            
            Iterator i = getVirtualMachine ().classesByName (className).
                iterator ();
            while (i.hasNext ()) {
                classLoaded ((ReferenceType) i.next ());
            }
        } catch (VMDisconnectedException e) {
        }
    }

    public boolean exec (Event event) {
        classLoaded (((ClassPrepareEvent) event).referenceType ());
        return true;
    }
    
    protected abstract void classLoaded (ReferenceType referenceType);
}

