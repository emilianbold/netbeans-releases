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

import java.util.Vector;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 *
 * @author   Jan Jancura
 */
public class SessionsTableModel implements TableModel, Constants {

    private Vector listeners = new Vector ();

    
    public Object getValueAt (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Session) {
            if (columnID.equals (SESSION_STATE_COLUMN_ID))
                return "";
            else
            if (columnID.equals (SESSION_LANGUAGE_COLUMN_ID))
                return row;
            else
            if (columnID.equals (SESSION_HOST_NAME_COLUMN_ID))
                return ((Session) row).getLocationName ();
        }
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Session) {
            if (columnID.equals (SESSION_STATE_COLUMN_ID))
                return true;
            else
            if (columnID.equals (SESSION_LANGUAGE_COLUMN_ID))
                return false;
            else
            if (columnID.equals (SESSION_HOST_NAME_COLUMN_ID))
                return true;
        }
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (Object row, String columnID, Object value) 
    throws UnknownTypeException {
        throw new UnknownTypeException (row);
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }

}
