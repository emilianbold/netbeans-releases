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
/*
 * BeanTableModel.java
 *
 * Created on October 3, 2003, 10:47 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

/**
 * DBeanTableModel.java - table model over the base bean array
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


public abstract class BeanTableModel extends AbstractTableModel {
	/* A class implementation comment can go here. */
	private List children;
	private Object parent;


	// AbstractTableModel methods
	public int getColumnCount() {
		return getColumnNames().length;
	}


	public int getRowCount() {
		if (children != null){
			return children.size();
		} else {
			return 0;
		}
	}


	public String getColumnName(int column) {
		return getColumnNames()[column];
	}


	public boolean isCellEditable(int row, int column) {
		return false;
	}


	// BeanTableModel methods
	protected abstract String[] getColumnNames();

	public abstract Object addRow(Object[] values);

	public abstract void editRow(int row, Object[] values);

	public abstract void removeRow(int row);

	public abstract boolean alreadyExists(Object[] values);

	public abstract Object[] getValues(int row);


	public void setData(Object parent, CommonDDBean[] children) {
		this.parent = parent;
		this.children = new ArrayList();
		
		if (children == null) {
			return;
		}
		
		for(int i = 0;i < children.length; i++) {
			this.children.add(children[i]);
		}
		
		fireTableDataChanged();
	}


	public int getRowWithValue(int column, Object value) {
		for(int row = 0, max = getRowCount(); row < max; row++) {
			Object obj = getValueAt(row, column);
			if (obj.equals(value)) {
				return row;
			}
		}
		return -1;
	}


	protected Object getParent() {
		return parent;
	}


	protected List getChildren() {
		return children;
	}
}
