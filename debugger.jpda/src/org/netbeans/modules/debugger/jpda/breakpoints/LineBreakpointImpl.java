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
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;

import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.jpda.EngineContextProvider;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class LineBreakpointImpl extends ClassBasedBreakpoint {

    
    private LineBreakpoint breakpoint;
    private EngineContextProvider engineContextProvider;
    
    public LineBreakpointImpl (
        LineBreakpoint breakpoint, 
        JPDADebuggerImpl debugger,
        EngineContextProvider engineContextProvider
    ) {
        super (breakpoint, debugger);
        this.breakpoint = breakpoint;
        this.engineContextProvider = engineContextProvider;
        set ();
    }
    
    protected void setRequests () {
        String className = engineContextProvider.getRelativePath (
            breakpoint.getURL (),
            '.', false
        );
        if (className == null) 
            className = breakpoint.getURL ();
        setClassRequests (
            new String[] {className + '*'}, // innerclasses
            new String[0],
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
        );
        if (className.endsWith(".")) {
            checkLoadedClasses(className, true);
        } else {
            checkLoadedClasses(className, false);
        }
    }
    
    protected void classLoaded (ReferenceType referenceType) {
        Location location = getLocation (
            referenceType,
            breakpoint.getStratum (),
            breakpoint.getSourceName (),
            breakpoint.getLineNumber ()
        );
        if (location == null)
            location = getLocation (
                referenceType,
                breakpoint.getStratum (),
                breakpoint.getSourceName (),
                breakpoint.getLineNumber () + 1
            );
        if (location == null)
            location = getLocation (
                referenceType,
                breakpoint.getStratum (),
                breakpoint.getSourceName (),
                breakpoint.getLineNumber () - 1
            );
        if (location == null)
            location = getLocation (
                referenceType,
                breakpoint.getStratum (),
                breakpoint.getSourceName (),
                breakpoint.getLineNumber () + 2
            );
        if (location == null)
            location = getLocation (
                referenceType,
                breakpoint.getStratum (),
                breakpoint.getSourceName (),
                breakpoint.getLineNumber () - 2
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
                null,
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
                if (!list.isEmpty ()) return (Location) list.get (0);
            }
        } catch (AbsentInformationException ex) {
        }
        return null;
    }
}

