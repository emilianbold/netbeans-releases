/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */ 

/*
 * SimpleTableModel.java
 *
 * Created on August 2, 2005, 3:17 PM
 *
 */

package org.netbeans.microedition.lcdui;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Simple implementation of TableModel. This model can hold a matrix of String
 * values. A matrix means all rows have to have the same number of columns
 * and rows cannot be null.
 * @author breh
 */
public class SimpleTableModel implements TableModel {

	private Vector modelListeners = new Vector();
	private String[][] values = new String[0][0];
	private String[] columnNames = new String[0];
	
	private int cols;
	private int rows;

	
	/**
	 * Creates a model with given values and column names.
	 * @param values values to be used in this table model. Please note, the values cannot be null
	 * and have to be a valid matrix.
	 * @param columnNames column names to be used. Can be null if the column names are not going
	 * to be used (see @isUsingHeaders method).
	 * @throws java.lang.IllegalArgumentException If the values parameter is null, or if it is not a valid rectangular matrix.
	 */
	public SimpleTableModel(String[][] values, String[] columnNames) throws IllegalArgumentException {
		setValues(values);
		setColumnNames(columnNames);
	}
	
	
	/**
	 * Creates a model with given number of rows and columns.
	 * @param rows number of rows to be used in the model. Cannot be negative.
	 * @param cols number of columns to be used in the model. Cannot be negative.	 
	 * @throws java.lang.IllegalArgumentException if the cols or rows argument are lower than zero
	 */
	public SimpleTableModel(int rows, int cols) throws IllegalArgumentException {
		if (rows < 0) throw new IllegalArgumentException("row size cannot be negative");
		if (cols < 0) throw new IllegalArgumentException("column size cannot be negative");
		String[][] newValues = new String[rows][cols];
		for (int i = 0; i < rows; i++) {
			newValues[i] = new String[cols];
		}
		setValues(newValues);
		setColumnNames(new String[cols]);
	}
	
	
	
	/**
	 * Creates a new empty table model.
	 */
	public SimpleTableModel() {
	}
	
	/**
	 * Gets number of columns of the supplied values matrix.
	 * @return values matrix column count
	 */
	public int getColumnCount() {
		return cols;
	}
	
	/**
	 * Gets number of rows of the supplied values matrix.
	 * @return values matrix row count
	 */
	public int getRowCount() {
		return rows;
	}

	
	/**
	 * Sets the value to the defined row and column of the model. 
	 * <p>
	 * Please note, this method does not call fireTableModelChanged method 
	 * automatically, so you have to call it manually if you would like to redraw
	 * the table. This is designed in this way, because of the performance reasons
	 * - you might want to update several values at a time and repaint the 
	 * table at the end of the update.
	 *
	 * @throws java.lang.IllegalArgumentException if the values are not defined, or 
	 * the specifed row or column is larger than the size of the values.
	 */
	public void setValue(int col, int row, String value) throws IllegalArgumentException {
		if (values == null) throw new IllegalArgumentException("No values set to the model");
		if (values.length < row) throw new IllegalArgumentException("Specified row ("
				+ row + ") is larger than the number of rows available in values ("
				+ values.length + ").");
		if ( (values[row].length < col)) throw new IllegalArgumentException("Specified column ("
				+ col + ") is larger than the number of columns available in values ("
				+ values[row].length + ").");
		// change the value
		values[row][col] = value;
	}
	
	
	/**
	 * Sets the values of the model. Values of this model have to be a rectangular matrix - 
	 * this means all rows have to have the same number of columns and rows canot be null.
	 * <p/>
	 * Please note, this class is holding just reference to the passed values array, so 
	 * any change you do to the model via setValue method is actually made in the array.
	 *
	 * @param values values to be used in this table model. Please note, the values cannot be null
	 * and have to be a valid matrix.
	 * @throws java.lang.IllegalArgumentException If the values parameter is null, or if it is not a valid rectangular matrix.
	 */
	public void setValues(String[][] values) throws IllegalArgumentException {
		// check values validity ...
		checkValues(values);
		this.values = values;
		fireTableModelChanged();
	}
	
	/**
	 * Gets values of the model
	 * @return values matrix
	 */
	public String[][] getValues() {
		return values;
	}
	
	/*
	public void setValue(int col, int row, String value) {
		
	}*/
	
	/**
	 * Gets the value of a table cell at a specified location. Always returns 
	 * <code>String</code>.
	 * @return value for the given cell coordinates. May return null if there is no value.
	 * @param col col index of the value
	 * @param row row index of the value
	 */
	public Object getValue(int col, int row) {
		return values[row][col];
	}
	
	/**
	 * Decides wheter this table is using headers (column names). This simple
	 * model simply checks whether the supplied column names are null and in 
	 * such a case this method returns true.
	 * @return true if the column names are being supplied and should be visualized, false otherwise
	 */	
	public boolean isUsingHeaders() {
		return columnNames != null;
	}
	
	/**
	 * Sets the column names for this model. The array of names
	 * should have the same length as the column count.
	 * @param columnNames array of names. May be null if the column headers should not be visualized
	 */
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
		fireTableModelChanged();
	}
	
	
	/**
	 * Gets the column name for the specified index
	 * @param column column index
	 * @return column name
	 */
	public String getColumnName(int column) {
		if ((columnNames != null) && (column < columnNames.length)) {
			return columnNames[column];
		} else {
			return null;
		}
	}
	

	/**
	 * Fires an event that the values in the table has been changed and
	 * the table should be repainted. This method is intended to be used by
	 * the user, since the model cannot track changes of
	 * values in the supplied arrays.
	 */
	public void fireTableModelChanged() {
		Enumeration e = modelListeners.elements();
		while (e.hasMoreElements()) {
			((TableModelListener)e.nextElement()).tableModelChanged(this);
		}
	}

	/**
	 * Adds a <code>TableModelListener</code> to this instance of the model.
	 * @param listener listener to be addded
	 */
	public synchronized void addTableModelListener(TableModelListener listener) {
		if (listener != null) {
			modelListeners.addElement(listener);
		}
	}

	/**
	 * Removes a <code>TableModelListener</code> from this instance of the model.
	 * @param listener listener to be removed
	 */
	public synchronized void removeTableModelListener(TableModelListener listener) {
		modelListeners.removeElement(listener);
	}
	
	
		
	/**
	 * Checks values if they are rectangular matrix
	 */
	private void checkValues(String[][] values) throws IllegalArgumentException {
		rows = 0;
		cols = 0;
		if (values == null) throw new IllegalArgumentException("Values cannot be null.");
		rows = values.length;
		if (rows > 0) {
			cols = values[0].length;
		}
		for (int i=0; i < values.length; i++) {
			String[] row = values[i];
			if (row == null) {
				throw new IllegalArgumentException("Data cannot contain null rows (row "+i+").");
			} else if (values[0].length != row.length) {
				throw new IllegalArgumentException("Data cannot contain different row lengths (row "+i+").");
			} 
		}
	}	
	
}
