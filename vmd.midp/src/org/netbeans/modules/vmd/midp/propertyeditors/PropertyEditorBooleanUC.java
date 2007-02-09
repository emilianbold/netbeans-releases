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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorBooleanUC extends PropertyEditorUserCode implements PropertyEditorElement {
    
    private static final PropertyValue TRUE_VALUE = MidpTypes.createBooleanValue(true);
    private static final PropertyValue FALSE_VALUE = MidpTypes.createBooleanValue(false);
    private static final String TRUE_TEXT = String.valueOf(MidpTypes.getBoolean(TRUE_VALUE));
    private static final String FALSE_TEXT = String.valueOf(MidpTypes.getBoolean(FALSE_VALUE));
    
    private String[] tags = {TRUE_TEXT, FALSE_TEXT};
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    
    private InplaceEditor inplaceEditor;
    
    private PropertyEditorBooleanUC() {
        super();
        initComponents();
        
        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }
    
    public static PropertyEditorBooleanUC createInstance() {
        return new PropertyEditorBooleanUC();
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorBooleanUC.class, "LBL_VALUE_BOOLEAN")); // NOI18N
        customEditor = new CustomEditor();
    }
    
    public JComponent getComponent() {
        return customEditor;
    }
    
    public JRadioButton getRadioButton() {
        return radioButton;
    }
    
    public boolean isVerticallyResizable() {
        return true;
    }
    
    public boolean isInitiallySelected() {
        return false;
    }
    
    public String getAsText() {
        if (isCurrentValueAUserCodeType()) {
            return USER_CODE_TEXT;
        } else if (isCurrentValueANull()) {
            return FALSE_TEXT;
        }
        
        return MidpTypes.getBoolean((PropertyValue) super.getValue()) ? TRUE_TEXT : FALSE_TEXT;
    }
    
    public void setText(String text) {
        saveValue(text);
    }
    
    public String getText() {
        return null;
    }
    
    public void setPropertyValue(PropertyValue value) {
        customEditor.setValue(value);
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }
    
    private void saveValue(String text) {
        super.setValue(FALSE_TEXT.equals(text) ? FALSE_VALUE : TRUE_VALUE);
    }
    
    public void customEditorOKButtonPressed() {
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }
    
    public String[] getTags() {
        return isCurrentValueAUserCodeType() ? null : tags;
    }
    
    public boolean canWrite() {
        if (!MidpPropertyEditorSupport.singleSelectionEditAsTextOnly()) {
            return false;
        }
        return true;
    }
    
//    public InplaceEditor getInplaceEditor() {
//        if (inplaceEditor == null) {
//            inplaceEditor = new CheckboxInplaceEditor();
//        }
//        return inplaceEditor;
//    }
    
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
                combobox.setSelectedItem(FALSE_TEXT);
            } else {
                combobox.setSelectedItem(TRUE_TEXT);
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
