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

import org.netbeans.modules.git.ui.status.VersioningPanelController.ModeKeeper;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
import org.netbeans.modules.versioning.util.status.VCSStatusTable;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.EnumSet;
import javax.swing.Action;
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
import org.netbeans.modules.git.ui.diff.DiffAction;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.status.VCSStatusNode;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Controls the {@link #getComponent() tsble} that displays nodes
 * in the Versioning view. The table is  {@link #setTableModel populated)
 * from VersioningPanel.
 * 
 * @author Maros Sandor
 */
class GitStatusTable extends VCSStatusTable<GitStatusNode> {
    private final ModeKeeper modeKeeper;

    public GitStatusTable (VCSStatusTableModel<GitStatusNode> model, VersioningPanelController.ModeKeeper modeKeeper) {
        super(model);
        this.modeKeeper = modeKeeper;
        setDefaultRenderer(new SyncTableCellRenderer());
    }

    @Override
    protected void setModelProperties () {
        Node.Property [] properties = new Node.Property[3];
        properties[0] = new ColumnDescriptor<String>(GitStatusNode.NameProperty.NAME, String.class, GitStatusNode.NameProperty.DISPLAY_NAME, GitStatusNode.NameProperty.DESCRIPTION);
        properties[1] = new ColumnDescriptor<String>(GitStatusNode.GitStatusProperty.NAME, String.class, GitStatusNode.GitStatusProperty.DISPLAY_NAME, GitStatusNode.GitStatusProperty.DESCRIPTION);
        properties[2] = new ColumnDescriptor<String>(GitStatusNode.PathProperty.NAME, String.class, GitStatusNode.PathProperty.DISPLAY_NAME, GitStatusNode.PathProperty.DESCRIPTION);
        tableModel.setProperties(properties);
    }
        
    @Override
    protected JPopupMenu getPopup () {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        item = menu.add(new OpenInEditorAction(getSelectedFiles()));
        Mnemonics.setLocalizedText(item, item.getText());
        menu.addSeparator();
        item = menu.add(new SystemActionBridge(SystemAction.get(CommitAction.class), NbBundle.getMessage(CommitAction.class, "LBL_CommitAction.popupName"))); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(DiffAction.class), NbBundle.getMessage(DiffAction.class, "LBL_DiffAction_PopupName")) { //NOI18N
            @Override
            public void actionPerformed (ActionEvent e) {
                modeKeeper.storeMode();
                super.actionPerformed(e);
            }
        });
        Mnemonics.setLocalizedText(item, item.getText());

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
    protected void mouseClicked (VCSStatusNode node) {
        Action action = node.getPreferredAction();
        if (action != null && action.isEnabled()) {
            if (action instanceof DiffAction) {
                modeKeeper.storeMode();
            }
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, node.getFile().getAbsolutePath()));
        }
    }

    private class SyncTableCellRenderer extends DefaultTableCellRenderer {
        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();

        @Override
        public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            GitStatusNode node = null;
            if (modelColumnIndex == 0) {
                node = tableModel.getNode(table.convertRowIndexToModel(row));
                if (!isSelected) {
                    value = node.getHtmlDisplayName();
                }
                if (GitModuleConfig.getDefault().isExcludedFromCommit(node.getFile().getAbsolutePath())) {
                    value = "<s>" + value + "</s>"; //NOI18N
                }
                value = "<html>" + value; // NOI18N
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
