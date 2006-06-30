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

package org.netbeans.modules.web.debug.watchesfiltering;

import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.ModelListener;
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
    
    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }
}
