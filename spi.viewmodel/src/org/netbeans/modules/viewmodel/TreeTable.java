/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.viewmodel;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.NodeTableModel;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.windows.TopComponent;


/**
 * Implements root node of hierarchy created for given TreeModel.
 *
 * @author   Jan Jancura
 */
public class TreeTable extends JPanel implements 
ExplorerManager.Provider, PropertyChangeListener {
    
    private ExplorerManager explorerManager;
    private MyTreeTable treeTable;
    private Node.Property[] columns;
    
    
    public TreeTable () {
        setLayout (new BorderLayout ());
            treeTable = new MyTreeTable ();
            treeTable.setTreePreferredWidth (200);
            treeTable.setRootVisible (false);
            treeTable.setVerticalScrollBarPolicy 
                (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            treeTable.setHorizontalScrollBarPolicy 
                (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add (treeTable, "Center");  //NOI18N
    }
    
    public void setModel (CompoundModel model) {
        saveWidths ();
        
        // 1) no model => set empty root node
        if (model == null) {
            getExplorerManager ().setRootContext (
                new AbstractNode (Children.LEAF)
            );
            return;
        }
        
        // 2) set columns for given model
        treeTable.setProperties (columns = createColumns (model));
//        try {
//            treeTable.setToolTipText (model.getShortDescription (
//                model.getRoot ()
//            ));
//        } catch (ComputingException ex) {
//        } catch (UnknownTypeException ex) {
//            ex.printStackTrace ();
//        }
        
        // 3) update column widths
        updateColumnWidths ();
        
        // 3) set root node for given model
        getExplorerManager ().setRootContext (
            new TreeModelRoot (model).getRootNode ()
        );
    }
    
    public ExplorerManager getExplorerManager () {
        if (explorerManager == null) {
            explorerManager = new ExplorerManager ();
            explorerManager.addPropertyChangeListener (this);
        }
        return explorerManager;
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (
            !evt.getPropertyName ().equals (ExplorerManager.PROP_SELECTED_NODES)
        ) return;
        
        TopComponent tc = (TopComponent) SwingUtilities.
            getAncestorOfClass (TopComponent.class, this);
        if (tc == null) return;
        tc.setActivatedNodes ((Node[]) evt.getNewValue ());
    }
    
    private Node.Property[] createColumns (CompoundModel model) {
        ColumnModel[] cs = model.getColumns ();
        int i, k = cs.length;
        Node.Property[] columns = new Column [k];
        boolean addDefaultColumn = true;
        for (i = 0; i < k; i++) {
            columns [i] = new Column (
                cs [i], this
            );
            if (cs [i].getType () == null)
                addDefaultColumn = false;
        }
        if (!addDefaultColumn) return columns;
        PropertySupport.ReadWrite[] columns2 = 
            new PropertySupport.ReadWrite [columns.length + 1];
        System.arraycopy (columns, 0, columns2, 1, columns.length);
        columns2 [0] = new DefaultColumn ();
        return columns2;
    }

    void updateColumnWidths () {
        int i, k = columns.length;
        for (i = 0; i < k; i++) {
            if (Boolean.TRUE.equals (columns [i].getValue 
                ("InvisibleInTreeTableView"))
            ) continue;
            if (columns [i] instanceof Column) {
                Column column = (Column) columns [i];
                if (column.isDefault ()) {
                    int width = column.getColumnWidth ();
                    treeTable.setTreePreferredWidth (width);
                } else {
                    int order = column.getOrderNumber ();
                    if (order == -1) continue;
                    int width = column.getColumnWidth ();
                    treeTable.setTableColumnPreferredWidth (order, width);
                }
            }
        }
    }
    
    private void saveWidths () {
        if (columns == null) return;
        int i, k = columns.length;
        for (i = 0; i < k; i++) {
            if (Boolean.TRUE.equals (columns [i].getValue 
                ("InvisibleInTreeTableView"))
            ) continue;
            if (!(columns [i] instanceof Column)) continue;
            Column column = (Column) columns [i];
            if (column.isDefault ()) {
                TableColumn tc = treeTable.getTable ().getColumnModel ().
                    getColumn (0);
                if (tc == null) continue;
                int width = tc.getWidth ();
                column.setColumnWidth (width);
            } else {
                int order = column.getOrderNumber ();
                if (order == -1) continue;

                TableColumn tc = treeTable.getTable ().getColumnModel ().
                    getColumn (order + 1);
                if (tc == null) continue;
                int width = tc.getWidth ();
                column.setColumnWidth (width);
            }
        }
    }
    
    private static class MyTreeTable extends TreeTableView {
        MyTreeTable () {
            super ();
            treeTable.setShowHorizontalLines (true);
            treeTable.setShowVerticalLines (false);
        }
        
        JTable getTable () {
            return treeTable;
        }
    }
}

