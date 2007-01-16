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
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.points.CallPointCD;
import org.netbeans.modules.vmd.midp.components.points.IfPointCD;
import org.netbeans.modules.vmd.midp.components.points.MethodPointCD;
import org.netbeans.modules.vmd.midp.components.points.SwitchPointCD;
import org.netbeans.modules.vmd.midp.components.sources.SwitchCaseEventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;

/**
 *
 * @author Anton Chechel
 */
public final class PropertyEditorJavaString extends DesignPropertyEditor {
    
    private TypeID typeID;
    private final CustomEditor customEditor;
    
    private PropertyEditorJavaString(TypeID typeID) {
        this.typeID = typeID;
        customEditor = new CustomEditor();
    }
    
    public static final PropertyEditorJavaString createInstance(TypeID typeID) {
        return new PropertyEditorJavaString(typeID);
    }
    
    public Component getCustomEditor() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value != null) {
            customEditor.setText(MidpTypes.getJavaCode(value));
        }
        return customEditor;
    }
    
    public String getAsText() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value == null) {
            return PropertyEditorUserCode.NULL_TEXT;
        }
        return MidpTypes.getJavaCode(value);
    }
    
    public void setAsText(String text) {
        saveValue(text);
    }
    
    private void saveValue(String text) {
        if (text != null) {
            super.setValue(MidpTypes.createJavaCodeValue(text));
        }
    }
    
    public void customEditorOKButtonPressed() {
        String text = customEditor.getText();
//        if (text.length() > 0) {
            saveValue(text);
//        }
    }
    
    public boolean supportsDefaultValue() {
        return false;
    }
    
    public String getCustomEditorTitle() {
        return getLabelName();
    }
    
    private String getLabelName() {
        if (typeID.equals(CallPointCD.TYPEID)) {
            return "Java code";
        } else if (typeID.equals(MethodPointCD.TYPEID)) {
            return "Method Name";
        } else if (typeID.equals(IfPointCD.TYPEID)) {
            return "Condition Expression";
        } else if (typeID.equals(SwitchPointCD.TYPEID)) {
            return "Switch Operand:";
        } else if (typeID.equals(SwitchCaseEventSourceCD.TYPEID)) {
            return "Case Operand:";
        }
        return "Java Expression";
    }
    
    private final class CustomEditor extends JPanel {
        private JEditorPane textPane;
        
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
            constraints.fill = GridBagConstraints.BOTH;
            add(label, constraints);
            
            textPane = new JEditorPane();
            SwingUtilities.invokeLater(new Runnable() {//otherwise we get: java.lang.AssertionError: BaseKit.install() incorrectly called from non-AWT thread.
                public void run() {
                    textPane.setContentType("text/x-java"); // NOI18N
                }
            });
            textPane.setPreferredSize(new Dimension(400, 100));
            JScrollPane jsp = new JScrollPane();
            jsp.setViewportView(textPane);
            constraints.insets = new Insets(0, 12, 12, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            add(jsp, constraints);
        }
        
        public void setText(String text) {
            textPane.setText(text);
        }
        
        public String getText() {
            return textPane.getText();
        }
    }
}
