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
        chackLoadedClasses (className);
    }
    
    protected void classLoaded (ReferenceType referenceType) {
        try {
            List list = new ArrayList (referenceType.locationsOfLine (
                breakpoint.getStratum (),
                breakpoint.getSourceName (),
                breakpoint.getLineNumber ()
            ));
            
            // add lines from innerclasses
            Iterator i = referenceType.nestedTypes ().iterator ();
            while (i.hasNext ()) {
                ReferenceType rt = (ReferenceType) i.next ();
                list.addAll (rt.locationsOfLine (
                    breakpoint.getStratum (),
                    breakpoint.getSourceName (),
                    breakpoint.getLineNumber ()
                ));
            }
            
            if (list.size () < 1) return; 
            Location l = (Location) list.get (0);
            try {
                BreakpointRequest br = getEventRequestManager ().
                    createBreakpointRequest (l);
                addEventRequest (br);
            } catch (VMDisconnectedException e) {
            }
        } catch (AbsentInformationException ex) {
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
}

