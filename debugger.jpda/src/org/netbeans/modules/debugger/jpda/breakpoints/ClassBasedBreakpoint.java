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
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;

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
        String[] classFilters,
        String[] classExclusionFilters,
        int breakpointType
    ) {
        try {
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED) != 0
            ) {
                ClassPrepareRequest cpr = getEventRequestManager ().
                    createClassPrepareRequest ();
                int i, k = classFilters.length;
                for (i = 0; i < k; i++)
                    cpr.addClassFilter (classFilters [i]);
                k = classExclusionFilters.length;
                for (i = 0; i < k; i++)
                    cpr.addClassExclusionFilter (classExclusionFilters [i]);
                addEventRequest (cpr);
            }
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED) != 0
            ) {
                ClassUnloadRequest cur = getEventRequestManager ().
                    createClassUnloadRequest ();
                int i, k = classFilters.length;
                for (i = 0; i < k; i++)
                    cur.addClassFilter (classFilters [i]);
                k = classExclusionFilters.length;
                for (i = 0; i < k; i++)
                    cur.addClassExclusionFilter (classExclusionFilters [i]);
                addEventRequest (cur);
            }
        } catch (VMDisconnectedException e) {
        }
    }
    
    protected void checkLoadedClasses (
        String className, 
        boolean all
    ) {
        try {
            Iterator i = null;
            if (all) {
                i = getVirtualMachine().allClasses().iterator ();
            } else {
                i = getVirtualMachine ().classesByName (className).iterator();
            }
            while (i.hasNext ()) {
                Object ref = i.next();
                if (i != null) {
                    String name = ((ReferenceType)ref).name();
                    if (name.startsWith(className)) {
                        classLoaded ((ReferenceType) ref);
                    }
                }
            }
        } catch (VMDisconnectedException e) {
        }
    }

    public boolean exec (Event event) {
        if (event instanceof ClassPrepareEvent)
            classLoaded (((ClassPrepareEvent) event).referenceType ());
        else
            classUnloaded (((ClassUnloadEvent) event).className ());
        return true;
    }
    
    protected void classLoaded (ReferenceType referenceType) {}
    protected void classUnloaded (String className) {}
}

