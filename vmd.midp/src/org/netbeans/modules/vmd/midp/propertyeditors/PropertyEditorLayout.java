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
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
 * 
 * 
 */
public final class PropertyEditorLayout extends PropertyEditorUserCode implements PropertyEditorElement {

    private CustomEditorConstraints customEditor;
    private JRadioButton radioButton;
    private TypeID parentTypeID;
    private ItemLayouts layouts;
    private int bitMask;
    private HashMap<Integer, BitmaskItem> bits;

    private PropertyEditorLayout(TypeID parentTypeID) {
        super(NbBundle.getMessage(PropertyEditorLayout.class, "LBL_LAYOUT_STR_UCLABEL")); // NOI18N
        this.parentTypeID = parentTypeID;
        initMap();
    }

    public static final PropertyEditorLayout createInstance() {
        return new PropertyEditorLayout(null);
    }

    public static final PropertyEditorLayout createInstance(TypeID parentTypeID) {
        return new PropertyEditorLayout(parentTypeID);
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        radioButton = null;
        parentTypeID = null;
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorLayout.class, "LBL_LAYOUT_STR")); // NOI18N

        radioButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PropertyEditorLayout.class, "ACSN_LAYOUT_STR")); // NOI18N
        radioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PropertyEditorLayout.class, "ACSD_LAYOUT_STR")); // NOI18N

        customEditor = new CustomEditorConstraints();
    }

    public JComponent getCustomEditorComponent() {
        return customEditor;
    }

    private void combineMaskWithGuiName(Integer value, BitmaskItem bitmaskItem) {
        //guiItem.setSelected(layouts.isSet(bitmaskItem));
        bits.put(value, bitmaskItem);
    }

    void initMap() {
        bits = new HashMap<Integer, BitmaskItem>();
        layouts = new ItemLayouts(0);
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_DEFAULT, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_DEFAULT));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_2, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_2));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_LEFT, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_LEFT));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_RIGHT, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_RIGHT));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_CENTER, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_CENTER));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_TOP, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_TOP));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_BOTTOM, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_BOTTOM));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_VCENTER, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_VCENTER));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_NEWLINE_BEFORE, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_NEWLINE_BEFORE));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_NEWLINE_AFTER, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_NEWLINE_AFTER));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_SHRINK, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_SHRINK));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_VSHRINK, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_VSHRINK));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_EXPAND, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_EXPAND));
        combineMaskWithGuiName(ItemCD.VALUE_LAYOUT_VEXPAND, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_VEXPAND));
    }

    @Override
    public Component getCustomEditor() {
        assert EventQueue.isDispatchThread();
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
        return true;
    }

    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
       
        setConstant(MidpTypes.getInteger((PropertyValue) super.getValue()));
        return getBitmaskAsText();
    }

    public void setTextForPropertyValue(String text) {
    }

    public String getTextForPropertyValue() {
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

    private void setConstant(int bitmask) {
        layouts.setBitmask(bitmask);
        this.bitMask = bitmask;
    }

    public String getBitmaskAsText() {
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

        for (Integer intValue : bits.keySet()) {
            if (layouts.isSet(bits.get(intValue)) && bits.get(intValue).getAffectedBits() < 16 && bitmaskRadioButton1 < bits.get(intValue).getAffectedBits()) {
                bitmaskRadioButton1 = bits.get(intValue).getAffectedBits();
                bitmaskAsTextRadioButton1 = new StringBuffer(bits.get(intValue).getName());
                bitmaskAsTextRadioButton1.append(separator);
            } else if (layouts.isSet(bits.get(intValue)) && bits.get(intValue).getAffectedBits() >= 16 && bitmaskRadioButton2 < bits.get(intValue).getAffectedBits()) {
                bitmaskRadioButton2 = bits.get(intValue).getAffectedBits();
                bitmaskAsTextRadioButton2 = new StringBuffer(bits.get(intValue).getName());
                bitmaskAsTextRadioButton2.append(separator);

            } else if (layouts.isSet(bits.get(intValue)) &&
                       bits.get(intValue).getAffectedBits() != ItemCD.VALUE_LAYOUT_DEFAULT &&
                       bits.get(intValue).getAffectedBits() >= 0x30) {
                bitmaskAsTextCheckBoxes.append(bits.get(intValue).getName());
                bitmaskAsTextCheckBoxes.append(separator);
            }
        }

        bitmaskAsTextRadioButton1.append(bitmaskAsTextRadioButton2.append(bitmaskAsTextCheckBoxes));
        bitmaskAsTextRadioButton1.deleteCharAt(bitmaskAsTextRadioButton1.lastIndexOf(separator.toString().trim()));

        return bitmaskAsTextRadioButton1.toString();
    }

    private final class CustomEditorConstraints extends JPanel implements ItemListener {

        private JPanel generalPanel;
        private JPanel horizontalAlignmentPanel;
        private JPanel verticalAlignmentPanel;
        private JPanel newlinePanel;
        private JPanel shrinkPanel;
        private JPanel expandPanel;
        private JCheckBox defaultCheckBox;
        private JRadioButton horizontalAlignmentNoneCheckBox;
        private JRadioButton verticalAlignmentNoneCheckBox;
        private List<JToggleButton> guiItems;

        CustomEditorConstraints() {
            
            initComponents();
            setLayoutDefault(defaultCheckBox.isSelected());
        }

        void cleanUp() {
            layouts = null;
            if (bits != null) {
                bits.clear();
                bits = null;
            }
            generalPanel = null;
            horizontalAlignmentPanel = null;
            verticalAlignmentPanel = null;
            newlinePanel = null;
            shrinkPanel = null;
            expandPanel = null;
            defaultCheckBox = null;
            horizontalAlignmentNoneCheckBox = null;
            verticalAlignmentNoneCheckBox = null;
            this.removeAll();
        }

        void initComponents() {

            JToggleButton guiItem;
            JRadioButton centerButton;
            ButtonGroup buttonGroup;

            guiItems = new ArrayList<JToggleButton>();

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

            defaultCheckBox.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_GEN_DEFAULT"));        // NOI18N
            defaultCheckBox.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_GEN_DEFAULT"));        // NOI18N

            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_GEN_MIDP2")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_GEN_MIDP2")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_2));
            guiItems.add(guiItem);

            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_GEN_MIDP2"));          // NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_GEN_MIDP2"));          // NOI18N

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

            horizontalAlignmentNoneCheckBox.getAccessibleContext().
                    setAccessibleName(Bundle.getMessage("ACSN_ITEMLAYOUTPE_HOR_NONE"));      // NOI18N
            horizontalAlignmentNoneCheckBox.getAccessibleContext().
                    setAccessibleDescription(Bundle.getMessage("ACSD_ITEMLAYOUTPE_HOR_NONE"));      // NOI18N

            guiItem = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_LEFT")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_HOR_LEFT")); // NOI18N
            buttonGroup.add(guiItem);
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_LEFT));
            guiItems.add(guiItem);

            guiItem.getAccessibleContext().
                    setAccessibleName(Bundle.getMessage("ACSN_ITEMLAYOUTPE_HOR_LEFT"));      // NOI18N
            guiItem.getAccessibleContext().
                    setAccessibleDescription(Bundle.getMessage("ACSD_ITEMLAYOUTPE_HOR_LEFT"));      // NOI18N

            horizontalAlignmentPanel.add(guiItem);

            centerButton = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_CENTER")); // NOI18N
            centerButton.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_HOR_CENTER")); // NOI18N
            buttonGroup.add(centerButton);
            guiItems.add(centerButton);

            centerButton.getAccessibleContext().
                    setAccessibleName(Bundle.getMessage("ACSN_ITEMLAYOUTPE_HOR_CENTER"));      // NOI18N
            centerButton.getAccessibleContext().
                    setAccessibleDescription(Bundle.getMessage("ACSD_ITEMLAYOUTPE_HOR_CENTER"));      // NOI18N

            horizontalAlignmentPanel.add(centerButton);

            guiItem = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_RIGHT")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_HOR_RIGHT")); // NOI18N
            buttonGroup.add(guiItem);
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_RIGHT));
            guiItems.add(guiItem);

            guiItem.getAccessibleContext().
                    setAccessibleName(Bundle.getMessage("ACSN_ITEMLAYOUTPE_HOR_RIGHT"));      // NOI18N
            guiItem.getAccessibleContext().
                    setAccessibleDescription(Bundle.getMessage("ACSD_ITEMLAYOUTPE_HOR_RIGHT"));      // NOI18N

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

            verticalAlignmentNoneCheckBox.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_VER_NONE"));      // NOI18N
            verticalAlignmentNoneCheckBox.getAccessibleContext().
                    setAccessibleDescription(Bundle.getMessage("ACSD_ITEMLAYOUTPE_VER_NONE"));      // NOI18N

            guiItem = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_TOP")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_VER_TOP")); // NOI18N
            buttonGroup.add(guiItem);
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_TOP));
            guiItems.add(guiItem);
            verticalAlignmentPanel.add(guiItem);

            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_VER_TOP"));      // NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_VER_TOP"));      // NOI18N

            centerButton = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_CENTER")); // NOI18N
            centerButton.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_VER_CENTER")); // NOI18N
            buttonGroup.add(centerButton);
            guiItems.add(centerButton);

            centerButton.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_VER_CENTER"));     // NOI18N
            centerButton.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_VER_CENTER"));     // NOI18N
            verticalAlignmentPanel.add(centerButton);

            guiItem = new JRadioButton(Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_BOTTOM")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_VER_BOTTOM")); // NOI18N
            buttonGroup.add(guiItem);
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_BOTTOM));
            guiItems.add(guiItem);
            verticalAlignmentPanel.add(guiItem);

            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_VER_BOTTOM"));     // NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_VER_BOTTOM"));     // NOI18N

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

            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_NEWLINE"));     // NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_NEWLINE"));     // NOI18N

            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_NL_AFTER")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_NL_AFTER")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_NEWLINE_AFTER));
            guiItems.add(guiItem);
            newlinePanel.add(guiItem);

            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_NL_AFTER"));     // NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_NL_AFTER"));     // NOI18N

            this.add(newlinePanel);

            // Shrink
            shrinkPanel = new JPanel();
            shrinkPanel.setLayout(new GridLayout(1, 2));
            shrinkPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PNL_ITEMLAYOUTPE_SHRINK"))); // NOI18N
            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_SH_HORIZONTAL")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_SH_HORIZONTAL")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_SHRINK));
            guiItems.add(guiItem);

            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_SH_HORIZONTAL"));     // NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_SH_HORIZONTAL"));     // NOI18N

            shrinkPanel.add(guiItem);

            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_SH_VERTICAL")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_SH_VERTICAL")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_VSHRINK));
            guiItems.add(guiItem);
            shrinkPanel.add(guiItem);

            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_SH_VERTICAL"));     // NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_SH_VERTICAL"));     // NOI18N

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

            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_EX_HORIZONTAL"));     // NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_EX_HORIZONTAL"));     // NOI18N

            guiItem = new JCheckBox(Bundle.getMessage("LBL_ITEMLAYOUTPE_EX_VERTICAL")); // NOI18N
            guiItem.setMnemonic(Bundle.getChar("MNM_ITEMLAYOUTPE_EX_VERTICAL")); // NOI18N
            integrateGuiItem(guiItem, layouts.getBitmaskItem(ItemCD.VALUE_LAYOUT_VEXPAND));
            guiItems.add(guiItem);
            expandPanel.add(guiItem);

            guiItem.getAccessibleContext().setAccessibleName(
                    Bundle.getMessage("ACSN_ITEMLAYOUTPE_EX_VERTICAL"));     // NOI18N
            guiItem.getAccessibleContext().setAccessibleDescription(
                    Bundle.getMessage("ACSD_ITEMLAYOUTPE_EX_VERTICAL"));     // NOI18N

            this.add(expandPanel);

            // now add listeners to all guiItems
            for (JToggleButton button : guiItems) {
                button.addItemListener(this);
            }
        }

        private void integrateGuiItem(JToggleButton guiItem, BitmaskItem bitmaskItem) {
            guiItem.setSelected(layouts.isSet(bitmaskItem));
            guiItem.setName(Integer.toString(bitmaskItem.getAffectedBits()));
        }

        public void itemStateChanged(ItemEvent e) {
            Object component = e.getItemSelectable();
            JToggleButton tButton;

            if (!(component instanceof JToggleButton)) {
                return;
            } else {
                tButton = (JToggleButton) component;
            }

            String value =  tButton.getName();

            if (value.equals(defaultCheckBox.getName())) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                setLayoutDefault(selected);
                int newBitmask = 0;
                if (!selected) {
                    newBitmask = getComponentsBitmask().getBitmask();
                }
                bitMask = newBitmask;
            } else {
                BitmaskItem bitmaskItem = bits.get(new Integer(String.valueOf(value)));
                assert (bitmaskItem != null);
                boolean state = (e.getStateChange() == ItemEvent.SELECTED);
                bitMask = layouts.addToBitmask(bitmaskItem, state);
            }
        }

        private void setLayoutDefault(boolean layoutDefault) {
            // need to disable all groups except default button
            boolean nonDefault = !layoutDefault;

            for (Integer value : bits.keySet()) {
                for (JToggleButton tButton : guiItems) {
                    if (tButton.getName().equals(Integer.toString(value))) {
                        tButton.setEnabled(nonDefault);
                    }
                }
            }
            defaultCheckBox.setEnabled(true);
            // don't forget at horizontal/vertical none settings
            horizontalAlignmentNoneCheckBox.setEnabled(nonDefault);
            verticalAlignmentNoneCheckBox.setEnabled(nonDefault);
        }

        private ItemLayouts getComponentsBitmask() {
            ItemLayouts _layouts = new ItemLayouts(0);
            for (JToggleButton button : guiItems) {
                if (button.isSelected()) {
                    _layouts.addToBitmask(bits.get(Integer.valueOf(button.getName())), true);
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
    }
}
