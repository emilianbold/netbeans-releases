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
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.general.Bitmask.BitmaskItem;
import org.netbeans.modules.vmd.midp.components.items.Constraints;
import org.netbeans.modules.vmd.midp.components.items.TextFieldCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
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
        customEditor = new CustomEditorConstraints(0);
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
    
    public void setTextForPropertyValue (String text) {
    }
    
    public String getTextForPropertyValue () {
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
            this.constraints =  new Constraints(bitmask);
            this.radioButtonsMap = new HashMap<JToggleButton, BitmaskItem>();
            this.checkBoxesMap = new HashMap<JToggleButton, BitmaskItem>();
            this.guiItems = new ArrayList<JToggleButton>();
            this.bitMask = bitmask;
            initComponents();
        }
        
        private void initComponents() {
            JToggleButton guiItem;
            ButtonGroup buttonGroup = new ButtonGroup();
            
            setLayout(new GridLayout(6,2));
            this.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PNL_TEXTFIELDPE_NAME"))); // NOI18N
            
            // ANY
            anyRadioButton = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_ANY).getDisplayName());
            anyRadioButton.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_ANY")); // NOI18N
            buttonGroup.add(anyRadioButton);
            radioButtonsMap.put(anyRadioButton, constraints.getBitmaskItem(TextFieldCD.VALUE_ANY));
            //anyRadioButton.setSelected( bitmask == TextFieldCD.VALUE_ANY);
            guiItems.add(anyRadioButton);
            this.add(anyRadioButton);
            
            // PASSWORD
            passwordCheckBox = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_PASSWORD).getDisplayName());
            passwordCheckBox.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_PASSWORD")); // NOI18N
            checkBoxesMap.put(passwordCheckBox, constraints.getBitmaskItem(TextFieldCD.VALUE_PASSWORD)) ;
            guiItems.add(passwordCheckBox);
            this.add(passwordCheckBox);
            
            
            // NUMERIC
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_NUMERIC).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_NUMERIC")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_NUMERIC));
            guiItems.add(guiItem);
            this.add(guiItem);
            
            // UNEDITABLE
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_UNEDITABLE).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_UNEDITABLE")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_UNEDITABLE));
            guiItems.add(guiItem);
            this.add(guiItem);
            
            
            // EMAIL
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_EMAILADDR).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_EMAIL")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_EMAILADDR));
            guiItems.add(guiItem);
            this.add(guiItem);
            
            // SENSITIVE
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_SENSITIVE).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_SENSITIVE")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_SENSITIVE));
            guiItems.add(guiItem);
            this.add(guiItem);
            
            // PHONE NUMBER
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_PHONENUMBER).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_PHONE")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_PHONENUMBER));
            guiItems.add(guiItem);
            this.add(guiItem);
            
            // NON_PREDICTIVE
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_NON_PREDICTIVE).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_NONPREDICTIVE")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_NON_PREDICTIVE));
            guiItems.add(guiItem);
            this.add(guiItem);
            
            // URL
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_URL).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_URL")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_URL));
            guiItems.add(guiItem);
            this.add(guiItem);
            
            // INITIAL_CAPS_WORD
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_WORD).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_CAPS_WORD")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_WORD));
            guiItems.add(guiItem);
            this.add(guiItem);
            
            // DECIMAL
            guiItem = new JRadioButton(constraints.getBitmaskItem(TextFieldCD.VALUE_DECIMAL).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_DECIMAL")); // NOI18N
            buttonGroup.add(guiItem);
            radioButtonsMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_DECIMAL));
            guiItems.add(guiItem);
            this.add(guiItem);
            
            // INITIAL_CAPS_SENTENCE
            guiItem = new JCheckBox(constraints.getBitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_SENTENCE).getDisplayName());
            guiItem.setMnemonic(Bundle.getChar("MNM_TEXTFIELDPE_CAPS_SENTENCE")); // NOI18N
            checkBoxesMap.put(guiItem, constraints.getBitmaskItem(TextFieldCD.VALUE_INITIAL_CAPS_SENTENCE));
            guiItems.add(guiItem);
            this.add(guiItem);
            
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
                if (constraints.isSet(radioButtonsMap.get(button)) && (radioButtonBitmask <= radioButtonsMap.get(button).getAffectedBits())){
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
                if (constraints.isSet(radioButtonsMap.get(button)) && (radioButtonBitmask <= radioButtonsMap.get(button).getAffectedBits())){
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
