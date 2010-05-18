/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;
import java.util.EventObject;
import javax.naming.ldap.LdapName;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.soa.ldap.LDAPAttributeValue;
import org.netbeans.modules.soa.ldap.LDAPConnection;
import org.netbeans.modules.soa.ldap.browser.UserAndGroupNameTransferable;
import org.netbeans.modules.soa.ldap.browser.fulltree.LDAPFolderNode;
import org.netbeans.modules.soa.ldap.browser.fulltree.LDAPLoadingNode;
import org.netbeans.modules.soa.ldap.browser.fulltree.LDAPTreeModel;
import org.netbeans.modules.soa.ldap.browser.searchtree.LDAPSearchTreeModel;
import org.netbeans.modules.soa.ldap.browser.searchtree.LDAPSearchTreeNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.RequestProcessor;

/**
 *
 * @author anjeleevich
 */
public class LDAPTreeView extends JPanel implements DragGestureListener {
    private EventListenerList selectionListenerList = new EventListenerList();

    private LDAPConnection connection;

    private JTree tree;
    private JScrollPane scrollPane;

    private SelectionHandler selectionHandler = new SelectionHandler();

    private RequestProcessor browseRequestProcessor = new RequestProcessor(
            "LDAP browse request processor", 3, true); // NOI18N
    private RequestProcessor searchRequestProcessor = new RequestProcessor(
            "LDAP search request processor", 3, true); // NOI18N

    private String filter = null;

    private LdapName selectedName = null;

    private String userNameAttribute;
    private String groupNameAttribute;

    private DragSource dragSource;
    private DragGestureRecognizer dragGestureRecognizer;

    private boolean showAttributesDialogOnDoubleClick = false;

    public LDAPTreeView(LDAPConnection connection) {
        this(connection, null, null);
    }

    public LDAPTreeView(LDAPConnection connection, String userNameAttribute,
            String groupNameAttribute) {
        this.connection = connection;

        this.userNameAttribute = userNameAttribute;
        this.groupNameAttribute = groupNameAttribute;

        tree = new JTree(EMPTY_TREE_MODEL);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel
                .SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new LDAPTreeCellRenderer());
        
        scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(null);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        dragSource = new DragSource();
        dragGestureRecognizer = dragSource.createDefaultDragGestureRecognizer(
                tree, DnDConstants.ACTION_COPY_OR_MOVE, this);

        selectionHandler.install();

//        tree.setTransferHandler(new LDAPTreeTransferHandler());
        tree.setDragEnabled(false);
    }

    public void setShowAttributesDialogOnDoubleClick(boolean value) {
        showAttributesDialogOnDoubleClick = value;
        tree.setToggleClickCount(0);
    }

    public String getSelectedUserNameAttributeValue() {
        TreePath treePath = tree.getSelectionPath();
        Object lastPathComponent = (treePath == null) ? null 
                : treePath.getLastPathComponent();

        if (lastPathComponent instanceof UserAndGroupNameHolder) {
            UserAndGroupNameHolder holder = (UserAndGroupNameHolder)
                    lastPathComponent;
            String value = holder.getUserNameAttributeValue();
            if (value != null && value.length() > 0) {
                return value;
            }
        }

        return null;
    }

    public String getSelectedGroupNameAttributeValue() {
        TreePath treePath = tree.getSelectionPath();
        Object lastPathComponent = (treePath == null) ? null
                : treePath.getLastPathComponent();

        if (lastPathComponent instanceof UserAndGroupNameHolder) {
            UserAndGroupNameHolder holder = (UserAndGroupNameHolder)
                    lastPathComponent;
            String value = holder.getGroupNameAttributeValue();
            if (value != null && value.length() > 0) {
                return value;
            }
        }

        return null;
    }

    private void showAttributesDialog() {
        if (!showAttributesDialogOnDoubleClick) {
            return;
        }

        if (selectedName == null) {
            return;
        }

        LDAPItemView itemView = new LDAPItemView(connection);
        itemView.setLDAPName(selectedName);
        itemView.setBorder(LDAPItemView.LINE_BORDER);

        DialogDescriptor descriptor = new DialogDescriptor(itemView, 
                selectedName.toString(), true, 
                new Object[] { DialogDescriptor.OK_OPTION },
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.dispose();

        // TODO implement
    }

    public void addLDAPTreeViewListener(Listener listener) {
        selectionListenerList.add(Listener.class, listener);
    }

    public void removeLDAPTreeViewListener(Listener listener) {
        selectionListenerList.remove(Listener.class, listener);
    }

    public LdapName getSelectedName() {
        return selectedName;
    }

    public void abortBackgroundTasks() {
        TreeModel model = tree.getModel();

        if (model instanceof LDAPTreeModel) {
            ((LDAPTreeModel) model).abortBackgroundTasks();
        } else if (model instanceof LDAPSearchTreeModel) {
            ((LDAPSearchTreeModel) model).abortSearch();
        }
    }

    private void updateSelection() {
        LdapName oldSelectedName = this.selectedName;
        LdapName newSelectedName = null;
        
        TreePath treePath = tree.getSelectionPath();

        if (treePath != null) {
            Object lastPathComponent = treePath.getLastPathComponent();
            if (lastPathComponent instanceof Named) {
                newSelectedName = ((Named) lastPathComponent).getLDAPName();
            }
        }

        boolean equalNames = (oldSelectedName == null)
                ? (newSelectedName == null)
                : oldSelectedName.equals(newSelectedName);
        
        if (!equalNames) {
            this.selectedName = newSelectedName;

            Listener[] listeners = selectionListenerList
                    .getListeners(Listener.class);
            if (listeners != null && listeners.length > 0) {
                Event event = new Event(this, selectedName);
                for (int i = listeners.length - 1; i >= 0; i--) {
                    listeners[i].ldapTreeViewSelectionChanged(event);
                }
            }
        }
    }

    public void setFilter(String filter) {
        if (filter == null) {
            filter = "";
        } else {
            filter = filter.trim();
        }

        TreeModel oldTreeModel = null;

        if (this.filter == null || !this.filter.equals(filter)) {
            this.filter = filter;

            if (filter.length() == 0) {
                LDAPTreeModel model = new LDAPTreeModel(connection,
                        userNameAttribute,
                        groupNameAttribute,
                        browseRequestProcessor);
                oldTreeModel = tree.getModel();
                tree.setModel(model);
            } else {
                LDAPSearchTreeModel model = new LDAPSearchTreeModel(connection,
                        filter, userNameAttribute, groupNameAttribute);
                oldTreeModel = tree.getModel();
                tree.setModel(model);

                model.startSearch(searchRequestProcessor);
            }
        }

        if (oldTreeModel instanceof LDAPSearchTreeModel) {
            ((LDAPSearchTreeModel) oldTreeModel).abortSearch();
        } else if (oldTreeModel instanceof LDAPTreeModel) {
            ((LDAPTreeModel) oldTreeModel).abortBackgroundTasks();
        }
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        Point point = dge.getDragOrigin();

        if (tree.getRowForLocation(point.x, point.y) < 0) {
            return;
        }

        Transferable transferable = null;

        String userName = getSelectedUserNameAttributeValue();
        String groupName = getSelectedGroupNameAttributeValue();

        if ((userName != null && userName.length() > 0)
                || (groupName != null && groupName.length() > 0))
        {
            transferable = new UserAndGroupNameTransferable(userName,
                    groupName);
        }

        if (transferable != null) {
            dge.startDrag(null, transferable);
        }
    }

    private class SelectionHandler extends AbstractAction implements
            TreeSelectionListener, MouseListener
    {
        private Timer timer;

        public SelectionHandler() {
            timer = new Timer(500, this);
            timer.setRepeats(false);
        }

        void install() {
            tree.addTreeSelectionListener(this);
            tree.addMouseListener(this);

            InputMap inputMap = tree.getInputMap();
            ActionMap actionMap = tree.getActionMap();

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                    "SelectionHandler"); // NOI18N
            actionMap.put("SelectionHandler", this);

            updateSelection();
        }

        void uninstall() {
            timer.stop();
            tree.removeMouseListener(this);
            tree.removeTreeSelectionListener(this);

            InputMap inputMap = tree.getInputMap();
            ActionMap actionMap = tree.getActionMap();

            inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            actionMap.remove("SelectionHandler"); // NOI18N
        }

        public void valueChanged(TreeSelectionEvent e) {
            timer.restart();
        }

        public void actionPerformed(ActionEvent e) {
            updateSelection();

            if (e.getSource() != timer) {
               showAttributesDialog();
            }
        }

        public void mousePressed(MouseEvent e) {
            int row = tree.getRowForLocation(e.getX(), e.getY());
            if (row < 0 || row >= tree.getRowCount()) {
                return;
            }

            if (tree.isRowSelected(row)) {
                return;
            }

            tree.setSelectionRow(row);
            updateSelection();
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
                showAttributesDialog();
            }
        }

        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    }

    private static class LDAPTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);

            if (!sel) {
                if (row == 0) {
                    setForeground(new Color(0x008800));
                } else if (value instanceof LDAPLoadingNode) {
                    setForeground(Color.GRAY);
                } else if (value instanceof LDAPFolderNode) {
                    setForeground(Color.BLUE);
                } else if (value instanceof LDAPSearchTreeNode) {
                    LDAPSearchTreeNode node = (LDAPSearchTreeNode) value;
                    if (!node.isSearchResult()) {
                        setForeground(Color.GRAY);
                    }
                }
            }

            if (value instanceof Iconed) {
                Icon icon = ((Iconed) value).getIcon();
                if (icon != null) {
                    setIcon(icon);
                } else {
                    setIcon((leaf) ? IconPool.LEAF_ICON : IconPool.FOLDER_ICON);
                }
            }

            return this;
        }
    }

    public static interface Listener extends EventListener {
        public void ldapTreeViewSelectionChanged(Event event);
    }

    public static class Event extends EventObject {
        private LdapName ldapName;

        public Event(LDAPTreeView treeView, LdapName ldapName) {
            super(treeView);
            this.ldapName = ldapName;
        }

        @Override
        public LDAPTreeView getSource() {
            return (LDAPTreeView) super.getSource();
        }

        public LdapName getLdapName() {
            return ldapName;
        }
    }

//    private class LDAPTreeTransferHandler extends TransferHandler {
//        @Override
//        protected Transferable createTransferable(JComponent c) {
//            String userName = getSelectedUserNameAttributeValue();
//            String groupName = getSelectedGroupNameAttributeValue();
//
//            if ((userName != null && userName.length() > 0)
//                    || (groupName != null && groupName.length() > 0))
//            {
//                return new UserAndGroupNameTransferable(userName,
//                        groupName);
//            }
//
//            return null;
//        }
//
//        @Override
//        public int getSourceActions(JComponent c) {
//            String userName = getSelectedUserNameAttributeValue();
//            String groupName = getSelectedGroupNameAttributeValue();
//
//            return ((userName != null) || (groupName != null))
//                    ? TransferHandler.MOVE
//                    : TransferHandler.MOVE;
//        }
//    }

    private static final TreeModel EMPTY_TREE_MODEL = new TreeModel() {
        public Object getRoot() { return null; }
        public Object getChild(Object parent, int index) { return null; }
        public int getChildCount(Object parent) { return 0; }
        public boolean isLeaf(Object node) { return true; }
        public void valueForPathChanged(TreePath path, Object newValue) {}
        public int getIndexOfChild(Object parent, Object child) { return 0; }
        public void addTreeModelListener(TreeModelListener l) {}
        public void removeTreeModelListener(TreeModelListener l) {}
    };
}
