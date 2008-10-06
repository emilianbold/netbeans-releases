/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.midpnb.propertyeditors;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class PropertyEditorPhoneNumber extends PropertyEditorUserCode implements PropertyEditorElement {

    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private String label;
    private TypeID parentTypeID;

    private PropertyEditorPhoneNumber(String label, String ucLabel, TypeID parentTypeID) {
        super(ucLabel);
        this.label = label;
        this.parentTypeID = parentTypeID;
        initComponents();
        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        radioButton = null;
        label = null;
    }

    public static PropertyEditorPhoneNumber createInstance(String label, String ucLabel) {
        return new PropertyEditorPhoneNumber(label, ucLabel, null);
    }

    public static PropertyEditorPhoneNumber createInstance(String label, String ucLabel, TypeID parentTypeID) {
        return new PropertyEditorPhoneNumber(label, ucLabel, parentTypeID);
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, label);

        radioButton.getAccessibleContext().setAccessibleName(
                radioButton.getText());
        radioButton.getAccessibleContext().setAccessibleDescription(
                radioButton.getText());

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
        return String.valueOf(valueValue);
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public void updateState(PropertyValue value) {
        if (value != null) {
            customEditor.setText(String.valueOf(value.getPrimitiveValue()));
        } else
            customEditor.setText(""); //NOI18N
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue(String text) {
        text = text.replaceAll("[^0-9]", ""); // NOI18N
        super.setValue(MidpTypes.createStringValue(text));
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

        }
        return false;
    }

    @Override
    public boolean canWrite() {
        if (!isWriteableByParentType()) {
            return false;
        }

        return super.canWrite();
    }

    @Override
    public boolean supportsCustomEditor() {
        if (!isWriteableByParentType()) {
            return false;
        }

        return super.supportsCustomEditor();
    }

    private boolean isWriteableByParentType() {
        if (component == null || component.get() == null) {
            return false;
        }

        if (parentTypeID != null) {
            final DesignComponent _component = component.get();
            final DesignComponent[] parent = new DesignComponent[1];
            _component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    parent[0] = _component.getParentComponent();
                }
            });

            if (parent[0] != null && parentTypeID.equals(parent[0].getType())) {
                return false;
            }

        }
        return true;
    }

    private class CustomEditor
            extends JPanel implements ActionListener, DocumentListener, FocusListener {

        private JTextField textField;

        public CustomEditor() {
            radioButton.addFocusListener(this);
            initComponents();
        }

       void cleanUp() {
            if (textField != null && textField.getDocument() != null) {
                textField.getDocument().removeDocumentListener(this);
            }
            textField = null;
            this.removeAll();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            textField = new JTextField();
            textField.getDocument().addDocumentListener(this);
            textField.addFocusListener(this);
            
            textField.getAccessibleContext().setAccessibleName( 
                    radioButton.getAccessibleContext().getAccessibleName());
            
            textField.getAccessibleContext().setAccessibleDescription(
                    radioButton.getAccessibleContext().getAccessibleDescription());
            
            add(textField, BorderLayout.SOUTH);
        }

        public void setText(String text) {
            textField.setText(text);
        }

        public String getText() {
            return textField.getText();
        }

        public void actionPerformed(ActionEvent evt) {

        }

        public void insertUpdate(DocumentEvent evt) {
            if (textField.hasFocus()) {
                radioButton.setSelected(true);
                checkNumberStatus();
            }
        }

        public void removeUpdate(DocumentEvent evt) {
            if (textField.hasFocus()) {
                radioButton.setSelected(true);
                checkNumberStatus();
            }
        }

        public void changedUpdate(DocumentEvent evt) {
        }

        private void checkNumberStatus() {
            if (!Pattern.matches("[+\\d\\-]+", textField.getText())) { //NOI18N
                displayWarning(NbBundle.getMessage(PropertyEditorPhoneNumber.class, "LBL_PhoneNumber_Warning")); //NOI18N
            } else {
                clearErrorStatus();
            }

        }

        public void focusGained(FocusEvent e) {
            if (e.getSource() == radioButton || e.getSource() == textField) {
                checkNumberStatus();
            }
        }

        public void focusLost(FocusEvent e) {
            clearErrorStatus();
        }
    }
}
