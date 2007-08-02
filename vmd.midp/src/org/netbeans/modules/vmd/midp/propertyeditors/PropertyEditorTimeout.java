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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public final class PropertyEditorTimeout extends PropertyEditorUserCode implements PropertyEditorElement {

    private static final String FOREVER_TEXT = NbBundle.getMessage(PropertyEditorTimeout.class, "LBL_TIMEOUTPE_FOREVER_TXT"); // NOI18N
    private static final String FOREVER_NUM_TEXT = String.valueOf(AlertCD.FOREVER_VALUE.getPrimitiveValue());

    private CustomEditor customEditor;
    private JRadioButton radioButton;

    private PropertyEditorTimeout() {
        initComponents();

        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }

    public static final PropertyEditorTimeout createInstance() {
        return new PropertyEditorTimeout();
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorTimeout.class, "LBL_TIMEOUT_STR")); // NOI18N
        customEditor = new CustomEditor();
    }

    public JComponent getCustomEditorComponent() {
        return customEditor;
    }

    public JRadioButton getRadioButton() {
        return radioButton;
    }

    public boolean isInitiallySelected() {
        return true;
    }

    public boolean isVerticallyResizable() {
        return false;
    }

    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }

        Object valueValue = ((PropertyValue) super.getValue()).getPrimitiveValue();
        Object foreverValueValue = AlertCD.FOREVER_VALUE.getPrimitiveValue();
        if (foreverValueValue.equals(valueValue)) {
            return FOREVER_TEXT;
        }
        return String.valueOf(valueValue);
    }

    public void setTextForPropertyValue (String text) {
        saveValue(text);
    }

    public String getTextForPropertyValue () {
        return null;
    }

    public void updateState(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            customEditor.unsetForever(true);
        } else if (AlertCD.FOREVER_VALUE.getPrimitiveValue().equals(value.getPrimitiveValue())) {
            customEditor.setForever(true);
        } else {
            customEditor.unsetForever(true);
            customEditor.setText(String.valueOf(value.getPrimitiveValue()));
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue(String text) {
        if (text.length() > 0) {
            if (FOREVER_TEXT.equals(text) || FOREVER_NUM_TEXT.equals(text)) {
                super.setValue(AlertCD.FOREVER_VALUE);
                return;
            }

            int intValue = 0;
            try {
                text = text.replaceAll("[^0-9\\-]+", ""); // NOI18N
                intValue = Integer.parseInt(text);
            } catch (NumberFormatException e) {
            }
            super.setValue(MidpTypes.createIntegerValue(intValue));
        }
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }

    @Override
    public Boolean canEditAsText() {
        if (!isCurrentValueAUserCodeType()) {
            PropertyValue value = (PropertyValue) super.getValue();
            if (value == null) {
                return false;
            }
            Object foreverValueValue = AlertCD.FOREVER_VALUE.getPrimitiveValue();
            return !foreverValueValue.equals(value.getPrimitiveValue());
        }
        return false;
    }

    private class CustomEditor extends JPanel implements ActionListener, DocumentListener, FocusListener {

        private JTextField textField;
        private JCheckBox foreverCheckBox;

        public CustomEditor() {
            radioButton.addFocusListener(this);
            initComponents();
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            foreverCheckBox = new JCheckBox();
            foreverCheckBox.addActionListener(this);
            foreverCheckBox.addFocusListener(this);
            Mnemonics.setLocalizedText(foreverCheckBox, NbBundle.getMessage(PropertyEditorTimeout.class, "LBL_TIMEOUTPE_FOREVER")); // NOI18N
            add(foreverCheckBox, BorderLayout.NORTH);

            textField = new JTextField();
            textField.getDocument().addDocumentListener(this);
            textField.addFocusListener(this);
            add(textField, BorderLayout.SOUTH);
        }

        public void setText(String text) {
            textField.setText(text);
        }

        public String getText() {
            return textField.getText();
        }

        public void setForever(boolean changeCheckBox) {
            setText(FOREVER_NUM_TEXT);
            textField.setEditable(false);
            if (changeCheckBox) {
                foreverCheckBox.setSelected(true);
            }
        }

        public void unsetForever(boolean changeCheckBox) {
            setText(null);
            textField.setEditable(true);
            if (changeCheckBox) {
                foreverCheckBox.setSelected(false);
            }
        }

        public void actionPerformed(ActionEvent evt) {
            if (foreverCheckBox.isSelected()) {
                setForever(false);
            } else {
                unsetForever(false);
            }
        }

        public void insertUpdate(DocumentEvent evt) {
            radioButton.setSelected(true);
            checkNumberStatus();
        }

        public void removeUpdate(DocumentEvent evt) {
            radioButton.setSelected(true);
            checkNumberStatus();
        }

        public void changedUpdate(DocumentEvent evt) {
        }

        private void checkNumberStatus() {
            if (!Pattern.matches("[\\d\\-]+", textField.getText())) { // NOI18N
                displayWarning(PropertyEditorNumber.NON_DIGITS_TEXT);
            } else {
                clearErrorStatus();
            }
        }

        public void focusGained(FocusEvent e) {
            if (e.getSource() == radioButton || e.getSource() == textField || e.getSource() == foreverCheckBox) {
                checkNumberStatus();
            }
        }

        public void focusLost(FocusEvent e) {
            clearErrorStatus();
        }
    }
}
