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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.wlm.model.api.LocalNotificationsHolder;
import org.netbeans.modules.wlm.model.api.TEscalation;
import org.netbeans.modules.wlm.model.api.TLocalNotification;
import org.netbeans.modules.wlm.model.api.TNotification;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMReference;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.LinkButton;
import org.netbeans.modules.worklist.editor.designview.components.TitleLabel;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.LocalNotificationNode;
import org.netbeans.modules.worklist.editor.nodes.LocalNotificationsNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class LocalNotificationsPanel extends DesignViewPanel implements 
        Widget, FocusListener, ListSelectionListener
{
    private LocalNotificationsHolder localNotificationsHolder;

    private TitleLabel notificationsTitle;
    private JComponent notificationsHeader;
    
    private LocalNotificationTableModel notificationTableModel;
    
    private JTable notificationsTable;
    private JScrollPane notificationsScrollPane;
    
    private AddNotificationAction addNotificationAction;
    private RemoveNotificationAction removeNotificationAction;
    private Widget widgetParent;

    private JButton addNotificationButton;
    private JButton removeNotificationButton;

    public LocalNotificationsPanel(Widget widgetParent, DesignView designView,
            LocalNotificationsHolder localNotificationsHolder) 
    {
        super(designView);

        ExUtils.setA11Y(this, "LocalNotificationPanel"); // NOI18N

        this.widgetParent = widgetParent;
        this.localNotificationsHolder = localNotificationsHolder;
        
        setBorder(null);
        setBackground(TitledPanel.BACKGROUND_COLOR);
        setOpaque(true);
        setLayout(new BorderLayout());

        addNotificationAction = new AddNotificationAction();
        removeNotificationAction = new RemoveNotificationAction();
        
        notificationsTitle = new TitleLabel(getMessage(
                "LBL_LOCAL_NOTIFICATIONS"), false); // NOI18N
        notificationsTitle.setBackground(TitledPanel.BACKGROUND_COLOR);
        ExUtils.setA11Y(notificationsTitle, LocalNotificationsPanel.class,
                "LocalNotificationTitle"); // NOI18N

        addNotificationButton = new LinkButton(addNotificationAction);
        addNotificationButton.addFocusListener(this);
        ExUtils.setA11Y(addNotificationButton, LocalNotificationsPanel.class,
                "AddLocalNotificationButton"); // NOI18N

        removeNotificationButton = new LinkButton(removeNotificationAction);
        removeNotificationButton.addFocusListener(this);
        ExUtils.setA11Y(removeNotificationButton, LocalNotificationsPanel.class,
                "RemoveLocalNotificationButton"); // NOI18N

        notificationsHeader = Box.createHorizontalBox();
        notificationsHeader.add(notificationsTitle);
        notificationsHeader.add(Box.createHorizontalGlue()).setFocusable(false);
        notificationsHeader.add(addNotificationButton);
        notificationsHeader.add(removeNotificationButton);
        ExUtils.setA11Y(notificationsHeader, LocalNotificationsPanel.class,
                "LocalNotificationsTableHeader"); // NOI18N
                
        notificationTableModel = new LocalNotificationTableModel();
        
        notificationsTable = new JTable(notificationTableModel);
        notificationsTable.setTableHeader(null);
        notificationsTable.setRowHeight(20);
        notificationsTable.putClientProperty(
                "terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        notificationsTable.addFocusListener(this);
        notificationsTable.getSelectionModel().addListSelectionListener(this);
        ExUtils.setA11Y(notificationsTable, LocalNotificationsPanel.class,
                "LocalNotificationsTable"); // NOI18N
        
        TableColumnModel columnModel = notificationsTable.getColumnModel();
        TableColumn typeColumn = columnModel.getColumn(0);
        typeColumn.setCellEditor(new NotificationCellEditor());
        
        notificationsScrollPane = new JScrollPane(notificationsTable);
        notificationsScrollPane.getViewport().setBackground(Color.WHITE);
        ExUtils.setA11Y(notificationsScrollPane, LocalNotificationsPanel.class,
                "LocalNotificationsScrollPane"); // NOI18N
        
        add(notificationsHeader, BorderLayout.NORTH);
        add(notificationsScrollPane, BorderLayout.CENTER);
        
        processWLMModelChanged();
    }
    
    private void stopEditing() {
        if (notificationsTable.isEditing()) {
            TableCellEditor editor = notificationsTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
    }    
    
    void processWLMModelChanged() {
        notificationTableModel.processWlmModelChanged();
    }
    
    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        Dimension size = notificationsHeader.getPreferredSize();
        size.height += 55;

        size.width += insets.left + insets.right;
        size.height += insets.top + insets.bottom;
        
        return size;
    }

    public Widget getWidgetParent() {
        return widgetParent;
    }

    public Widget getWidget(int index) {
        return notificationTableModel.get(index);
    }

    public int getWidgetCount() {
        return notificationTableModel.getRowCount();
    }

    public Node getWidgetNode() {
        return new LocalNotificationsNode(localNotificationsHolder,
                Children.LEAF, getNodeLookup());
    }

    public WLMComponent getWidgetWLMComponent() {
        return localNotificationsHolder;
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.LOCAL_NOTIFICATIONS;
    }

    public void requestFocusToWidget() {
        if (localNotificationsHolder instanceof TEscalation) {
            getDesignView().showEscalationsTab();
        } else {
            getDesignView().showActionsTab();
        }
        scrollRectToVisible(new Rectangle(getSize()));
    }

    public void focusGained(FocusEvent e) {
        selectWidget(widgetParent);
    }

    public void focusLost(FocusEvent e) {
        Object source = e.getSource();
        if (source == addNotificationButton 
                || source == removeNotificationButton
                || source == notificationsScrollPane)
        {
            selectWidget(this);
        } else {
            int row = notificationsTable.getSelectedRow();
            if (row >= 0) {
                selectWidget(notificationTableModel.get(row));
            } else {
                selectWidget(this);
            }
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (notificationsTable.hasFocus() || notificationsTable.isEditing()) {
            int row = notificationsTable.getSelectedRow();
            if (row >= 0) {
                selectWidget(notificationTableModel.get(row));
            } else {
                selectWidget(this);
            }
        } else {
            selectWidget(this);
        }
    }

    private class AddNotificationAction extends AbstractAction {
        AddNotificationAction() {
            super(getMessage("LBL_ADD_LOCAL_NOTIFICATION")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            stopEditing();
            
            WLMModel model = getModel();

            if (model.startTransaction()) {
                try {
                    TLocalNotification localNotification = model.getFactory()
                            .createLocalNotification(model);
                    
                    localNotificationsHolder.addLocalNotification(
                            localNotification);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }
    
    private class RemoveNotificationAction extends AbstractAction {
        RemoveNotificationAction() {
            super(getMessage("LBL_REMOVE_LOCAL_NOTIFICATION")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            stopEditing();
            
            int selectedRow = notificationsTable.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }
            
            if (selectedRow >= notificationTableModel.getRowCount()) {
                return;
            } 
            
            LocalNotificationCell localNotificationCell 
                    = notificationTableModel.get(selectedRow);
            
            TLocalNotification localNotification = localNotificationCell
                    .getLocalNotificationElement();
               
            WLMModel model = getModel();
            
            if (model.startTransaction()) {
                try {
                    localNotificationsHolder.removeLocalNotification(
                            localNotification);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }
    
    private class LocalNotificationCell implements Widget {
        private TLocalNotification localNotificationElement;
        private String notificationRefString;
        
        public LocalNotificationCell(TLocalNotification 
                localNotificationElement) 
        {
            this.localNotificationElement = localNotificationElement;
            update();
        }
        
        public TLocalNotification getLocalNotificationElement() {
            return localNotificationElement;
        }
        
        public String getUserName() {
            return notificationRefString;
        }
        
        @Override
        public String toString() {
            return notificationRefString;
        }
        
        public void update() {
            WLMReference<TNotification> notificationRef 
                    = localNotificationElement.getNotification();
            notificationRefString = (notificationRef == null) ? null
                    : notificationRef.getRefString();
            
            if (notificationRefString == null) {
                notificationRefString = ""; // NOI18N
            } else {
                notificationRefString = notificationRefString.trim();
            }
        }

        public Widget getWidgetParent() {
            return LocalNotificationsPanel.this;
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new LocalNotificationNode(localNotificationElement,
                    Children.LEAF, getNodeLookup());
        }

        public void requestFocusToWidget() {
            if (localNotificationsHolder instanceof TEscalation) {
                getDesignView().showEscalationsTab();
            } else {
                getDesignView().showActionsTab();
            }

            int index = notificationTableModel.localNoticationCellList
                    .indexOf(this);

            if (index >= 0) {
                notificationsTable.getSelectionModel()
                        .setSelectionInterval(index, index);
            }

            notificationsTable.requestFocusInWindow();
        }

        public WLMComponent getWidgetWLMComponent() {
            return localNotificationElement;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.LOCAL_NOTIFICATION;
        }
    }

    private class LocalNotificationTableModel extends AbstractTableModel {
        private List<LocalNotificationCell> localNoticationCellList;
        
        public LocalNotificationTableModel() {
            localNoticationCellList = new ArrayList<LocalNotificationCell>();
        }
        
        public int getRowCount() {
            return localNoticationCellList.size();
        }

        public int getColumnCount() {
            return 1;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            return localNoticationCellList.get(rowIndex).toString();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex >= getRowCount()) {
                return;
            }

            LocalNotificationCell localNotificationCell = get(rowIndex);
            NewCellValue value = (NewCellValue) aValue;
            WLMModel model = getModel();
            TLocalNotification localNotification = localNotificationCell
                    .getLocalNotificationElement();

            if (model.startTransaction()) {
                try {
                    if (value == null) {
                        localNotification.removeNotification();
                    } else {
                        localNotification.setNotification(localNotification
                                .createNotificationReferences(value
                                .getNotification()));
                    }
                } finally {
                    model.endTransaction();
                }
                fireTableCellUpdated(rowIndex, columnIndex);
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
        
        public void clear() {
            localNoticationCellList.clear();
        }
        
        public LocalNotificationCell get(int row) {
            return localNoticationCellList.get(row);
        }
        
        public void add(LocalNotificationCell userCell) {
            localNoticationCellList.add(userCell);
        }        
        
        public void remove(LocalNotificationCell userCell) {
            localNoticationCellList.remove(userCell);
        }
        
        void processWlmModelChanged() {
            List<TLocalNotification> localNotificationsList 
                    = localNotificationsHolder.getLocalNotifications();
            
            if (localNotificationsList != null) {
                Set<TLocalNotification> newLocalNotifications = new HashSet
                        <TLocalNotification>(localNotificationsList);
                
                for (int i = localNoticationCellList.size() - 1; i >= 0; i--) {
                    LocalNotificationCell localNotificationCell 
                            = localNoticationCellList.get(i);
                    TLocalNotification localNotifications 
                            = localNotificationCell
                            .getLocalNotificationElement();
                    
                    newLocalNotifications.remove(localNotifications);
                    
                    if (localNotificationsList.contains(localNotifications)) {
                        localNotificationCell.update();
                        fireTableRowsUpdated(i, i);
                    } else {
                        localNoticationCellList.remove(i);
                        fireTableRowsDeleted(i, i);
                    }
                }
                
                for (int i = 0; i < localNotificationsList.size(); i++) {
                    TLocalNotification localNotification 
                            = localNotificationsList.get(i);
                    if (newLocalNotifications.contains(localNotification)) {
                        localNoticationCellList.add(i, 
                                new LocalNotificationCell(localNotification));
                        fireTableRowsInserted(i, i);
                    }
                }
            } else {
                int rowCount = localNoticationCellList.size();
                localNoticationCellList.clear();
                
                if (rowCount > 0) {
                    fireTableRowsDeleted(0, rowCount - 1);
                }
            }
        }
    }
    
    private class NewCellValue {
        private TNotification notification;
        private String name;
        
        NewCellValue(TNotification notification, String name) {
            this.notification = notification;
            this.name = name;
        }
        
        TNotification getNotification() {
            return notification;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    private class NotificationCellEditor extends DefaultCellEditor {
        public NotificationCellEditor() {
            super(new JComboBox());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, 
                boolean isSelected, int row, int column) 
        {
            Component component = super.getTableCellEditorComponent(table, 
                    value, isSelected, row, column);
            
            JComboBox comboBox = (JComboBox) component;
            for (int i = comboBox.getItemCount() - 1; i >= 0; i--) {
                comboBox.removeItemAt(i);
            }
            
            WLMModel model = getModel();
            TTask task = model.getTask();
            List<TNotification> notificationsList = task.getNotifications();
            
            NewCellValue toSelect = null;
            
            if (notificationsList != null) {
                for (TNotification notification : notificationsList) {
                    String name = notification.getName();
                    if (name == null) {
                        name = ""; // NOI18N
                    } else {
                        name = name.trim();
                    }
                    
                    if (name.length() == 0) {
                        continue;
                    }
                    
                    NewCellValue newCellValue = new NewCellValue(notification, 
                            name);
                    comboBox.addItem(newCellValue);
                    if (toSelect == null && name.equals(value)) {
                        toSelect = newCellValue;
                    }
                }
            }
            
            comboBox.setSelectedItem(toSelect);
            
            return comboBox;
        }
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(LocalNotificationsPanel.class, key);
    }           
}
