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

import java.beans.PropertyChangeListener;
import java.util.HashMap;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyDebuggerEngineListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * Listens on JPDADebugger.PROP_STATE and DebuggerManager.PROP_BREAKPOINTS, and
 * and creates XXXBreakpointImple classes for all JPDABreakpoints.
 *
 * @author   Jan Jancura
 */
public class BreakpointsEngineListener extends LazyDebuggerEngineListener 
implements PropertyChangeListener, DebuggerManagerListener {

    private JPDADebuggerImpl        debugger;
    private DebuggerEngine          engine;
    private boolean                 started = false;
    
    
    public BreakpointsEngineListener (DebuggerEngine engine) {
        super (engine);
        this.debugger = (JPDADebuggerImpl) engine.lookupFirst 
            (JPDADebugger.class);
        this.engine = engine;
        debugger.addPropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
    }

    protected void destroy () {
        debugger.removePropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
        DebuggerManager.getDebuggerManager ().removeDebuggerListener (
            DebuggerManager.PROP_BREAKPOINTS,
            this
        );
        removeBreakpoints ();
    }
    
    public String[] getProperties () {
        return new String[] {"a"};
    }

    public void propertyChange (java.beans.PropertyChangeEvent evt) {
        if (debugger.getState () == JPDADebugger.STATE_RUNNING) {
            if (started) return;
            started = true;
            updateBreakpoints ();
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_BREAKPOINTS,
                this
            );
        }
    }

    public void breakpointAdded (Breakpoint breakpoint) {
        updateBreakpoint (breakpoint);
    }    

    public void breakpointRemoved (Breakpoint breakpoint) {
        removeBreakpoint (breakpoint);
    }
    

    public Breakpoint[] initBreakpoints () {return new Breakpoint [0];}
    public Watch[] initWatches () {return new Watch [0];}
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void watchAdded (Watch watch) {}
    public void watchRemoved (Watch watch) {}


    // helper methods ..........................................................
    
    private HashMap breakpointToImpl = new HashMap ();
    
    
    private void updateBreakpoints () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().getBreakpoints ();
        int i, k = bs.length;
        for (i = 0; i < k; i++)
            updateBreakpoint (bs [i]);
    }
    
    private void removeBreakpoints () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().getBreakpoints ();
        int i, k = bs.length;
        for (i = 0; i < k; i++)
            removeBreakpoint (bs [i]);
    }

    private void updateBreakpoint (Breakpoint b) {
        if (breakpointToImpl.containsKey (b)) return;
        if (b instanceof LineBreakpoint) {
            breakpointToImpl.put (
                b,
                new JPDALineBreakpointImpl (
                    (LineBreakpoint) b,
                    debugger,
                    engine
                )
            );
        } else
        if (b instanceof ExceptionBreakpoint) {
            breakpointToImpl.put (
                b,
                new JPDAExceptionBreakpointImpl (
                    (ExceptionBreakpoint) b,
                    debugger
                )
            );
        } else
        if (b instanceof MethodBreakpoint) {
            breakpointToImpl.put (
                b,
                new JPDAMethodBreakpointImpl (
                    (MethodBreakpoint) b,
                    debugger
                )
            );
        } else
        if (b instanceof FieldBreakpoint) {
            breakpointToImpl.put (
                b,
                new JPDAFieldBreakpointImpl (
                    (FieldBreakpoint) b,
                    debugger
                )
            );
        } else
        if (b instanceof ThreadBreakpoint) {
            breakpointToImpl.put (
                b,
                new JPDAThreadBreakpointImpl (
                    (ThreadBreakpoint) b,
                    debugger
                )
            );
        } else
        if (b instanceof ClassLoadUnloadBreakpoint) {
            breakpointToImpl.put (
                b,
                new JPDAClassBreakpointImpl (
                    (ClassLoadUnloadBreakpoint) b,
                    debugger
                )
            );
        }
    }

    private void removeBreakpoint (Breakpoint b) {
        JPDABreakpointImpl impl = (JPDABreakpointImpl) breakpointToImpl.get (b);
        if (impl == null) return;
        impl.remove ();
        breakpointToImpl.remove (b);
    }
}
