/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.util.Collections;
import javax.swing.*;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorBooleanUC extends PropertyEditorUserCode implements PropertyEditorElement {

    private static final PropertyValue TRUE_VALUE = MidpTypes.createBooleanValue(true);
    private static final PropertyValue FALSE_VALUE = MidpTypes.createBooleanValue(false);
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private BooleanInplaceEditor inplaceEditor;
    private boolean supportsCustomEditor;
    private TypeID parentTypeID;
    private String rbLabel;

   

    private PropertyEditorBooleanUC(boolean supportsCustomEditor, TypeID parentTypeID, String rbLabel) {
        super(NbBundle.getMessage(PropertyEditorBooleanUC.class, "LBL_VALUE_BOOLEAN_UCLABEL")); // NOI18N
        this.supportsCustomEditor = supportsCustomEditor;
        this.parentTypeID = parentTypeID;
        this.rbLabel = rbLabel;

        initElements(Collections.<PropertyEditorElement>singleton(this));
        super.getUserCodeRadioButton().addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                customEditor.checkBox.setSelected(false);
            }
            
            public void focusLost(FocusEvent e) {
            }
        });
    }

    public static PropertyEditorBooleanUC createInstance() {
        return new PropertyEditorBooleanUC(false, null, null);
    }

    public static PropertyEditorBooleanUC createInstance(String rbLabel) {
        return new PropertyEditorBooleanUC(true, null, rbLabel);
    }

    public static PropertyEditorBooleanUC createInstance(TypeID parentTypeID, String rbLabel) {
        return new PropertyEditorBooleanUC(true, parentTypeID, rbLabel);
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.cleanUp();
        }
        customEditor = null;
        radioButton = null;
        if (inplaceEditor != null) {
            inplaceEditor.cleanUp();
        }
        parentTypeID = null;
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        if (inplaceEditor == null) {
            inplaceEditor = new BooleanInplaceEditor(this);
            PropertyValue propertyValue = (PropertyValue) getValue();
            Boolean value = (Boolean) propertyValue.getPrimitiveValue();
            JCheckBox checkBox = (JCheckBox) inplaceEditor.getComponent();
            if (value != null) {
                checkBox.setSelected(value);
            }
            checkBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    JCheckBox checkBox = (JCheckBox) inplaceEditor.getComponent();
                    PropertyValue value = MidpTypes.createBooleanValue(checkBox.isSelected());
                    PropertyEditorBooleanUC.this.setValue(value);
                    //PropertyEditorBooleanUC.this.invokeSaveToModel();
                }
            });
        } else {
            PropertyValue propertyValue = (PropertyValue) getValue();
            Boolean value = (Boolean) propertyValue.getPrimitiveValue();
            JCheckBox checkBox = (JCheckBox) inplaceEditor.getComponent();
            if (value == null || value == false) {
                checkBox.setSelected(false);
            } else {
                checkBox.setSelected(true);
            }
        }
        return inplaceEditor;
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        JComponent _component = inplaceEditor.getComponent();
        _component.setSize(box.width, box.height);
        _component.doLayout();
        _component.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Graphics g = gfx.create(box.x, box.y, box.width, box.height);
        _component.setOpaque(false);
        _component.paint(g);
        g.dispose();
    }

    @Override
    public boolean supportsCustomEditor() {
        if (!isWriteableByParentType()) {
            return false;
        }

        return supportsCustomEditor ? super.supportsCustomEditor() : false;
    }

    public JComponent getCustomEditorComponent() {
        if (customEditor == null) {
            customEditor = new CustomEditor();
        }
        return customEditor;
    }

    public JRadioButton getRadioButton() {
        if (radioButton == null) {
            radioButton = new JRadioButton();
            Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorBooleanUC.class, "LBL_VALUE_BOOLEAN")); // NOI18N
            
            radioButton.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(PropertyEditorBooleanUC.class, 
                            "ACSN_VALUE_BOOLEAN")); // NOI18N
            radioButton.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(PropertyEditorBooleanUC.class, 
                            "ACSD_VALUE_BOOLEAN")); // NOI18N
        }
        return radioButton;
    }

    @Override
    public boolean isPaintable() {
        PropertyValue propertyValue = (PropertyValue) getValue();
        return propertyValue.getKind() == PropertyValue.Kind.VALUE;
    }

    public boolean isVerticallyResizable() {
        return false;
    }

    public boolean isInitiallySelected() {
        return false;
    }

    @Override
    public String getAsText() {
        if (isCurrentValueAUserCodeType()) {
            return USER_CODE_TEXT;
        } else if (isCurrentValueANull()) {
            return "false"; // NOI18N
        }
        return MidpTypes.getBoolean((PropertyValue) super.getValue()) ? "true" : "false"; // NOI18N
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public void updateState(PropertyValue value) {
        customEditor.setValue(value);
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue(String text) {
        super.setValue("false".equals(text) ? FALSE_VALUE : TRUE_VALUE); // NOI18N
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
            if ("true".equals(customEditor.getText())) { // NOI18N
                updateInplaceEditorComponent(true);
            } else {
                updateInplaceEditorComponent(false);
            }
        }
    }

    @Override
    public boolean canWrite() {
        if (!isWriteableByParentType()) {
            return false;
        }

        return MidpPropertyEditorSupport.singleSelectionEditAsTextOnly();
    }

    @Override
    public Object getDefaultValue() {
        PropertyValue value = (PropertyValue) super.getDefaultValue();
        if (value.getKind() == PropertyValue.Kind.VALUE && value.getPrimitiveValue() instanceof Boolean) {
            updateInplaceEditorComponent((Boolean) value.getPrimitiveValue());
        }
        return super.getDefaultValue();
    }
    
    private boolean isWriteableByParentType() {
        if (component == null || component.get() == null) {
            return false;
        }

        if (parentTypeID != null) {
            final DesignComponent _component = component.get();
            final DesignComponent[] parent = new DesignComponent[1];
            _component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    parent[0] = _component.getParentComponent();
                }
            });
            
            if (parent[0] != null && parentTypeID.equals(parent[0].getType())) {
                return false;
            }
        }
        return true;
    }

    private void updateInplaceEditorComponent(boolean selected) {
        JCheckBox ic = (JCheckBox) inplaceEditor.getComponent();
        ic.setSelected(selected);
    }

    private class CustomEditor extends JPanel implements ActionListener {

        private JCheckBox checkBox;

        public CustomEditor() {
            initComponents();
        }

        void cleanUp() {
            checkBox.removeActionListener(this);
            checkBox = null;
            this.removeAll();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            if (rbLabel != null) {
                Mnemonics.setLocalizedText(checkBox, rbLabel);
                
                checkBox.getAccessibleContext().setAccessibleName( 
                        checkBox.getText());
                checkBox.getAccessibleContext().setAccessibleDescription( 
                        checkBox.getText());
            }
            checkBox.addActionListener(this);
            add(checkBox, BorderLayout.CENTER);
        }

        public void setValue(PropertyValue value) {
            checkBox.setSelected(value != null && value.getPrimitiveValue() != null && MidpTypes.getBoolean(value));
        }

        public String getText() {
            return checkBox.isSelected() ? "true" : "false"; // NOI18N
        }

        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
}