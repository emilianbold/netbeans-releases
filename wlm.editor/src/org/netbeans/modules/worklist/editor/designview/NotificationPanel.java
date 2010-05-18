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
import java.awt.Dimension;
import java.awt.Graphics;
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
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.wlm.model.api.EmailAddress;
import org.netbeans.modules.wlm.model.api.MessageBody;
import org.netbeans.modules.wlm.model.api.MessageSubject;
import org.netbeans.modules.wlm.model.api.TEmail;
import org.netbeans.modules.wlm.model.api.TMessage;
import org.netbeans.modules.wlm.model.api.TNotification;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WSDLReference;
import org.netbeans.modules.wlm.model.spi.OperationReference;
import org.netbeans.modules.worklist.editor.chooser.OperationChooser;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.LinkButton;
import org.netbeans.modules.worklist.editor.designview.components.TextAreaEditor;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldBorder;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.netbeans.modules.worklist.editor.designview.components.TitleLabel;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.EmailAddressNode;
import org.netbeans.modules.worklist.editor.nodes.NotificationBodyNode;
import org.netbeans.modules.worklist.editor.nodes.NotificationEMailsNode;
import org.netbeans.modules.worklist.editor.nodes.NotificationNode;
import org.netbeans.modules.worklist.editor.nodes.NotificationSubjectNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class NotificationPanel extends DesignViewPanel implements Widget, 
        FocusListener
{
    private TNotification notification;
    
    private JLabel nameLabel;
    private NameEditor nameEditor;
    
    private JLabel subjectLabel;
    private SubjectEditor subjectEditor;
    private BodyEditor bodyEditor;

    private EmailAddressTableModel emailAddressTableModel;
    private JTable emailAddressTable;
    
    private AddEmailAddressAction addEmailAddressAction;
    private RemoveEmailAddressAction removeEmailAddressAction;
    
    private EMailPanel emailPanel;
    
    private TitledPanel titledPanel;
    
    private RemoveNotificationAction removeNotificationAction;

    private Widget widgetParent;

    public NotificationPanel(Widget widgetParent, DesignView designView,
            TNotification notification) 
    {
        super(designView);

        ExUtils.setA11Y(this, "NotificationPanel"); // NOI18N

        this.widgetParent = widgetParent;
        this.notification = notification;
        
        removeNotificationAction = new RemoveNotificationAction();

        setBackground(TitledPanel.BACKGROUND_COLOR);
        setOpaque(false);
        setBorder(new EmptyBorder(4, 0, 0, 0));
        setLayout(new BorderLayout());
        
        nameEditor = new NameEditor();
        nameEditor.addFocusListener(this);
        ExUtils.setA11Y(nameEditor, NotificationPanel.class,
                "NotificationNameEditor"); // NOI18N

        nameLabel = new JLabel(getMessage("LBL_NOTIFICATION_NAME")); // NOI18N
        nameLabel.setLabelFor(nameEditor);
        ExUtils.setA11Y(nameLabel, NotificationPanel.class,
                "NotificationNameLabel"); // NOI18N

        emailAddressTableModel = new EmailAddressTableModel();
        addEmailAddressAction = new AddEmailAddressAction();
        removeEmailAddressAction = new RemoveEmailAddressAction();
        emailPanel = new EMailPanel();
        
        subjectEditor = new SubjectEditor();
        ExUtils.setA11Y(subjectEditor, NotificationPanel.class,
                "NotificationSubjectEditor"); // NOI18N

        subjectLabel = new JLabel(getMessage(
                "LBL_NOTIFICATION_MESSAGE_SUBJECT")); // NOI18N
        subjectLabel.setLabelFor(subjectEditor);
        ExUtils.setA11Y(subjectLabel, NotificationPanel.class,
                "NotificationSubjectLabel"); // NOI18N
        
        bodyEditor = new BodyEditor();
        ExUtils.setA11Y(bodyEditor, NotificationPanel.class,
                "NotificationBodyEditor"); // NOI18N
        
        add(nameLabel);
        add(nameEditor);
        
        add(emailPanel);

        add(subjectLabel);
        add(subjectEditor);
        
        add(bodyEditor.getView());
        
        titledPanel = new TitledPanel(getMessage("LBL_NOTIFICATION"), // NOI18N
                removeNotificationAction, this, 0);
        ExUtils.setA11Y(titledPanel, NotificationPanel.class,
                "NotificationTitlePanel"); // NOI18N
        
        updateTableModels();
    }


    private void updateTableModels() {
        emailAddressTableModel.clear();
        
        TNotification notification = getNotification();
        TEmail email = notification.getEmail();
        
        if (email == null) {
            return;
        }
        
        List<EmailAddress> emailAddresses = email.getAddresses();
        
        if (emailAddresses != null) {
            for (EmailAddress emailAddress : emailAddresses) {
                emailAddressTableModel.add(new EmailAddressCell(emailAddress));
            }
        }
        
        emailAddressTableModel.fireTableDataChanged();
    }
    
    private void updateTitles() {
        
    }

    public TNotification getNotification() {
        return notification;
    }

    public void processWLMModelChanged() {
        emailPanel.processWLMModelChanged();
        nameEditor.updateContent();
        subjectEditor.updateContent();
        bodyEditor.updateContent();
    }
    
    private void stopEditing() {
        if (emailAddressTable.isEditing()) {
            TableCellEditor editor = emailAddressTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
    }    
    
    public JComponent getView() {
        return titledPanel;
    }
    
    private String getNewEmailAddress() {
        return "new email address"; // NOI18N
    }

    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        
        Dimension nameLabelSize = nameLabel.getPreferredSize();
        Dimension nameEditorSize = nameEditor.getPreferredSize();
        
        Dimension emailPanelSize = emailPanel.getPreferredSize();
        
        Dimension subjectLabelSize = subjectLabel.getPreferredSize();
        Dimension subjectEditorSize = subjectEditor.getPreferredSize();
        
        Dimension bodyEditorSize = bodyEditor.getView().getPreferredSize();
        
        int labelWidth = getLabelColumnWidth();
        
        int editorWidth = ExUtils.maxWidth(nameEditorSize, subjectEditorSize);
        
        int rowHeight = ExUtils.maxHeight(nameLabelSize, nameEditorSize,
                subjectLabelSize, subjectEditorSize);
        
        int w = Math.max(labelWidth + HGAP1 + editorWidth,
                Math.max(bodyEditorSize.width, emailPanelSize.width));
        int h = rowHeight + VGAP2 + emailPanelSize.height + VGAP1 
                + rowHeight + VGAP1 + bodyEditorSize.height;
        
        w += insets.left + insets.right;
        h += insets.top + insets.bottom;
        
        return new Dimension(w, h);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private int getLabelColumnWidth() {
        int w = nameLabel.getPreferredSize().width;
        w = Math.max(w, subjectLabel.getPreferredSize().width);
        w = Math.max(w, emailPanel.portTypeLabel.getPreferredSize().width);
        w = Math.max(w, emailPanel.operationLabel.getPreferredSize().width);
        return w;
    }
    
    @Override
    public void doLayout() {
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = getWidth() - x - insets.right;
        int h = getHeight() - y - insets.bottom;
        
        Dimension nameLabelSize = nameLabel.getPreferredSize();
        Dimension nameEditorSize = nameEditor.getPreferredSize();
        
        Dimension emailPanelSize = emailPanel.getPreferredSize();
        
        Dimension subjectLabelSize = subjectLabel.getPreferredSize();
        Dimension subjectEditorSize = subjectEditor.getPreferredSize();
        
        int labelWidth = getLabelColumnWidth();
        int rightSpace = emailPanel.configureButton.getPreferredSize().width
                + HGAP2;
        int editorWidth = w - HGAP1 - labelWidth;
        
        int rowHeight = ExUtils.maxHeight(nameLabelSize, nameEditorSize,
                subjectLabelSize, subjectEditorSize);
        
        int x2 = x + labelWidth + HGAP1;
        
        nameLabel.setBounds(x, y, Math.min(labelWidth, nameLabelSize.width), 
                rowHeight);
        nameEditor.setBounds(x2, y, editorWidth - rightSpace, rowHeight);
        
        y += rowHeight + VGAP2;
        h -= rowHeight + VGAP2;
        
        emailPanel.setBounds(x, y, w, emailPanelSize.height);
        
        y += emailPanelSize.height + VGAP1;
        h -= emailPanelSize.height + VGAP1;
        
        subjectLabel.setBounds(x, y, Math.min(labelWidth, 
                subjectLabelSize.width), rowHeight);
        subjectEditor.setBounds(x2, y, editorWidth, rowHeight);
        
        y += rowHeight + VGAP1;
        h -= rowHeight + VGAP1;
        
        bodyEditor.getView().setBounds(x, y, w, h);
    }

    public Widget getWidgetParent() {
        return widgetParent;
    }

    public Widget getWidget(int index) {
        if (index == 0) {
            return emailPanel;
        }

        if (index == 1) {
            return subjectEditor;
        }

        if (index == 2) {
            return bodyEditor;
        }

        throw new IndexOutOfBoundsException();
    }

    public int getWidgetCount() {
        return 3;
    }

    public Node getWidgetNode() {
        return new NotificationNode(notification, Children.LEAF,
                getNodeLookup());
    }

    public void requestFocusToWidget() {
        getDesignView().showNotificationsTab();
        scrollRectToVisible(new Rectangle(getSize()));
    }

    public WLMComponent getWidgetWLMComponent() {
        return notification;
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.NOTIFICATION;
    }

    public void focusGained(FocusEvent e) {
        Object source = e.getSource();
        if (source == nameEditor) {
            selectWidget(this);
        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    private class EMailPanel extends JPanel implements FocusListener, Widget, 
            ListSelectionListener
    {
        JLabel portTypeLabel;
        JLabel operationLabel;
        TitleLabel addressesLabel;
        
        JTextField portTypeValue;
        JTextField operationValue;
        
        JButton configureButton;
        
        LinkButton addButton;
        LinkButton removeButton;
        
        JComponent addressesHeader;
        JScrollPane addressesScrollPane;
        
        JPanel addressesPanel;
        
        Action configureAction;
        
        EMailPanel() {
            setBackground(TitledPanel.BACKGROUND_COLOR);
            setBorder(new EmptyBorder(VGAP2 + 1, 0, 0, 0));
            setOpaque(true);
            
            portTypeValue = new JTextField();
            portTypeValue.setBorder(TextFieldBorder.INSTANCE);
            portTypeValue.setEditable(false);
            portTypeValue.addFocusListener(this);
            ExUtils.setA11Y(portTypeValue, NotificationPanel.class,
                    "NotificationPortTypeValue"); // NOI18N

            portTypeLabel = new JLabel(getMessage(
                    "LBL_NOTIFICATION_EMAIL_PORT_TYPE")); // NOI18N
            portTypeLabel.setLabelFor(portTypeValue);
            ExUtils.setA11Y(portTypeLabel, NotificationPanel.class,
                    "NotificationPortTypeLabel"); // NOI18N

            operationValue = new JTextField();
            operationValue.setBorder(TextFieldBorder.INSTANCE);
            operationValue.setEditable(false);
            operationValue.addFocusListener(this);
            ExUtils.setA11Y(operationValue, NotificationPanel.class,
                    "NotificationOperationValue"); // NOI18N

            operationLabel = new JLabel(getMessage(
                    "LBL_NOTIFICATION_EMAIL_OPERATION")); // NOI18N
            operationLabel.setLabelFor(operationValue);
            ExUtils.setA11Y(operationLabel, NotificationPanel.class,
                    "NotificationOperationLabel"); // NOI18N

            configureButton = new JButton(getMessage(
                    "LBL_NOTIFICATION_EMAIL_CONFIGURE")); // NOI18N
            configureButton.setMargin(new Insets(0, 4, 0, 4));
            configureButton.addFocusListener(this);
            ExUtils.setA11Y(configureButton, NotificationPanel.class,
                    "NotificationConfigureButton"); // NOI18N

            addressesLabel = new TitleLabel(getMessage(
                    "LBL_NOTIFICATION_EMAIL_ADDRESSES"), false); // NOI18N
            addressesLabel.setBackground(TitledPanel.BACKGROUND_COLOR);
            ExUtils.setA11Y(addressesLabel, NotificationPanel.class,
                    "NotificationAddressesLabel"); // NOI18N
            
            addButton = new LinkButton(addEmailAddressAction);
            addButton.addFocusListener(this);
            ExUtils.setA11Y(addButton, NotificationPanel.class,
                    "NotificationAddAddressButton"); // NOI18N
            
            removeButton = new LinkButton(removeEmailAddressAction);
            removeButton.addFocusListener(this);
            ExUtils.setA11Y(removeButton, NotificationPanel.class,
                    "NotificationRemoveAddressButton"); // NOI18N
            
            addressesHeader = Box.createHorizontalBox();
            addressesHeader.setBackground(TitledPanel.BACKGROUND_COLOR);
            ExUtils.setA11Y(addressesHeader, NotificationPanel.class,
                    "NotificationAddressesTableHeader"); // NOI18N
            
            add(portTypeLabel);
            add(portTypeValue);
            
            add(operationLabel);
            add(operationValue);
            add(configureButton);
            
            addressesHeader.add(addressesLabel);
            addressesHeader.add(Box.createHorizontalGlue()).setFocusable(false);
            addressesHeader.add(addButton);
            addressesHeader.add(removeButton);
            
            emailAddressTable = new JTable(emailAddressTableModel);
            emailAddressTable.setTableHeader(null);
            emailAddressTable.setRowHeight(18);
            emailAddressTable.setShowVerticalLines(false);
            emailAddressTable.setSelectionMode(ListSelectionModel
                    .SINGLE_SELECTION);
            emailAddressTable.putClientProperty(
                    "terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
            emailAddressTable.addFocusListener(this);
            emailAddressTable.getSelectionModel()
                    .addListSelectionListener(this);
            ExUtils.setA11Y(emailAddressTable, NotificationPanel.class,
                    "NotificationAddressTable"); // NOI18N
            
            addressesScrollPane = new JScrollPane(emailAddressTable);
            addressesScrollPane.getViewport().setBackground(Color.WHITE);
            ExUtils.setA11Y(addressesScrollPane, NotificationPanel.class,
                    "NotificationAddressesScrollPane"); // NOI18N
            
            addressesPanel = new JPanel(new BorderLayout());
            addressesPanel.setBackground(TitledPanel.BACKGROUND_COLOR);
            addressesPanel.setOpaque(true);
            addressesPanel.add(addressesHeader, BorderLayout.NORTH);
            addressesPanel.add(addressesScrollPane, BorderLayout.CENTER);
            ExUtils.setA11Y(addressesPanel, NotificationPanel.class,
                    "NotificationAddressesPanel"); // NOI18N
            
            add(addressesPanel);
            
            configureAction = new ConfigureOperationAction();
            configureButton.addActionListener(configureAction);
            
            updatePortTypeAndOperation();
        }
        
        private void updatePortTypeAndOperation() {
            String portTypeText = null;
            String operationText = null;

            TTask task = getTask();
            if (task != null) {
                TEmail email = notification.getEmail();
                
                WSDLReference<Operation> operationRef = (email == null) ? null
                        : email.getOperation();
                WSDLReference<PortType> portTypeRef = (email == null) ? null
                        : email.getPortType();

                Operation operation = (operationRef == null) ? null 
                        : operationRef.get();
                PortType portType = (portTypeRef == null) ? null
                        : portTypeRef.get();

                if (operation != null) {
                    operationText = operation.getName();
                }

                if (portType != null) {
                    portTypeText = portType.getName();
                }
            }

            if (portTypeText == null) {
                portTypeText = ""; // NOI18N
            } else {
                portTypeText = portTypeText.trim();
            }

            if (operationText == null) {
                operationText = ""; // NOI18N
            } else {
                operationText = operationText.trim();
            }

            operationValue.setText(operationText);
            portTypeValue.setText(portTypeText);
        }
        
        
        @Override
        public void doLayout() {
            Insets insets = getInsets();
            int x = insets.left;
            int y = insets.top;
            
            int w = getWidth() - x - insets.right;
            int h = getHeight() - y - insets.bottom;
            
            Dimension size11 = portTypeLabel.getPreferredSize();
            Dimension size12 = portTypeValue.getPreferredSize();
            
            Dimension size21 = operationLabel.getPreferredSize();
            Dimension size22 = operationValue.getPreferredSize();
            Dimension size23 = configureButton.getPreferredSize();
            
            Dimension addressesSize = addressesHeader.getPreferredSize();
            addressesSize.height += 55;
            
            int row1 = ExUtils.maxHeight(size11, size12);
            int row2 = ExUtils.maxHeight(size21, size22, size23);
            int col1 = getLabelColumnWidth();
            int col3 = size23.width;
            int col2 = w - col3 - col1 - HGAP1 - HGAP2;
            
            int x2 = x + col1 + HGAP1;
            int x3 = x2 + col2 + HGAP2;
            
            int y2 = y + row1 + VGAP1;
            int y3 = y2 + row2 + VGAP2;
            
            portTypeLabel.setBounds(x, y, Math.min(col1, size11.width), row1);
            portTypeValue.setBounds(x2, y, col2, row1);
            
            operationLabel.setBounds(x, y2, Math.min(col1, size21.width), row2);
            operationValue.setBounds(x2, y2, col2, row2);
            configureButton.setBounds(x3, y2, col3, row2);
            
            h -= row1 + VGAP1 + row2 + VGAP2;
            
            addressesPanel.setBounds(x, y3, w, h);
        }
        
        @Override
        public Dimension getPreferredSize() {
            Dimension size11 = portTypeLabel.getPreferredSize();
            Dimension size12 = portTypeValue.getPreferredSize();
            
            Dimension size21 = operationLabel.getPreferredSize();
            Dimension size22 = operationValue.getPreferredSize();
            Dimension size23 = configureButton.getPreferredSize();
            
            Dimension addressesSize = addressesHeader.getPreferredSize();
            addressesSize.height += 55;
            
            int row1 = ExUtils.maxHeight(size11, size12);
            int row2 = ExUtils.maxHeight(size21, size22, size23);
            int col1 = getLabelColumnWidth();
            int col2 = ExUtils.maxWidth(size12, size22);
            int col3 = size23.width;
            
            Insets insets = getInsets();
            
            int w = Math.max(col1 + HGAP1 + col2 + HGAP2 + col3, 
                    addressesSize.width);
            int h = row1 + VGAP1 + row2 + VGAP2 + 70;
            
            w += insets.left + insets.right;
            h += insets.top + insets.bottom;
            
            return new Dimension(w, h);
        }
        
        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintBorder(Graphics g) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(0, 0, getWidth() - 1, 0);
        }

        private void processWLMModelChanged() {
            emailAddressTableModel.processWlmModelChanged();
            updatePortTypeAndOperation();
        }

        public void focusGained(FocusEvent e) {
            Object source = e.getSource();
            if (source == addButton
                    || source == removeButton
                    || source == operationValue
                    || source == portTypeValue
                    || source == configureButton)
            {
                selectWidget(this);
            } else if (source == emailAddressTable) {
                int row = emailAddressTable.getSelectedRow();
                if (row >= 0) {
                    selectWidget(emailAddressTableModel.get(row));
                } else {
                    selectWidget(this);
                }
            }
        }

        public void focusLost(FocusEvent e) {
            // do nothing
        }

        public void valueChanged(ListSelectionEvent e) {
            if (emailAddressTable.hasFocus() || emailAddressTable.isEditing()) {
                int row = emailAddressTable.getSelectedRow();
                if (row >= 0) {
                    selectWidget(emailAddressTableModel.get(row));
                } else {
                    selectWidget(this);
                }
            }
        }

        public Widget getWidgetParent() {
            return NotificationPanel.this;
        }

        public Widget getWidget(int index) {
            return emailAddressTableModel.get(index);
        }

        public int getWidgetCount() {
            return emailAddressTableModel.getRowCount();
        }

        public Node getWidgetNode() {
            return new NotificationEMailsNode(notification, Children.LEAF,
                    getNodeLookup());
        }

        public void requestFocusToWidget() {
            getDesignView().showNotificationsTab();
            scrollRectToVisible(new Rectangle(getSize()));
        }

        public WLMComponent getWidgetWLMComponent() {
            return notification;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.NOTIFICATION_EMAILS;
        }
    }
    
    
    private class NameEditor extends TextFieldEditor {
        NameEditor() {
            super(NotificationPanel.this.getDesignView(), false);
        }

        @Override
        public String getModelValue() {
            String name = notification.getName();
            if (name == null) {
                name = ""; // NOI18N
            } else {
                name = name.trim();
            }
            
            return name;
        }

        @Override
        public void setModelValue(String value) {
            if (value == null) {
                value = ""; // NOI18N
            } else {
                value = value.trim();
            }
            
            WLMModel model = getModel();
            if (model.startTransaction()) {
                try {
                    notification.setName(value);
                } finally {
                    model.endTransaction();
                }
            }            
        }
    }
    
    private class SubjectEditor extends TextFieldEditor implements Widget {

        SubjectEditor() {
            super(NotificationPanel.this.getDesignView());
        }

        @Override
        public void activateNode() {
            selectWidget(this);
        }

        @Override
        public String getModelValue() {
            TMessage message = notification.getMessage();
            MessageSubject messageSubject = (message == null) ? null
                    : message.getSubject();
            
            String subject = (messageSubject == null) ? null
                    : messageSubject.getContent();
            
            return subject;
        }

        @Override
        public void setModelValue(String value) {
            WLMModel model = getModel();
            if (model.startTransaction()) {
                try {
                    TMessage message = notification.getMessage();
                    if (message == null) {
                        message = model.getFactory().createMessage(model);
                        notification.setMessage(message);
                    }
                    
                    MessageSubject subject = message.getSubject();
                    if (subject == null) {
                        subject = model.getFactory()
                                .createMessageSubject(model);
                        message.setSubject(subject);
                    }
                    
                    subject.setContent(value);
                } finally {
                    model.endTransaction();
                }
            }
        }

        public Widget getWidgetParent() {
            return NotificationPanel.this;
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new NotificationSubjectNode(notification, Children.LEAF,
                    getNodeLookup());
        }

        public void requestFocusToWidget() {
            getDesignView().showNotificationsTab();
            scrollRectToVisible(new Rectangle(getSize()));
            requestFocusInWindow();
        }

        public WLMComponent getWidgetWLMComponent() {
            return notification;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.NOTIFICATION_SUBJECT;
        }
    }
    
    private class BodyEditor extends TextAreaEditor implements Widget, 
            FocusListener
    {
        BodyEditor() {
            super(NotificationPanel.this.getDesignView(), 6, 40);
            addFocusListener(this);
        }

        @Override
        public String getModelValue() {
            TMessage message = notification.getMessage();
            MessageBody body = (message == null) ? null
                    : message.getBody();
            String value = (body == null) ? null // NOI18N
                    : body.getContent();
            
            return value;
        }

        @Override
        public void setModelValue(String value) {
            WLMModel model = getModel();
            if (model.startTransaction()) {
                try {
                    TMessage message = notification.getMessage();
                    if (message == null) {
                        message = model.getFactory().createMessage(model);
                        notification.setMessage(message);
                    }
                    
                    MessageBody body = message.getBody();
                    if (body == null) {
                        body = model.getFactory().createMessageBody(model);
                        message.setBody(body);
                    }
                    
                    body.setContent(value);
                } finally {
                    model.endTransaction();
                }
            }
        }

        public Widget getWidgetParent() {
            return NotificationPanel.this;
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new NotificationBodyNode(notification, Children.LEAF,
                    getNodeLookup());
        }

        public void requestFocusToWidget() {
            getDesignView().showNotificationsTab();
            scrollRectToVisible(new Rectangle(getSize()));
            requestFocusInWindow();
        }

        public WLMComponent getWidgetWLMComponent() {
            return notification;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.NOTIFICATUIN_BODY;
        }

        public void focusGained(FocusEvent e) {
            selectWidget(this);
        }

        public void focusLost(FocusEvent e) {
            // do nothing
        }

    }
    
    private class RemoveNotificationAction extends AbstractAction {
        RemoveNotificationAction() {
            super(getMessage("LBL_REMOVE_NOTIFICATION")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            WLMModel model = getModel();
            TTask task = getTask();
            
            if (model.startTransaction()) {
                try {
                    task.removeNotification(notification);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }

    
    private class EmailAddressCell implements Widget {
        private EmailAddress emailAddressElement;
        private String emailAddress;
        
        public EmailAddressCell(EmailAddress emailAddressElement) {
            this.emailAddressElement = emailAddressElement;
            update();
        }
        
        public EmailAddress getEmailAddressElement() {
            return emailAddressElement;
        }
        
        public String getEmailAddress() {
            return emailAddress;
        }
        
        @Override
        public String toString() {
            return emailAddress;
        }
        
        public void update() {
            emailAddress = (emailAddressElement == null) ? null
                    : emailAddressElement.getContent();
            emailAddress = TextFieldEditor.xPathToText(emailAddress);
        }

        public Widget getWidgetParent() {
            return emailPanel;
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new EmailAddressNode(emailAddressElement, Children.LEAF,
                    getNodeLookup());
        }

        public void requestFocusToWidget() {
            getDesignView().showNotificationsTab();

            int row = emailAddressTableModel.emailAddressCellList.indexOf(this);
            if (row >= 0) {
                emailAddressTable.getSelectionModel()
                        .setSelectionInterval(row, row);
            }
            emailAddressTable.requestFocusInWindow();
        }

        public WLMComponent getWidgetWLMComponent() {
            return emailAddressElement;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.EMAIL_ADDRESS;
        }
    }

    
    private class EmailAddressTableModel extends AbstractTableModel {
        private List<EmailAddressCell> emailAddressCellList;
        
        public EmailAddressTableModel() {
            emailAddressCellList = new ArrayList<EmailAddressCell>();
        }
        
        public int getRowCount() {
            return emailAddressCellList.size();
        }

        public int getColumnCount() {
            return 1;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            return emailAddressCellList.get(rowIndex).toString();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex >= getRowCount()) {
                return;
            }

            String value = TextFieldEditor.textToXPath(aValue.toString());

            EmailAddressCell userCell = emailAddressCellList.get(rowIndex);
            if (userCell.getEmailAddress().equals(value)) {
                return;
            }
            
            WLMModel model = getModel();
            
            if (model.startTransaction()) {
                try {
                    EmailAddress emailAddress = userCell.getEmailAddressElement();
                    emailAddress.setContent(value);
                } finally {
                    model.endTransaction();
                }
                
                userCell.update();
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
            emailAddressCellList.clear();
        }
        
        public EmailAddressCell get(int row) {
            return emailAddressCellList.get(row);
        }
        
        public void add(EmailAddressCell userCell) {
            emailAddressCellList.add(userCell);
        }        
        
        public void remove(EmailAddressCell userCell) {
            emailAddressCellList.remove(userCell);
        }
        
        void processWlmModelChanged() {
            TNotification notification = getNotification();
            TEmail email = notification.getEmail();
            
            List<EmailAddress> emailAddressesList = (email == null) ? null :
                    email.getAddresses();
            
            if (emailAddressesList != null) {
                Set<EmailAddress> newEmailAddresses 
                        = new HashSet<EmailAddress>(emailAddressesList);
                
                for (int i = emailAddressCellList.size() - 1; i >= 0; i--) {
                    EmailAddressCell emailAddressCell = emailAddressCellList
                            .get(i);
                    EmailAddress emialAddress = emailAddressCell
                            .getEmailAddressElement();
                    
                    newEmailAddresses.remove(emialAddress);
                    
                    if (emailAddressesList.contains(emialAddress)) {
                        emailAddressCell.update();
                        fireTableRowsUpdated(i, i);
                    } else {
                        emailAddressCellList.remove(i);
                        fireTableRowsDeleted(i, i);
                    }
                }
                
                for (int i = 0; i < emailAddressesList.size(); i++) {
                    EmailAddress emailAddress = emailAddressesList.get(i);
                    if (newEmailAddresses.contains(emailAddress)) {
                        emailAddressCellList.add(i, 
                                new EmailAddressCell(emailAddress));
                        fireTableRowsInserted(i, i);
                    }
                }
            } else {
                int rowCount = emailAddressCellList.size();
                emailAddressCellList.clear();
                
                if (rowCount > 0) {
                    fireTableRowsDeleted(0, rowCount - 1);
                }
            }
        }
    }
    
    private class AddEmailAddressAction extends AbstractAction {
        AddEmailAddressAction() {
            super(getMessage("LBL_ADD_EMAIL_ADDRESS")); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            stopEditing();
            
            WLMModel model = getModel();
            TNotification notification = getNotification();
            
            if (model.startTransaction()) {
                try {
                    TEmail email = notification.getEmail();
                    if (email == null) {
                        email = model.getFactory().createEmail(model);
                        notification.setEmail(email);
                    }
                    
                    EmailAddress emailAddress = model.getFactory()
                            .createEmailAddress(model);

                    emailAddress.setContent(TextFieldEditor
                            .textToXPath(getNewEmailAddress()));
                    
                    email.addAddress(emailAddress);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }
    
    private class RemoveEmailAddressAction extends AbstractAction {
        RemoveEmailAddressAction() {
            super(getMessage("LBL_REMOVE_EMAIL_ADDRESS")); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            stopEditing();
            
            int selectedRow = emailAddressTable.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }
            
            if (selectedRow >= emailAddressTableModel.getRowCount()) {
                return;
            } 
            
            EmailAddressCell emailAddressCell = emailAddressTableModel
                    .get(selectedRow);
            
            EmailAddress emailAddress = emailAddressCell
                    .getEmailAddressElement();
               
            WLMModel model = getModel();
            TNotification notification = getNotification();
            TEmail email = notification.getEmail();
            
            if (email == null) {
                return;
            }
            
            if (model.startTransaction()) {
                try {
                    email.removeAddress(emailAddress);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }
    
    private class ConfigureOperationAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            TNotification wlmNotification = getNotification();
            TEmail wlmEmail = wlmNotification.getEmail();
            WSDLReference<Operation> oldOperationRef = (wlmEmail == null) ? null
                    : wlmEmail.getOperation();

            Operation oldOperation = (oldOperationRef == null) ? null
                    : oldOperationRef.get();
            Operation newOperation = new OperationChooser(getDataObject())
                    .choose(oldOperation);

            if (newOperation != null && newOperation != oldOperation) {
                WLMModel model = getModel();

                if (model.startTransaction()) {
                    try {
                        TEmail email = notification.getEmail();
                        if (email == null) {
                            email = model.getFactory().createEmail(model);
                            notification.setEmail(email);
                        }

                        OperationReference ref = email
                                .createOperationReference(
                                newOperation);
                        email.setOperation(ref);
                    } finally {
                        model.endTransaction();
                    }
                }
            }
        }
    }    
    
    private static final int HGAP1 = 8;
    private static final int HGAP2 = 4;
    private static final int VGAP1 = 4;
    private static final int VGAP2 = 8;        
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(NotificationPanel.class, key);
    }      
}
