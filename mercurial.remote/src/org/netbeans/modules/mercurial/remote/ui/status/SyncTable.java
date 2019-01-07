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

package org.netbeans.modules.mercurial.remote.ui.status;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.MercurialAnnotator;
import org.netbeans.modules.mercurial.remote.ui.add.AddAction;
import org.netbeans.modules.mercurial.remote.ui.annotate.AnnotateAction;
import org.netbeans.modules.mercurial.remote.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.remote.ui.commit.DeleteLocalAction;
import org.netbeans.modules.mercurial.remote.ui.commit.ExcludeFromCommitAction;
import org.netbeans.modules.mercurial.remote.ui.diff.DiffAction;
import org.netbeans.modules.mercurial.remote.ui.update.ResolveConflictsAction;
import org.netbeans.modules.mercurial.remote.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumn;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.openide.explorer.view.NodeTableModel;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 * Controls the {@link #getComponent() tsble} that displays nodes
 * in the Versioning view. The table is  {@link #setTableModel populated)
 * from VersioningPanel.
 * 
 * 
 */
class SyncTable implements MouseListener, ListSelectionListener, AncestorListener, PropertyChangeListener {

    private final NodeTableModel  tableModel;
    private ETable          table;
    private final JScrollPane     component;
    private SyncFileNode [] nodes = new SyncFileNode[0];
    
    private String []   tableColumns; 

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<>(4);
    static {
        ResourceBundle loc = NbBundle.getBundle(SyncTable.class);
        columnLabels.put(SyncFileNode.COLUMN_NAME_BRANCH, new String [] {
                                          loc.getString("CTL_VersioningView_Column_Branch_Title"),  // NOI18N
                                          loc.getString("CTL_VersioningView_Column_Branch_Desc")}); // NOI18N
        columnLabels.put(SyncFileNode.COLUMN_NAME_NAME, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_File_Title"),  // NOI18N
                                          loc.getString("CTL_VersioningView_Column_File_Desc")}); // NOI18N
        columnLabels.put(SyncFileNode.COLUMN_NAME_STATUS, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_Status_Title"),  // NOI18N
                                          loc.getString("CTL_VersioningView_Column_Status_Desc")}); // NOI18N
        columnLabels.put(SyncFileNode.COLUMN_NAME_PATH, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_Path_Title"),  // NOI18N
                                          loc.getString("CTL_VersioningView_Column_Path_Desc")}); // NOI18N
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
                    Mercurial.LOG.log(Level.INFO, null, e);
                    return 0;
                }
            }
        }
    };
    
    public SyncTable() {
        tableModel = new NodeTableModel();
        table = new ETable(tableModel);
        table.setColumnHidingAllowed(false);
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) {
            borderColor = UIManager.getColor("controlShadow"); // NOI18N
        }
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
        table.addMouseListener(this);
        table.setDefaultRenderer(Node.Property.class, new SyncTableCellRenderer());
        table.getSelectionModel().addListSelectionListener(this);
        table.addAncestorListener(this);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SyncTable.class, "ACSN_VersioningTable")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SyncTable.class, "ACSD_VersioningTable")); // NOI18N
        setColumns(new String[] {
            SyncFileNode.COLUMN_NAME_NAME,
            SyncFileNode.COLUMN_NAME_STATUS,
            SyncFileNode.COLUMN_NAME_PATH}
        );
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); // NOI18N
        table.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(table));
            }
        });
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DeleteAction"); //NOI18N
        table.getActionMap().put("DeleteAction", SystemAction.get(DeleteLocalAction.class)); //NOI18N
    }

    void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int width = table.getWidth();
                if (tableColumns.length == 3) {
                    for (int i = 0; i < tableColumns.length; i++) {
                        if (SyncFileNode.COLUMN_NAME_PATH.equals(tableColumns[i])) {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 60 / 100);
                        } else {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 20 / 100);
                        }
                    }
                } else if (tableColumns.length == 4) {
                    for (int i = 0; i < tableColumns.length; i++) {
                        if (SyncFileNode.COLUMN_NAME_PATH.equals(tableColumns[i])) {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 55 / 100);
                        } else if (SyncFileNode.COLUMN_NAME_BRANCH.equals(tableColumns[i])) {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 20 / 100);
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
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Mercurial.PROP_ANNOTATIONS_CHANGED.equals(evt.getPropertyName())) {
            refreshNodes();
        }
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
        for (int i = 0; i < columns.length; ++i) {
            ((ETableColumn) table.getColumnModel().getColumn(i)).setNestedComparator(NodeComparator);
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

    void setTableModel(SyncFileNode [] nodes) {
        assert EventQueue.isDispatchThread();
        Collection<VCSFileProxy> selectedFiles = getSelectedFiles();
        this.nodes = nodes;
        tableModel.setNodes(nodes);
        setSelectedNodes(selectedFiles);
        Mercurial.getInstance().getRequestProcessor().post(new Runnable () {
            @Override
            public void run() {
                refreshNodes();
            }
        });
    }

    void focus() {
        table.requestFocus();
    }

    private void refreshNodes () {
        SyncFileNode[] toRefreshNodes = nodes;
        for (SyncFileNode node : toRefreshNodes) {
            node.refresh();
        }
        if (toRefreshNodes.length > 0) {
            EventQueue.invokeLater(new Runnable () {
                @Override
                public void run() {
                    table.revalidate();
                    table.repaint();
                }
            });
        }
    }

    private SyncFileNode[] getSelectedNodes () {
        List<SyncFileNode> selectedNodes = new ArrayList<>();
        ListSelectionModel selectionModel = table.getSelectionModel();
        int min = selectionModel.getMinSelectionIndex();
        if (min != -1) {
            int max = selectionModel.getMaxSelectionIndex();
            for (int i = min; i <= max; i++) {
                if (selectionModel.isSelectedIndex(i)) {
                    selectedNodes.add(nodes[table.convertRowIndexToModel(i)]);
                }
            }
        }
        return selectedNodes.toArray(new SyncFileNode[selectedNodes.size()]);
    }

    private Collection<VCSFileProxy> getSelectedFiles () {
        SyncFileNode[] selectedNodes = getSelectedNodes();
        Collection<VCSFileProxy> files = new HashSet<>(selectedNodes.length);
        for (SyncFileNode node : selectedNodes) {
            files.add(node.getFile());
        }
        return files;
    }

    public final void setSelectedNodes (Collection<VCSFileProxy> selectedFiles) {
        ListSelectionModel selection = table.getSelectionModel();
        selection.setValueIsAdjusting(true);
        selection.clearSelection();
        for (int i = 0; i < table.getRowCount(); ++i) {
            SyncFileNode node = nodes[table.convertRowIndexToModel(i)];
            if (selectedFiles.contains(node.getFile())) {
                selection.addSelectionInterval(i, i);
            }
        }
        selection.setValueIsAdjusting(false);
    }

    private static class ColumnDescriptor extends ReadOnly {
        
        @SuppressWarnings("unchecked")
        public ColumnDescriptor(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
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
            @Override
            public void run() {
                // invoke later so the selection on the table will be set first
                if (table.isShowing()) {
                    JPopupMenu menu = getPopup();
                    menu.show(table, e.getX(), e.getY());
                }
            }
        });
    }

    private void showPopup(Point p) {
        JPopupMenu menu = getPopup();
        menu.show(table, p.x, p.y);
    }
    
    /**
     * Constructs contextual Menu: File Node
        <pre>
        Open
        -------------------
        Diff                 (default action)
        Add
        Commit...        
        --------------------
        Conflict Resolved    (on conflicting file)
        --------------------
        Show Annotations
        --------------------        
        Revert Modifications  (Revert Delete)(Delete)
        Exclude from Commit   (Include in Commit)
        </pre>
     */
    private JPopupMenu getPopup() {

        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        
        item = menu.add(new OpenInEditorAction());
        Mnemonics.setLocalizedText(item, item.getText());
        menu.addSeparator();
        item = menu.add(new SystemActionBridge(SystemAction.get(DiffAction.class), actionString("CTL_PopupMenuItem_Diff"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(AddAction.class), NbBundle.getMessage(AddAction.class, "CTL_PopupMenuItem_Add"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(CommitAction.class), actionString("CTL_PopupMenuItem_Commit"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        
        menu.addSeparator();

        item = menu.add(new SystemActionBridge(SystemAction.get(ResolveConflictsAction.class), actionString("CTL_PopupMenuItem_ResolveConflicts"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());

        menu.addSeparator();

        item = menu.add(new SystemActionBridge(SystemAction.get(AnnotateAction.class),
                                               ((AnnotateAction)SystemAction.get(AnnotateAction.class)).visible(null) ?
                                               actionString("CTL_PopupMenuItem_HideAnnotations") : //NOI18N
                                               actionString("CTL_PopupMenuItem_ShowAnnotations"))); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        menu.addSeparator();

        boolean allLocallyDeleted = true;
        boolean allLocallyNew = true;
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        Set<VCSFileProxy> files = HgUtils.getCurrentContext(null).getRootFiles();
        
        for (VCSFileProxy file : files) {
            FileInformation info = cache.getStatus(file);
            if ((info.getStatus() & DeleteLocalAction.LOCALLY_DELETABLE_MASK) == 0 ) {
                allLocallyNew = false;
            }
            if (info.getStatus() != FileInformation.STATUS_VERSIONED_DELETEDLOCALLY && info.getStatus() != FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                allLocallyDeleted = false;
            }
        }
        if (allLocallyNew) {
            SystemAction systemAction = SystemAction.get(DeleteLocalAction.class);
            item = menu.add(new SystemActionBridge(systemAction, actionString("CTL_PopupMenuItem_Delete"))); //NOI18N
            Mnemonics.setLocalizedText(item, item.getText());
        }
        if (allLocallyDeleted) {
            item = menu.add(new SystemActionBridge(SystemAction.get(RevertModificationsAction.class), actionString("CTL_PopupMenuItem_RevertDelete"))); //NOI18N
        } else {
            item = menu.add(new SystemActionBridge(SystemAction.get(RevertModificationsAction.class), actionString("CTL_PopupMenuItem_GetClean"))); //NOI18N
        }
        Mnemonics.setLocalizedText(item, item.getText());

        String label;
        ExcludeFromCommitAction exclude = (ExcludeFromCommitAction) SystemAction.get(ExcludeFromCommitAction.class);
        if (exclude.getActionStatus(null) == ExcludeFromCommitAction.INCLUDING) {
            label = actionString("CTL_PopupMenuItem_IncludeInCommit");  //NOI18N
        } else {
            label = actionString("CTL_PopupMenuItem_ExcludeFromCommit"); //NOI18N
        }
        item = menu.add(new SystemActionBridge(exclude, label));
        Mnemonics.setLocalizedText(item, item.getText());
        return menu;
    }

    /** 
     * Workaround.
     * I18N Test Wizard searches for keys in syncview package Bundle.properties 
     */
    private String actionString(String key) {
        ResourceBundle actionsLoc = NbBundle.getBundle(MercurialAnnotator.class);
        return actionsLoc.getString(key);
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
            row = table.convertRowIndexToModel(row);
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
        final TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, table);
        if (tc == null) {
            return; // table is no longer in component hierarchy
            // this method may be called outside of AWT if a node fires change events from some other thread, see #79174
        }
        
        final Node [] nodeArray = getSelectedNodes();
        if (SwingUtilities.isEventDispatchThread()) {
            tc.setActivatedNodes(nodeArray);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tc.setActivatedNodes(nodeArray);
                }
            });
        }
    }
    
    private class SyncTableCellRenderer extends DefaultTableCellRenderer {
        
        private final FilePathCellRenderer pathRenderer = new FilePathCellRenderer();
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            if (modelColumnIndex == 0) {
                SyncFileNode node = nodes[table.convertRowIndexToModel(row)];
                if (!isSelected) {
                    value = "<html>" + node.getHtmlDisplayName(); // NOI18N
                }
                final VCSFileProxy file = node.getFile();
                if (HgModuleConfig.getDefault(file).isExcludedFromCommit(node.getFile().getPath())) {
                    String nodeName = node.getDisplayName();
                    if (isSelected) {
                        value = "<html><s>" + nodeName + "</s></html>"; // NOI18N
                    } else {
                        value = "<html><s>" + Mercurial.getInstance().getMercurialAnnotator().annotateNameHtml(nodeName, node.getFileInformation(), null) + "</s>"; // NOI18N
                    }
                }
            }
            if (modelColumnIndex == 2) {
                renderer = pathRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            if (renderer instanceof JComponent) {
                String path = nodes[table.convertRowIndexToModel(row)].getFile().getPath(); 
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }
    }
}
