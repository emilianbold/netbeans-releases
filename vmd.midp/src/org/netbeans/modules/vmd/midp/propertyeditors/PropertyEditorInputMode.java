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
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class PropertyEditorInputMode extends PropertyEditorUserCode {

    private static final String[] PREDEFINED_INPUT_MODES = {"UCB_BASIC_LATIN", "UCB_GREEK", "UCB_CYRILLIC", "UCB_ARMENIAN", "UCB_HEBREW", "UCB_ARABIC", "UCB_DEVANAGARI", "UCB_BENGALI", "UCB_THAI", "UCB_HIRAGANA", "UCB_KATAKANA", "UCB_HANGUL_SYLLABLES"}; // NOI18N
    private List<PropertyEditorElement> elements;

    private PropertyEditorInputMode() {
        super(NbBundle.getMessage(PropertyEditorInputMode.class, "LBL_INPUT_MODE_UCLABEL")); // NOI18N
        elements = new ArrayList<PropertyEditorElement>(2);
        elements.add(new PredefinedEditor());
        elements.add(new CustomEditor());
        initElements(elements);
    }

    public static final PropertyEditorInputMode createInstance() {
        return new PropertyEditorInputMode();
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (elements != null) {
            for (PropertyEditorElement e : elements) {
                if (e instanceof CleanUp) {
                    ((CleanUp) e).clean(component);
                }
            } if (elements != null) {
                elements.clear();
                elements = null;
            }
        }
    }

    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }

        PropertyValue value = (PropertyValue) super.getValue();
        return (String) value.getPrimitiveValue();
    }

    private void saveValue(String text) {
        if (text.length() > 0) {
            super.setValue(MidpTypes.createStringValue(text));
        }
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        for (PropertyEditorElement element : elements) {
            if (element.getRadioButton().isSelected()) {
                saveValue(element.getTextForPropertyValue());
                break;
            }
        }
    }

    private boolean isPredefined(String str) {
        for (String inputMode : PREDEFINED_INPUT_MODES) {
            if (inputMode.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private final class PredefinedEditor implements PropertyEditorElement, ActionListener, CleanUp {

        private JRadioButton radioButton;
        private DefaultComboBoxModel model;
        private JComboBox combobox;

        public PredefinedEditor() {
            radioButton = new JRadioButton();
            Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorInputMode.class, "LBL_PREDEFINED")); // NOI18N

            radioButton.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(PropertyEditorInputMode.class, "ACSN_PREDEFINED")); // NOI18N
            radioButton.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(PropertyEditorInputMode.class, "ACSD_PREDEFINED")); // NOI18N

            model = new DefaultComboBoxModel(PREDEFINED_INPUT_MODES);
            combobox = new JComboBox(model);

            combobox.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(PropertyEditorInputMode.class,
                    "ACSN_PREDEFINED_LIST")); // NOI18N
            combobox.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(PropertyEditorInputMode.class,
                    "ACSD_PREDEFINED_LIST")); // NOI18N

            combobox.addActionListener(this);
        }

        public void clean(DesignComponent component) {
            radioButton.removeActionListener(this);
            radioButton = null;
            combobox.removeActionListener(this);
            combobox = null;
        }

        public void updateState(PropertyValue value) {
            if (!isCurrentValueANull() && value != null) {
                String inputMode;
                for (int i = 0; i < model.getSize(); i++) {
                    inputMode = (String) model.getElementAt(i);
                    if (inputMode.equals((String) value.getPrimitiveValue())) {
                        model.setSelectedItem(inputMode);
                        break;
                    }
                }
            }
        }

        public void setTextForPropertyValue(String text) {
            saveValue(text);
        }

        public String getTextForPropertyValue() {
            return (String) combobox.getSelectedItem();
        }

        public JComponent getCustomEditorComponent() {
            return combobox;
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

        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }

    private final class CustomEditor implements PropertyEditorElement, DocumentListener, CleanUp {

        private JRadioButton radioButton;
        private JTextField textField;

        public CustomEditor() {
            radioButton = new JRadioButton();
            Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorInputMode.class, "LBL_CUSTOM")); // NOI18N

            radioButton.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(PropertyEditorInputMode.class, "ACSN_CUSTOM")); // NOI18N`
            radioButton.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(PropertyEditorInputMode.class, "ACSD_CUSTOM")); // NOI18N

            textField = new JTextField();
            textField.getDocument().addDocumentListener(this);

            textField.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(PropertyEditorInputMode.class, "ACSN_CUSTOM_VALUE")); // NOI18N`
            textField.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(PropertyEditorInputMode.class, "ACSD_CUSTOM_VALUE")); // NOI18N
        }

        public void clean(DesignComponent component) {
            radioButton = null;
            if (textField.getDocument() != null) {
                textField.getDocument().removeDocumentListener(this);
            }
            textField = null;
        }

        public void updateState(PropertyValue value) {
            if (!isCurrentValueANull() && value != null) {
                String str = (String) value.getPrimitiveValue();
                if (!isPredefined(str)) {
                    // if that value is not predefined
                    textField.setText(str);
                }
            } else {
                textField.setText(null);
            }
        }

        public void setTextForPropertyValue(String text) {
            saveValue(text);
        }

        public String getTextForPropertyValue() {
            return textField.getText();
        }

        public JComponent getCustomEditorComponent() {
            return textField;
        }

        public JRadioButton getRadioButton() {
            return radioButton;
        }

        public boolean isInitiallySelected() {
            return false;
        }

        public boolean isVerticallyResizable() {
            return false;
        }

        public void insertUpdate(DocumentEvent evt) {
            if (textField.hasFocus()) {
                radioButton.setSelected(true);
            }
        }

        public void removeUpdate(DocumentEvent evt) {
            if (textField.hasFocus()) {
                radioButton.setSelected(true);
            }
        }

        public void changedUpdate(DocumentEvent evt) {
        }
    }
}
