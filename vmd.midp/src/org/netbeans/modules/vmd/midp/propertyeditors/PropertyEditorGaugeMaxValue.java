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
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.GaugeCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public final class PropertyEditorGaugeMaxValue extends PropertyEditorUserCode implements PropertyEditorElement {
    
    private static final String INDEFINITE_TEXT = NbBundle.getMessage(PropertyEditorGaugeMaxValue.class, "LBL_MAX_VALUE_INDEFINITE_TXT"); // NOI18N
    private static final String INDEFINITE_NUM_TEXT = String.valueOf(GaugeCD.VALUE_INDEFINITE);
    
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    
    private PropertyEditorGaugeMaxValue() {
        super();
        initComponents();
        
        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }
    
    public static final PropertyEditorGaugeMaxValue createInstance() {
        return new PropertyEditorGaugeMaxValue();
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorGaugeMaxValue.class, "LBL_GAUGE_MAX_VALUE_STR")); // NOI18N
        customEditor = new CustomEditor();
    }
    
    public JComponent getComponent() {
        return customEditor;
    }
    
    public JRadioButton getRadioButton() {
        return radioButton;
    }
    
    public boolean isInitiallySelected() {
        return true;
    }
    
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
        
        PropertyValue value = (PropertyValue) super.getValue();
        if (value == null) {
            return INDEFINITE_TEXT;
        }
        int intValue = MidpTypes.getInteger(value);
        if (intValue == GaugeCD.VALUE_INDEFINITE) {
            return INDEFINITE_TEXT;
        }
        return String.valueOf(intValue);
    }
    
    public void setText(String text) {
        saveValue(text);
    }
    
    public String getText() {
        return null;
    }
    
    public void setPropertyValue(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            customEditor.unsetForever(true);
        } else if (MidpTypes.getInteger(value) == GaugeCD.VALUE_INDEFINITE) {
            customEditor.setForever(true);
        } else {
            customEditor.unsetForever(true);
            customEditor.setText(String.valueOf(value.getPrimitiveValue ()));
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }
    
    private void saveValue(String text) {
        if (text.length() > 0) {
            if (INDEFINITE_TEXT.equals(text) || INDEFINITE_NUM_TEXT.equals(text)) {
                super.setValue(MidpTypes.createIntegerValue(GaugeCD.VALUE_INDEFINITE));
                
                final DesignComponent component = ActiveDocumentSupport.getDefault().getActiveComponents().iterator().next();
                DesignDocument document = component.getDocument();
                document.getTransactionManager().writeAccess( new Runnable() {
                    public void run() {
                        component.writeProperty(GaugeCD.PROP_VALUE, MidpTypes.createIntegerValue(GaugeCD.VALUE_CONTINUOUS_IDLE));
                    }
                });
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
    
    public void customEditorOKButtonPressed() {
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }
    
    public boolean canEditAsText() {
        if (!isCurrentValueAUserCodeType()) {
            PropertyValue value = (PropertyValue) super.getValue();
            if (value == null) {
                return false;
            }
            int intValue = MidpTypes.getInteger(value);
            return intValue != GaugeCD.VALUE_INDEFINITE;
        }
        return false;
    }
    
    private class CustomEditor extends JPanel implements ActionListener, DocumentListener {
        private JTextField textField;
        private JCheckBox foreverCheckBox;
        
        public CustomEditor() {
            initComponents();
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            
            foreverCheckBox = new JCheckBox();
            foreverCheckBox.addActionListener(this);
            Mnemonics.setLocalizedText(foreverCheckBox, NbBundle.getMessage(PropertyEditorGaugeMaxValue.class, "LBL_MAX_VALUE_INDEFINITE")); // NOI18N
            add(foreverCheckBox, BorderLayout.NORTH);
            
            textField = new JTextField();
            textField.getDocument().addDocumentListener(this);
            add(textField, BorderLayout.SOUTH);
        }
        
        public void setText(String text) {
            textField.setText(text);
        }
        
        public String getText() {
            return textField.getText();
        }
        
        public void setForever(boolean changeCheckBox) {
            setText(INDEFINITE_NUM_TEXT);
            textField.setEditable(false);
            if (changeCheckBox) {
                foreverCheckBox.setSelected(true);
            }
        }
        
        public void unsetForever(boolean changeCheckBox) {
            setText("");
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
            checkText();
        }
        
        public void removeUpdate(DocumentEvent evt) {
            radioButton.setSelected(true);
            checkText();
        }
        
        public void changedUpdate(DocumentEvent evt) {
        }
        
        private void checkText() {
            String text = textField.getText();
            if (text.length() > 0) {
                boolean isTextCorrect = Pattern.matches("[\\d\\-]+", text); // NOI18N
                if (!isTextCorrect) {
                    displayWarning(PropertyEditorInteger.NON_DIGITS_TEXT);
                } else {
                    clearErrorStatus();
                }
            }
        }
        
    }
}
