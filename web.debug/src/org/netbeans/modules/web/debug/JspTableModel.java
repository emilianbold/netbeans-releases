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

package org.netbeans.modules.web.debug;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.*;
import org.netbeans.modules.web.debug.breakpoints.*;

/**
 *
 * @author Martin Grebac
 */
public class JspTableModel implements TableModel, Constants {
    
    
    public Object getValueAt (Object row, String columnID) throws UnknownTypeException {
        if (row instanceof JspLineBreakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID))
                return Boolean.valueOf (((JspLineBreakpoint) row).isEnabled ());
        }
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws UnknownTypeException {
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (Object row, String columnID, Object value) throws UnknownTypeException {
        if (row instanceof JspLineBreakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID))
                if (((Boolean) value).equals (Boolean.TRUE))
                    ((Breakpoint) row).enable ();
                else
                    ((Breakpoint) row).disable ();
        }
        throw new UnknownTypeException (row);
    }


//    private static String getSessionState (Session s) {
//        DebuggerEngine e = s.getCurrentEngine ();
//        JPDADebugger d = JPDADebugger.getJPDADebugger (e);
//        switch (d.getState ()) {
//            case JPDADebugger.STATE_DISCONNECTED:
//                return "Not Running";
//            case JPDADebugger.STATE_RUNNING:
//                return "Running";
//            case JPDADebugger.STATE_STARTING:
//                return "Starting";
//            case JPDADebugger.STATE_STOPPED:
//                return "Stopped";
//        }
//        return null;
//    }
//    
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
    
//    private static String getShort (String c) {
//        int i = c.lastIndexOf ('.');
//        if (i < 0) return c;
//        return c.substring (i + 1);
//    }
}
