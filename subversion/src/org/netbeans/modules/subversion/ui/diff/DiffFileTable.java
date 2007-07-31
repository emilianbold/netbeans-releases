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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.diff;

import org.openide.explorer.view.NodeTableModel;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.util.TableSorter;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;

import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Point;
import java.awt.Component;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.io.File;

/**
 * 
 * @author Maros Sandor
 */
class DiffFileTable implements MouseListener, ListSelectionListener, AncestorListener {
    
    private NodeTableModel tableModel;
    private JTable table;
    private JScrollPane     component;
    private Node [] nodes = new Node[0];
    
    private String []   tableColumns; 
    private TableSorter sorter;

    /**
     * Defines labels for Diff view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);

    {
        ResourceBundle loc = NbBundle.getBundle(DiffFileTable.class);
        columnLabels.put(DiffNode.COLUMN_NAME_NAME, new String [] {
                loc.getString("CTL_DiffTable_Column_Name_Title"), 
                loc.getString("CTL_DiffTable_Column_Name_Desc")});
        columnLabels.put(DiffNode.COLUMN_NAME_PROPERTY, new String [] {
                loc.getString("CTL_DiffTable_Column_Property_Title"), 
                loc.getString("CTL_DiffTable_Column_Property_Desc")});
        columnLabels.put(DiffNode.COLUMN_NAME_STATUS, new String [] { 
                loc.getString("CTL_DiffTable_Column_Status_Title"), 
                loc.getString("CTL_DiffTable_Column_Status_Desc")});
        columnLabels.put(DiffNode.COLUMN_NAME_LOCATION, new String [] { 
                loc.getString("CTL_DiffTable_Column_Location_Title"), 
                loc.getString("CTL_DiffTable_Column_Location_Desc")});
    }

    
    
    private static final Comparator NodeComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            Node.Property p1 = (Node.Property) o1;
            Node.Property p2 = (Node.Property) o2;
            String sk1 = (String) p1.getValue("sortkey"); // NOI18N
            if (sk1 != null) {
                String sk2 = (String) p2.getValue("sortkey"); // NOI18N
                return sk1.compareToIgnoreCase(sk2);
            } else {
                try {
                    String s1 = (String) p1.getValue();
                    String s2 = (String) p2.getValue();
                    return s1.compareToIgnoreCase(s2);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                    return 0;
                }
            }
        }
    };
    private final MultiDiffPanel master;

    public DiffFileTable(MultiDiffPanel master) {
        this.master = master;
        tableModel = new NodeTableModel();
        sorter = new TableSorter(tableModel);
        sorter.setColumnComparator(Node.Property.class, DiffFileTable.NodeComparator);
        table = new JTable(sorter);
        table.getSelectionModel().addListSelectionListener(this);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter.setTableHeader(table.getTableHeader());
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
        table.addMouseListener(this);
        table.setDefaultRenderer(Node.Property.class, new DiffTableCellRenderer());
        table.getSelectionModel().addListSelectionListener(this);
        table.addAncestorListener(this);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffFileTable.class, "ACSN_DiffTable")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffFileTable.class, "ACSD_DiffTable")); // NOI18N
        setColumns(new String[] {
            DiffNode.COLUMN_NAME_NAME,
            DiffNode.COLUMN_NAME_PROPERTY,
            DiffNode.COLUMN_NAME_STATUS,
            DiffNode.COLUMN_NAME_LOCATION}
        );
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction");
        table.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(table));
            }
        });
    }

    void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int width = table.getWidth();
                if (tableColumns.length == 3) {
                    for (int i = 0; i < tableColumns.length; i++) {
                        if (DiffNode.COLUMN_NAME_LOCATION.equals(tableColumns[i])) {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 60 / 100);
                        } else {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 20 / 100);
                        }
                    }
                } else if (tableColumns.length == 4) {
                    for (int i = 0; i < tableColumns.length; i++) {
                        if (DiffNode.COLUMN_NAME_LOCATION.equals(tableColumns[i])) {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 55 / 100);
                        } else {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 15 / 100);
                        }
                    }
                }
            }
        });
    }

    public void ancestorAdded(AncestorEvent event) {
        setDefaultColumnSizes();
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
    }

    public JComponent getComponent() {
        return component;
    }
    
    /**
     * Sets visible columns in the Versioning table.
     * 
     * @param columns array of column names, they must be one of SyncFileNode.COLUMN_NAME_XXXXX constants.  
     */ 
    final void setColumns(String [] columns) {
        if (Arrays.equals(columns, tableColumns)) return;
        setModelProperties(columns);
        tableColumns = columns;
        for (int i = 0; i < tableColumns.length; i++) {
            sorter.setColumnComparator(i, null);
            sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
            if (DiffNode.COLUMN_NAME_STATUS.equals(tableColumns[i])) {
                sorter.setSortingStatus(i, TableSorter.ASCENDING);
                break;
            }
        }
        setDefaultColumnSizes();        
    }
        
    private void setModelProperties(String [] columns) {
        Node.Property [] properties = new Node.Property[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            String [] labels = (String[]) columnLabels.get(column);
            properties[i] = new ColumnDescriptor(column, String.class, labels[0], labels[1]);  
        }
        tableModel.setProperties(properties);
    }

    void setTableModel(Node [] nodes) {
        this.nodes = nodes;
        tableModel.setNodes(nodes);
    }

    void focus() {
        table.requestFocus();
    }

    void setSelectedIndex(int currentIndex) {
        if (currentIndex == table.getSelectedRow()) return;
        table.getSelectionModel().setSelectionInterval(currentIndex, currentIndex);
        table.scrollRectToVisible(table.getCellRect(currentIndex, 0, true));
    }

    public int getSelectedIndex() {
        return table.getSelectedRow();
    }

    public int getSelectedModelIndex() {
        return getModelIndex(table.getSelectedRow());
    }

    public int getModelIndex(int viewIndex) {
        return viewIndex != -1 ? sorter.modelIndex(viewIndex) : -1;
    }

    public JTable getTable() {
        return table;
    }

    private static class ColumnDescriptor extends PropertySupport.ReadOnly {
        
        public ColumnDescriptor(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    private void showPopup(final MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row != -1) {
            boolean makeRowSelected = true;
            int [] selectedrows = table.getSelectedRows();

            for (int i = 0; i < selectedrows.length; i++) {
                if (row == selectedrows[i]) {
                    makeRowSelected = false;
                    break;
                }
            }
            if (makeRowSelected) {
                table.getSelectionModel().setSelectionInterval(row, row);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // invoke later so the selection on the table will be set first
//                JPopupMenu menu = getPopup();
//                menu.show(table, e.getX(), e.getY());
            }
        });
    }

    private void showPopup(Point p) {
//        JPopupMenu menu = getPopup();
//        menu.show(table, p.x, p.y);
    }

    private JPopupMenu getPopup() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        return menu;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
/*
        if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) return;
            row = sorter.modelIndex(row);
            Action action = nodes[row].getPreferredAction();
            if (action == null || !action.isEnabled()) action = new OpenInEditorAction();
            if (action.isEnabled()) {
                action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
            }
        }
*/
    }

    public void valueChanged(ListSelectionEvent e) {
        final TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class,  table);
        if (tc == null) return; // table is no longer in component hierarchy
        master.setSelectedIndex(table.getSelectedRow());
    }
    
    private class DiffTableCellRenderer extends DefaultTableCellRenderer {
        
        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            if (modelColumnIndex == 0) {
                Node node = nodes[sorter.modelIndex(row)];
                if (!isSelected) {
                    value = "<html>" + node.getHtmlDisplayName(); // NOI18N
                }
            }
            if (modelColumnIndex == 2) {
                renderer = pathRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            if (renderer instanceof JComponent) {
                File file = nodes[sorter.modelIndex(row)].getLookup().lookup(File.class); 
                String path = file != null ? file.getAbsolutePath() : null; 
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }
    }
}
