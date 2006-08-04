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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Properties.Reader;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;

/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints. Watches are loaded by debuggercore's PersistentManager.
 * - listens on all changes of breakpoints and saves new values
 *
 * @author Jan Jancura
 */
public class PersistenceManager implements LazyDebuggerManagerListener {
    
    public Breakpoint[] initBreakpoints () {
        Properties p = Properties.getDefault ().getProperties ("debugger").
            getProperties (DebuggerManager.PROP_BREAKPOINTS);
        return (Breakpoint[]) p.getArray (
            "jpda", 
            new Breakpoint [0]
        );
    }
    
    public void initWatches () {
    }
    
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }
    
    public void breakpointAdded (Breakpoint breakpoint) {
        Properties p = Properties.getDefault ().getProperties ("debugger").
            getProperties (DebuggerManager.PROP_BREAKPOINTS);
        p.setArray (
            "jpda", 
            getBreakpoints ()
        );
        breakpoint.addPropertyChangeListener(this);
    }

    public void breakpointRemoved (Breakpoint breakpoint) {
        Properties p = Properties.getDefault ().getProperties ("debugger").
            getProperties (DebuggerManager.PROP_BREAKPOINTS);
        p.setArray (
            "jpda", 
            getBreakpoints ()
        );
        breakpoint.removePropertyChangeListener(this);
    }
    public void watchAdded (Watch watch) {
    }
    
    public void watchRemoved (Watch watch) {
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof JPDABreakpoint) {
            if (LineBreakpoint.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
                BreakpointsReader r = findBreakpointsReader();
                if (r != null) {
                    // Reset the class name, which might change
                    r.storeCachedClassName((JPDABreakpoint) evt.getSource(), null);
                }
            }
            storeBreakpoints();
        }
    }
    
    static BreakpointsReader findBreakpointsReader() {
        BreakpointsReader breakpointsReader = null;
        Iterator i = DebuggerManager.getDebuggerManager().lookup (null, Reader.class).iterator ();
        while (i.hasNext ()) {
            Reader r = (Reader) i.next ();
            String[] ns = r.getSupportedClassNames ();
            if (ns.length == 1 && JPDABreakpoint.class.getName().equals(ns[0])) {
                breakpointsReader = (BreakpointsReader) r;
                break;
            }
        }
        return breakpointsReader;
    }

    static void storeBreakpoints() {
        Properties.getDefault ().getProperties ("debugger").
            getProperties (DebuggerManager.PROP_BREAKPOINTS).setArray (
                "jpda",
                getBreakpoints ()
            );
    }
    
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}
    
    
    private static Breakpoint[] getBreakpoints () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        int i, k = bs.length;
        ArrayList<Breakpoint> bb = new ArrayList<Breakpoint>();
        for (i = 0; i < k; i++)
            // Don't store hidden breakpoints
            if ( bs[i] instanceof JPDABreakpoint &&
                 !((JPDABreakpoint) bs [i]).isHidden ()
            )
                bb.add (bs [i]);
        bs = new Breakpoint [bb.size ()];
        return bb.toArray (bs);
    }
}
