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
package org.netbeans.modules.subversion.ui.update;

import org.netbeans.modules.versioning.util.VersioningEvent;
import org.openide.explorer.view.NodeTableModel;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.openide.awt.MouseUtils;
import org.netbeans.modules.versioning.util.TableSorter;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.status.OpenInEditorAction;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.spi.project.ui.support.NodeList;

/**
 * Table that displays nodes in the Update Results view. 
 * 
 * @author Maros Sandor
 */
class UpdateResultsTable implements MouseListener, ListSelectionListener, AncestorListener, VersioningListener {

    private NodeTableModel tableModel;
    private JTable table;
    private JScrollPane     component;
    private UpdateResultNode[] nodes = new UpdateResultNode[0];
    
    private String []   tableColumns; 
    private TableSorter sorter;

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);
    {
        ResourceBundle loc = NbBundle.getBundle(UpdateResultsTable.class);
        columnLabels.put(UpdateResultNode.COLUMN_NAME_NAME, new String [] { 
                                          loc.getString("CTL_UpdateResults_Column_File_Title"), // NOI18N 
                                          loc.getString("CTL_UpdateResults_Column_File_Desc")}); // NOI18N
        columnLabels.put(UpdateResultNode.COLUMN_NAME_STATUS, new String [] { 
                                          loc.getString("CTL_UpdateResults_Column_Status_Title"), // NOI18N 
                                          loc.getString("CTL_UpdateResults_Column_Status_Desc")}); // NOI18N
        columnLabels.put(UpdateResultNode.COLUMN_NAME_PATH, new String [] { 
                                          loc.getString("CTL_UpdateResults_Column_Path_Title"), // NOI18N 
                                          loc.getString("CTL_UpdateResults_Column_Path_Desc")}); // NOI18N
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
    
    public UpdateResultsTable() {
        tableModel = new NodeTableModel();
        Subversion.getInstance().getStatusCache().addVersioningListener(this);
        sorter = new TableSorter(tableModel);
        sorter.setColumnComparator(Node.Property.class, NodeComparator);
        table = new JTable(sorter);
        sorter.setTableHeader(table.getTableHeader());
        int height = new JLabel("FONTSIZE").getPreferredSize().height * 6 / 5;  // NOI18N
        table.setRowHeight(height);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
        table.addMouseListener(this);
        table.setDefaultRenderer(Node.Property.class, new UpdateResultsTable.SyncTableCellRenderer());
        table.getSelectionModel().addListSelectionListener(this);
        table.addAncestorListener(this);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(UpdateResultsTable.class, "ACSN_UpdateResults")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(UpdateResultsTable.class, "ACSD_UpdateResults")); // NOI18N
        setColumns(new String [] { UpdateResultNode.COLUMN_NAME_NAME, UpdateResultNode.COLUMN_NAME_STATUS, UpdateResultNode.COLUMN_NAME_PATH });
    }

    void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int width = table.getWidth();
                for (int i = 0; i < tableColumns.length; i++) {
                    if (UpdateResultNode.COLUMN_NAME_PATH.equals(tableColumns[i])) {
                        table.getColumnModel().getColumn(i).setPreferredWidth(width * 60 / 100);
                    } else {
                        table.getColumnModel().getColumn(i).setPreferredWidth(width * 20 / 100);
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

    public UpdateResultNode [] getDisplayedNodes() {
        int n = sorter.getRowCount();
        UpdateResultNode [] ret = new UpdateResultNode[n];
        for (int i = 0; i < n; i++) {
            ret[i] = nodes[sorter.modelIndex(i)]; 
        }
        return ret;
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
        setDefaultColumnSizes();
        setModelProperties(columns);
        tableColumns = columns;
        for (int i = 0; i < tableColumns.length; i++) {
            sorter.setColumnComparator(i, null);
            sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
            if (UpdateResultNode.COLUMN_NAME_STATUS.equals(tableColumns[i])) {
                sorter.setSortingStatus(i, TableSorter.ASCENDING);
                break;
            }
        }
    }
        
    private void setModelProperties(String [] columns) {
        Node.Property [] properties = new Node.Property[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            String [] labels = (String[]) columnLabels.get(column);
            properties[i] = new UpdateResultsTable.ColumnDescriptor(column, String.class, labels[0], labels[1]);  
        }
        tableModel.setProperties(properties);
    }

    
    
    void setTableModel(UpdateResultNode [] nodes) {
        this.nodes = nodes;
        tableModel.setNodes(nodes);
    }

    void focus() {
        table.requestFocus();
    }

    private static class ColumnDescriptor extends PropertySupport.ReadOnly {
        
        public ColumnDescriptor(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if(e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger()) {
            onPopup(e);
        }        
    }

    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) return;
            row = sorter.modelIndex(row);
            performOpenInEditorAction(row);
        }
    }

    
    private interface ActionEvaluator {
        boolean isAction(FileUpdateInfo info);
    }
    private static ActionEvaluator conflictEvaluator = new ActionEvaluator() {
        public boolean isAction(FileUpdateInfo info) {
            return (info.getAction() & FileUpdateInfo.ACTION_CONFLICTED & FileUpdateInfo.ACTION_TYPE_FILE) != 0; 
        }
    };
    private static ActionEvaluator notDeletedEvaluator = new ActionEvaluator() {
        public boolean isAction(FileUpdateInfo info) {
            return (info.getAction() & ~FileUpdateInfo.ACTION_DELETED & FileUpdateInfo.ACTION_TYPE_FILE) != 0; 
        } 
    };    
    private void onPopup(MouseEvent e) {
        final int[] selection = table.getSelectedRows();
        JPopupMenu menu = new JPopupMenu();        
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(UpdateResultsTable.class, "CTL_MenuItem_Open")) { // NOI18N
            {
                setEnabled(selection.length == 1 && hasAction(selection, notDeletedEvaluator) );
            }
            public void actionPerformed(ActionEvent e) {
               performOpenInEditorAction(selection[0]);
            }
        }));        
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(UpdateResultsTable.class, "CTL_MenuItem_ResolveConflicts")) { // NOI18N
            {
                setEnabled(selection.length > -1 && hasAction(selection, conflictEvaluator));
            }
            public void actionPerformed(ActionEvent e) {                
                ResolveConflictsAction.resolveConflicts(getSelectedFiles(selection));
            }
        }));                
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    private boolean hasAction(int[] selection, ActionEvaluator ae) {
        for(int idx : selection) {
            int nodesIdx = sorter.modelIndex(idx);
            Node node = nodes[nodesIdx];
            FileUpdateInfo fui = (FileUpdateInfo) node.getLookup().lookup(FileUpdateInfo.class);
            if(fui != null && ae.isAction(fui) ) {
                continue;
            }
            return false;
        }        
        return true;
    }
    
    private void performOpenInEditorAction(int idx) {
        // XXX how is this supposed to work ???
        Action action = nodes[idx].getPreferredAction();
        if (action == null || !action.isEnabled()) action = new OpenInEditorAction();
        if (action.isEnabled()) {
            action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
        }
    }
    
    private File[] getSelectedFiles(int[] selection) {
        List<File> files = new ArrayList<File>();
        for(int idx : selection) {
            int nodesIdx = sorter.modelIndex(idx);
            Node node = nodes[nodesIdx];
            FileUpdateInfo fui = (FileUpdateInfo) node.getLookup().lookup(FileUpdateInfo.class);
            if(fui == null) {
                continue;
            }
            files.add(fui.getFile());
        }        
        return files.toArray(new File[files.size()]);
    }
    
    public void valueChanged(ListSelectionEvent e) {
        List<Node> selectedNodes = new ArrayList<Node>();
        ListSelectionModel selectionModel = table.getSelectionModel();
        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class,  table);
        if (tc == null) return; // table is no longer in component hierarchy
        
        int min = selectionModel.getMinSelectionIndex();
        if (min == -1) {
            tc.setActivatedNodes(new Node[0]);            
        }
        int max = selectionModel.getMaxSelectionIndex();
        for (int i = min; i <= max; i++) {
            if (selectionModel.isSelectedIndex(i)) {
                int idx = sorter.modelIndex(i);
                selectedNodes.add(nodes[idx]);
            }
        }
        tc.setActivatedNodes((Node[]) selectedNodes.toArray(new Node[selectedNodes.size()]));
    }
    
    private class SyncTableCellRenderer extends DefaultTableCellRenderer {
        
        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            if (modelColumnIndex == 0) {
                UpdateResultNode node = nodes[sorter.modelIndex(row)];
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
                String path = nodes[sorter.modelIndex(row)].getInfo().getFile().getAbsolutePath(); 
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }
    }

    public void versioningEvent(VersioningEvent event) {
        if(nodes.length == 0 ) {
            return;
        }        
        
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File changedFile = (File) event.getParams()[0];
            FileInformation newFileInfo = (FileInformation) event.getParams()[2];
            
            List<UpdateResultNode> nodesList = new ArrayList<UpdateResultNode>();                        
            for(UpdateResultNode node : nodes) {
                FileUpdateInfo fui = (FileUpdateInfo) node.getLookup().lookup(FileUpdateInfo.class);
                if(fui != null) {                    
                    int action = fui.getAction();
                    if((action & FileUpdateInfo.ACTION_CONFLICTED & FileUpdateInfo.ACTION_TYPE_FILE) != 0 && 
                       fui.getFile().equals(changedFile) && 
                       ( (newFileInfo.getStatus() & newFileInfo.STATUS_VERSIONED_CONFLICT) == 0) ) 
                    {
                        action &= ~FileUpdateInfo.ACTION_CONFLICTED;
                        action |=  FileUpdateInfo.ACTION_CONFLICTED_RESOLVED;
                        
                        FileUpdateInfo newFui = new FileUpdateInfo(fui.getFile(), action);
                        nodesList.add(new UpdateResultNode(newFui));
                    } else {
                        nodesList.add(node);
                    }                   
                } else {
                    nodesList.add(node);
                }                                                
            }
            
            // XXX reschedule !!!
            setTableModel(nodesList.toArray(new UpdateResultNode[nodesList.size()]));
        }
    }
}
