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

package org.netbeans.modules.web.debug.watchesfiltering;

import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.debugger.ui.Constants;

/**
 * Table model for JSP EL watches.
 * 
 * @author Maros Sandor
 */
public class JspWatchesTableModel implements TableModel {
    
    public Object getValueAt (Object row, String columnID) throws UnknownTypeException {
        if (!(row instanceof JspElWatch)) throw new UnknownTypeException(row);
        JspElWatch watch = (JspElWatch) row;
        if (columnID.equals(Constants.WATCH_TO_STRING_COLUMN_ID)) {
            return watch.getValue();
        } else if (columnID.equals (Constants.WATCH_TYPE_COLUMN_ID)) {
            return watch.getType();
        } else if (columnID.equals (Constants.WATCH_VALUE_COLUMN_ID)) {
            String e = watch.getExceptionDescription ();
            if (e != null) return "> " + e + " <";
            return watch.getValue();
        }
        throw new UnknownTypeException(row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws UnknownTypeException {
        if (!(row instanceof JspElWatch)) throw new UnknownTypeException(row);
        return true;
    }
    
    public void setValueAt (Object row, String columnID, Object value) throws UnknownTypeException {
        throw new UnknownTypeException (row);
    }
    
    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
}
