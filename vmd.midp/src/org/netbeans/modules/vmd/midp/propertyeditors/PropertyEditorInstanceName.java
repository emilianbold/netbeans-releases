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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.points.MethodPointCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public final class PropertyEditorInstanceName extends DesignPropertyEditor {
    
    private TypeID typeID;
    private final CustomEditor customEditor;
    private DesignComponent component;
    private boolean canWrite;
    
    private PropertyEditorInstanceName(TypeID typeID) {
        this.typeID = typeID;
        customEditor = new CustomEditor();
    }
    
    public static final DesignPropertyEditor createInstance(TypeID typeID) {
        return new PropertyEditorInstanceName(typeID);
    }
    
    public Component getCustomEditor() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value != null) {
            customEditor.setText((String) value.getPrimitiveValue ());
        }
        return customEditor;
    }
    
    public String getAsText() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value == null) {
            return PropertyEditorUserCode.NULL_TEXT;
        }
        return (String) value.getPrimitiveValue ();
    }
    
    public void setAsText(String text) {
        String suggestedName = saveValue(text);
        customEditor.setText(suggestedName);
    }
    
    private String saveValue(final String text) {
        final String[] str = new String[1];
        component.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                PropertyValue newInstanceName = InstanceNameResolver.createFromSuggested(component, text);
                PropertyEditorInstanceName.super.setValue(newInstanceName);
                str[0] = (String) newInstanceName.getPrimitiveValue ();
            }
        });
        return str[0];
    }
    
    public void customEditorOKButtonPressed() {
        String text = customEditor.getText();
        if (text.length() > 0) {
            saveValue(text);
        }
    }
    
    public void init(DesignComponent component) {
        this.component = component;
    }
    
    public boolean supportsDefaultValue() {
        return false;
    }

    public boolean canWrite() {
        final DesignDocument document = component.getDocument();
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (document.getSelectedComponents().size() > 1)
                    canWrite = false;
                else
                    canWrite = true;
            }
        });
        
        return canWrite;
    }
    
    public String getCustomEditorTitle() {
        return getLabelName();
    }
    
    private String getLabelName() {
        if (typeID.equals(MethodPointCD.TYPEID)) {
            return "Method Name";
        }
        return "Instance Name";
    }
    
    private final class CustomEditor extends JPanel {
        private JTextField textField;
        
        public CustomEditor() {
            initComponents();
        }
        
        private void initComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            JLabel label = new JLabel(getLabelName() + ':');
            constraints.insets = new Insets(12, 12, 6, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.NONE;
            add(label, constraints);
            
            textField = new JTextField();
            constraints.insets = new Insets(0, 12, 12, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            add(textField, constraints);
            
            JPanel spacer = new JPanel();
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            add(spacer, constraints);
            setPreferredSize(new Dimension(300, 64));
        }
        
        public void setText(String text) {
            textField.setText(text);
        }
        
        public String getText() {
            return textField.getText();
        }
    }
}
