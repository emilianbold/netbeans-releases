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
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.general.Bitmask.BitmaskItem;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.items.ItemLayouts;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public final class PropertyEditorLayout extends PropertyEditorUserCode implements PropertyEditorElement {
    
    private CustomEditorConstraints customEditor;
    private JRadioButton radioButton;
    private TypeID parentTypeID;
    
    private PropertyEditorLayout(TypeID parentTypeID) {
        super(NbBundle.getMessage(PropertyEditorLayout.class, "LBL_LAYOUT_STR_UCLABEL")); // NOI18N
        this.parentTypeID = parentTypeID; 
        initComponents();
        
        initElements(Collections.<PropertyEditorElement>singleton(this));
    }
    
    public static final PropertyEditorLayout createInstance() {
        return new PropertyEditorLayout(null);
    }
    
    public static final PropertyEditorLayout createInstance(TypeID parentTypeID) {
        return new PropertyEditorLayout(parentTypeID);
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorLayout.class, "LBL_LAYOUT_STR")); // NOI18N
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
        customEditor.setConstant(MidpTypes.getInteger((PropertyValue) super.getValue()));
        return customEditor.getBitmaskAsText();
    }
    
    public void setTextForPropertyValue (String text) {
    }
    
    public String getTextForPropertyValue () {
        return null;
    }
    
    public void updateState(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            customEditor.setValue(0);
        } else {
            customEditor.setValue(MidpTypes.getInteger(value));
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

    private final class CustomEditorConstraints extends JPanel implements ItemListener {
        
        private ItemLayouts layouts;
        private Map<JToggleButton, BitmaskItem> bits;
        private JPanel generalPanel;
        private JPanel horizontalAlignmentPanel;
        private JPanel verticalAlignmentPanel;
        private JPanel newlinePanel;
        private JPanel shrinkPanel;
        private JPanel expandPanel;
        private JCheckBox defaultCheckBox;
        private JRadioButton horizontalAlignmentNoneCheckBox;
        private JRadioButton verticalAlignmentNoneCheckBox;
        
        private int bitMask;
        
        CustomEditorConstraints(int bitmask) {
            
            this.bitMask = bitmask;
            layouts = new ItemLayouts(bitmask);
            bits = new HashMap<JToggleButton, BitmaskItem>();
            
            initComponents();
            setLayoutDefault(defaultCheckBox.isSelected());
        }
        
        void initComponents() {
            
            JToggleButton guiItem;
            JRadioButton centerButton;
            ButtonGroup buttonGroup;
            
            List<JToggleButton> guiItems = new ArrayList<JToggleButton>();
            
            setLayout(new GridLayout(6, 1));
            generalPanel = new JPanel();
            generalPanel.setLayout(new GridLayout(1, 2));
            generalPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PNL_ITEMLAYOUTPE_GENERAL"))); // NOI18N
            
            defaultCheckBox = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_GEN_DEFAULT")); // NOI18N
            defaultCheckBox.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_GEN_DEFAULT")); // NOI18N
            integrateGuiItem(defaultCheckBox, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_DEFAULT));
            defaultCheckBox.setSelected(layouts.getBitmask() == 0);
            guiItems.add(defaultCheckBox);
            generalPanel.add(defaultCheckBox);
            
            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_GEN_MIDP2")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_GEN_MIDP2")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_2));
            guiItems.add(guiItem);
            generalPanel.add(guiItem);
            
            this.add(generalPanel);
            
            horizontalAlignmentPanel = new JPanel();
            horizontalAlignmentPanel.setLayout(new GridLayout(1, 4));
            horizontalAlignmentPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PNL_ITEMLAYOUTPE_HORIZONTAL"))); // NOI18N
            buttonGroup = new ButtonGroup();
            
            horizontalAlignmentNoneCheckBox = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_NONE")); // NOI18N
            horizontalAlignmentNoneCheckBox.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_HOR_NONE")); // NOI18N
            buttonGroup.add(horizontalAlignmentNoneCheckBox);
            horizontalAlignmentPanel.add(horizontalAlignmentNoneCheckBox);
            horizontalAlignmentNoneCheckBox.setSelected(true);
            
            guiItem = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_LEFT")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_HOR_LEFT")); // NOI18N
            buttonGroup.add(guiItem);
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_LEFT));
            guiItems.add(guiItem);
            horizontalAlignmentPanel.add(guiItem);
            
            centerButton = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_CENTER")); // NOI18N
            centerButton.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_HOR_CENTER")); // NOI18N
            buttonGroup.add(centerButton);
            guiItems.add(centerButton);
            horizontalAlignmentPanel.add(centerButton);
            
            guiItem = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_RIGHT")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_HOR_RIGHT")); // NOI18N
            buttonGroup.add(guiItem);
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_RIGHT));
            guiItems.add(guiItem);
            horizontalAlignmentPanel.add(guiItem);
            
            integrateGuiItem(centerButton, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_CENTER));
            
            this.add(horizontalAlignmentPanel);
            
            // vertical alignment
            verticalAlignmentPanel = new JPanel();
            verticalAlignmentPanel.setLayout(new GridLayout(1, 4));
            verticalAlignmentPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PNL_ITEMLAYOUTPE_VERTICAL"))); // NOI18N
            buttonGroup = new ButtonGroup();
            
            verticalAlignmentNoneCheckBox = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_NONE")); // NOI18N
            verticalAlignmentNoneCheckBox.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_VER_NONE")); // NOI18N
            verticalAlignmentNoneCheckBox.setSelected(true);
            buttonGroup.add(verticalAlignmentNoneCheckBox);
            verticalAlignmentPanel.add(verticalAlignmentNoneCheckBox);
            
            guiItem = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_TOP")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_VER_TOP")); // NOI18N
            buttonGroup.add(guiItem);
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_TOP));
            guiItems.add(guiItem);
            verticalAlignmentPanel.add(guiItem);
            
            centerButton = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_CENTER")); // NOI18N
            centerButton.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_VER_CENTER")); // NOI18N
            buttonGroup.add(centerButton);
            guiItems.add(centerButton);
            verticalAlignmentPanel.add(centerButton);
            
            guiItem = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_BOTTOM")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_VER_BOTTOM")); // NOI18N
            buttonGroup.add(guiItem);
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_BOTTOM));
            guiItems.add(guiItem);
            verticalAlignmentPanel.add(guiItem);
            
            integrateGuiItem(centerButton, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_VCENTER));
            
            this.add(verticalAlignmentPanel);
            
            newlinePanel = new JPanel();
            newlinePanel.setLayout(new GridLayout(1, 2));
            newlinePanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PNL_ITEMLAYOUTPE_NEWLINE"))); // NOI18N
            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_NL_BEFORE")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_NL_BEFORE")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_NEWLINE_BEFORE));
            guiItems.add(guiItem);
            newlinePanel.add(guiItem);
            
            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_NL_AFTER")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_NL_AFTER")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_NEWLINE_AFTER));
            guiItems.add(guiItem);
            newlinePanel.add(guiItem);
            
            this.add(newlinePanel);
            
            // Shrink
            shrinkPanel = new JPanel();
            shrinkPanel.setLayout(new GridLayout(1, 2));
            shrinkPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PNL_ITEMLAYOUTPE_SHRINK"))); // NOI18N
            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_SH_HORIZONTAL")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_SH_HORIZONTAL")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_SHRINK));
            guiItems.add(guiItem);
            shrinkPanel.add(guiItem);
            
            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_SH_VERTICAL")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_SH_VERTICAL")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_VSHRINK));
            guiItems.add(guiItem);
            shrinkPanel.add(guiItem);
            
            this.add(shrinkPanel);
            
            // Expand
            expandPanel = new JPanel();
            expandPanel.setLayout(new GridLayout(1, 2));
            expandPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PNL_ITEMLAYOUTPE_EXPAND"))); // NOI18N
            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_EX_HORIZONTAL")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_EX_HORIZONTAL")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_EXPAND));
            guiItems.add(guiItem);
            expandPanel.add(guiItem);
            
            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_EX_VERTICAL")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_EX_VERTICAL")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_VEXPAND));
            guiItems.add(guiItem);
            expandPanel.add(guiItem);
            
            this.add(expandPanel);
            
            // now add listeners to all guiItems
            for (JToggleButton button : guiItems){
                button.addItemListener(this);
            }
        }
        
        public void setConstant(int bitmask){
            layouts.setBitmask(bitmask);
            this.bitMask = bitmask;
        }
        
        public void itemStateChanged(ItemEvent e) {
            Object source = e.getItemSelectable();
            
            if (source == defaultCheckBox) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                setLayoutDefault(selected);
                int newBitmask = 0;
                if (!selected)
                    newBitmask = getComponentsBitmask().getBitmask();
                bitMask = newBitmask;
            } else {
                BitmaskItem bitmaskItem = bits.get(source);
                assert (bitmaskItem != null);
                boolean state = (e.getStateChange() == ItemEvent.SELECTED);
                bitMask = layouts.addToBitmask(bitmaskItem, state);
            }
        }
        
        private void integrateGuiItem(JToggleButton guiItem, BitmaskItem bitmaskItem) {
            guiItem.setSelected(layouts.isSet(bitmaskItem));
            bits.put(guiItem, bitmaskItem);
        }
        
        
        private void setLayoutDefault(boolean layoutDefault) {
            // need to disable all groups except default button
            boolean nonDefault = !layoutDefault;
            
            for (JToggleButton button : bits.keySet()){
                button.setEnabled(nonDefault);
            }
            defaultCheckBox.setEnabled(true);
            // don't forget at horizontal/vertical none settings
            horizontalAlignmentNoneCheckBox.setEnabled(nonDefault);
            verticalAlignmentNoneCheckBox.setEnabled(nonDefault);
        }
        
        private ItemLayouts getComponentsBitmask() {
            ItemLayouts _layouts = new ItemLayouts(0);
            for (JToggleButton button : bits.keySet()) {
                if (button.isSelected()) {
                    _layouts.addToBitmask(bits.get(button), true);
                }
            }
            
            return _layouts;
        }
        
        public void setValue(int value) {
            if (value == 0) {
                defaultCheckBox.setSelected(true);
                itemStateChanged(new ItemEvent(defaultCheckBox, ItemEvent.ITEM_STATE_CHANGED, null, ItemEvent.SELECTED));
            } else {
                setConstant(value);
            }
        }
        
        public int getBitMask() {
            return bitMask;
        }
        
        public String getBitmaskAsText(){
            if (layouts.getBitmask() == ItemCD.VALUE_LAYOUT_DEFAULT) {
                return layouts.getBitmaskItem(layouts.getBitmask()).getName();
            } else if (layouts.getBitmask() == ItemCD.VALUE_LAYOUT_2) {
                return layouts.getBitmaskItem(layouts.getBitmask()).getName();
            }
            
            int bitmaskRadioButton1 = 0;
            int bitmaskRadioButton2 = 0;
            StringBuffer bitmaskAsTextCheckBoxes = new StringBuffer();
            StringBuffer bitmaskAsTextRadioButton1 = new StringBuffer();
            StringBuffer bitmaskAsTextRadioButton2 = new StringBuffer();
            StringBuffer separator = new StringBuffer(" | "); // NOI18N
            
            for (JToggleButton button : bits.keySet()){
                if ( layouts.isSet(bits.get(button))
                        && button instanceof JRadioButton
                        && bits.get(button).getAffectedBits() < 16
                        && bitmaskRadioButton1 < bits.get(button).getAffectedBits() ) {
                    bitmaskRadioButton1 = bits.get(button).getAffectedBits();
                    bitmaskAsTextRadioButton1 = new StringBuffer(bits.get(button).getName());
                    bitmaskAsTextRadioButton1.append(separator);
                } else if ( layouts.isSet(bits.get(button))
                        && button instanceof JRadioButton
                        && bits.get(button).getAffectedBits() >= 16
                        && bitmaskRadioButton2 < bits.get(button).getAffectedBits() ) {
                    bitmaskRadioButton2 = bits.get(button).getAffectedBits();
                    bitmaskAsTextRadioButton2 = new StringBuffer(bits.get(button).getName());
                    bitmaskAsTextRadioButton2.append(separator);
                    
                } else if ( layouts.isSet(bits.get(button))
                        &&  button instanceof JCheckBox
                        &&  bits.get(button).getAffectedBits() != ItemCD.VALUE_LAYOUT_DEFAULT) {
                    bitmaskAsTextCheckBoxes.append(bits.get(button).getName());
                    bitmaskAsTextCheckBoxes.append(separator);
                }
            }
            
            bitmaskAsTextRadioButton1.append(bitmaskAsTextRadioButton2.append(bitmaskAsTextCheckBoxes));
            bitmaskAsTextRadioButton1.deleteCharAt(bitmaskAsTextRadioButton1.lastIndexOf(separator.toString().trim()));
            
            return bitmaskAsTextRadioButton1.toString();
        }
    }
}
