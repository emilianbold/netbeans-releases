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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.mapper.common.util;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

/**
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting
 * a TableMap which has not been subclassed into a chain of table filters
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne
 **/
public class TableMap extends AbstractTableModel
                      implements TableModelListener {

   /**
    * table model to use
    */
    protected TableModel model;

    /**
     *
     * @return  TableModel model
     */
    public TableModel getModel() {
        return model;
    }

    /**
     * @param model  TableModel to use
     */
    public void setModel(TableModel model) {
        this.model = model;
        model.addTableModelListener(this);
    }

    // By default, implement TableModel by forwarding all messages
    // to the model.
    /**
     *
     * @param aRow  row
     * @param aColumn  column
     * @return object value at row, column position
     */
    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn);
    }

    /**
     *
     * @param aValue  value to set
     * @param aRow    row
     * @param aColumn column
     */
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn);
    }

    /**
     *
     * @return int row count
     */
    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount();
    }

    /**
     *
     * @return int column count
     */
    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount();
    }

    /**
     *
     * @param aColumn aColumn
     * @return String name of this column
     */
    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn);
    }

    /**
     *
     * @param aColumn aColumn
     * @return Class  class for this column
     */
    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn);
    }

    /**
     *
     * @param row row
     * @param column column
     * @return boolean true if this cell is editable; otherwise false
     */
    public boolean isCellEditable(int row, int column) {
         return model.isCellEditable(row, column);
    }
//
// Implementation of the TableModelListener interface,
//
    // By default forward all events to all the listeners.
    /**
     *
     * @param e  TableModelEvent
     */
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }
}

