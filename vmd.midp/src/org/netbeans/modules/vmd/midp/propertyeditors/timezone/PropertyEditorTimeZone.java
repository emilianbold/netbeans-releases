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

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
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
public class PropertyEditorTimeZone extends PropertyEditorUserCode implements PropertyEditorElement {
    private TimeZoneEditorPanel customEditor;
    private JRadioButton radioButton;
    
    private PropertyEditorTimeZone() {
        super();
        initComponents();
        
        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }
    
    public static final PropertyEditorTimeZone createInstance() {
        return new PropertyEditorTimeZone();
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorTimeZone.class, "LBL_VALUE")); // NOI18N
        customEditor = new TimeZoneEditorPanel();
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
        return (String) value.getValue();
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
            customEditor.setText((String) value.getValue());
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }
    
    private void saveValue(String text) {
        if (text.length() > 0) {
            super.setValue(MidpTypes.createStringValue(text));
        }
    }
    
    public void customEditorOKButtonPressed() {
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }
}
