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

package org.netbeans.modules.git.ui.diff;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.actions.AddAction;
import org.netbeans.modules.git.ui.checkout.CheckoutPathsAction;
import org.netbeans.modules.git.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsAction;
import org.netbeans.modules.git.ui.status.GitStatusNode;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
import org.netbeans.modules.versioning.util.status.VCSStatusTable;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Controls the {@link #getComponent() tsble} that displays nodes
 * in the Versioning view. The table is  {@link #setTableModel populated)
 * from VersioningPanel.
 * 
 * @author Maros Sandor
 */
class DiffFileTable extends VCSStatusTable<DiffNode> {

    /**
     * editor cookies belonging to the files being diffed.
     * The array may contain {@code null}s if {@code EditorCookie}s
     * for the corresponding files were not found.
     *
     * @see  #nodes
     */
    private Map<File, EditorCookie> editorCookies;
    
    private PropertyChangeListener changeListener;
    
    public DiffFileTable (VCSStatusTableModel<DiffNode> model) {
        super(model);
        setDefaultRenderer(new DiffTableCellRenderer());
    }

    @Override
    protected void setModelProperties () {
        Node.Property [] properties = new Node.Property[3];
        properties[0] = new ColumnDescriptor<String>(DiffNode.NameProperty.NAME, String.class, DiffNode.NameProperty.DISPLAY_NAME, DiffNode.NameProperty.DESCRIPTION);
        properties[1] = new ColumnDescriptor<String>(DiffNode.GitStatusProperty.NAME, String.class, DiffNode.GitStatusProperty.DISPLAY_NAME, DiffNode.GitStatusProperty.DESCRIPTION);
        properties[2] = new ColumnDescriptor<String>(DiffNode.PathProperty.NAME, String.class, DiffNode.PathProperty.DISPLAY_NAME, DiffNode.PathProperty.DESCRIPTION);
        tableModel.setProperties(properties);
    }

    @Override
    protected JPopupMenu getPopup () {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        item = menu.add(new OpenInEditorAction(getSelectedFiles()));
        Mnemonics.setLocalizedText(item, item.getText());
        menu.addSeparator();
        GitStatusNode[] selectedNodes = getSelectedNodes();
        boolean displayAdd = false;
        for (GitStatusNode node : selectedNodes) {
            // is there any change between index and WT?
            if (node.getFileNode().getInformation().containsStatus(EnumSet.of(Status.NEW_INDEX_WORKING_TREE,
                    Status.IN_CONFLICT,
                    Status.MODIFIED_INDEX_WORKING_TREE))) {
                displayAdd = true;
            }
        }
        if (displayAdd) {
            item = menu.add(new SystemActionBridge(SystemAction.get(AddAction.class), NbBundle.getMessage(AddAction.class, "LBL_AddAction.popupName"))); //NOI18N
            Mnemonics.setLocalizedText(item, item.getText());
        }
        item = menu.add(new SystemActionBridge(SystemAction.get(CommitAction.class), NbBundle.getMessage(CommitAction.class, "LBL_CommitAction.popupName"))); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(RevertChangesAction.class), NbBundle.getMessage(CheckoutPathsAction.class, "LBL_RevertChangesAction_PopupName"))); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(CheckoutPathsAction.class), NbBundle.getMessage(CheckoutPathsAction.class, "LBL_CheckoutPathsAction_PopupName"))); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());

        ResolveConflictsAction a = SystemAction.get(ResolveConflictsAction.class);
        if (a.isEnabled()) {
            menu.addSeparator();
            item = menu.add(new SystemActionBridge(a, NbBundle.getMessage(ResolveConflictsAction.class, "LBL_ResolveConflictsAction_PopupName"))); //NOI18N);
            Mnemonics.setLocalizedText(item, item.getText());
        }
        return menu;
    }

    @Override
    public void setNodes (DiffNode[] nodes) {
        throw new UnsupportedOperationException("Do not call this method."); //NOI18N
    }

    void setNodes (Map<File, EditorCookie> editorCookies, DiffNode[] nodes) {
        setEditorCookies(editorCookies);
        super.setNodes(nodes);
    }

    @Override
    public void updateNodes (List<DiffNode> toRemove, List<DiffNode> toRefresh, List<DiffNode> toAdd) {
        throw new UnsupportedOperationException("Do not call this method."); //NOI18N
    }

    void updateNodes (Map<File, EditorCookie> editorCookies, List<DiffNode> toRemove, List<DiffNode> toRefresh, List<DiffNode> toAdd) {
        setEditorCookies(editorCookies);
        super.updateNodes(toRemove, toRefresh, toAdd);
        if (getTable().getRowCount() == 1) {
            getTable().getSelectionModel().addSelectionInterval(0, 0);
        }
    }

    private void setEditorCookies (Map<File, EditorCookie> editorCookies) {
        this.editorCookies = editorCookies;
        changeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent e) {
                Object source = e.getSource();
                String propertyName = e.getPropertyName();
                if (EditorCookie.Observable.PROP_MODIFIED.equals(propertyName) && (source instanceof EditorCookie.Observable)) {
                    final EditorCookie.Observable cookie = (EditorCookie.Observable) source;
                    Mutex.EVENT.readAccess(new Runnable () {
                        @Override
                        public void run() {
                            for (int i = 0; i < tableModel.getRowCount(); ++i) {
                                if (DiffFileTable.this.editorCookies.get(tableModel.getNode(i).getFile()) == cookie) {
                                    tableModel.fireTableCellUpdated(i, 0);
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        };
        for (Map.Entry<File, EditorCookie> e : editorCookies.entrySet()) {
            EditorCookie editorCookie = e.getValue();
            if (editorCookie instanceof EditorCookie.Observable) {
                ((EditorCookie.Observable) editorCookie).addPropertyChangeListener(WeakListeners.propertyChange(changeListener, editorCookie));
            }
        }
    }

    protected JTable getDiffTable () {
        return super.getTable();
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

    private class DiffTableCellRenderer extends DefaultTableCellRenderer {
        
        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();

        @Override
        public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            DiffNode node = null;
            if (modelColumnIndex == 0) {
                node = tableModel.getNode(table.convertRowIndexToModel(row));
                String htmlDisplayName = DiffUtils.getHtmlDisplayName(node, isModified(node.getFile()), isSelected);
                if (GitModuleConfig.getDefault().isExcludedFromCommit(node.getFile().getAbsolutePath())) {
                    htmlDisplayName = "<s>" + (htmlDisplayName == null ? node.getFileNode().getName() : htmlDisplayName) + "</s>"; //NOI18N
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
                if (node == null) {
                    node = tableModel.getNode(table.convertRowIndexToModel(row));
                }
                String path = node.getFile().getAbsolutePath();
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }

        private boolean isModified (File file) {
            EditorCookie editorCookie = editorCookies.get(file);
            return (editorCookie != null) ? editorCookie.isModified() : false;
        }
    }
}
