/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.util.status;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.awt.MouseUtils;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;

/**
 *
 * @author ondra
 */
public abstract class VCSStatusTable<T extends VCSStatusNode> implements MouseListener, ListSelectionListener {

    private final ETable          table;
    private final JScrollPane     component;
    protected final VCSStatusTableModel<T> tableModel;
    private final PropertyChangeSupport support;
    private static final Logger LOG = Logger.getLogger(VCSStatusTable.class.getName());
    public static final String PROP_SELECTED_FILES = "selectedFiles"; //NOI18N

    public VCSStatusTable (VCSStatusTableModel<T> tableModel) {
        this.tableModel = tableModel;
        this.support = new PropertyChangeSupport(this);
        table = new ETable(tableModel);
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        table.addMouseListener(this);
        table.getSelectionModel().addListSelectionListener(this);

        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));

        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); // NOI18N
        table.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(table));
            }
        });
        initColumns();
    }

    protected final void setDefaultRenderer (TableCellRenderer renderer)  {
        table.setDefaultRenderer(Node.Property.class, renderer);
    }

    protected static final Comparator NodeComparator = new Comparator () {
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
                    LOG.log(Level.INFO, null, e);
                    return 0;
                }
            }
        }
    };

    public final void focus () {
        table.requestFocusInWindow();
    }

    protected abstract void setModelProperties ();

    public final JComponent getComponent () {
        return component;
    }

    protected final JTable getTable () {
        return table;
    }

    public Map<File, T> getNodes () {
        T[] nodes = tableModel.getNodes();
        Map<File, T> nodesAsMap = new HashMap<File, T>(nodes.length);
        for (T node : nodes) {
            nodesAsMap.put(node.getFile(), node);
        }
        return nodesAsMap;
    }

    protected final T[] getSelectedNodes () {
        int[] selection = table.getSelectedRows();
        List<T> nodes = new LinkedList<T>();
        for (int i : selection) {
            T selectedNode = tableModel.getNode(table.convertRowIndexToModel(i));
            nodes.add(selectedNode);
        }
        return nodes.toArray((T[]) java.lang.reflect.Array.newInstance(tableModel.getItemClass(), nodes.size()));
    }

    public final File[] getSelectedFiles () {
        int[] selection = table.getSelectedRows();
        List<File> files = new LinkedList<File>();
        for (int i : selection) {
            T selectedNode = tableModel.getNode(table.convertRowIndexToModel(i));
            files.add(selectedNode.getFile());
        }
        return files.toArray(new File[files.size()]);
    }

    public final void setSelectedNodes (File[] selectedFiles) {
        Set<File> files = new HashSet<File>(Arrays.asList(selectedFiles));
        ListSelectionModel selection = table.getSelectionModel();
        selection.setValueIsAdjusting(true);
        selection.clearSelection();
        for (int i = 0; i < table.getRowCount(); ++i) {
            T node = tableModel.getNode(table.convertRowIndexToModel(i));
            if (files.contains(node.getFile())) {
                selection.addSelectionInterval(i, i);
            }
        }
        selection.setValueIsAdjusting(false);
    }

    public void setNodes (T[] nodes) {
        File[] selectedFiles = getSelectedFiles();
        tableModel.setNodes(nodes);
        setSelectedNodes(selectedFiles);
        if (selectedFiles.length == 0 && nodes.length > 0) {
            table.getSelectionModel().addSelectionInterval(0, 0);
        }
    }

    public void updateNodes (List<T> toRemove, List<T> toRefresh, List<T> toAdd) {
        File[] selectedFiles = getSelectedFiles();
        for (VCSStatusNode node : toRefresh) {
            node.refresh();
        }
        tableModel.remove(toRemove);
        tableModel.add(toAdd);
        tableModel.fireTableDataChanged();
        setSelectedNodes(selectedFiles);
    }

    public File getNextFile (File file) {
        return getNeighbouringFile(file, 1);
    }

    public File getPrevFile (File file) {
        return getNeighbouringFile(file, -1);
    }

    public File getNeighbouringFile (File file, int indexDelta) {
        assert EventQueue.isDispatchThread();
        int tableIndex = findIndex(file);
        File neighbour = null;
        if (tableIndex > -1) {
            tableIndex += indexDelta;
            if (tableIndex >= 0 && tableIndex < table.getRowCount()) {
                neighbour = tableModel.getNode(table.convertRowIndexToModel(tableIndex)).getFile();
            }
        }
        return neighbour;
    }

    public void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }

    public void removePropertyChangeListener (PropertyChangeListener list) {
        support.removePropertyChangeListener(list);
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

    protected abstract JPopupMenu getPopup ();

    /**
     * Called when user dbl-clicks on a node. May be intercepted and handled in a different way. By default a node's preferred action is invoked.
     * @param node 
     */
    protected void mouseClicked (VCSStatusNode node) {
        Action action = node.getPreferredAction();
        if (action != null && action.isEnabled()) {
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, node.getFile().getAbsolutePath()));
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
            VCSStatusNode node = tableModel.getNode(table.convertRowIndexToModel(row));
            mouseClicked(node);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void valueChanged (ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        List<VCSStatusNode> selectedNodes = new ArrayList<VCSStatusNode>();
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
        final T[] nodeArray = selectedNodes.toArray((T[]) java.lang.reflect.Array.newInstance(tableModel.getItemClass(), selectedNodes.size()));
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                File[] selectedFiles = new File[nodeArray.length];
                for (int i = 0; i < nodeArray.length; ++i) {
                    selectedFiles[i] = nodeArray[i].getFile();
                }
                support.firePropertyChange(PROP_SELECTED_FILES, null, selectedFiles);
                tc.setActivatedNodes(nodeArray);
            }
        });
    }// </editor-fold>

    private void initColumns () {
        setModelProperties();
        table.setColumnHidingAllowed(false);
        for (int i = 0; i < table.getColumnCount(); ++i) {
            ((ETableColumn) table.getColumnModel().getColumn(i)).setNestedComparator(NodeComparator);
        }
    }

    private int findIndex (File file) {
        // try faster search among selected rows
        int[] selection = table.getSelectedRows();
        for (int i : selection) {
            T selectedNode = tableModel.getNode(table.convertRowIndexToModel(i));
            if (selectedNode.getFile().equals(file)) {
                return i;
            }
        }
        // slower, search among all rows
        for (int i = 0; i < table.getRowCount(); ++i) {
            T selectedNode = tableModel.getNode(table.convertRowIndexToModel(i));
            if (selectedNode.getFile().equals(file)) {
                return i;
            }
        }
        return -1;
    }

    protected static class ColumnDescriptor<T> extends ReadOnly<T> {

        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }
}
