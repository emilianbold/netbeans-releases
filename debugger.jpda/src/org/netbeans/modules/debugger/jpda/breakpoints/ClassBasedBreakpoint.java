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
import com.sun.jdi.VirtualMachine;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
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
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.breakpoints") != null;

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
                int i, k = classFilters.length;
                for (i = 0; i < k; i++) {
                    ClassPrepareRequest cpr = getEventRequestManager ().
                        createClassPrepareRequest ();
                    cpr.addClassFilter (classFilters [i]);
                    if (verbose)
                        System.out.println ("B     set class load request: " + classFilters [i]);
                    addEventRequest (cpr);
                }
                k = classExclusionFilters.length;
                for (i = 0; i < k; i++) {
                    ClassPrepareRequest cpr = getEventRequestManager ().
                        createClassPrepareRequest ();
                    cpr.addClassExclusionFilter (classExclusionFilters [i]);
                    if (verbose)
                        System.out.println ("B     set class load exclusion request: " + classExclusionFilters [i]);
                    addEventRequest (cpr);
                }
            }
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED) != 0
            ) {
                int i, k = classFilters.length;
                for (i = 0; i < k; i++) {
                    ClassUnloadRequest cur = getEventRequestManager ().
                        createClassUnloadRequest ();
                    cur.addClassFilter (classFilters [i]);
                    if (verbose)
                        System.out.println ("B     set class unload request: " + classFilters [i]);
                    addEventRequest (cur);
                }
                k = classExclusionFilters.length;
                for (i = 0; i < k; i++) {
                    ClassUnloadRequest cur = getEventRequestManager ().
                        createClassUnloadRequest ();
                    cur.addClassExclusionFilter (classExclusionFilters [i]);
                    if (verbose)
                        System.out.println ("B     set class unload exclusion request: " + classExclusionFilters [i]);
                    addEventRequest (cur);
                }
            }
        } catch (VMDisconnectedException e) {
        }
    }
    
    protected void checkLoadedClasses (
        String className, 
        boolean all
    ) {
        if (verbose)
            System.out.println("B   check loaded classes: " + className + " : " + all);
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return;
        try {
            Iterator i = null;
            if (all) {
                i = getVirtualMachine ().allClasses ().iterator ();
            } else {
                i = getVirtualMachine ().classesByName (className).iterator ();
            }
            while (i.hasNext ()) {
                ReferenceType referenceType = (ReferenceType) i.next ();
                if (verbose)
                    System.out.println("B     cls: " + referenceType);
                if (i != null) {
                    String name = referenceType.name ();
                    if ( ( className.endsWith ("*") &&
                           name.startsWith (className.substring 
                             (0, className.length () - 1))
                         ) ||
                         ( className.startsWith ("*") &&
                           name.endsWith (className.substring (1))
                         ) ||
                         className.equals (name)
                    ) {
                        if (verbose)
                            System.out.println("B       cls loaded! ");
                        classLoaded (referenceType);
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

