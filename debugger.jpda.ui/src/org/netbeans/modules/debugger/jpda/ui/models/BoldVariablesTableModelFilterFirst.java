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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;



/**
 * Filters some original tree of nodes (represented by {@link TreeModel}).
 *
 * @author   Jan Jancura
 */
public class BoldVariablesTableModelFilterFirst implements TableModelFilter, 
Constants {
    
    private Map variableToValueType = new WeakHashMap ();
    private Map variableToValueValue = new WeakHashMap ();
    private Map variableToValueToString = new WeakHashMap ();
    
    
    
    public Object getValueAt (
        TableModel original, 
        Object row, 
        String columnID
    ) throws UnknownTypeException {
        Object result = original.getValueAt (row, columnID);
        if ( columnID.equals (LOCALS_TYPE_COLUMN_ID) ||
             columnID.equals (WATCH_TYPE_COLUMN_ID)
        )
            return bold (row, (String) result, variableToValueType);
        if ( columnID.equals (LOCALS_VALUE_COLUMN_ID) ||
             columnID.equals (WATCH_VALUE_COLUMN_ID)
        )
            return bold (row, (String) result, variableToValueValue);
        if ( columnID.equals (LOCALS_TO_STRING_COLUMN_ID) ||
             columnID.equals (WATCH_TO_STRING_COLUMN_ID)
        )
            return bold (row, (String) result, variableToValueToString);
        return result;
    }
    
    public boolean isReadOnly (
        TableModel original, 
        Object row, 
        String columnID
    ) throws UnknownTypeException {
        return original.isReadOnly (row, columnID);
    }
    
    public void setValueAt (
        TableModel original, 
        Object row, 
        String columnID, 
        Object value
    ) throws UnknownTypeException {
        original.setValueAt (row, columnID, value);
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addTreeModelListener (TreeModelListener l) {
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    private String bold (Object variable, String value, Map map) {
        if (map.containsKey (variable)) {
            String oldValue = (String) map.get (variable);
//            S ystem.out.println("bold " + value + " : contains " + oldValue);
//            T hread.dumpStack();
//            S ystem.out.println("");
            if (oldValue == value) return value;
            if ( (oldValue != null) && 
                 oldValue.equals (value)
            )   return value;
            map.put (variable, value);
            return "<html><b>" + value + "</b></html>";
        } else {
//            S ystem.out.println("bold " + value + " : new ");
//            T hread.dumpStack();
//            S ystem.out.println("");
            map.put (variable, value);
            return "<html><b>" + value + "</b></html>";
        }
    }
}
