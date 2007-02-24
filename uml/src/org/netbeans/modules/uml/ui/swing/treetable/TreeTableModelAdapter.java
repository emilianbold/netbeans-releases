/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.ui.swing.treetable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * This is a wrapper class takes a TreeTableModel and implements
 * the table model interface. The implementation is trivial, with
 * all of the event dispatching support provided by the superclass: 
 * the AbstractTableModel. 
 *
 * @version 1.2 10/27/98
 *
 * @author Philip Milne
 * @author Scott Violet
 */
public class TreeTableModelAdapter extends AbstractTableModel
{
    JTree tree;
    TreeTableModel treeTableModel;

    public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree) {
        this.tree = tree;
        this.treeTableModel = treeTableModel;

	tree.addTreeExpansionListener(new TreeExpansionListener() {
	    // Don't use fireTableRowsInserted() here; the selection model
	    // would get updated twice. 
	    public void treeExpanded(TreeExpansionEvent event) {  
	      fireTableDataChanged(); 
	    }
            public void treeCollapsed(TreeExpansionEvent event) {  
	      fireTableDataChanged(); 
	    }
	});

	// Install a TreeModelListener that can update the table when
	// tree changes. We use delayedFireTableDataChanged as we can
	// not be guaranteed the tree will have finished processing
	// the event before us.
	treeTableModel.addTreeModelListener(new TreeModelListener() {
	    public void treeNodesChanged(TreeModelEvent e) {
		delayedFireTableDataChanged();
	    }

	    public void treeNodesInserted(TreeModelEvent e) {
		delayedFireTableDataChanged();
	    }

	    public void treeNodesRemoved(TreeModelEvent e) {
		delayedFireTableDataChanged();
	    }

	    public void treeStructureChanged(TreeModelEvent e) {
		delayedFireTableDataChanged();
	    }
	});
    }

    // Wrappers, implementing TableModel interface. 

    public int getColumnCount() {
	return treeTableModel.getColumnCount();
    }

    public String getColumnName(int column) {
	return treeTableModel.getColumnName(column);
    }

    public Class getColumnClass(int column) {
	return treeTableModel.getColumnClass(column);
    }

    public int getRowCount() {
	return tree.getRowCount();
    }

    protected Object nodeForRow(int row) {
	TreePath treePath = tree.getPathForRow(row);
	return treePath == null? null : treePath.getLastPathComponent();         
    }

    public Object getValueAt(int row, int column) {
	return treeTableModel.getValueAt(nodeForRow(row), column);
    }

    public boolean isCellEditable(int row, int column) {
         return treeTableModel.isCellEditable(nodeForRow(row), column); 
    }

    public void setValueAt(Object value, int row, int column) {
	treeTableModel.setValueAt(value, nodeForRow(row), column);
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    protected void delayedFireTableDataChanged() {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		fireTableDataChanged();
	    }
	});
    }
}

