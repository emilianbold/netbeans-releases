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

package org.netbeans.modules.git.ui.status;

import org.openide.windows.TopComponent;
import org.openide.awt.MouseUtils;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
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
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.git.ui.checkout.CheckoutPathsAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumn;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Controls the {@link #getComponent() tsble} that displays nodes
 * in the Versioning view. The table is  {@link #setTableModel populated)
 * from VersioningPanel.
 * 
 * @author Maros Sandor
 */
class StatusTable implements MouseListener, ListSelectionListener {

    private ETable          table;
    private JScrollPane     component;
    
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
                    VersioningPanelController.LOG.log(Level.INFO, null, e);
                    return 0;
                }
            }
        }
    };
    private final StatusTableModel tableModel;
    
    public StatusTable (StatusTableModel model) {
        this.tableModel = model;
        table = new ETable(tableModel);
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        table.addMouseListener(this);
        table.getSelectionModel().addListSelectionListener(this);

        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));

        table.setDefaultRenderer(Node.Property.class, new SyncTableCellRenderer());
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); // NOI18N
        table.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(table));
            }
        });
        setModelProperties();
    }

    private void setModelProperties () {
        Node.Property [] properties = new Node.Property[3];
        properties[0] = new ColumnDescriptor<String>(StatusNode.NameProperty.NAME, String.class, StatusNode.NameProperty.DISPLAY_NAME, StatusNode.NameProperty.DESCRIPTION);
        properties[1] = new ColumnDescriptor<String>(StatusNode.StatusProperty.NAME, String.class, StatusNode.StatusProperty.DISPLAY_NAME, StatusNode.StatusProperty.DESCRIPTION);
        properties[2] = new ColumnDescriptor<String>(StatusNode.PathProperty.NAME, String.class, StatusNode.PathProperty.DISPLAY_NAME, StatusNode.PathProperty.DESCRIPTION);
        tableModel.setProperties(properties);
        for (int i = 0; i < table.getColumnCount(); ++i) {
            ((ETableColumn) table.getColumnModel().getColumn(i)).setNestedComparator(NodeComparator);
        }
    }

    public JComponent getComponent() {
        return component;
    }

    final void setColumns (String [] columns) {
    }
        
    void focus () {
        table.requestFocus();
    }

// <editor-fold defaultstate="collapsed" desc="popup and selection">
    private void showPopup(final MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row != -1) {
            boolean makeRowSelected = true;
            int[] selectedrows = table.getSelectedRows();

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
        EventQueue.invokeLater(new Runnable() {

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

    private JPopupMenu getPopup() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        item = menu.add(new OpenInEditorAction(getSelectedFiles()));
        Mnemonics.setLocalizedText(item, item.getText());
        menu.addSeparator();
        item = menu.add(new SystemActionBridge(SystemAction.get(CommitAction.class), NbBundle.getMessage(CommitAction.class, "LBL_CommitAction.popupName"))); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(CheckoutPathsAction.class), NbBundle.getMessage(CheckoutPathsAction.class, "LBL_CheckoutPathsAction_PopupName"))); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        return menu;
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
    public void mouseReleased (MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseClicked (MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) {
                return;
            }
            StatusNode node = tableModel.getNode(table.convertRowIndexToModel(row));
            Action action = node.getPreferredAction();
            if (action != null && action.isEnabled()) {
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, node.getFile().getAbsolutePath()));
            }
        }
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        List<StatusNode> selectedNodes = new ArrayList<StatusNode>();
        ListSelectionModel selection = table.getSelectionModel();
        final TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, table);
        if (tc == null) {
            return; // table is no longer in component hierarchy
        }
        int min = selection.getMinSelectionIndex();
        if (min != -1) {
            int max = selection.getMaxSelectionIndex();
            for (int i = min; i <= max; i++) {
                if (selection.isSelectedIndex(i)) {
                    int idx = table.convertRowIndexToModel(i);
                    selectedNodes.add(tableModel.getNode(idx));
                }
            }
        }
        // this method may be called outside of AWT if a node fires change events from some other thread, see #79174
        final Node[] nodeArray = selectedNodes.toArray(new Node[selectedNodes.size()]);
        if (SwingUtilities.isEventDispatchThread()) {
            tc.setActivatedNodes(nodeArray);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tc.setActivatedNodes(nodeArray);
                }
            });
        }
    }// </editor-fold>

    private File[] getSelectedFiles() {
        int[] selection = table.getSelectedRows();
        List<File> files = new LinkedList<File>();
        for (int i : selection) {
            StatusNode selectedNode = tableModel.getNode(table.convertRowIndexToModel(i));
            files.add(selectedNode.getFile());
        }
        return files.toArray(new File[files.size()]);
    }

    private void setSelectedNodes (File[] selectedFiles) {
        Set<File> files = new HashSet<File>(Arrays.asList(selectedFiles));
        ListSelectionModel selection = table.getSelectionModel();
        selection.setValueIsAdjusting(true);
        selection.clearSelection();
        for (int i = 0; i < table.getRowCount(); ++i) {
            StatusNode node = tableModel.getNode(table.convertRowIndexToModel(i));
            if (files.contains(node.getFile())) {
                selection.addSelectionInterval(i, i);
            }
        }
        selection.setValueIsAdjusting(false);
    }

    void setNodes (StatusNode[] nodes) {
        File[] selectedFiles = getSelectedFiles();
        tableModel.setNodes(nodes);
        setSelectedNodes(selectedFiles);
    }

    Collection<StatusNode> getNodes () {
        return tableModel.getNodes();
    }

    void updateNodes (List<StatusNode> toRemove, List<StatusNode> toRefresh, List<StatusNode> toAdd) {
        File[] selectedFiles = getSelectedFiles();
        for (StatusNode node : toRefresh) {
            node.refresh();
        }
        tableModel.remove(toRemove);
        tableModel.add(toAdd);
        tableModel.fireTableDataChanged();
        setSelectedNodes(selectedFiles);
    }

    private static class ColumnDescriptor<T> extends ReadOnly<T> {
        @SuppressWarnings("unchecked")
        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    private class SyncTableCellRenderer extends DefaultTableCellRenderer {
        
        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();

        @Override
        public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            StatusNode node = null;
            if (modelColumnIndex == 0) {
                node = tableModel.getNode(table.convertRowIndexToModel(row));
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
                if (node == null) {
                    node = tableModel.getNode(table.convertRowIndexToModel(row));
                }
                String path = node.getFile().getAbsolutePath();
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }
    }
}
