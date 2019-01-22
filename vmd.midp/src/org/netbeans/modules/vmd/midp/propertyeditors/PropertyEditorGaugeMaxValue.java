/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.GaugeCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public final class PropertyEditorGaugeMaxValue extends PropertyEditorUserCode implements PropertyEditorElement {

    private static final String INDEFINITE_TEXT = NbBundle.getMessage(PropertyEditorGaugeMaxValue.class, "LBL_MAX_VALUE_INDEFINITE_TXT"); // NOI18N
    private static final String INDEFINITE_NUM_TEXT = String.valueOf(GaugeCD.VALUE_INDEFINITE);
    private CustomEditor customEditor;
    private JRadioButton radioButton;

    private PropertyEditorGaugeMaxValue() {
        super(NbBundle.getMessage(PropertyEditorGaugeMaxValue.class, "LBL_GAUGE_MAX_VALUE_UCLABEL")); // NOI18N
    }

    public static final PropertyEditorGaugeMaxValue createInstance() {
        return new PropertyEditorGaugeMaxValue();
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        radioButton = null;
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorGaugeMaxValue.class, "LBL_GAUGE_MAX_VALUE_STR")); // NOI18N
        customEditor = new CustomEditor();
    }

    public JComponent getCustomEditorComponent() {
        return customEditor;
    }

    @Override
    public Component getCustomEditor() {
        if (customEditor == null) {
            initComponents();
            initElements(Collections.<PropertyEditorElement>singleton(this));
        }

        return super.getCustomEditor();
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

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public void updateState(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            customEditor.unsetForever(true);
        } else if (MidpTypes.getInteger(value) == GaugeCD.VALUE_INDEFINITE) {
            customEditor.setForever(true);
        } else {
            customEditor.unsetForever(true);
            customEditor.setText(String.valueOf(value.getPrimitiveValue()));
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue(String text) {
        if (text.length() <= 0) {
            return;
        }

        if (INDEFINITE_TEXT.equals(text) || INDEFINITE_NUM_TEXT.equals(text)) {
            if (component != null && component.get() != null) {
                final DesignComponent _component = component.get();
                _component.getDocument().getTransactionManager().writeAccess(new Runnable() {

                    public void run() {
                        _component.writeProperty(GaugeCD.PROP_VALUE, MidpTypes.createIntegerValue(GaugeCD.VALUE_CONTINUOUS_IDLE));
                    }
                });
            }
            super.setValue(MidpTypes.createIntegerValue(GaugeCD.VALUE_INDEFINITE));
        } else {
            int intValue = 0;
            try {
                text = text.replaceAll("[^0-9\\-]+", ""); // NOI18N
                intValue = Integer.parseInt(text);
            } catch (NumberFormatException e) {
            }
            super.setValue(MidpTypes.createIntegerValue(intValue));
        }
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
            if (value == null || value.getPrimitiveValue() == null) {
                return false;
            }
            int intValue = MidpTypes.getInteger(value);
            return intValue != GaugeCD.VALUE_INDEFINITE;
        }
        return false;
    }

    private class CustomEditor extends JPanel implements ActionListener, DocumentListener, FocusListener {

        private JTextField textField;
        private JCheckBox foreverCheckBox;

        public CustomEditor() {
            radioButton.addFocusListener(this);
            initComponents();
        }

        void cleanUp() {
            radioButton.removeActionListener(this);
            textField = null;
            foreverCheckBox = null;
            this.removeAll();
        }

        private void initComponents() {
            setLayout(new GridBagLayout());

            textField = new JTextField();
            textField.getDocument().addDocumentListener(this);
            textField.addFocusListener(this);
            textField.setPreferredSize(new Dimension(100, textField.getPreferredSize().height));
            add(textField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            foreverCheckBox = new JCheckBox();
            Mnemonics.setLocalizedText(foreverCheckBox, NbBundle.getMessage(PropertyEditorGaugeMaxValue.class, "LBL_MAX_VALUE_INDEFINITE")); // NOI18N
            foreverCheckBox.addActionListener(this);
            foreverCheckBox.addFocusListener(this);
            add(foreverCheckBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));

            add(new JPanel(), new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
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
            setText(null);
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
            if (!Pattern.matches("[\\d\\-]+", textField.getText())) { // NOI18N
                displayWarning(PropertyEditorNumber.NON_DIGITS_TEXT);
            } else {
                clearErrorStatus();
            }
        }

        public void focusGained(FocusEvent e) {
            if (e.getSource() == radioButton || e.getSource() == textField || e.getSource() == foreverCheckBox) {
                checkNumberStatus();
            }
        }

        public void focusLost(FocusEvent e) {
            clearErrorStatus();
        }
    }
}
