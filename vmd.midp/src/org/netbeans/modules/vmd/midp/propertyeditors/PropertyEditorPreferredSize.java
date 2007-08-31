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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
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
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorPreferredSize extends PropertyEditorUserCode implements PropertyEditorElement {

    private static final String UNLOCKED_TEXT = NbBundle.getMessage(PropertyEditorPreferredSize.class, "LBL_PREF_SIZE_UNLOCKED_TXT"); // NOI18N
    private static final String UNLOCKED_NUM_TEXT = String.valueOf(ItemCD.UNLOCKED_VALUE.getPrimitiveValue());

    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private String label;

    private PropertyEditorPreferredSize(String label, String ucLabel) {
        super(ucLabel);
        this.label = label;
        initComponents();

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static PropertyEditorPreferredSize createInstance(String label, String ucLabel) {
        return new PropertyEditorPreferredSize(label, ucLabel);
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, label);
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
        Object unlockedValueValue = ItemCD.UNLOCKED_VALUE.getPrimitiveValue();
        if (unlockedValueValue.equals(valueValue)) {
            return UNLOCKED_TEXT;
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
            customEditor.unsetUnlocked(true);
        } else if (ItemCD.UNLOCKED_VALUE.getPrimitiveValue().equals(value.getPrimitiveValue())) {
            customEditor.setUnlocked(true);
        } else {
            customEditor.unsetUnlocked(true);
            customEditor.setText(String.valueOf(value.getPrimitiveValue()));
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue(String text) {
        if (text.length() > 0) {
            if (UNLOCKED_TEXT.equals(text) || UNLOCKED_NUM_TEXT.equals(text)) {
                super.setValue(ItemCD.UNLOCKED_VALUE);
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
            Object foreverValueValue = ItemCD.UNLOCKED_VALUE.getPrimitiveValue();
            return !foreverValueValue.equals(value.getPrimitiveValue());
        }
        return false;
    }

    private class CustomEditor extends JPanel implements ActionListener, DocumentListener, FocusListener {

        private JTextField textField;
        private JCheckBox unlockedCheckBox;

        public CustomEditor() {
            radioButton.addFocusListener(this);
            initComponents();
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            unlockedCheckBox = new JCheckBox();
            unlockedCheckBox.addActionListener(this);
            unlockedCheckBox.addFocusListener(this);
            Mnemonics.setLocalizedText(unlockedCheckBox, NbBundle.getMessage(PropertyEditorPreferredSize.class, "LBL_PREF_SIZE_UNLOCKED")); // NOI18N
            add(unlockedCheckBox, BorderLayout.NORTH);

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

        public void setUnlocked(boolean changeCheckBox) {
            setText(UNLOCKED_NUM_TEXT);
            textField.setEditable(false);
            if (changeCheckBox) {
                unlockedCheckBox.setSelected(true);
            }
        }

        public void unsetUnlocked(boolean changeCheckBox) {
            setText(null);
            textField.setEditable(true);
            if (changeCheckBox) {
                unlockedCheckBox.setSelected(false);
            }
        }

        public void actionPerformed(ActionEvent evt) {
            if (unlockedCheckBox.isSelected()) {
                setUnlocked(false);
            } else {
                unsetUnlocked(false);
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
            if (e.getSource() == radioButton || e.getSource() == textField || e.getSource() == unlockedCheckBox) {
                checkNumberStatus();
            }
        }

        public void focusLost(FocusEvent e) {
            clearErrorStatus();
        }
    }
}