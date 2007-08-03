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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midp.propertyeditors;


import java.awt.Component;

import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 *
 * @author Karol Harezlak
 */
public class BooleanInplaceEditor implements InplaceEditor {
    
    private JCheckBox checkBox;
    private DesignPropertyEditor propertyEditor;
    private PropertyModel model;
    
    // Do not create this InplaceEditor in PropertyEditor constructor!!!!!! 
    public BooleanInplaceEditor(DesignPropertyEditor propertyEditor) {    
        this.propertyEditor = propertyEditor;
        checkBox = new JCheckBox();
        PropertyValue value = (PropertyValue) propertyEditor.getValue();
        if (value != null && value.getKind() == PropertyValue.Kind.VALUE) {
            if (!(value.getPrimitiveValue() instanceof Boolean)) {
                Boolean selected = (Boolean) value.getPrimitiveValue();
                checkBox.setSelected(selected);
            }
        } else if (value == PropertyValue.createNull())
            checkBox.setSelected(false);
    }
    
    public void connect(PropertyEditor propertyEditor, PropertyEnv env) {
    }
    
    public JComponent getComponent() {
        checkBox.setBorder(BorderFactory.createEmptyBorder(0,3,0,0));
        return checkBox;
    }
    
    public void clear() {
    }
    
    public Object getValue() {
        return propertyEditor.getValue();
    }
    
    public void setValue(Object value) {
    }
    
    public boolean supportsTextEntry() {
        return true;
    }
    
    public void reset() {
    }
    
    public void addActionListener(ActionListener al) {
    }
    
    public void removeActionListener(ActionListener al) {
    }
    
    public KeyStroke[] getKeyStrokes() {
        return new KeyStroke[0];
    }
    
    public PropertyEditor getPropertyEditor() {
        return propertyEditor;
    }
    
    public PropertyModel getPropertyModel() {
        return model;
    }
    
    public void setPropertyModel(PropertyModel model) {
        this.model = model;
    }
    
    public boolean isKnownComponent(Component c) {
        return true;
    }
    
}
