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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.soa.ldap.LDAPAttributeValue;
import org.netbeans.modules.wlm.model.api.AssignmentHolder;
import org.netbeans.modules.wlm.model.api.Group;
import org.netbeans.modules.wlm.model.api.TAssignment;
import org.netbeans.modules.wlm.model.api.TEscalation;
import org.netbeans.modules.wlm.model.api.TExcluded;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.User;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.LinkButton;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.netbeans.modules.worklist.editor.designview.components.TitleLabel;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.AssignmentNode;
import org.netbeans.modules.worklist.editor.nodes.GroupNode;
import org.netbeans.modules.worklist.editor.nodes.GroupsNode;
import org.netbeans.modules.worklist.editor.nodes.UserNode;
import org.netbeans.modules.worklist.editor.nodes.UsersNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class AssignmentPanel extends DesignViewPanel implements Widget, 
        FocusListener, ListSelectionListener
{
    private AssignmentHolder assignmentHolder;
    
    private JPanel usersPanel;
    private JPanel groupsPanel;
    
    private JLabel usersTitle;
    private JLabel groupsTitle;
    
    private UsersTableModel usersModel;
    private GroupsTableModel groupsModel;
    
    private JTable usersTable;
    private JTable groupsTable;
    
    private JScrollPane usersScrollPane;
    private JScrollPane groupsScrollPane;
    
    private JComponent usersHeader;
    private JComponent groupsHeader;
    
    private LinkButton addUserButton;
    private LinkButton addExcludedUserButton;
    private LinkButton removeUserButton;
    
    private LinkButton addGroupButton;
    private LinkButton addExcludedGroupButton;
    private LinkButton removeGroupButton;
    
    private Action addUserAction;
    private Action addExcludedUserAction;
    private Action removeUserAction;
    
    private Action addGroupAction;
    private Action addExcludedGroupAction;
    private Action removeGroupAction;
    
    private TitledPanel titledPanel;

    private Widget widgetParent;

    private UserDropHandler userDropHandler;
    private GroupDropHandler groupDropHandler;

    public AssignmentPanel(Widget widgetParent, DesignView designView,
            AssignmentHolder assignmentHolder)
    {
        super(designView);

        ExUtils.setA11Y(this, "AssignmentPanel"); // NOI18N

        this.assignmentHolder = assignmentHolder;

        setBackground(TitledPanel.BACKGROUND_COLOR);
        setOpaque(true);
        setBorder(null);

        this.widgetParent = widgetParent;

        usersModel = new UsersTableModel();
        groupsModel = new GroupsTableModel();
        
        addUserAction = new AddUserAction();
        addExcludedUserAction = new AddExcludedUserAction();
        removeUserAction = new RemoveUserAction();
        
        addGroupAction = new AddGroupAction();
        addExcludedGroupAction = new AddExcludedGroupAction();
        removeGroupAction = new RemoveGroupAction();
        
        usersTitle = new TitleLabel(getMessage("LBL_USERS"), false); // NOI18N
        usersTitle.setBackground(TitledPanel.BACKGROUND_COLOR);
        ExUtils.setA11Y(usersTitle, AssignmentPanel.class, 
                "UsersTitle"); // NOI18N

        groupsTitle = new TitleLabel(getMessage("LBL_GROUPS"), false); // NOI18N
        groupsTitle.setBackground(TitledPanel.BACKGROUND_COLOR);
        ExUtils.setA11Y(groupsTitle, AssignmentPanel.class,
                "GroupsTitle"); // NOI18N
        
        addUserButton = new LinkButton(getMessage("LBL_ADD_USER")); // NOI18N
        addUserButton.addActionListener(addUserAction);
        addUserButton.addFocusListener(this);
        ExUtils.setA11Y(addUserButton, AssignmentPanel.class,
                "AddUserButton"); // NOI18N

        addExcludedUserButton = new LinkButton(getMessage("LBL_ADD_EXCLUDED_USER")); // NOI18N
        addExcludedUserButton.addActionListener(addExcludedUserAction);
        addExcludedUserButton.addFocusListener(this);
        ExUtils.setA11Y(addExcludedUserButton, AssignmentPanel.class,
                "AddExcludedUserButton"); // NOI18N
        
        removeUserButton = new LinkButton(getMessage("LBL_REMOVE_USER")); // NOI18N
        removeUserButton.addActionListener(removeUserAction);
        removeUserButton.addFocusListener(this);
        ExUtils.setA11Y(removeUserButton, AssignmentPanel.class,
                "RemoveUserButton"); // NOI18N
        
        addGroupButton = new LinkButton(getMessage("LBL_ADD_GROUP")); // NOI18N
        addGroupButton.addActionListener(addGroupAction);
        addGroupButton.addFocusListener(this);
        ExUtils.setA11Y(addGroupButton, AssignmentPanel.class,
                "AddGroupButton"); // NOI18N

        addExcludedGroupButton = new LinkButton(getMessage("LBL_ADD_EXCLUDED_GROUP")); // NOI18N
        addExcludedGroupButton.addActionListener(addExcludedGroupAction);
        addExcludedGroupButton.addFocusListener(this);
        ExUtils.setA11Y(addExcludedGroupButton, AssignmentPanel.class,
                "AddExcludedGroupButton"); // NOI18N
        
        removeGroupButton = new LinkButton(getMessage("LBL_REMOVE_GROUP")); // NOI18N
        removeGroupButton.addActionListener(removeGroupAction);
        removeGroupButton.addFocusListener(this);
        ExUtils.setA11Y(removeGroupButton, AssignmentPanel.class,
                "RemoveGroupButton"); // NOI18N
        
        usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBackground(TitledPanel.BACKGROUND_COLOR);
        ExUtils.setA11Y(usersPanel, AssignmentPanel.class,
                "UsersPanel"); // NOI18N
        
        groupsPanel = new JPanel(new BorderLayout());
        groupsPanel.setBackground(TitledPanel.BACKGROUND_COLOR);
        ExUtils.setA11Y(groupsPanel, AssignmentPanel.class,
                "GroupsPanel"); // NOI18N

        usersTable = new JTable(usersModel);
        usersTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        usersTable.setTableHeader(null);
        usersTable.setRowHeight(18);
        usersTable.setShowVerticalLines(false);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.addFocusListener(this);
        usersTable.getSelectionModel().addListSelectionListener(this);
        usersTable.getColumnModel().getColumn(0)
                .setCellRenderer(new CellRenderer());
        ExUtils.setA11Y(usersTable, AssignmentPanel.class,
                "UsersTable"); // NOI18N
        
        groupsTable = new JTable(groupsModel);
        groupsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        groupsTable.setTableHeader(null);
        groupsTable.setRowHeight(18);
        groupsTable.setShowVerticalLines(false);
        groupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupsTable.addFocusListener(this);
        groupsTable.getSelectionModel().addListSelectionListener(this);
        groupsTable.getColumnModel().getColumn(0)
                .setCellRenderer(new CellRenderer());
        ExUtils.setA11Y(groupsTable, AssignmentPanel.class,
                "GroupsTable"); // NOI18N

        usersScrollPane = new JScrollPane(usersTable);
        usersScrollPane.setBackground(Color.WHITE);
        usersScrollPane.getViewport().setBackground(Color.WHITE);
        ExUtils.setA11Y(usersScrollPane, AssignmentPanel.class,
                "UsersScrollPane"); // NOI18N

        groupsScrollPane = new JScrollPane(groupsTable);
        groupsScrollPane.setBackground(Color.WHITE);
        groupsScrollPane.getViewport().setBackground(Color.WHITE);
        ExUtils.setA11Y(groupsScrollPane, AssignmentPanel.class,
                "GroupsScrollPane"); // NOI18N
        
        usersHeader = Box.createHorizontalBox();
        usersHeader.setBackground(TitledPanel.BACKGROUND_COLOR);
        ExUtils.setA11Y(usersHeader, AssignmentPanel.class,
                "UsersHeader"); // NOI18N
        
        groupsHeader = Box.createHorizontalBox();
        groupsHeader.setOpaque(false);
        groupsHeader.setBackground(TitledPanel.BACKGROUND_COLOR);
        ExUtils.setA11Y(groupsHeader, AssignmentPanel.class,
                "GroupsHeader"); // NOI18N
        
        usersHeader.add(usersTitle);
        usersHeader.add(Box.createHorizontalGlue()).setFocusable(false);
        usersHeader.add(addUserButton);
        usersHeader.add(addExcludedUserButton);
        usersHeader.add(removeUserButton);

        groupsHeader.add(groupsTitle);
        groupsHeader.add(Box.createHorizontalGlue()).setFocusable(false);
        groupsHeader.add(addGroupButton);
        groupsHeader.add(addExcludedGroupButton);
        groupsHeader.add(removeGroupButton);

        usersPanel.add(usersHeader, BorderLayout.NORTH);
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);
        
        groupsPanel.add(groupsHeader, BorderLayout.NORTH);
        groupsPanel.add(groupsScrollPane, BorderLayout.CENTER);

        userDropHandler = new UserDropHandler();
        groupDropHandler = new GroupDropHandler();

        add(usersPanel);
        add(groupsPanel);
        
        processWLMModelChanged();
    }
    
    public JComponent getView() {
        if (titledPanel == null) {
            int userCount = 0;
            int groupCount = 0;
            
            TAssignment assignment = getAssignment();
            if (assignment != null) {
                List<User> users = assignment.getUsers();
                List<Group> groups = assignment.getGroups();
                
                if (users != null) {
                    userCount = users.size();
                }
                
                if (groups != null) {
                    groupCount = groups.size();
                }
            }
            
            titledPanel = new TitledPanel(getMessage("LBL_ASSIGNMENT"), // NOI18N
                    this, Integer.MAX_VALUE);
        }
        return titledPanel;
    }
    
    public void processWLMModelChanged() {
        usersModel.processWlmModelChanged();
        groupsModel.processWlmModelChanged();
    }
    
    private String getNewUserName() {
        String defaultName = "NewUser"; // NOI18N
        return defaultName;
    }
    
    private String getNewGroupName() {
        String defaultName = "NewGroup"; // NOI18N
        return defaultName;
    }

    private String getNewExcludedUserName() {
        String defaultName = "NewExcludedUser"; // NOI18N
        return defaultName;
    }

    private String getNewExcludedGroupName() {
        String defaultName = "NewExcludedGroup"; // NOI18N
        return defaultName;
    }

    
    private TAssignment getAssignment() {
        return getAssignment(false);
    }
    
    private TAssignment getAssignment(boolean create) {
        TAssignment assignment = null;
        
        if (assignmentHolder instanceof TTask) {
            TTask task = (TTask) assignmentHolder;
            assignment = task.getAssignment();
            
            if ((assignment == null) && create) {
                WLMModel model = getModel();
                assignment = model.getFactory().createAssignment(model);
                task.setAssignment(assignment);
            }
        } else if (assignmentHolder instanceof TEscalation) {
            TEscalation escalation = (TEscalation) assignmentHolder;
            assignment = escalation.getAssignment();

            if ((assignment == null) && create) {
                WLMModel model = getModel();
                assignment = model.getFactory().createAssignment(model);
                escalation.setAssignment(assignment);
            }
        }
        
        return assignment;
    }

    private TExcluded getExcluded(boolean create) {
        TAssignment assignment = getAssignment(create);
        if (assignment == null) {
            return null;
        }

        TExcluded excluded = assignment.getExcluded();
        if ((excluded == null) && create) {
            WLMModel model = getModel();
            excluded = model.getFactory().createExcluded(model);
            assignment.setExcluded(excluded);
        }

        return excluded;
    }

    @Override
    public Dimension getPreferredSize() {
        synchronized (getTreeLock()) {
            Dimension usersSize = usersHeader.getPreferredSize();
            Dimension groupsSize = groupsHeader.getPreferredSize();

            usersSize.height += 100;
            groupsSize.height += 100;

            int h = ExUtils.maxHeight(usersSize, groupsSize);
            int w = ExUtils.maxWidth(usersSize, groupsSize);

            Insets insets = getInsets();

            return new Dimension(insets.left + w + HGAP + w + insets.right,
                    insets.top + h + insets.bottom);
        }
    }
    
    @Override
    public Dimension getMaximumSize() {
        Dimension size = getPreferredSize();
        size.width = Integer.MAX_VALUE;
        return size;
    }

    @Override
    public void doLayout() {
        synchronized (getTreeLock()) {
            Insets insets = getInsets();

            int x1 = insets.left;
            int y = insets.top;

            int w = getWidth() - x1 - insets.right;
            int h = getHeight() - y - insets.bottom;

            int col1 = (w - HGAP + 1) / 2;
            int col2 = w - col1 - HGAP;

            int x2 = x1 + col1 + HGAP;

            usersPanel.setBounds(x1, y, col1, h);
            groupsPanel.setBounds(x2, y, col2, h);
        }
    }

    private void stopEditing() {
        if (usersTable.isEditing()) {
            TableCellEditor editor = usersTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
        
        if (groupsTable.isEditing()) {
            TableCellEditor editor = groupsTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
    }
    
    private static final int HGAP = 6;

    public Widget getWidget(int index) {
        if (index == 0) {
            return usersModel;
        }

        if (index == 1) {
            return groupsModel;
        }

        throw new IndexOutOfBoundsException();
    }

    public int getWidgetCount() {
        return 2;
    }

    public Node getWidgetNode() {
        return new AssignmentNode(assignmentHolder, Children.LEAF,
                getNodeLookup());
    }

    public void requestFocusToWidget() {
        if (assignmentHolder instanceof TTask) {
            getDesignView().showBasicPropertiesTab();
        } else {
            getDesignView().showEscalationsTab();
        }
    }

    public Widget getWidgetParent() {
        return widgetParent;
    }

    public WLMComponent getWidgetWLMComponent() {
        return assignmentHolder;
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.ASSIGNMENT;
    }


    public void focusGained(FocusEvent e) {
        Object source = e.getSource();
        if (source == addGroupButton
                || source == removeGroupButton
                || source == groupsPanel 
                || source == groupsTable 
                || source == groupsScrollPane 
                || source == groupsTitle)
        {
            if (source == groupsTable) {
                int selectedRow = groupsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectWidget(groupsModel.get(selectedRow));
                    return;
                }
            }
            selectWidget(groupsModel);
        } else if (source == addUserButton
                || source == removeUserButton
                || source == usersPanel
                || source == usersTable
                || source == usersScrollPane
                || source == usersTitle)
        {
            if (source == usersTable) {
                int selectedRow = usersTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectWidget(usersModel.get(selectedRow));
                    return;
                }
            }
            selectWidget(usersModel);
        } else {
            selectWidget(this);
        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    // ListSelectionListener
    public void valueChanged(ListSelectionEvent e) {
        if (usersTable.hasFocus() || usersTable.isEditing()) {
            int row = usersTable.getSelectedRow();
            if (row >= 0) {
                selectWidget(usersModel.get(row));
                return;
            }
        } else if (groupsTable.hasFocus() || groupsTable.isEditing()) {
            int row = groupsTable.getSelectedRow();
            if (row >= 0) {
                selectWidget(groupsModel.get(row));
                return;
            }
        }
    }
    
    private class ListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {
            super.getListCellRendererComponent(list, value, index, isSelected, 
                    cellHasFocus);

            if (!isSelected) {
                if (index % 2 == 0) {
                    setBackground(new Color(0xEEEEEE));
                } else {
                    setBackground(Color.WHITE);
                }
            }

            return this;
        }
    }
    
    private class UserCell implements Widget {
        private User userElement;
        private String userName;
        private boolean excluded;

        public UserCell(User userElement) {
            this(userElement, false);
        }

        public UserCell(User userElement, boolean excluded) {
            this.userElement = userElement;
            this.excluded = excluded;
            update();
        }
        
        public User getUserElement() {
            return userElement;
        }
        
        public String getUserName() {
            return userName;
        }
        
        public boolean isExcluded() {
            return excluded;
        }

        @Override
        public String toString() {
            return userName;
        }
        
        public void update() {
            userName = (userElement != null) ? userElement.getContent() : null;
            userName = TextFieldEditor.xPathToText(userName);
        }

        public Widget getWidgetParent() {
            return usersModel;
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new UserNode(userElement, Children.LEAF, getNodeLookup());
        }

        public void requestFocusToWidget() {
            if (assignmentHolder instanceof TTask) {
                getDesignView().showBasicPropertiesTab();
            } else {
                getDesignView().showEscalationsTab();
            }

            int index = usersModel.includedUserCellList.indexOf(this);

            if (index >= 0) {
                usersTable.getSelectionModel().setSelectionInterval(index,
                        index);
            }

            usersTable.requestFocusInWindow();
        }

        public WLMComponent getWidgetWLMComponent() {
            return userElement;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.USER;
        }
    }
    
    private class GroupCell implements Widget {
        private Group groupElement;
        private String groupName;
        private boolean excluded;

        public GroupCell(Group groupElement) {
            this(groupElement, false);
        }

        public GroupCell(Group groupElement, boolean excluded) {
            this.groupElement = groupElement;
            this.excluded = excluded;
            update();
        }
        
        public Group getGroupElement() {
            return groupElement;
        }
        
        public String getGroupName() {
            return groupName;
        }

        public boolean isExcluded() {
            return excluded;
        }
        
        @Override
        public String toString() {
            return groupName;
        }
        
        public void update() {
            groupName = (groupElement != null) ? groupElement.getContent() : null;
            groupName = TextFieldEditor.xPathToText(groupName);
        }

        public Widget getWidgetParent() {
            return groupsModel;
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new GroupNode(groupElement, Children.LEAF, getNodeLookup());
        }

        public void requestFocusToWidget() {
            if (assignmentHolder instanceof TTask) {
                getDesignView().showBasicPropertiesTab();
            } else {
                getDesignView().showEscalationsTab();
            }

            int index = groupsModel.includedGroupCellList.indexOf(this);

            if (index >= 0) {
                groupsTable.getSelectionModel().setSelectionInterval(index,
                        index);
            }

            groupsTable.requestFocusInWindow();
        }

        public WLMComponent getWidgetWLMComponent() {
            return groupElement;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.GROUP;
        }
    }
    
    private class UsersTableModel extends AbstractTableModel implements Widget {
        private List<UserCell> includedUserCellList;
        private List<UserCell> excludedUserCellList;
        
        public UsersTableModel() {
            includedUserCellList = new ArrayList<UserCell>();
            excludedUserCellList = new ArrayList<UserCell>();
        }
        
        public int getRowCount() {
            return includedUserCellList.size() + excludedUserCellList.size();
        }

        public int getColumnCount() {
            return 1;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            return get(rowIndex).toString();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String value = TextFieldEditor.textToXPath(aValue.toString());

            if (rowIndex >= getRowCount()) {
                return;
            }

            UserCell userCell = usersModel.get(rowIndex);
            if (userCell.getUserName().equals(value)) {
                return;
            }
            
            WLMModel model = getModel();
            
            if (model.startTransaction()) {
                try {
                    User user = userCell.getUserElement();
                    user.setContent(value);
                } finally {
                    model.endTransaction();
                }
                
                userCell.update();
                usersModel.fireTableCellUpdated(rowIndex, columnIndex);
            } 
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
        
        public UserCell get(int row) {
            int size = includedUserCellList.size();
            return (row < size)
                    ? includedUserCellList.get(row)
                    : excludedUserCellList.get(row - size);

        }
        
        void processWlmModelChanged() {
            TAssignment assignment = getAssignment();
            TExcluded excluded = getExcluded(false);
            
            List<User> includedUsersList = (assignment == null) ? null
                    : assignment.getUsers();
            List<User> excludedUsersList = (excluded == null) ? null
                    : excluded.getUsers();


            if (includedUsersList == null) {
                includedUsersList = EMPTY_USER_LIST;
            }

            if (excludedUsersList == null) {
                excludedUsersList = EMPTY_USER_LIST;
            }

            Set<User> includedUsersSet = new HashSet<User>(includedUsersList);
            Set<User> excludedUsersSet = new HashSet<User>(excludedUsersList);

            for (int i = includedUserCellList.size() - 1; i >= 0; i--) {
                UserCell userCell = includedUserCellList.get(i);
                User user = userCell.getUserElement();

                if (includedUsersSet.remove(user)) {
                    userCell.update();
                    fireTableRowsUpdated(i, i);
                } else {
                    includedUserCellList.remove(i);
                    fireTableRowsDeleted(i, i);
                }
            }

            // Now included users set contains only new users

            int index = 0;
            for (User user : includedUsersList) {
                if (includedUsersSet.remove(user)) {
                    includedUserCellList.add(index, new UserCell(user));
                    fireTableRowsInserted(index, index);
                }
                index++;
            }

            int size = includedUserCellList.size();
            for (int i = excludedUserCellList.size() - 1; i >= 0; i--) {
                UserCell userCell = excludedUserCellList.get(i);
                User user = userCell.getUserElement();

                int j = i + size;

                if (excludedUsersSet.remove(user)) {
                    userCell.update();
                    fireTableRowsUpdated(j, j);
                } else {
                    excludedUserCellList.remove(i);
                    fireTableRowsDeleted(j, j);
                }
            }

            index = 0;
            for (User user : excludedUsersList) {
                if (excludedUsersSet.remove(user)) {
                    excludedUserCellList.add(index, new UserCell(user, true));
                    fireTableRowsInserted(index + size, index + size);
                }
                index++;
            }
        }

        public Widget getWidgetParent() {
            return AssignmentPanel.this;
        }

        public Widget getWidget(int index) {
            return get(index);
        }

        public int getWidgetCount() {
            return getRowCount();
        }

        public Node getWidgetNode() {
            return new UsersNode(assignmentHolder, Children.LEAF, getNodeLookup());
        }

        public void requestFocusToWidget() {
            if (assignmentHolder instanceof TTask) {
                getDesignView().showBasicPropertiesTab();
            } else {
                getDesignView().showEscalationsTab();
            }
        }

        public WLMComponent getWidgetWLMComponent() {
            return assignmentHolder;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.USERS;
        }
    }
    
    private class GroupsTableModel extends AbstractTableModel 
            implements Widget
    {
        private List<GroupCell> includedGroupCellList;
        private List<GroupCell> excludedGroupCellList;
        
        public GroupsTableModel() {
            includedGroupCellList = new ArrayList<GroupCell>();
            excludedGroupCellList = new ArrayList<GroupCell>();
        }
        
        public int getRowCount() {
            return includedGroupCellList.size() + excludedGroupCellList.size();
        }

        public int getColumnCount() {
            return 1;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            return get(rowIndex).toString();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String value = TextFieldEditor.textToXPath(aValue.toString());

            if (rowIndex >= getRowCount()) {
                return;
            }

            GroupCell groupCell = get(rowIndex);
            if (groupCell.getGroupName().equals(value)) {
                return;
            }
            
            WLMModel model = getModel();
            
            if (model.startTransaction()) {
                try {
                    Group group = groupCell.getGroupElement();
                    group.setContent(value);
                } finally {
                    model.endTransaction();
                }
                
                groupCell.update();
                groupsModel.fireTableCellUpdated(rowIndex, columnIndex);
            } 
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
        
        public GroupCell get(int row) {
            int size = includedGroupCellList.size();
            return (row < size) 
                    ? includedGroupCellList.get(row)
                    : excludedGroupCellList.get(row - size);
        }
        
        void processWlmModelChanged() {
            TAssignment assignment = getAssignment();
            TExcluded excluded = getExcluded(false);
            
            List<Group> includedGroupsList = (assignment == null) ? null :
                    assignment.getGroups();
            List<Group> excludedGroupsList = (excluded == null) ? null :
                    excluded.getGroups();

            if (includedGroupsList == null) {
                includedGroupsList = EMPTY_GROUP_LIST;
            }

            if (excludedGroupsList == null) {
                excludedGroupsList = EMPTY_GROUP_LIST;
            }

            Set<Group> includedGroupsSet = new HashSet<Group>(
                    includedGroupsList);
            Set<Group> excludedGroupsSet = new HashSet<Group>(
                    excludedGroupsList);

            for (int i = includedGroupCellList.size() - 1; i >= 0; i--) {
                GroupCell groupCell = includedGroupCellList.get(i);
                Group group = groupCell.getGroupElement();

                if (includedGroupsSet.remove(group)) {
                    groupCell.update();
                    fireTableRowsUpdated(i, i);
                } else {
                    includedGroupCellList.remove(i);
                    fireTableRowsDeleted(i, i);
                }
            }

            int index = 0;
            for (Group group : includedGroupsList) {
                if (includedGroupsSet.remove(group)) {
                    includedGroupCellList.add(index, new GroupCell(group));
                    fireTableRowsInserted(index, index);
                }
                index++;
            }

            int size = includedGroupCellList.size();

            for (int i = excludedGroupCellList.size() - 1; i >= 0; i--) {
                GroupCell groupCell = excludedGroupCellList.get(i);
                Group group = groupCell.getGroupElement();

                int j = i + size;

                if (excludedGroupsSet.remove(group)) {
                    groupCell.update();
                    fireTableRowsUpdated(j, j);
                } else {
                    excludedGroupCellList.remove(i);
                    fireTableRowsDeleted(j, j);
                }
            }

            index = 0;
            for (Group group : excludedGroupsList) {
                if (excludedGroupsSet.remove(group)) {
                    excludedGroupCellList.add(index,
                            new GroupCell(group, true));
                    fireTableRowsInserted(index + size, index + size);
                }
                index++;
            }
        }

        public Widget getWidgetParent() {
            return AssignmentPanel.this;
        }

        public Widget getWidget(int index) {
            return get(index);
        }

        public int getWidgetCount() {
            return getRowCount();
        }

        public Node getWidgetNode() {
            return new GroupsNode(assignmentHolder, Children.LEAF, getNodeLookup());
        }

        public void requestFocusToWidget() {
            if (assignmentHolder instanceof TTask) {
                getDesignView().showBasicPropertiesTab();
            } else {
                getDesignView().showEscalationsTab();
            }
        }

        public WLMComponent getWidgetWLMComponent() {
            return assignmentHolder;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.GROUPS;
        }
    }    
    
    
    private class AddUserAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            stopEditing();
            
            WLMModel model = getModel();

            if (model.startTransaction()) {
                User user = null;
                
                try {
                    TAssignment assignment = getAssignment(true);
                    
                    user = model.getFactory().createUser(model);
                    user.setContent(TextFieldEditor
                            .textToXPath(getNewUserName()));
                    
                    assignment.addUser(user);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }

    private class AddExcludedUserAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            stopEditing();

            WLMModel model = getModel();

            if (model.startTransaction()) {
                User user = null;

                try {
                    TExcluded excluded = getExcluded(true);

                    user = model.getFactory().createUser(model);
                    user.setContent(TextFieldEditor
                            .textToXPath(getNewExcludedUserName()));

                    excluded.addUser(user);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }

    private class RemoveUserAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            stopEditing();
            
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }
            
            if (selectedRow >= usersModel.getRowCount()) {
                return;
            } 
            
            UserCell userCell = usersModel.get(selectedRow);
            
            User user = userCell.getUserElement();
               
            WLMModel model = getModel();

            if (userCell.isExcluded()) {
                TExcluded excluded = getExcluded(false);
                if (excluded == null) {
                    return;
                }
                if (model.startTransaction()) {
                    try {
                        excluded.removeUser(user);
                    } finally {
                        model.endTransaction();
                    }
                }
            } else {
                TAssignment assignment = getAssignment();
                if (assignment == null) {
                    return;
                }
                if (model.startTransaction()) {
                    try {
                        assignment.removeUser(user);
                    } finally {
                        model.endTransaction();
                    }
                }
            }
        }
    }
    
    private class AddGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            stopEditing();
            
            WLMModel model = getModel();
            
            if (model.startTransaction()) {
                Group group = null;
                
                try {
                    TAssignment assignment = getAssignment(true);
                    
                    group = model.getFactory().createGroup(model);
                    group.setContent(TextFieldEditor
                            .textToXPath(getNewGroupName()));
                    
                    assignment.addGroup(group);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }

    private class AddExcludedGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            stopEditing();

            WLMModel model = getModel();

            if (model.startTransaction()) {
                Group group = null;

                try {
                    TExcluded excluded = getExcluded(true);

                    group = model.getFactory().createGroup(model);
                    group.setContent(TextFieldEditor
                            .textToXPath(getNewExcludedGroupName()));

                    excluded.addGroup(group);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }

    private class RemoveGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            stopEditing();
            
            int selectedRow = groupsTable.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }
            
            if (selectedRow >= groupsModel.getRowCount()) {
                return;
            } 
            
            GroupCell groupCell = groupsModel.get(selectedRow);
            
            Group group = groupCell.getGroupElement();
               
            WLMModel model = getModel();
            if (groupCell.isExcluded()) {
                TExcluded excluded = getExcluded(false);
                if (excluded == null) {
                    return;
                }
                if (model.startTransaction()) {
                    try {
                        excluded.removeGroup(group);
                    } finally {
                        model.endTransaction();
                    }
                }
            } else {
                TAssignment assignment = getAssignment();
                if (assignment == null) {
                    return;
                }
                if (model.startTransaction()) {
                    try {
                        assignment.removeGroup(group);
                    } finally {
                        model.endTransaction();
                    }
                }
            }
        }
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(AssignmentPanel.class, key);
    }

    private abstract class AbstractDropHandler implements DropTargetListener {
        private boolean canImport;
        private List<DropTarget> dropTargets = new LinkedList<DropTarget>();

        protected void registerComponent(JComponent component) {
            dropTargets.add(new DropTarget(component, this));
        }

        private boolean actionSupported(int action) {
            return (action & (TransferHandler.COPY_OR_MOVE
                    | DnDConstants.ACTION_LINK)) != TransferHandler.NONE;
        }

        public void dragEnter(DropTargetDragEvent e) {
	    DataFlavor[] flavors = e.getCurrentDataFlavors();

            canImport = canImport(flavors);

            int dropAction = e.getDropAction();

            if (canImport && actionSupported(dropAction)) {
		e.acceptDrag(dropAction);
	    } else {
		e.rejectDrag();
	    }
	}

        public void dragOver(DropTargetDragEvent e) {
            int dropAction = e.getDropAction();

            if (canImport && actionSupported(dropAction)) {
                e.acceptDrag(dropAction);
            } else {
                e.rejectDrag();
            }
	}

        public void dragExit(DropTargetEvent e) {
	}

        public void drop(DropTargetDropEvent e) {
            int dropAction = e.getDropAction();

	    if (canImport && actionSupported(dropAction)) {
		e.acceptDrop(dropAction);

                try {
                    Transferable t = e.getTransferable();
		    e.dropComplete(importData(t, isCreateExcluded(dropAction)));
                } catch (RuntimeException re) {
                    e.dropComplete(false);
                }
	    } else {
		e.rejectDrop();
	    }
	}

        public void dropActionChanged(DropTargetDragEvent e) {
            int dropAction = e.getDropAction();

            if (canImport && actionSupported(dropAction)) {
                e.acceptDrag(dropAction);
            } else {
                e.rejectDrag();
            }
	}

        protected boolean isCreateExcluded(int dropAction) {
            return (dropAction != TransferHandler.MOVE);
        }

        protected abstract boolean canImport(DataFlavor[] flavors);
        protected abstract boolean importData(Transferable t,
                boolean createExcluded);
    }

    private class UserDropHandler extends AbstractDropHandler {
        public UserDropHandler() {
            registerComponent(usersScrollPane);
            registerComponent(usersTable);
        }

        @Override
        public boolean canImport(DataFlavor[] transferFlavors) {
            if (transferFlavors != null) {
                for (DataFlavor dataFlavor : transferFlavors) {
                    if (dataFlavor == LDAPAttributeValue
                            .USER_NAME_FLAVOR)
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean importData(Transferable t, boolean createExcluded) {
            Object object = null;

            try {
                object = t.getTransferData(LDAPAttributeValue.USER_NAME_FLAVOR);
            } catch (UnsupportedFlavorException ex) {
                // do nothing
            } catch (IOException ex) {
                // do nothing
            }

            String userName = (object instanceof LDAPAttributeValue)
                    ? ((LDAPAttributeValue) object).getValue() : null;

            if (userName != null) {
                WLMModel model = getModel();

                if (model.startTransaction()) {
                    User user = null;

                    try {
                        user = model.getFactory().createUser(model);
                        user.setContent(wrapWithQuotes(userName));

                        if (createExcluded) {
                            TExcluded excluded = getExcluded(true);
                            excluded.addUser(user);
                        } else {
                            TAssignment assignment = getAssignment(true);
                            assignment.addUser(user);
                        }
                    } finally {
                        model.endTransaction();
                    }
                }
            }

            return true;
        }
    }

    private class GroupDropHandler extends AbstractDropHandler {
        public GroupDropHandler() {
            registerComponent(groupsScrollPane);
            registerComponent(groupsTable);
        }
  
        @Override
        public boolean canImport(DataFlavor[] transferFlavors) {
            if (transferFlavors != null) {
                for (DataFlavor dataFlavor : transferFlavors) {
                    if (dataFlavor == LDAPAttributeValue
                            .GROUP_NAME_FLAVOR)
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean importData(Transferable t, boolean createExcluded) {
            Object object = null;

            try {
                object = t.getTransferData(LDAPAttributeValue
                        .GROUP_NAME_FLAVOR);
            } catch (UnsupportedFlavorException ex) {
                // do nothing
            } catch (IOException ex) {
                // do nothing
            }

            String groupName = (object instanceof LDAPAttributeValue)
                    ? ((LDAPAttributeValue) object).getValue() : null;

            if (groupName != null) {
                WLMModel model = getModel();

                if (model.startTransaction()) {
                    Group group = null;
                    try {
                        group = model.getFactory().createGroup(model);
                        group.setContent(wrapWithQuotes(groupName));

                        if (createExcluded) {
                            TExcluded excluded = getExcluded(true);
                            excluded.addGroup(group);
                        } else {
                            TAssignment assignment = getAssignment(true);
                            assignment.addGroup(group);
                        }
                    } finally {
                        model.endTransaction();
                    }
                }
            }

            return true;
        }
    }

    private class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            boolean strikenOut = false;
            if (table == usersTable) {
                strikenOut = usersModel.get(row).isExcluded();
            } else if (table == groupsTable) {
                strikenOut = groupsModel.get(row).isExcluded();
            }

            if (strikenOut) {
                setText("<html><body><s>" + getText() // NOI18N
                        + "</s></body></html>"); // NOI18N
            }

            return this;
        }
    }

    private static String wrapWithQuotes(String value) {
        return ((value.indexOf('\'') > 0))
                ? "\"" + value + "\""  // NOI18N
                : "\'" + value + "\'"; // NOI18N
    }

    private static final List<User> EMPTY_USER_LIST 
            = new AbstractList<User>()
    {
        @Override
        public User get(int index) {
            throw new IndexOutOfBoundsException("List is empty");
        }

        @Override
        public int size() {
            return 0;
        }
    };

    private static final List<Group> EMPTY_GROUP_LIST 
            = new AbstractList<Group>()
    {
        @Override
        public Group get(int index) {
            throw new IndexOutOfBoundsException("List is empty");
        }

        @Override
        public int size() {
            return 0;
        }
    };
}
