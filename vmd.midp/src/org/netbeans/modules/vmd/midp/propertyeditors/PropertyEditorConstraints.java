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

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.general.Bitmask.BitmaskItem;
import org.netbeans.modules.vmd.midp.components.items.Constraints;
import org.netbeans.modules.vmd.midp.components.items.TextFieldCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 * @author Karol Harezlak
 */
public class PropertyEditorConstraints extends PropertyEditorUserCode implements PropertyEditorElement {

    private CustomEditorConstraints customEditor;
    private JRadioButton radioButton;

    private PropertyEditorConstraints() {
        super(NbBundle.getMessage(PropertyEditorConstraints.class, "LBL_CONSTR_UCLABEL")); // NOI18N
        initComponents();
        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static final PropertyEditorConstraints createInstance() {
        return new PropertyEditorConstraints();
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorConstraints.class, "LBL_CONSTR_STR")); // NOI18N

        radioButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PropertyEditorConstraints.class, "ACSN_CONSTR_STR")); // NOI18N
        radioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PropertyEditorConstraints.class, "ACSD_CONSTR_STR")); // NOI18N

        customEditor = new CustomEditorConstraints(0);
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        customEditor = null;
        radioButton = null;
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
        return true;
    }

    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
        customEditor.setBitmask(MidpTypes.getInteger((PropertyValue) super.getValue()));
        return customEditor.getBitmaskAsText();
    }

    public void setTextForPropertyValue(String text) {
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public void updateState(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            customEditor.setBitmask(0);
        } else {
            customEditor.setBitmask(MidpTypes.getInteger(value));
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue() {
        super.setValue(MidpTypes.createIntegerValue(customEditor.getBitMask()));
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue();
        }
    }

    @Override
    public Boolean canEditAsText() {
        return false;
    }

    private class CustomEditorConstraints extends JPanel implements ItemListener {

        private Constraints constraints;
        private Map<JToggleButton, BitmaskItem> radioButtonsMap;
        private Map<JToggleButton, BitmaskItem> checkBoxesMap;
        private List<JToggleButton> guiItems;
        private JRadioButton anyRadioButton;
        private JCheckBox passwordCheckBox;
        private int bitMask;

        public CustomEditorConstraints(int bitmask) {
            this.constraints = new Constraints(bitmask);
            this.radioButtonsMap = new HashMap<JToggleButton, BitmaskItem>();
            this.checkBoxesMap = new HashMap<JToggleButton, BitmaskItem>();
            this.guiItems = new ArrayList<JToggleButton>();
            this.bitMask = bitmask;
            initComponents();
        }

        void cleanUp() {
            constraints = null;
            radioButtonsMap.clear();
            radioButtonsMap = null;
            checkBoxesMap.clear();
            checkBoxesMap = null;
            guiItems.clear();
            guiItems = null;
            anyRadioButton = null;
            passwordCheckBox = null;
            this.removeAll();
        }

        private void initComponents() {
            JToggleButton guiItem;
            ButtonGroup buttonGroup = new ButtonGroup();

            setLayout(new GridLayout(6, 2));
            this.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PNL_TEXTFIELDPE_NAME"))); // NOI18N

            // ANY
            anyRadioButton = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_ANY).getDisplayName());
            anyRadioButton.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_ANY")); // NOI18N
            buttonGroup.add(anyRadioButton);
            radioButtonsMap.put(anyRadioButton, constraints.getBitmaskItem(TextFieldCD.VALUE_ANY));
            //anyRadioButton.setSelected( bitmask == TextFieldCD.VALUE_ANY);
            guiItems.add(anyRadioButton);
            this.add(anyRadioButton);
            anyRadioButton.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintAny"));// NOI18N
            anyRadioButton.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintAny"));// NOI18N


            // PASSWORD
            passwordCheckBox = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_PASSWORD).getDisplayName());
            passwordCheckBox.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_PASSWORD")); // NOI18N
            checkBoxesMap.put(passwordCheckBox, constraints.getBitmaskItem(TextFieldCD.VALUE_PASSWORD));
            guiItems.add(passwordCheckBox);
            this.add(passwordCheckBox);
            passwordCheckBox.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintPasswd"));// NOI18N
            passwordCheckBox.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintPasswd"));// NOI18N


            // NUMERIC
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_NUMERIC).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_NUMERIC")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_NUMERIC));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintNum"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintNum"));// NOI18N

            // UNEDITABLE
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_UNEDITABLE).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_UNEDITABLE")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_UNEDITABLE));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintUnedit"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintUnedit"));// NOI18N


            // EMAIL
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_EMAILADDR).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_EMAIL")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_EMAILADDR));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintEmail"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintEmail"));// NOI18N

            // SENSITIVE
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_SENSITIVE).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_SENSITIVE")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_SENSITIVE));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintSensetive"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintSensetive"));// NOI18N

            // PHONE NUMBER
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_PHONENUMBER).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_PHONE")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_PHONENUMBER));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintPhone"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintPhone"));// NOI18N

            // NON_PREDICTIVE
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_NON_PREDICTIVE).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_NONPREDICTIVE")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_NON_PREDICTIVE));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintNonPredictive"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintNonPredictive"));// NOI18N

            // URL
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_URL).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_URL")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_URL));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintUrl"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintUrl"));// NOI18N

            // INITIAL_CAPS_WORD
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_WORD).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_CAPS_WORD")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_WORD));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintInitial_CAPS_WORD"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintInitial_CAPS_WORD"));// NOI18N

            // DECIMAL
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_DECIMAL).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_DECIMAL")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_DECIMAL));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintDec"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintDec"));// NOI18N

            // INITIAL_CAPS_SENTENCE
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_SENTENCE).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_CAPS_SENTENCE")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_SENTENCE));
            guiItems.add(guiItem);
            this.add(guiItem);
            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ContraintInitial_CAPS_SENTENCE"));// NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ContraintInitial_CAPS_SENTENCE"));// NOI18N


            setGui();

            for (JToggleButton button : guiItems) {
                button.addItemListener(this);
            }
        }

        public void setBitmask(int bitmask) {
            for (JToggleButton button : guiItems) {
                button.removeItemListener(this);
            }
            constraints.setBitmask(bitmask);
            setGui();
            for (JToggleButton button : guiItems) {
                button.addItemListener(this);
            }
            this.bitMask = bitmask;
        }

        public void itemStateChanged(ItemEvent e) {
            constraints.setBitmask(0);
            for (JToggleButton button : radioButtonsMap.keySet()) {
                if (button.isSelected()) {
                    constraints.addToBitmask(radioButtonsMap.get(button), true);
                }
            }
            for (JToggleButton button : checkBoxesMap.keySet()) {
                if (button.isSelected()) {
                    constraints.addToBitmask(checkBoxesMap.get(button), true);
                }
            }
            bitMask = constraints.getBitmask();
        }

        private void setGui() {
            int radioButtonBitmask = 0;

            for (JToggleButton button : radioButtonsMap.keySet()) {
                if (constraints.isSet(radioButtonsMap.get(button)) && (radioButtonBitmask <= radioButtonsMap.get(button).getAffectedBits())) {
                    radioButtonBitmask = radioButtonsMap.get(button).getAffectedBits();
                    button.setSelected(true);
                }
            }
            for (JToggleButton button : checkBoxesMap.keySet()) {
                if (constraints.isSet(checkBoxesMap.get(button))) {
                    button.setSelected(true);
                }
            }
        }

        public int getBitMask() {
            return bitMask;
        }

        public String getBitmaskAsText() {
            int radioButtonBitmask = 0;
            StringBuffer bitmaskAsText = null;

            for (JToggleButton button : radioButtonsMap.keySet()) {
                if (constraints.isSet(radioButtonsMap.get(button)) && (radioButtonBitmask <= radioButtonsMap.get(button).getAffectedBits())) {
                    radioButtonBitmask = radioButtonsMap.get(button).getAffectedBits();
                    bitmaskAsText = new StringBuffer(radioButtonsMap.get(button).getName());
                }
            }
            for (JToggleButton button : checkBoxesMap.keySet()) {
                if (constraints.isSet(checkBoxesMap.get(button))) {
                    bitmaskAsText.append(" | "); // NOI18N
                    bitmaskAsText.append(checkBoxesMap.get(button).getName());
                }
            }

            return bitmaskAsText.toString();
        }
    }
}
