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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;

/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints on startup
 * - listens on all changes of breakpoints (like breakoint 
 *     added / removed, or some property change) and saves a new values
 *
 * @author Alexander Zgursky
 */
public class PersistenceManager implements LazyDebuggerManagerListener {
    
    public Breakpoint[] initBreakpoints() {
        final Properties p = Properties.getDefault().getProperties("debugger").
                getProperties(DebuggerManager.PROP_BREAKPOINTS);
        return (Breakpoint[]) p.getArray("bpel", new Breakpoint[0]);
    }
    
    public void initWatches() {
        // Does nothing
    }
    
    public String[] getProperties() {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }
    
    public void breakpointAdded (Breakpoint breakpoint) {
        if (breakpoint instanceof BpelBreakpoint) {
            Properties p = Properties.getDefault().getProperties("debugger").
                    getProperties(DebuggerManager.PROP_BREAKPOINTS);
            p.setArray("bpel", getBreakpoints());
            breakpoint.addPropertyChangeListener(this);
        }
    }

    public void breakpointRemoved (Breakpoint breakpoint) {
        if (breakpoint instanceof BpelBreakpoint) {
            Properties p = Properties.getDefault().getProperties("debugger").
                    getProperties(DebuggerManager.PROP_BREAKPOINTS);
            p.setArray("bpel", getBreakpoints());
            breakpoint.removePropertyChangeListener(this);
        }
    }
    public void watchAdded (Watch watch) {
    }
    
    public void watchRemoved (Watch watch) {
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof BpelBreakpoint) {
            Properties p = Properties.getDefault().getProperties("debugger").
                    getProperties(DebuggerManager.PROP_BREAKPOINTS);
            p.setArray("bpel", getBreakpoints());
        }
    }
    
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}
    
    private static Breakpoint[] getBreakpoints() {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().
            getBreakpoints();
        int i, k = bs.length;
        ArrayList bb = new ArrayList();
        for (i = 0; i < k; i++) {
            if (bs[i] instanceof BpelBreakpoint) {
                bb.add(bs[i]);
            }
        }
        bs = new Breakpoint[bb.size()];
        return (Breakpoint[])bb.toArray(bs);
    }
}
