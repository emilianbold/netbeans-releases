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

package org.netbeans.modules.bpel.debugger.ui.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 * @author Alexander Zgursky
 */
public class BpelSessionView
        implements TableModel, Constants, PropertyChangeListener
{
    private Vector listeners = new Vector();

    public BpelSessionView(ContextProvider contextProvider) {
        BpelDebugger debugger = contextProvider.lookupFirst(null, BpelDebugger.class);
        debugger.addPropertyChangeListener(debugger.PROP_STATE, this);
    }
    
    private static String loc(String key) {
        return NbBundle.getBundle(BpelSessionView.class).getString(key);
    }

    private static String getSessionState(Session s) {
        DebuggerEngine e = s.getCurrentEngine ();
        if (e == null) {
            return "";
        }
        BpelDebugger d = e.lookupFirst(null, BpelDebugger.class);
        if (d != null) {
            switch (d.getState()) {
            case BpelDebugger.STATE_DISCONNECTED:
                return loc("MSG_Session_State_Disconnected"); // NOI18N
            case BpelDebugger.STATE_RUNNING:
                return loc("MSG_Session_State_Running"); // NOI18N
            case BpelDebugger.STATE_STARTING:
                return loc("MSG_Session_State_Starting"); // NOI18N
            }
        }
        return "";
    }
    
    //*********************
    // TableModel interface
    //*********************

    public Object getValueAt(Object row, String columnID)
            throws UnknownTypeException
    {
        if (row instanceof Session) {
            if (columnID.equals (SESSION_STATE_COLUMN_ID)) {
                return getSessionState ((Session) row);
            } else if (columnID.equals (SESSION_LANGUAGE_COLUMN_ID)) {
                return row;
            } else if (columnID.equals (SESSION_HOST_NAME_COLUMN_ID)) {
                return ((Session) row).getLocationName ();
            }
        }
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly(Object row, String columnID)
            throws UnknownTypeException
    {
        if (row instanceof Session) {
            return true;
        }
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt(Object row, String columnID, Object value) 
            throws UnknownTypeException
    {
        throw new UnknownTypeException (row);
    }
    
    //*********************************
    // PropertyChangeListener interface
    //*********************************

    public void propertyChange(PropertyChangeEvent e) {
        fireTreeChanged();
        if (e.getNewValue().equals(BpelDebugger.STATE_DISCONNECTED)) {
            ((BpelDebugger)e.getSource()).removePropertyChangeListener(
                BpelDebugger.PROP_STATE, this);
        }
    }
    
    //*********************
    // Other public methods
    //*********************

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove(l);
    }
    
    //****************
    // Private methods
    //****************
    
    private void fireTreeChanged() {
        Vector v = (Vector)listeners.clone();
        int  k = v.size();
        for (int i = 0; i < k; i++)
            ((ModelListener)v.get(i)).modelChanged(null);
    }
}
