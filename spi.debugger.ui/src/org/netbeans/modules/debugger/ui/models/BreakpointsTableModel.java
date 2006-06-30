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

package org.netbeans.modules.debugger.ui.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;


/**
 *
 * @author   Jan Jancura
 */
public class BreakpointsTableModel implements TableModel, Constants {
    
    private Map breakpointsBeingEnabled = new HashMap();
    private RequestProcessor rp;
    private Collection modelListeners = new ArrayList();

    public Object getValueAt (Object row, String columnID) throws 
    UnknownTypeException {
        if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID)) {
            if (row instanceof Breakpoint) {
                return Boolean.valueOf (((Breakpoint) row).isEnabled ());
            } else if (row instanceof String) {
                // group name
                String groupName = (String) row;
                Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                    getBreakpoints ();
                Boolean enabled = null;
                for (int i = 0; i < bs.length; i++) {
                    if (bs [i].getGroupName ().equals (groupName)) {
                        if (enabled == null) {
                            enabled = Boolean.valueOf (bs[i].isEnabled ());
                        } else {
                            if (enabled.booleanValue() != bs[i].isEnabled ()) {
                                return null; // Some are enabled, some disabled
                            }
                        }
                    }
                }
                return enabled;
            }
        }
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Breakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID)) {
                synchronized (breakpointsBeingEnabled) {
                    if (breakpointsBeingEnabled.containsKey(row)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else if (row instanceof String) {
            // group name
            return false;
        }
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (final Object row, final String columnID, final Object value) 
    throws UnknownTypeException {
        if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID)) {
            if (row instanceof Breakpoint) {
                synchronized (breakpointsBeingEnabled) {
                    // Keep the original value until we change the BP state...
                    breakpointsBeingEnabled.put(row, Boolean.valueOf(((Breakpoint) row).isEnabled()));
                    if (rp == null) {
                        rp = new RequestProcessor("Enable Breakpoints RP", 1); // NOI18N
                    }
                }
                rp.post(new BreakpointEnabler((Breakpoint) row, ((Boolean) value).booleanValue ()));
                return;
            } else if (row instanceof String) {
                String groupName = (String) row;
                Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                    getBreakpoints ();
                ArrayList breakpoints = new ArrayList();
                for (int i = 0; i < bs.length; i++) {
                    if (bs [i].getGroupName ().equals (groupName)) {
                        breakpoints.add(bs[i]);
                    }
                }
                if (breakpoints.size() > 0) {
                    synchronized (breakpointsBeingEnabled) {
                        // Keep the original value until we change the BP state...
                        for (Iterator it = breakpoints.iterator(); it.hasNext(); ) {
                            Breakpoint bp = (Breakpoint) it.next();
                            breakpointsBeingEnabled.put(bp, Boolean.valueOf(bp.isEnabled()));
                        }
                        if (rp == null) {
                            rp = new RequestProcessor("Enable Breakpoints RP", 1); // NOI18N
                        }
                        for (Iterator it = breakpoints.iterator(); it.hasNext(); ) {
                            Breakpoint bp = (Breakpoint) it.next();
                            rp.post(new BreakpointEnabler(bp, ((Boolean) value).booleanValue ()));
                        }
                    }
                }
                return ;
            }
        }
        throw new UnknownTypeException (row);
    }
        
    private class BreakpointEnabler extends Object implements Runnable {
        
        private Breakpoint bp;
        private boolean enable;
        
        public BreakpointEnabler(Breakpoint bp, boolean enable) {
            this.bp = bp;
            this.enable = enable;
        }
        
        public void run() {
            if (enable)
                bp.enable ();
            else
                bp.disable ();
            synchronized (breakpointsBeingEnabled) {
                breakpointsBeingEnabled.remove(bp);
            }
            fireModelEvent(new ModelEvent.TableValueChanged(
                    BreakpointsTableModel.this,
                    bp, BREAKPOINT_ENABLED_COLUMN_ID));
            // re-calculate the enabled state of the BP group
            String groupName = bp.getGroupName();
            if (groupName != null) {
                fireModelEvent(new ModelEvent.TableValueChanged(
                    BreakpointsTableModel.this,
                    groupName, BREAKPOINT_ENABLED_COLUMN_ID));
            }
        }
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }
    
    private void fireModelEvent(ModelEvent ev) {
        Collection listeners;
        synchronized (modelListeners) {
            listeners = new ArrayList(modelListeners);
        }
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            ModelListener l = (ModelListener) it.next();
            l.modelChanged(ev);
        }
    }
}
