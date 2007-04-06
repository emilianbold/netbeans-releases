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

package org.netbeans.modules.form;

import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

import java.awt.*;
import java.beans.PropertyEditor;
import javax.swing.*;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 *
 * @author  Ian Formanek, Vladimir Zboril
 */
public class FormCustomEditor extends JPanel
                              implements EnhancedCustomPropertyEditor
{
    private static final int DEFAULT_WIDTH  = 350;
    private static final int DEFAULT_HEIGHT = 350;

    // -----------------------------------------------------------------------------
    // Private variables

    private FormPropertyEditor editor;
    private PropertyEditor[] allEditors;
    private Component[] allCustomEditors;
    private boolean[] validValues;
    private int originalEditorIndex;

    private javax.swing.JPanel cardPanel;
    private javax.swing.JComboBox editorsCombo;

    /** Creates new form FormCustomEditor */
    public FormCustomEditor(FormPropertyEditor editor,
                            Component currentCustomEditor)
    {
        JLabel modeLabel = new JLabel();
        editorsCombo = new JComboBox();
        editorsCombo.setRenderer(new EditorComboRenderer());
        JPanel borderPanel = new JPanel(); // panel with a border containing the panel with editors (cardPanel)
        borderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(1, 1, 2, 2)));
        borderPanel.setLayout(new BorderLayout());
        cardPanel = new JPanel();
        cardPanel.setLayout(new CardLayout());
        borderPanel.add(cardPanel, BorderLayout.CENTER);

        FormProperty property = editor.getProperty();
        String selectModeText;
        if (property instanceof RADProperty) {
            selectModeText = FormUtils.getFormattedBundleString("FMT_EditingModeLabel1", // NOI18N
                new Object[] { ((RADProperty)property).getRADComponent().getName(),
                                property.getName() });
        } else {
            selectModeText = FormUtils.getFormattedBundleString("FMT_EditingModeLabel2", // NOI18N
                new Object[] { property.getName() });
        }
        Mnemonics.setLocalizedText(modeLabel, selectModeText);
        editorsCombo.setToolTipText(FormUtils.getBundleString("EditingMode_Hint")); // NOI18N
        modeLabel.setLabelFor(editorsCombo);

        this.editor = editor;
        allEditors = editor.getAllEditors();

        PropertyEditor currentEditor = editor.getCurrentEditor();

        allCustomEditors = new Component[allEditors.length];
        validValues = new boolean[allEditors.length];

        PropertyEnv env = editor.getPropertyEnv();
        Object currentValue = editor.getValue();

        // go through all available property editors, set their values and
        // setup their custom editors
        for (int i=0; i < allEditors.length; i++) {
            PropertyEditor prEd = allEditors[i];
            boolean current = currentEditor == prEd;
            boolean valueSet = false;
            Component custEd = null;

            if (current) {
                valueSet = true;
                custEd = currentCustomEditor;
            }
            else {
                editor.getPropertyContext().initPropertyEditor(prEd, property);
                if (env != null && prEd instanceof ExPropertyEditor)
                    ((ExPropertyEditor)prEd).attachEnv(env);

                if (currentValue != null) {
                    try {
                        if (editor.getPropertyType().isAssignableFrom(
                                               currentValue.getClass()))
                        {   // currentValue is a real property value corresponding
                            // to property editor value type
                            prEd.setValue(currentValue);
                            valueSet = true;
                        }
                        else if (currentValue instanceof FormDesignValue) {
                            Object realValue = // get real value of the design value
                                ((FormDesignValue)currentValue).getDesignValue();
                            if (realValue != FormDesignValue.IGNORED_VALUE) {
                                // there is a known real value
                                prEd.setValue(realValue); 
                                valueSet = true;
                            }
                        }
                    }
                    catch (IllegalArgumentException ex) {} // ignore
                }
                // [null value should not be set?]

                if (!valueSet) {
                    // no reasonable value for this property editor, try to
                    // set the default value
                    Object defaultValue = property.getDefaultValue();
                    if (defaultValue != BeanSupport.NO_VALUE) {
                        prEd.setValue(defaultValue);
                        valueSet = true;
                    }
                    // [but if there's no default value it is not possible to
                    // switch to this property editor and enter something - see
                    // getPropertyValue() - it returns BeanSupport.NO_VALUE]
                }

                if (prEd.supportsCustomEditor())
                    custEd = prEd.getCustomEditor();
            }

            validValues[i] = valueSet;

            String editorName;
            if (prEd instanceof NamedPropertyEditor) {
                editorName = ((NamedPropertyEditor)prEd).getDisplayName();
            } else {
                editorName = i == 0 ?
                    FormUtils.getBundleString("CTL_DefaultEditor_DisplayName") // NOI18N
                    : Utilities.getShortClassName(prEd.getClass());
            }

            if (custEd == null || custEd instanceof Window) {
                JPanel p = new JPanel(new GridBagLayout());
                JLabel label = new JLabel(
                    FormUtils.getBundleString("CTL_PropertyEditorDoesNot")); // NOI18N
                p.add(label);
                p.getAccessibleContext().setAccessibleDescription(label.getText());
                custEd = p;
            }

            allCustomEditors[i] = custEd;
            cardPanel.add(editorName, custEd);
            editorsCombo.addItem(editorName);
            if (current) {
                originalEditorIndex = i;
                editorsCombo.setSelectedIndex(i);
                updateAccessibleDescription(custEd);
            }
        }

        // build layout when the combo box is filled
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .add(layout.createParallelGroup(GroupLayout.LEADING)
                .add(borderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createSequentialGroup()
                    .add(modeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(editorsCombo, GroupLayout.PREFERRED_SIZE, editorsCombo.getPreferredSize().width*5/4, GroupLayout.PREFERRED_SIZE)))
            .addContainerGap()
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .add(layout.createParallelGroup(GroupLayout.BASELINE)
                .add(modeLabel)
                .add(editorsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.UNRELATED)
            .add(borderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, (String) editorsCombo.getSelectedItem());

        editorsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CardLayout cl2 = (CardLayout) cardPanel.getLayout();
                cl2.show(cardPanel, (String) editorsCombo.getSelectedItem());

                int i = editorsCombo.getSelectedIndex();
                HelpCtx helpCtx = i < 0 ? null :
                                  HelpCtx.findHelp(cardPanel.getComponent(i));
                String helpID = helpCtx != null ? helpCtx.getHelpID() : ""; // NOI18N
                HelpCtx.setHelpIDString(FormCustomEditor.this, helpID);

                updateAccessibleDescription(i < 0 ? null : cardPanel.getComponent(i));
            }
        });

        editorsCombo.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_EditingMode")); // NOI18N
    }
    
    private void updateAccessibleDescription(Component comp) {
        if (comp instanceof javax.accessibility.Accessible
            && comp.getAccessibleContext().getAccessibleDescription() != null) {

            getAccessibleContext().setAccessibleDescription(
                FormUtils.getFormattedBundleString(
                    "ACSD_FormCustomEditor", // NOI18N
                    new Object[] {
                        comp.getAccessibleContext().getAccessibleDescription()
                    }
                )
            );
        } else {
            getAccessibleContext().setAccessibleDescription(null);
        }
    }

    public Dimension getPreferredSize() {
        Dimension inh = super.getPreferredSize();
        return new Dimension(Math.max(inh.width, DEFAULT_WIDTH), Math.max(inh.height, DEFAULT_HEIGHT));
    }

    private class EditorComboRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (index == originalEditorIndex && editorsCombo.isPopupVisible()) {
                setFont(list.getFont().deriveFont(Font.BOLD));
            }
            return this;
        }
    }

    // -----------------------------------------------------------------------------
    // EnhancedCustomPropertyEditor implementation

    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does
     * not contain a valid property value (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        int currentIndex = editorsCombo.getSelectedIndex();
        PropertyEditor currentEditor = currentIndex > -1 ?
                                       allEditors[currentIndex] : null;
        Component currentCustomEditor = currentIndex > -1 ?
                                        allCustomEditors[currentIndex] : null;
        Object value;

        if (currentCustomEditor instanceof EnhancedCustomPropertyEditor) {
            // current editor is EnhancedCustomPropertyEditor too
            value = ((EnhancedCustomPropertyEditor) currentCustomEditor)
                                                        .getPropertyValue();
        }
        else if (currentIndex > -1) {
            value = validValues[currentIndex] ? currentEditor.getValue() :
                                                BeanSupport.NO_VALUE;
        }
        else value = editor.getValue();

        // set the current property editor to FormPropertyEditor (to be used as
        // the custom editor provider next time; and also for code generation);
        // it should be set for all properties (of all nodes selected)
        if (currentIndex > -1) {
            Object[] nodes = editor.getPropertyEnv().getBeans();
            if (nodes == null || nodes.length <= 1) {
                value = new FormProperty.ValueWithEditor(value, currentEditor);
            }
            else { // there are more nodes selected
                value = new FormProperty.ValueWithEditor(value, currentIndex);
            }
        }

        if (!editor.getProperty().canWrite() && (value instanceof FormProperty.ValueWithEditor)) { // Issue 83770
            value = ((FormProperty.ValueWithEditor)value).getValue();
        }
        return value;
    }
}
