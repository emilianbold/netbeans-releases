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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.TActionType;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.ActionNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class ActionPanel extends DesignViewPanel implements Widget,
        FocusListener
{
    private TAction action;
    
    private RemoveActionAction removeActionAction;
    private TitledPanel titledPanel;
    
    private JLabel nameLabel;
    private JLabel typeLabel;
    
    private NameEditor nameEditor;
    private TypeChooser typeChooser;

    private LocalNotificationsPanel localNotificationsPanel;

    private Widget widgetParent;

    public ActionPanel(Widget widgetParent, DesignView designView, 
            TAction action)
    {
        super(designView);

        ExUtils.setA11Y(this, "ActionPanel"); // NOI18N

        setBorder(null);
        setBackground(TitledPanel.BACKGROUND_COLOR);
        setOpaque(true);

        this.widgetParent = widgetParent;
        this.action = action;
        
        removeActionAction = new RemoveActionAction();
        titledPanel = new TitledPanel(getMessage("LBL_ACTION"), // NOI18N
                removeActionAction, this, 0); // NOI18N
        ExUtils.setA11Y(titledPanel, ActionPanel.class, 
                "ActionTitlePanel"); // NOI18N

        nameLabel = new JLabel(getMessage("LBL_ACTION_NAME")); // NOI18N
        ExUtils.setA11Y(nameLabel, ActionPanel.class,
                "ActionNameLabel"); // NOI18N

        typeLabel = new JLabel(getMessage("LBL_ACTION_TYPE")); // NOI18N
        ExUtils.setA11Y(typeLabel, ActionPanel.class,
                "ActionTypeLabel"); // NOI18N
        
        nameEditor = new NameEditor(designView);
        ExUtils.setA11Y(nameEditor, ActionPanel.class,
                "ActionNameEditor"); // NOI18N

        typeChooser = new TypeChooser();
        typeChooser.addFocusListener(this);
        ExUtils.setA11Y(typeChooser, ActionPanel.class,
                "ActionTypeChooser"); // NOI18N
        
        localNotificationsPanel = new LocalNotificationsPanel(this, designView,
                action);
        
        add(nameLabel);
        add(nameEditor);
        
        add(typeLabel);
        add(typeChooser);
        
        add(localNotificationsPanel);
    }
    
    public TAction getAction() {
        return action;
    }

    public JComponent getView() {
        return titledPanel;
    }

    public void processWLMModelChanged() {
        nameEditor.updateContent();
        typeChooser.updateContent();
        localNotificationsPanel.processWLMModelChanged();
    }

    public void activateNode() {
        Node node = new ActionNode(getAction(), Children.LEAF,
                getNodeLookup());
        getDesignView().setActivatedNode(node);
    }

    @Override
    public Dimension getPreferredSize() {
        synchronized (getTreeLock()) {
            Dimension size11 = nameLabel.getPreferredSize();
            Dimension size12 = nameEditor.getPreferredSize();
            
            Dimension size21 = typeLabel.getPreferredSize();
            Dimension size22 = typeChooser.getPreferredSize();
            
            Dimension notificationSize = localNotificationsPanel
                    .getPreferredSize();
            
            Insets insets = getInsets();
            
            int col1 = ExUtils.maxWidth(size11, size21);
            int col2 = ExUtils.maxWidth(size12, size22);
            
            int row1 = ExUtils.maxHeight(size11, size12);
            int row2 = ExUtils.maxHeight(size21, size22);
            
            int w = Math.max(col1 + HGAP1 + col2, notificationSize.width);
            int h = row1 + VGAP1 + row2 + VGAP2 + notificationSize.height;
            
            w += insets.left + insets.right;
            h += insets.top + insets.bottom;
            
            return new Dimension(w, h);
        }
    }

    @Override
    public void doLayout() {
        synchronized (getTreeLock()) {
            Insets insets = getInsets();
            int x = insets.left;
            int w = getWidth() - x - insets.right;
            
            int y = insets.top;
            int h = getHeight() - y - insets.bottom;
            
            Dimension size11 = nameLabel.getPreferredSize();
            Dimension size12 = nameEditor.getPreferredSize();
            
            Dimension size21 = typeLabel.getPreferredSize();
            Dimension size22 = typeChooser.getPreferredSize();
            
            Dimension notificationSize = localNotificationsPanel
                    .getPreferredSize();
            
            int col1 = ExUtils.maxWidth(size11, size21);
            int col2 = ExUtils.maxWidth(size12, size22);
            
            col2 = Math.max(col2, (w - col1 - HGAP1) / 2);
            col2 = Math.min(col2, w - col1 - HGAP1);
            
            int row1 = ExUtils.maxHeight(size11, size12);
            int row2 = ExUtils.maxHeight(size21, size22);
            
            int x2 = x + col1 + HGAP1;
            
            int y2 = y + row1 + VGAP1;
            int y3 = y2 + row2 + VGAP2;
            
            nameLabel.setBounds(x, y, Math.min(col1, size11.width), row1);
            nameEditor.setBounds(x2, y, col2, row1);
            
            typeLabel.setBounds(x, y2, Math.min(col1, size21.width), row2);
            typeChooser.setBounds(x2, y2, col2, row2);
            
            h -= row1 + VGAP1 + row2 + VGAP2;
            
            localNotificationsPanel.setBounds(x, y3, w, h);
        }
    }

    public Widget getWidgetParent() {
        return widgetParent;
    }

    public Widget getWidget(int index) {
        if (index == 0) {
            return localNotificationsPanel;
        }

        throw new IndexOutOfBoundsException();
    }

    public int getWidgetCount() {
        return 1;
    }

    public Node getWidgetNode() {
        return new ActionNode(action, Children.LEAF, getNodeLookup());
    }

    public void requestFocusToWidget() {
        getDesignView().showActionsTab();
        scrollRectToVisible(new Rectangle(getSize()));
        if (!typeChooser.hasFocus()) {
            nameEditor.requestFocusInWindow();
        }
    }

    public WLMComponent getWidgetWLMComponent() {
        return action;
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.ACTION;
    }

    public void focusGained(FocusEvent e) {
        selectWidget(this);
    }

    public void focusLost(FocusEvent e) {
    }

    private class RemoveActionAction extends AbstractAction {
        RemoveActionAction() {
            super("Remove"); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            WLMModel model = getModel();
            TTask task = getTask();
            
            if (model.startTransaction()) {
                try {
                    task.removeAction(action);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }
    
    private class NameEditor extends TextFieldEditor {
        public NameEditor(DesignView designView) {
            super(designView, false);
        }
        
        @Override
        public String getModelValue() {
            String name = action.getName();
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
                    action.setName(value);
                } finally {
                    model.endTransaction();
                }
            }
        }

        @Override
        public void activateNode() {
            selectWidget(ActionPanel.this);
        }
    }
    
    private class TypeChooser extends JComboBox {
        private ActionListener actionListener;
        
        TypeChooser() {
            TActionType currentActionType = getModelValue();
            ActionTypeWrapper toSelect = null;
            TActionType[] actionTypes = TActionType.values();
            for (TActionType actionType : actionTypes) {
                ActionTypeWrapper wrapper = new ActionTypeWrapper(actionType);
                addItem(wrapper);
                if (actionType == currentActionType) {
                    toSelect = wrapper;
                }
            }
            setSelectedItem(toSelect);
            
            actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ActionTypeWrapper wrapper = (ActionTypeWrapper) 
                            getSelectedItem();
                    TActionType actionType = (wrapper == null) ? null
                            : wrapper.getActionType();
                    setModelValue(actionType);
                }
            };
            
            addActionListener(actionListener);
        }
        
        
        public void updateContent() {
            removeActionListener(actionListener);
            TActionType currentActionType = getModelValue();
            for (int i = getItemCount() - 1; i >= 0; i--) {
                ActionTypeWrapper item = (ActionTypeWrapper) getItemAt(i);
                if (item.getActionType() == currentActionType) {
                    setSelectedItem(item);
                    addActionListener(actionListener);
                    return;
                }
            }
            setSelectedItem(null);
            addActionListener(actionListener);
        }
        
        public TActionType getModelValue() {
            return action.getType();
        }
        
        public void setModelValue(TActionType actionType) {
            WLMModel model = ActionPanel.this.getModel();
            TAction action = ActionPanel.this.getAction();
            
            if (model.startTransaction()) {
                try {
                    action.setType(actionType);
                } finally {
                    model.endTransaction();
                }
            } 
        }
    }
    
    private static class ActionTypeWrapper {
        private TActionType actionType;
        
        ActionTypeWrapper(TActionType actionType) {
            this.actionType = actionType;
        }
        
        TActionType getActionType() {
            return actionType;
        }
        
        @Override
        public String toString() {
            return actionType.value();
        }
    }
    
    private static final int HGAP1 = 8;
    private static final int HGAP2 = 4;
    private static final int VGAP1 = 4;
    private static final int VGAP2 = 8;        
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(ActionPanel.class, key);
    }      
}
