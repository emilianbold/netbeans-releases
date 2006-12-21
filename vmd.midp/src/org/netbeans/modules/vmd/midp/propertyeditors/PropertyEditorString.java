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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.TextBoxCD;
import org.netbeans.modules.vmd.midp.components.items.TextFieldCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public class PropertyEditorString extends PropertyEditorUserCode implements PropertyEditorElement {
    
    public static final int DEPENDENCE_NONE = 0;
    public static final int DEPENDENCE_TEXT_BOX = 1;
    public static final int DEPENDENCE_TEXT_FIELD = 2;
    
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private int dependance;
    
    private PropertyEditorString(int dependance) {
        super();
        this.dependance = dependance;
        initComponents();
        
        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }
    
    public static final PropertyEditorString createInstance() {
        return new PropertyEditorString(DEPENDENCE_NONE);
    }
    
    public static final PropertyEditorString createInstance(int dependance) {
        return new PropertyEditorString(dependance);
    }
    
    public static final PropertyEditorString createInstanceReadOnly() {
        return new PropertyEditorString(DEPENDENCE_NONE) {
            public boolean canWrite() {
                return false;
            }
        };
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorString.class, "LBL_STRING_STR")); // NOI18N
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
        final int length = text.length();
        if (length > 0) {
            super.setValue(MidpTypes.createStringValue(text));
            final DesignComponent component = ActiveDocumentSupport.getDefault().getActiveComponents().iterator().next();
            DesignDocument document = component.getDocument();
            switch (dependance) {
                case DEPENDENCE_TEXT_BOX:
                    document.getTransactionManager().writeAccess( new Runnable() {
                        public void run() {
                            PropertyValue value = component.readProperty(TextBoxCD.PROP_MAX_SIZE);
                            if (MidpTypes.getInteger(value) < length) {
                                component.writeProperty(TextBoxCD.PROP_MAX_SIZE, MidpTypes.createIntegerValue(length));
                            }
                        }
                    });
                    break;
                case DEPENDENCE_TEXT_FIELD:
                    document.getTransactionManager().writeAccess( new Runnable() {
                        public void run() {
                            PropertyValue value = component.readProperty(TextFieldCD.PROP_MAX_SIZE);
                            if (MidpTypes.getInteger(value) < length) {
                                component.writeProperty(TextFieldCD.PROP_MAX_SIZE, MidpTypes.createIntegerValue(length));
                            }
                        }
                    });
                    break;
            }
        }
    }
    
    public void customEditorOKButtonPressed() {
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }
    
    private class CustomEditor extends JPanel {
        private JEditorPane editorPane;
        
        public CustomEditor() {
            initComponents();
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(400, 100));
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewportView(editorPane);
            add(scrollPane, BorderLayout.CENTER);
        }
        
        public void setText(String text) {
            editorPane.setText(text);
        }
        
        public String getText() {
            return editorPane.getText();
        }
    }
}
