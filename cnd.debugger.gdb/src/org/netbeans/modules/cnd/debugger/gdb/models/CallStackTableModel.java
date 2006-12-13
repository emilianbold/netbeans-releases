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

package org.netbeans.modules.cnd.debugger.gdb.models;

import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;


/**
 *
 * @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CallStackTableModel implements TableModel, Constants {
    
    public Object getValueAt(Object row, String columnID) throws UnknownTypeException {
        if (row instanceof CallStackFrame) {
            if (columnID.equals(CALL_STACK_FRAME_LOCATION_COLUMN_ID)) {
                String loc = ((CallStackFrame) row).getFullname();
                loc += ":"; // NOI18N
                loc += ((CallStackFrame) row).getLineNumber();
		return (loc);
            }
        }
        throw new UnknownTypeException(row);
    }
    
    public boolean isReadOnly(Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof CallStackFrame) {
            if (columnID.equals(CALL_STACK_FRAME_LOCATION_COLUMN_ID)) {
		return true;
	    }
        }
        throw new UnknownTypeException(row);
    }
    
    public void setValueAt(Object row, String columnID, Object value) throws UnknownTypeException {
        throw new UnknownTypeException(row);
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
    }
}
