/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui;

import java.beans.PropertyChangeEvent;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;


/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints & watches on startup
 * - listens on all changes of breakpoints and watches (like breakoint / watch
 *     added / removed, or some property change) and saves a new values
 *
 * @author Jan Jancura
 */
public class PersistenceManager implements LazyDebuggerManagerListener {
    
    public Breakpoint[] initBreakpoints () {
        return new Breakpoint [0];
    }
    
    public void initWatches () {
        // As a side-effect, creates the watches. WatchesReader is triggered.
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.getArray (
            DebuggerManager.PROP_WATCHES, 
            new Watch [0]
        );
    }
    
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_WATCHES_INIT,
            DebuggerManager.PROP_WATCHES
        };
    }
    
    public void breakpointAdded (Breakpoint breakpoint) {
    }

    public void breakpointRemoved (Breakpoint breakpoint) {
    }
    
    public void watchAdded (Watch watch) {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.setArray (
            DebuggerManager.PROP_WATCHES, 
            DebuggerManager.getDebuggerManager ().getWatches ()
        );
        watch.addPropertyChangeListener (this);
    }
    
    public void watchRemoved (Watch watch) {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.setArray (
            DebuggerManager.PROP_WATCHES, 
            DebuggerManager.getDebuggerManager ().getWatches ()
        );
        watch.removePropertyChangeListener(this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Watch) {
            Properties.getDefault ().getProperties ("debugger").setArray (
                DebuggerManager.PROP_WATCHES,
                DebuggerManager.getDebuggerManager ().getWatches ()
            );
        }
    }
    
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}
}
