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

