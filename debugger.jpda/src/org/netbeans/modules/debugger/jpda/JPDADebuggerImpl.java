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

package org.netbeans.modules.debugger.jpda;

import javax.swing.JComponent;
import org.openide.debugger.Debugger;

import org.netbeans.modules.debugger.*;


/**
 * This class represents JPDA Debugger Implementation.
 *
 * @author Jan Jancura
 */
public class JPDADebuggerImpl extends DebuggerImpl 
implements EventsProducer, ConnectSupport {
    
    private static CoreBreakpoint.Event[]      breakpointEvents;

    static {
        breakpointEvents = new CoreBreakpoint.Event[] {
            new LineBreakpoint (),
            new MethodBreakpoint (),
            new ExceptionBreakpoint (),
            new VariableBreakpoint (),
            new ThreadBreakpoint (),
            new ClassBreakpoint ()
        };
    }

    
    /**
     * Returns displayable name of JPDA debugger.
     *
     * @return displayable name of JPDA debugger
     */
    public  String getDisplayName () {
        return JPDADebugger.getLocString ("CTL_Debugger_version");
    }

    /**
     * True, if current Debugger Implementation supports ConnectAction.
     *
     * @return true if current Debugger Implementation supports ConnectAction
     */
    public boolean supportsConnectAction () {
        return true;
    }

    /**
     * Returns panel for ConectAction dialog or null.
     *
     * @return panel for ConectAction dialog or null
     */
    public JComponent getConnectPanel () {
        return new ConnectPanel ();
    }

    /**
     * Each Debugger Implementation can define its own set of breakpoint
     * types (= Breakpoint Events). Set of Breakpoint Events should be 
     * static. This property can be set invoking {@link #setBreakpointEvents} method.
     * Default implementation returns empty field.
     *
     * @return set of Breakpoint Events of this Debugger Implementation
     */
    public CoreBreakpoint.Event[] getEvents () {
        return breakpointEvents;
    }
    
    /**
     * Registers a new breakpoint type.
     *
     * @return e new breakpoint type to be registered
     */
    public void registerEvent (CoreBreakpoint.Event e) {
        CoreBreakpoint.Event [] bes = new CoreBreakpoint.Event 
            [breakpointEvents.length + 1];
        System.arraycopy (
            breakpointEvents, 0,
            bes, 0,
            breakpointEvents.length
        );
        bes [breakpointEvents.length] = e;
        breakpointEvents = bes;
    }

    /**
     * Returns a new instance of Debugger.
     */
    public Debugger createDebugger () {
        return new JPDADebugger ();
    }
}

