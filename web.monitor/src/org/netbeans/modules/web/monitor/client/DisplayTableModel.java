/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
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

