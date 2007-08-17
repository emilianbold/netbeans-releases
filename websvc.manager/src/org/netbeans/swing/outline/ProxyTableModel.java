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

/*
 * ConverterTableModel.java
 *
 * Created on January 28, 2004, 7:02 PM
 */

package org.netbeans.swing.outline;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/** A TableModel which is driven by a RowModel - the RowModel
 * supplies row contents, based on nodes suppled by the tree
 * column of an OutlineModel.  This model supplies the additional
 * rows of the TableModel to the OutlineModel.
 *
 * @author  Tim Boudreau
 */
final class ProxyTableModel implements TableModel, NodeRowModel {
    private List listeners = new ArrayList();
    private RowModel rowmodel;
    private OutlineModel outlineModel;
    /** Creates a new instance of ProxyTableModel that will use the supplied
     * RowModel to produce its values.  */
    public ProxyTableModel(RowModel rowmodel) {
        this.rowmodel = rowmodel;
    }
    
    /** Set the OutlineModel that will be used to find nodes for
     * rows.  DefaultOutlineModel will do this in its constructor. */
    void setOutlineModel (OutlineModel mdl) {
        this.outlineModel = mdl;
    }
    
    /** Get the outline model used to provide column 0 nodes to the
     * RowModel for setting the values.  */
    OutlineModel getOutlineModel () {
        return outlineModel;
    }
    
    public Class getColumnClass(int columnIndex) {
        return rowmodel.getColumnClass(columnIndex);
    }
    
    public int getColumnCount() {
        return rowmodel.getColumnCount();
    }
    
    public String getColumnName(int columnIndex) {
        return rowmodel.getColumnName(columnIndex);
    }
    
    public int getRowCount() {
        //not interesting, will never be called - the outline model
        //handles this
        return -1;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object node = getNodeForRow(rowIndex);
        return rowmodel.getValueFor(node, columnIndex);
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        Object node = getNodeForRow(rowIndex);
        return rowmodel.isCellEditable (node, columnIndex);
    }
    
    public synchronized void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
    
    public synchronized void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }
    
    private void fire (TableModelEvent e) {
        TableModelListener[] l;
        synchronized (this) {
            l = new TableModelListener[listeners.size()];
            l = (TableModelListener[]) listeners.toArray(l);
        }
        for (int i=0; i < l.length; i++) {
            l[i].tableChanged(e);
        }
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Object node = getNodeForRow(rowIndex);
        rowmodel.setValueFor (node, columnIndex, aValue);
        TableModelEvent e = new TableModelEvent (this, rowIndex, rowIndex, 
            columnIndex);
        fire(e);
    }
    
    /** Get the object that will be passed to the RowModel to fetch values
     * for the given row. 
     * changed to public 4/19/2004 so a cell editor can figure out information
     * about the node being edited. - David Botterill
     * @param row The row we need the tree node for */
    public Object getNodeForRow(int row) {
        return getOutlineModel().getValueAt(row, 0);
    }    

    
}
