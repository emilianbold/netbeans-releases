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

package org.netbeans.modules.vmd.midp.propertyeditors.date;

import java.awt.BorderLayout;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public final class PropertyEditorDate extends PropertyEditorUserCode implements PropertyEditorElement {
    
    private static final DateFormat DATE_TIME_FORMAT = DateFormat.getDateTimeInstance();
    
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    
    private PropertyEditorDate() {
        super();
        initComponents();
        
        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }
    
    public static final DesignPropertyEditor createInstance() {
        return new PropertyEditorDate();
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorDate.class, "LBL_DATE_STR")); // NOI18N
        customEditor = new CustomEditor();
    }
    
    public JComponent getComponent() {
        return customEditor;
    }
    
    public JRadioButton getRadioButton() {
        return radioButton;
    }
    
    public boolean isInitiallySelected() {
        return false;
    }
    
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
        
        return getValueAsText((PropertyValue) super.getValue());
    }
    
    public void setText(String text) {
        saveValue(text);
    }
    
    public String getText() {
        return null;
    }

    public void setPropertyValue(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            customEditor.setText("");
        } else {
            customEditor.setText(getValueAsText(value));
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }
    
    private void saveValue(String text) {
        if (text.length() > 0) {
            try {
                Date date = DATE_TIME_FORMAT.parse(text);
                super.setValue(MidpTypes.createLongValue(date.getTime()));
            } catch (ParseException ex) {
            }
        }
    }
    
    public void customEditorOKButtonPressed() {
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }
    
    private String getValueAsText(PropertyValue value) {
        Date date = new Date();
        Object valueValue = value.getValue();
        date.setTime((Long) valueValue);
        return DATE_TIME_FORMAT.format(date);
    }
    
    private class CustomEditor extends JPanel {
        private JFormattedTextField textField;
        
        public CustomEditor() {
            initComponents();
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            textField = new JFormattedTextField(DATE_TIME_FORMAT);
            textField.setToolTipText("Enter date in \"dd.mm.yyyy hh:mm:ss\" format");
            add(textField, BorderLayout.CENTER);
        }
        
        public void setText(String text) {
            textField.setText(text);
        }
        
        public String getText() {
            return textField.getText();
        }
    }
}
