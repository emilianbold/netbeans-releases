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

package org.netbeans.modules.web.monitor.client;

import java.util.*;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 * DisplayTableModel.java
 *
 *
 * Created: Fri Jan 25 13:37:39 2002
 *
 * @author Ana von Klopp
 * @version
 */
public class DisplayTableModel extends AbstractTableModel {

    private Object[][] data = null;
    private boolean editable, editableNames;
    private int numCols = 3;

    private static final boolean debug = false;

    public DisplayTableModel(Object[][] data, 
			boolean editable,
			boolean editableNames) {
	this.data = data;
	this.editable = editable;
	this.editableNames = editableNames;
    }
    
    public String getColumnName(int col) { 
	return ""; // NOI18N
	//return headers[col].toString(); 
    }
	
    public int getRowCount() { return data.length; }
    public int getColumnCount() { return numCols; }
    public Object getValueAt(int row, int col) { 
	return data[row][col]; 
    }
    public boolean isCellEditable(int row, int col) { 
	if(editable) {
	    if(col == 0) return editableNames;
	    if(col == 1) return true;
	    if(col == 2) return true;
	}
	if(col == 2) return true; 
	return false; 
    }
    
    public void setValueAt(Object value, int row, int col) {
	data[row][col] = value;
	fireTableCellUpdated(row, col);
    }

    void log(String s) {
	System.out.println("DisplayTableModel::" + s);  // NOI18N
    }
    
} // DisplayTableModel

