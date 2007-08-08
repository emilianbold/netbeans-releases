/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.event.ItemEvent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Collections;
import javax.swing.*;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorBooleanUC extends PropertyEditorUserCode implements PropertyEditorElement {

    private static final PropertyValue TRUE_VALUE = MidpTypes.createBooleanValue(true);
    private static final PropertyValue FALSE_VALUE = MidpTypes.createBooleanValue(false);

    private final String[] tags = {"true", "false"}; // NOI18N
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private BooleanInplaceEditor inplaceEditor;
    private boolean supportsCustomEditor;

    private PropertyEditorBooleanUC(boolean supportsCustomEditor) {
        super(NbBundle.getMessage(PropertyEditorBooleanUC.class, "LBL_VALUE_BOOLEAN_UCLABEL")); // NOI18N
        this.supportsCustomEditor = supportsCustomEditor;

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static PropertyEditorBooleanUC createInstance(boolean supportsCustomEditor) {
        return new PropertyEditorBooleanUC(supportsCustomEditor);
    }

    public static PropertyEditorBooleanUC createInstance() {
        return new PropertyEditorBooleanUC(true);
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
                    PropertyEditorBooleanUC.this.invokeSaveToModel();
                }
            });
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
        if (!supportsCustomEditor) {
            return false;
        }
        return super.supportsCustomEditor();
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
        }
        return radioButton;
    }

    @Override
    public boolean isPaintable() {
        PropertyValue propertyValue = (PropertyValue) getValue();
        return propertyValue.getKind() == PropertyValue.Kind.VALUE;
    }

    public boolean isVerticallyResizable() {
        return true;
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

    private void updateInplaceEditorComponent(boolean selected) {
        JCheckBox ic = (JCheckBox) inplaceEditor.getComponent();
        ic.setSelected(selected);
    }

    private class CustomEditor extends JPanel implements ActionListener {

        private JComboBox combobox;

        public CustomEditor() {
            initComponents();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            combobox = new JComboBox(tags);
            combobox.addActionListener(this);
            add(combobox, BorderLayout.CENTER);
        }

        public void setValue(PropertyValue value) {
            if (value == null || value.getPrimitiveValue() == null || !MidpTypes.getBoolean(value)) {
                combobox.setSelectedItem("false"); // NOI18N
            } else {
                combobox.setSelectedItem("true"); // NOI18N
            }
        }

        public String getText() {
            return (String) combobox.getSelectedItem();
        }

        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
}
