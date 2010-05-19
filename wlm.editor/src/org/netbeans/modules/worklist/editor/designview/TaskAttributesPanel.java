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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.netbeans.modules.wlm.model.api.TPriority;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTitle;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WSDLReference;
import org.netbeans.modules.wlm.model.spi.OperationReference;
import org.netbeans.modules.worklist.editor.chooser.OperationChooser;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.HSeparator;
import org.netbeans.modules.worklist.editor.designview.components.StyledLabel;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldBorder;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.netbeans.modules.worklist.editor.nodes.PriorityNode;
import org.netbeans.modules.worklist.editor.nodes.TitleNode;
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
public class TaskAttributesPanel extends DesignViewPanel implements
        FocusListener
{
    private JLabel nameLabel;
    
    private JLabel portTypeLabel;
    private JLabel operationLabel;
    
    private JLabel titleLabel;
    private JLabel priorityLabel;
    
    private TextFieldEditor nameValue;

    private JTextField portTypeValue;
    private JTextField operationValue;
    
    private TaskTitleEditor titleValue;
    private TaskPriorityEditor priorityValue;
    
    private JButton chooseButton;
    
    private HSeparator topSeparator;
    private HSeparator bottomSeparator;
    
    private Action configureOperationAction;
    
    public TaskAttributesPanel(DesignView designView) {
        super(designView);

        ExUtils.setA11Y(this, "TaskAttributesPanel"); // NOI18N
        
        setBorder(null);
        
        nameValue = new TaskNameEditor(designView);
        nameValue.addFocusListener(this);

        ExUtils.setA11Y(nameValue, TaskAttributesPanel.class,
                "TaskNameEditor"); // NOI18N
        
        portTypeValue = new JTextField(15);
        portTypeValue.setEditable(false);
        portTypeValue.setBorder(TextFieldBorder.INSTANCE);
        portTypeValue.addFocusListener(this);

        ExUtils.setA11Y(nameValue, TaskAttributesPanel.class,
                "PortTypeValue"); // NOI18N
        
        operationValue = new JTextField(15);
        operationValue.setEditable(false);
        operationValue.setBorder(TextFieldBorder.INSTANCE);
        operationValue.addFocusListener(this);

        ExUtils.setA11Y(operationValue, TaskAttributesPanel.class,
                "OperationValue"); // NOI18N
        
        titleValue = new TaskTitleEditor(designView);
        ExUtils.setA11Y(titleValue, TaskAttributesPanel.class,
                "TitleValue"); // NOI18N

        priorityValue = new TaskPriorityEditor(designView);
        ExUtils.setA11Y(priorityValue, TaskAttributesPanel.class,
                "PriorityValue"); // NOI18N

        nameLabel = new StyledLabel(getMessage("LBL_TASK_NAME")); // NOI18N
        nameLabel.setLabelFor(nameValue);
        ExUtils.setA11Y(nameLabel, TaskAttributesPanel.class,
                "NameLabel"); // NOI18N
        
        portTypeLabel = new StyledLabel(getMessage(
                "LBL_TASK_PORT_TYPE")); // NOI18N
        portTypeLabel.setLabelFor(portTypeValue);
        ExUtils.setA11Y(portTypeLabel, TaskAttributesPanel.class,
                "PortTypeLabel"); // NOI18N

        operationLabel = new StyledLabel(getMessage(
                "LBL_TASK_OPERATION")); // NOI18N
        operationLabel.setLabelFor(operationValue);
        ExUtils.setA11Y(operationLabel, TaskAttributesPanel.class,
                "OperationLabel"); // NOI18N
        
        titleLabel = new StyledLabel(getMessage(
                "LBL_TASK_TITLE")); // NOI18N
        titleLabel.setLabelFor(titleValue);
        ExUtils.setA11Y(titleLabel, TaskAttributesPanel.class,
                "TitleLabel"); // NOI18N
        
        priorityLabel = new StyledLabel(getMessage(
                "LBL_TASK_PRIORITY")); // NOI18N
        priorityLabel.setLabelFor(priorityValue);
        ExUtils.setA11Y(priorityLabel, TaskAttributesPanel.class,
                "PriorityLabel"); // NOI18N
        
        chooseButton = new JButton(getMessage("LBL_TASK_CONFIGURE")); // NOI18N
        chooseButton.setMargin(new Insets(0, 4, 0, 4));
        chooseButton.addFocusListener(this);
        ExUtils.setA11Y(chooseButton, TaskAttributesPanel.class,
                "ChooseOperationButton"); // NOI18N
        
        topSeparator = new HSeparator();
        ExUtils.setA11Y(topSeparator, TaskAttributesPanel.class,
                "Separator"); // NOI18N

        bottomSeparator = new HSeparator();
        ExUtils.setA11Y(bottomSeparator, TaskAttributesPanel.class,
                "Separator"); // NOI18N
        
        add(nameLabel);
        add(nameValue);
        
        add(topSeparator);
        
        add(portTypeLabel);
        add(portTypeValue);

        add(operationLabel);
        add(operationValue);
        add(chooseButton);        
        
        add(titleLabel);
        add(titleValue);
        
        add(bottomSeparator);
        
        add(priorityLabel);
        add(priorityValue);
        
        configureOperationAction = new ConfigureOperationAction();
        chooseButton.addActionListener(configureOperationAction);
        
        updatePortTypeAndOperation();
    }

    Widget getTitleWidget() {
        return titleValue;
    }

    Widget getPriorityWidget() {
        return priorityValue;
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension size11 = nameLabel.getPreferredSize();
        Dimension size12 = nameValue.getPreferredSize();
        
        Dimension size21 = portTypeLabel.getPreferredSize();
        Dimension size22 = portTypeValue.getPreferredSize();
        
        Dimension size31 = operationLabel.getPreferredSize();
        Dimension size32 = operationValue.getPreferredSize();
        Dimension size33 = chooseButton.getPreferredSize();
        
        Dimension size41 = titleLabel.getPreferredSize();
        Dimension size42 = titleValue.getPreferredSize();
        
        Dimension size51 = priorityLabel.getPreferredSize();
        Dimension size52 = priorityValue.getPreferredSize();
        
        int row1 = ExUtils.maxHeight(size11, size12);
        
        int row2 = ExUtils.maxHeight(size21, size22);
        int row3 = ExUtils.maxHeight(size31, size32, size33);
        
        int row4 = ExUtils.maxHeight(size41, size42);
        int row5 = ExUtils.maxHeight(size51, size52);
        
        int col1 = ExUtils.maxWidth(size11, size21, size31, size41, size51);
        int col2 = ExUtils.maxWidth(size12, size22, size32, size42, size52);
        int col3 = size33.width;
        
        int separatorHeight = topSeparator.getPreferredSize().height;
        
        Insets insets = getInsets();
        
        int w = insets.left + col1 + HGAP1 + col2 + HGAP2 + col3 + insets.right;
        int h = insets.top 
                + row1 
                + VGAP2 + separatorHeight + VGAP2 
                + row2 + VGAP1 + row3 
                + VGAP2 + separatorHeight + VGAP2 
                + row4 + VGAP1 + row5 
                + insets.bottom;
        
        return new Dimension(w, h);
    }
    
    @Override
    public Dimension getMaximumSize() {
        Dimension size = getPreferredSize();
        size.width = Integer.MAX_VALUE;
        return size;
    }

    @Override
    public void doLayout() {
        Insets insets = getInsets();
        int w = getWidth() - insets.left - insets.right;
        
        Dimension size11 = nameLabel.getPreferredSize();
        Dimension size12 = nameValue.getPreferredSize();
        
        Dimension size21 = portTypeLabel.getPreferredSize();
        Dimension size22 = portTypeValue.getPreferredSize();
        
        Dimension size31 = operationLabel.getPreferredSize();
        Dimension size32 = operationValue.getPreferredSize();
        Dimension size33 = chooseButton.getPreferredSize();
        
        Dimension size41 = titleLabel.getPreferredSize();
        Dimension size42 = titleValue.getPreferredSize();
        
        Dimension size51 = priorityLabel.getPreferredSize();
        Dimension size52 = priorityValue.getPreferredSize();
        
        int row1 = ExUtils.maxHeight(size11, size12);
        
        int row2 = ExUtils.maxHeight(size21, size22);
        int row3 = ExUtils.maxHeight(size31, size32, size33);
        
        int row4 = ExUtils.maxHeight(size41, size42);
        int row5 = ExUtils.maxHeight(size51, size52);
        
        int col1 = ExUtils.maxWidth(size11, size21, size31, size41, size51);
        int col3 = size33.width;
        int col2 = w - col1 - col3 - HGAP1 - HGAP2;
        
        int separatorHeight = topSeparator.getPreferredSize().height;
        
        // --- 
        
        int x1 = insets.left;
        int x2 = x1 + col1 + HGAP1;
        int x3 = x2 + col2 + HGAP2;
        
        int y1 = insets.top;
        int yTopSeparator = y1 + row1 + VGAP2;
        int y2 = yTopSeparator + separatorHeight + VGAP2;
        int y3 = y2 + row2 + VGAP1;
        int yBottomSeparator = y3 + row3 + VGAP2;
        int y4 = yBottomSeparator + separatorHeight + VGAP2;
        int y5 = y4 + row4 + VGAP1;
        
        nameLabel.setBounds(x1, y1, col1, row1);
        nameValue.setBounds(x2, y1, col2, row1);
        
        topSeparator.setBounds(x1, yTopSeparator, w, separatorHeight);
        
        portTypeLabel.setBounds(x1, y2, col1, row2);
        portTypeValue.setBounds(x2, y2, col2, row2);
        
        operationLabel.setBounds(x1, y3, col1, row3);
        operationValue.setBounds(x2, y3, col2, row3);
        chooseButton.setBounds(x3, y3, col3, row3);

        bottomSeparator.setBounds(x1, yBottomSeparator, w, separatorHeight);
        
        titleLabel.setBounds(x1, y4, col1, row4);
        titleValue.setBounds(x2, y4, col2, row4);
        
        priorityLabel.setBounds(x1, y5, col1, row5);
        priorityValue.setBounds(x2, y5, col2, row5);
    }

    void processWLMModelChanged() {
        updatePortTypeAndOperation();
        nameValue.updateContent();
        titleValue.updateContent();
        priorityValue.updateContent();
    }
    
    private void updatePortTypeAndOperation() {
        String portTypeText = null;
        String operationText = null;
        
        TTask task = getTask();
        if (task != null) {
            WSDLReference<Operation> operationRef = task.getOperation();
            WSDLReference<PortType> portTypeRef = task.getPortType();
            
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

    public void focusGained(FocusEvent e) {
        selectWidget(getDesignView().getBasicPropertiesPanel());
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    public void requestFocusToWidget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private static class TaskNameEditor extends TextFieldEditor {
        public TaskNameEditor(DesignView designView) {
            super(designView, false);
        }
        
        @Override
        public String getModelValue() {
            TTask task = getTask();
            if (task == null) {
                return ""; // NOI18N
            }

            String value = task.getName();
            if (value == null) {
                return ""; // NOI18N
            }

            return value;
        }

        @Override
        public void setModelValue(String value) {
            TTask task = getTask();
            WLMModel model = task.getModel();

            if (model.startTransaction()) {
                try {
                    if (value == null) {
                        value = ""; // NOI18N
                    } else {
                        value = value.trim();
                    }
                    task.setName(value);
                } finally {
                    model.endTransaction();
                }
            } 
        }
    }

    private static class TaskPriorityEditor extends TextFieldEditor 
            implements Widget
    {
        public TaskPriorityEditor(DesignView designView) {
            super(designView);
        }
        
        @Override
        public String getModelValue() {
            TTask task = getTask();
            if (task == null) {
                return ""; // NOI18N
            }

            TPriority priority = task.getPriority();
            if (priority == null) {
                return ""; // NOI18N;
            }
            
            String content = priority.getContent();
            if (content == null) {
                return ""; // NOI18N
            }

            return content;
        }

        @Override
        public void setModelValue(String value) {
            TTask task = getTask();
            WLMModel model = task.getModel();

            if (model.startTransaction()) {
                try {
                    TPriority priority = task.getPriority();
                    if (priority == null) {
                        priority = model.getFactory().createPriority(model);
                        task.setPriority(priority);
                    }
                    priority.setContent(value);
                } finally {
                    model.endTransaction();
                }
            } 
        }

        public Widget getWidgetParent() {
            return getDesignView().getBasicPropertiesPanel();
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new PriorityNode(getTask(), Children.LEAF, getDesignView()
                    .getNodeLookup());
        }

        public void requestFocusToWidget() {
            getDesignView().showBasicPropertiesTab();
            requestFocusInWindow();
        }

        public WLMComponent getWidgetWLMComponent() {
            return getTask();
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.PRIORITY;
        }

        @Override
        public void activateNode() {
            getDesignView().selectWidget(this);
        }
    }

    private static class TaskTitleEditor extends TextFieldEditor 
            implements Widget
    {
        public TaskTitleEditor(DesignView designView) {
            super(designView);
        }
        
        @Override
        public String getModelValue() {
            TTask task = getTask();
            if (task == null) {
                return ""; // NOI18N
            }
            
            TTitle title = task.getTitle();
            if (title == null) {
                return ""; // NOI18N
            }

            String content = title.getContent();
            if (content == null) {
                return ""; // NOI18N
            }

            return content;
        }

        @Override
        public void setModelValue(String value) {
            TTask task = getTask();
            WLMModel model = task.getModel();

            if (model.startTransaction()) {
                try {
                    TTitle title = task.getTitle();
                    if (title == null) {
                        title = model.getFactory().createTitle(model);
                        task.setTitle(title);
                    }
                    title.setContent(value);
                } finally {
                    model.endTransaction();
                }
            } 
        }

        public Widget getWidgetParent() {
            return getDesignView().getBasicPropertiesPanel();
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new TitleNode(getTask(), Children.LEAF, getDesignView()
                    .getNodeLookup());
        }

        public void requestFocusToWidget() {
            getDesignView().showBasicPropertiesTab();
            requestFocusInWindow();
        }

        public WLMComponent getWidgetWLMComponent() {
            return getTask();
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.TITLE;
        }

        @Override
        public void activateNode() {
            getDesignView().selectWidget(this);
        }
    }
    
    private class ConfigureOperationAction extends AbstractAction {
        public void actionPerformed(ActionEvent event) {
            // TODO replace
            TTask wlmTask = getTask();
            WSDLReference<Operation> oldOperationRef = (wlmTask == null) ? null
                    : wlmTask.getOperation();
            Operation oldOperation = (oldOperationRef == null) ? null
                    : oldOperationRef.get();

            Operation newOperation = new OperationChooser(getDataObject())
                    .choose(oldOperation);

            if (newOperation != null && newOperation != oldOperation) {
                TTask task = getTask();
                WLMModel model = getModel();

                if (model.startTransaction()) {
                    try {
                        OperationReference ref = task
                                .createOperationReference(
                                newOperation);
                        task.setOperation(ref);
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
    private static final int VGAP2 = 12;
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(TaskAttributesPanel.class, key);
    }       
}
