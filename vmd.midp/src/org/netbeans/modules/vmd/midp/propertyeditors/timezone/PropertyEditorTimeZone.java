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

package org.netbeans.modules.vmd.midp.propertyeditors.timezone;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorTimeZone extends PropertyEditorUserCode {
    
    private List<PropertyEditorElement> elements;
    private PredefinedEditor predefinedEditor;
    
    private PropertyEditorTimeZone() {
        super(NbBundle.getMessage(PropertyEditorTimeZone.class, "LBL_TIME_ZONE_UCLABEL")); // NOI18N
        
        elements = new ArrayList<PropertyEditorElement>(2);
        elements.add(predefinedEditor = new PredefinedEditor());
        elements.add(new CustomEditor());
        initElements(elements);
    }
    
    public static final PropertyEditorTimeZone createInstance() {
        return new PropertyEditorTimeZone();
    }
    
    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
        
        PropertyValue value = (PropertyValue) super.getValue();
        return (String) value.getPrimitiveValue();
    }
    
    private void saveValue(String text) {
        if (text.length() > 0) {
            super.setValue(MidpTypes.createStringValue(text));
        }
    }
    
    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        for (PropertyEditorElement element : elements) {
            if (element.getRadioButton().isSelected()) {
                saveValue(element.getTextForPropertyValue ());
                break;
            }
        }
    }
    
    private boolean isPredefined(String str) {
        return predefinedEditor.isPredefined(str);
    }
    
    private final class PredefinedEditor implements PropertyEditorElement, ActionListener {
        private JRadioButton radioButton;
        private TimeZoneComboboxModel model;
        private JComboBox combobox;
        
        public PredefinedEditor() {
            radioButton = new JRadioButton();
            Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorTimeZone.class, "LBL_PREDEFINED")); // NOI18N
            model = new TimeZoneComboboxModel();
            combobox = new JComboBox(model);
            combobox.addActionListener(this);
        }
        
        public boolean isPredefined(String str) {
            return model.contains(str);
        }
        
        public void updateState(PropertyValue value) {
            if (!isCurrentValueANull() && value != null) {
                String timeZone;
                for (int i = 0; i < model.getSize(); i++) {
                    timeZone = (String) model.getElementAt(i);
                    if (timeZone.equals((String) value.getPrimitiveValue())) {
                        model.setSelectedItem(timeZone);
                        break;
                    }
                }
            }
        }
        
        public void setTextForPropertyValue (String text) {
            saveValue(text);
        }
        
        public String getTextForPropertyValue () {
            return (String) combobox.getSelectedItem();
        }
        
        public JComponent getCustomEditorComponent() {
            return combobox;
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
        
        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
    
    private final class CustomEditor implements PropertyEditorElement, DocumentListener {
        private JRadioButton radioButton;
        private JTextField textField;
        
        public CustomEditor() {
            radioButton = new JRadioButton();
            Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorTimeZone.class, "LBL_CUSTOM")); // NOI18N
            textField = new JTextField();
            textField.getDocument().addDocumentListener(this);
        }
        
        public void updateState(PropertyValue value) {
            if (!isCurrentValueANull() && value != null) {
                String str = (String) value.getPrimitiveValue();
                if (!isPredefined(str)) { // if that value is not predefined
                    textField.setText(str);
                }
            } else {
                textField.setText(null);
            }
        }
        
        public void setTextForPropertyValue (String text) {
            saveValue(text);
        }
        
        public String getTextForPropertyValue () {
            return textField.getText();
        }
        
        public JComponent getCustomEditorComponent() {
            return textField;
        }
        
        public JRadioButton getRadioButton() {
            return radioButton;
        }
        
        public boolean isInitiallySelected() {
            return false;
        }
        
        public boolean isVerticallyResizable() {
            return false;
        }
        
        public void insertUpdate(DocumentEvent evt) {
            radioButton.setSelected(true);
        }
        
        public void removeUpdate(DocumentEvent evt) {
            radioButton.setSelected(true);
        }
        
        public void changedUpdate(DocumentEvent evt) {
        }
    }
}
