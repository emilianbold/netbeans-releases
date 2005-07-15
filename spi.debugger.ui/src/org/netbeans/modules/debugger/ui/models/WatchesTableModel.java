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

import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 *
 * @author   Jan Jancura
 */
public class WatchesTableModel implements TableModel, Constants {

    public Object getValueAt (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Watch) {
            if (columnID.equals (WATCH_TO_STRING_COLUMN_ID))
                return "";
            else
            if (columnID.equals (WATCH_TYPE_COLUMN_ID))
                return "";
            else
            if (columnID.equals (WATCH_VALUE_COLUMN_ID))
                return "";
        }
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Watch) {
            if (columnID.equals (WATCH_TO_STRING_COLUMN_ID))
                return true;
            else
            if (columnID.equals (WATCH_TYPE_COLUMN_ID))
                return true;
            else
            if (columnID.equals (WATCH_VALUE_COLUMN_ID))
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
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
    }
}
