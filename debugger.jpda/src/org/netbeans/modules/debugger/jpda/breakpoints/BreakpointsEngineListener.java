/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.breakpoints;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;

import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;

import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * Listens on JPDADebugger.PROP_STATE and DebuggerManager.PROP_BREAKPOINTS, and
 * and creates XXXBreakpointImpl classes for all JPDABreakpoints.
 *
 * @author   Jan Jancura
 */
public class BreakpointsEngineListener extends LazyActionsManagerListener 
implements PropertyChangeListener, DebuggerManagerListener {
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.breakpoints") != null;

    private JPDADebuggerImpl        debugger;
    private SourcePath           engineContext;
    private boolean                 started = false;
    private Session                 session;


    public BreakpointsEngineListener (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst 
            (null, JPDADebugger.class);
        engineContext = (SourcePath) lookupProvider.
            lookupFirst (null, SourcePath.class);
        session = (Session) lookupProvider.lookupFirst(null, Session.class);
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
        removeBreakpointImpls ();
    }
    
    public String[] getProperties () {
        return new String[] {"asd"};
    }

    public void propertyChange (java.beans.PropertyChangeEvent evt) {
        if (debugger.getState () == JPDADebugger.STATE_RUNNING) {
            if (started) return;
            started = true;
            createBreakpointImpls ();
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_BREAKPOINTS,
                this
            );
        }
        if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) {
            removeBreakpointImpls ();
            started = false;
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_BREAKPOINTS,
                this
            );
        }
    }
    
    public void actionPerformed (Object action) {
//        if (action == ActionsManager.ACTION_FIX)
//            fixBreakpointImpls ();
    }

    public void breakpointAdded (Breakpoint breakpoint) {
        createBreakpointImpl (breakpoint);
    }    

    public void breakpointRemoved (Breakpoint breakpoint) {
        removeBreakpointImpl (breakpoint);
    }
    

    public Breakpoint[] initBreakpoints () {return new Breakpoint [0];}
    public void initWatches () {}
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void watchAdded (Watch watch) {}
    public void watchRemoved (Watch watch) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}


    // helper methods ..........................................................
    
    private HashMap breakpointToImpl = new HashMap ();
    
    
    private void createBreakpointImpls () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().getBreakpoints ();
        int i, k = bs.length;
        for (i = 0; i < k; i++)
            createBreakpointImpl (bs [i]);
    }
    
    private void removeBreakpointImpls () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().getBreakpoints ();
        int i, k = bs.length;
        for (i = 0; i < k; i++)
            removeBreakpointImpl (bs [i]);
    }
    
    public void fixBreakpointImpls () {
        Iterator i = breakpointToImpl.values ().iterator ();
        while (i.hasNext ())
            ((BreakpointImpl) i.next ()).fixed ();
    }

    private void createBreakpointImpl (Breakpoint b) {
        if (breakpointToImpl.containsKey (b)) return;
        if (verbose)
            System.out.println ("B create breakpoint impl for breakpoint: " + b);
        if (b instanceof LineBreakpoint) {
            breakpointToImpl.put (
                b,
                new LineBreakpointImpl (
                    (LineBreakpoint) b,
                    debugger,
                    session,
                    engineContext
                )
            );
        } else
        if (b instanceof ExceptionBreakpoint) {
            breakpointToImpl.put (
                b,
                new ExceptionBreakpointImpl (
                    (ExceptionBreakpoint) b,
                    debugger,
                    session
                )
            );
        } else
        if (b instanceof MethodBreakpoint) {
            breakpointToImpl.put (
                b,
                new MethodBreakpointImpl (
                    (MethodBreakpoint) b,
                    debugger,
                    session
                )
            );
        } else
        if (b instanceof FieldBreakpoint) {
            breakpointToImpl.put (
                b,
                new FieldBreakpointImpl (
                    (FieldBreakpoint) b,
                    debugger,
                    session
                )
            );
        } else
        if (b instanceof ThreadBreakpoint) {
            breakpointToImpl.put (
                b,
                new ThreadBreakpointImpl (
                    (ThreadBreakpoint) b,
                    debugger,
                    session
                )
            );
        } else
        if (b instanceof ClassLoadUnloadBreakpoint) {
            breakpointToImpl.put (
                b,
                new ClassBreakpointImpl (
                    (ClassLoadUnloadBreakpoint) b,
                    debugger,
                    session
                )
            );
        }
    }

    private void removeBreakpointImpl (Breakpoint b) {
        if (verbose)
            System.out.println ("B remove breakpoint impl for breakpoint: " + b);
        BreakpointImpl impl = (BreakpointImpl) breakpointToImpl.get (b);
        if (impl == null) return;
        impl.remove ();
        breakpointToImpl.remove (b);
    }
}
