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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;

import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class LineBreakpointImpl extends ClassBasedBreakpoint {
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.breakpoints") != null;

    
    private LineBreakpoint      breakpoint;
    private SourcePath          engineContext;
    private int                 lineNumber;
    
    
    public LineBreakpointImpl (
        LineBreakpoint breakpoint, 
        JPDADebuggerImpl debugger,
        SourcePath engineContext
    ) {
        super (breakpoint, debugger);
        this.breakpoint = breakpoint;
        this.engineContext = engineContext;
        lineNumber = breakpoint.getLineNumber ();
        set ();
    }
    
    void fixed () {
        if (verbose)
            System.out.println ("B fix breakpoint impl: " + this);
        lineNumber = breakpoint.getLineNumber ();
        super.fixed ();
    }
    
    protected void setRequests () {
        String className = engineContext.getRelativePath (
            breakpoint.getURL (),
            '.', false
        );
        if (className == null) {
            //HACK: for JSPs.
            //PENDING
            className = breakpoint.getURL ();
            setClassRequests (
                new String[] {
                    className
                }, 
                new String [0],
                ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
            );
            checkLoadedClasses (className, true);
        } else {
            setClassRequests (
                new String[] {
                    className,
                    className + ".*", // innerclasses
                    className + "$*", // innerclasses
                }, 
                new String [0],
                ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
            );
            checkLoadedClasses (className, false);
        }
    }
    
    protected void classLoaded (ReferenceType referenceType) {
        if (verbose)
            System.out.println ("B class loaded: " + referenceType);
        
        Location location = getLocation (
            referenceType,
            breakpoint.getStratum (),
            breakpoint.getSourceName (),
            lineNumber
        );
        if (location == null)
            location = getLocation (
                referenceType,
                breakpoint.getStratum (),
                breakpoint.getSourceName (),
                lineNumber + 1
            );
        if (location == null)
            location = getLocation (
                referenceType,
                breakpoint.getStratum (),
                breakpoint.getSourceName (),
                lineNumber - 1
            );
        if (location == null)
            location = getLocation (
                referenceType,
                breakpoint.getStratum (),
                breakpoint.getSourceName (),
                lineNumber + 2
            );
        if (location == null)
            location = getLocation (
                referenceType,
                breakpoint.getStratum (),
                breakpoint.getSourceName (),
                lineNumber - 2
            );
            
        if (location == null) return; 
        try {
            BreakpointRequest br = getEventRequestManager ().
                createBreakpointRequest (location);
            addEventRequest (br);
        } catch (VMDisconnectedException e) {
        }
    }

    public boolean exec (Event event) {
        if (event instanceof BreakpointEvent)
            return perform (
                breakpoint.getCondition (),
                ((BreakpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                null
            );
        return super.exec (event);
    }
    
    private static Location getLocation (
        ReferenceType referenceType,
        String stratum,
        String sourceName,
        int lineNumber
    ) {
        try {
            List list = new ArrayList (referenceType.locationsOfLine (
                stratum,
                sourceName,
                lineNumber
            ));
            if (verbose)
                System.out.println ("B   get location: referenceType=" + 
                    referenceType + " stratum=" + stratum + 
                    " source name=" + sourceName + " lineNumber " + lineNumber + 
                    " (#" + list.size () + ")");
            if (!list.isEmpty ()) return (Location) list.get (0);

            // add lines from innerclasses
            Iterator i = referenceType.nestedTypes ().iterator ();
            while (i.hasNext ()) {
                ReferenceType rt = (ReferenceType) i.next ();
                list = rt.locationsOfLine (
                    stratum,
                    sourceName,
                    lineNumber
                );
                if (verbose)
                    System.out.println ("B   get location2: referenceType=" + 
                        referenceType + " stratum=" + stratum + 
                        " source name=" + sourceName + " lineNumber" + lineNumber + 
                        ":= " + list.size ());
                if (!list.isEmpty ()) return (Location) list.get (0);
            }
        } catch (AbsentInformationException ex) {
            // we are not able to create breakpoint in this situation. 
            // should we write some message?!?
        } catch (ClassNotPreparedException ex) {
            // should not occurre. VirtualMachine.allClasses () returns prepared
            // classes only. But...
            ex.printStackTrace ();
        }
        return null;
    }
}

