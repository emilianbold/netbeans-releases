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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.subversion.remote.ui.diff;

import org.openide.explorer.view.NodeTableModel;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.ui.status.OpenInEditorAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.netbeans.modules.versioning.util.SortedTable;
import org.openide.awt.MouseUtils;
import org.openide.cookies.EditorCookie;
import org.openide.util.WeakListeners;

/**
 * 
 * 
 */
class DiffFileTable implements MouseListener, ListSelectionListener, AncestorListener {
    
    private final NodeTableModel tableModel;
    private JTable table;
    private final JScrollPane     component;
    private DiffNode [] nodes = new DiffNode[0];
    /**
     * editor cookies belonging to the files being diffed.
     * The array may contain {@code null}s if {@code EditorCookie}s
     * for the corresponding files were not found.
     *
     * @see  #nodes
     */
    private EditorCookie[] editorCookies;
    
    private String []   tableColumns; 
    private final TableSorter sorter;

    /**
     * Defines labels for Diff view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<>(4);

    static {
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
        @Override
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
                    Subversion.LOG.log(Level.SEVERE, null, e);
                    return 0;
                }
            }
        }
    };
    private final MultiDiffPanel master;
    private PropertyChangeListener changeListener;

    public DiffFileTable(MultiDiffPanel master) {
        this.master = master;
        tableModel = new NodeTableModel();
        sorter = new TableSorter(tableModel);
        sorter.setColumnComparator(Node.Property.class, DiffFileTable.NodeComparator);
        table = new SortedTable(sorter);
        table.getSelectionModel().addListSelectionListener(this);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
        table.addMouseListener(this);
        table.setDefaultRenderer(Node.Property.class, new DiffTableCellRenderer());
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
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); //NOI18N
        table.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() { //NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(table));
            }
        });
    }

    void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
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

    @Override
    public void ancestorAdded(AncestorEvent event) {
        setDefaultColumnSizes();
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
    }

    @Override
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
        if (Arrays.equals(columns, tableColumns)) {
            return;
        }
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
            properties[i] = new ColumnDescriptor(column, labels[0], labels[1]);  
        }
        tableModel.setProperties(properties);
    }

    void setTableModel(Setup[] setups, EditorCookie[] editorCookies) {
        this.editorCookies = editorCookies;
        tableModel.setNodes(nodes = setupsToNodes(setups));
        changeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                Object source = e.getSource();
                String propertyName = e.getPropertyName();
                if (EditorCookie.Observable.PROP_MODIFIED.equals(propertyName)
                        && (source instanceof EditorCookie.Observable)) {
                    statusModifiedChanged((EditorCookie.Observable) source);
                }
            }
        };
        for (EditorCookie editorCookie : this.editorCookies) {
            if (editorCookie instanceof EditorCookie.Observable) {
                ((EditorCookie.Observable) editorCookie).addPropertyChangeListener(WeakListeners.propertyChange(changeListener, editorCookie));
            }
        }
    }

    /**
     * Updates the corresponding table row - makes the file name bold or plain,
     * depending on the new <em>modified</em> state of the corresponing file.
     * 
     * @param  editorCookie  {@code EditorCookie} that fired the change
     *                       if <em>modified</em> status
     */
    private void statusModifiedChanged(EditorCookie editorCookie) {
        int index = CollectionUtils.findInArray(editorCookies, editorCookie);

        if (index == -1) {
            return;
        }

        tableModel.fireTableChanged(
              new TableSorter.SortingSafeTableModelEvent(tableModel, index, 0));
    }

    private static DiffNode[] setupsToNodes(Setup[] setups) {
        DiffNode[] nodes = new DiffNode[setups.length];
        for (int i = 0; i < setups.length; i++) {
            nodes[i] = setups[i].getNode();
        }
        return nodes;
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

    private static class ColumnDescriptor extends PropertySupport.ReadOnly<String> {
        
        public ColumnDescriptor(String name, String displayName, String shortDescription) {
            super(name, String.class, displayName, shortDescription);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
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
            @Override
            public void run() {
                // invoke later so the selection on the table will be set first
                if (table.isShowing()) {
                    JPopupMenu menu = master.getPopup();
                    if (menu != null) {
                        menu.show(table, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private void showPopup(Point p) {
        JPopupMenu menu = master.getPopup();
        if (menu != null) {
            menu.show(table, p.x, p.y);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) {
                return;
            }
            row = sorter.modelIndex(row);
            Action action = nodes[row].getPreferredAction();
            if (action == null || !action.isEnabled()) {
                action = new OpenInEditorAction();
            }
            if (action.isEnabled()) {
                action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        ListSelectionModel selectionModel = table.getSelectionModel();
        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();
        if (min != -1 && min == max) {
            // single selection
            master.tableRowSelected(table.getSelectedRow());
        } else {
            List<DiffNode> selectedNodes = new ArrayList<>();
            if (min == -1) {
                master.tableRowSelected(-1);
            } else {
                for (int i = min; i <= max; i++) {
                    if (selectionModel.isSelectedIndex(i)) {
                        int idx = sorter.modelIndex(i);
                        selectedNodes.add(nodes[idx]);
                    }
                }
            }
            final TopComponent tc = (TopComponent) master.getClientProperty(TopComponent.class);
            if (tc == null) {
                return; // table is no longer in component hierarchy
            }
            Node [] nodesToActivate = selectedNodes.toArray(new Node[selectedNodes.size()]);
            tc.setActivatedNodes(nodesToActivate);
        }
    }

    private class DiffTableCellRenderer extends DefaultTableCellRenderer {
        
        private final FilePathCellRenderer pathRenderer = new FilePathCellRenderer();
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            if (modelColumnIndex == 0) {
                int modelRow = sorter.modelIndex(row);
                String htmlDisplayName
                       = DiffUtils.getHtmlDisplayName(nodes[modelRow],
                                                      isModified(modelRow),
                                                      isSelected);
                if (SvnModuleConfig.getDefault(master.getFileSystem()).isExcludedFromCommit(nodes[modelRow].getSetup().getBaseFile().getPath())) {
                    htmlDisplayName = "<s>" + (htmlDisplayName == null ? nodes[modelRow].getName() : htmlDisplayName) + "</s>"; //NOI18N
                }
                if (htmlDisplayName != null) {
                    value = "<html>" + htmlDisplayName;                 //NOI18N
                }
            }
            if (modelColumnIndex == 2) {
                renderer = pathRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            if (renderer instanceof JComponent) {
                VCSFileProxy file = nodes[sorter.modelIndex(row)].getLookup().lookup(VCSFileProxy.class); 
                String path = file != null ? file.getPath() : null; 
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }

        private boolean isModified(int row) {
            EditorCookie editorCookie = editorCookies[row];
            return (editorCookie != null) ? editorCookie.isModified() : false;
        }
    }
}
