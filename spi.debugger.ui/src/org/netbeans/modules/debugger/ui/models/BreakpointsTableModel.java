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

package org.netbeans.modules.debugger.ui.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
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
        if (row instanceof Breakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID)) {
                return Boolean.valueOf (((Breakpoint) row).isEnabled ());
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
        }
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (final Object row, final String columnID, final Object value) 
    throws UnknownTypeException {
        if (row instanceof Breakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID)) {
                synchronized (breakpointsBeingEnabled) {
                    // Keep the original value until we change the BP state...
                    breakpointsBeingEnabled.put(row, Boolean.valueOf(((Breakpoint) row).isEnabled()));
                    if (rp == null) {
                        rp = new RequestProcessor("Enable Breakpoints RP", 1); // NOI18N
                    }
                }
                rp.post(new Runnable() {
                    public void run() {
                        if (((Boolean) value).booleanValue ())
                            ((Breakpoint) row).enable ();
                        else
                            ((Breakpoint) row).disable ();
                        synchronized (breakpointsBeingEnabled) {
                            breakpointsBeingEnabled.remove(row);
                        }
                        fireModelEvent(new ModelEvent.TableValueChanged(
                                BreakpointsTableModel.this,
                                row, columnID));
                    }
                });
                return;
            }
        }
        throw new UnknownTypeException (row);
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
