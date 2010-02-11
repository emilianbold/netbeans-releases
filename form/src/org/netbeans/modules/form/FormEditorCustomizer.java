/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.form;

import java.lang.reflect.Modifier;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Stola, Jan Jancura
 */
public final class FormEditorCustomizer extends JPanel implements  ActionListener, ChangeListener {
    private JCheckBox cbFold = new JCheckBox ();
    private JCheckBox cbAssistant = new JCheckBox();
    private JCheckBox cbFQN = new JCheckBox();
    private JComboBox cbModifier = new JComboBox ();
    private JRadioButton rbGenerateLocals = new JRadioButton ();
    private JRadioButton rbGenerateFields = new JRadioButton ();
    private JComboBox cbLayoutStyle = new JComboBox ();
    private JComboBox cbComponentNames = new JComboBox ();
    private JComboBox cbListenerStyle = new JComboBox ();
    private JComboBox cbAutoI18n = new JComboBox();
    private PropertyPanel guideLineColEditor = new PropertyPanel();
    private PropertyPanel selectionBorderColEditor = new PropertyPanel();    

    private boolean changed = false;
    private boolean listen = false;

    public FormEditorCustomizer () {
        ButtonGroup group = new ButtonGroup ();
        loc(cbFold, "Fold"); // NOI18N
        loc(cbAssistant, "Assistant"); // NOI18N
        loc(cbFQN, "Generate_FQN"); // NOI18N
        loc(rbGenerateLocals, "Generate_Locals"); // NOI18N
        group.add (rbGenerateLocals);
        loc(rbGenerateFields, "Generate_Fields"); // NOI18N
        group.add (rbGenerateFields);
        cbModifier.addItem(loc("Public_Modifier")); // NOI18N
        cbModifier.addItem(loc("Default_Modifier")); // NOI18N
        cbModifier.addItem(loc("Protected_Modifier")); // NOI18N
        cbModifier.addItem(loc("Private_Modifier")); // NOI18N
        cbLayoutStyle.addItem(loc("CTL_LAYOUT_CODE_AUTO")); // NOI18N
        cbLayoutStyle.addItem(loc("CTL_LAYOUT_CODE_JDK6")); // NOI18N
        cbLayoutStyle.addItem(loc("CTL_LAYOUT_CODE_LIBRARY")); // NOI18N
        cbComponentNames.addItem(loc("CTL_AUTO_NAMING_DEFAULT")); // NOI18N
        cbComponentNames.addItem(loc("CTL_AUTO_NAMING_ON")); // NOI18N
        cbComponentNames.addItem(loc("CTL_AUTO_NAMING_OFF")); // NOI18N
        cbListenerStyle.addItem(loc("Anonymous")); // NOI18N
        cbListenerStyle.addItem(loc("InnerClass")); // NOI18N
        cbListenerStyle.addItem(loc("MainClass")); // NOI18N
        cbAutoI18n.addItem(loc("CTL_AUTO_RESOURCE_DEFAULT")); // NOI18N
        cbAutoI18n.addItem(loc("CTL_AUTO_RESOURCE_ON")); // NOI18N
        cbAutoI18n.addItem(loc("CTL_AUTO_RESOURCE_OFF")); // NOI18N

        JLabel generateComponetsLabel = new JLabel(loc("Generate_Components")); // NOI18N
        JLabel variableModifierLabel = new JLabel();
        JLabel layoutStyleLabel = new JLabel();
        JLabel componentNamesLabel = new JLabel();
        JLabel selectionBorderColLabel = new JLabel();
        JLabel guideLineColLabel = new JLabel();
        JLabel listenerStyleLabel = new JLabel();
        JLabel autoI18nLabel = new JLabel();
        loc(variableModifierLabel, "Variable_Modifier"); // NOI18N
        loc(layoutStyleLabel, "Layout_Style"); // NOI18N
        loc(componentNamesLabel,"Component_Names"); // NOI18N
        loc(listenerStyleLabel, "Listener_Style"); // NOI18N
        loc(autoI18nLabel, "Auto_I18n"); // NOI18N
        loc(selectionBorderColLabel, "Selection_Border_Color"); // NOI18N
        loc(guideLineColLabel, "Guiding_Line_Color"); // NOI18N

        generateComponetsLabel.setToolTipText(loc("Generate_Components_Hint")); // NOI18N
        variableModifierLabel.setToolTipText(loc("HINT_VARIABLES_MODIFIER")); // NOI18N
        layoutStyleLabel.setToolTipText(loc("HINT_LAYOUT_CODE_TARGET")); // NOI18N
        componentNamesLabel.setToolTipText(loc("HINT_AUTO_SET_COMPONENT_NAME")); // NOI18N
        listenerStyleLabel.setToolTipText(loc("HINT_LISTENER_GENERATION_STYLE")); // NOI18N
        autoI18nLabel.setToolTipText(loc("HINT_AUTO_RESOURCE_GLOBAL")); // NOI18N
        guideLineColLabel.setToolTipText(loc("HINT_GUIDING_LINE_COLOR")); // NOI18N
        selectionBorderColLabel.setToolTipText(loc("HINT_SELECTION_BORDER_COLOR")); // NOI18N
        cbFold.setToolTipText(loc("HINT_FOLD_GENERATED_CODE")); // NOI18N
        cbAssistant.setToolTipText(loc("HINT_ASSISTANT_SHOWN")); // NOI18N
        cbFQN.setToolTipText(loc("HINT_GENERATE_FQN")); // NOI18N
        rbGenerateLocals.getAccessibleContext().setAccessibleDescription(loc("Generate_Locals_ACSD")); // NOI18N
        rbGenerateFields.getAccessibleContext().setAccessibleDescription(loc("Generate_Fields_ACSD")); // NOI18N

        variableModifierLabel.setLabelFor(cbModifier);
        layoutStyleLabel.setLabelFor(cbLayoutStyle);
        componentNamesLabel.setLabelFor(cbComponentNames);        
        listenerStyleLabel.setLabelFor(cbListenerStyle);
        autoI18nLabel.setLabelFor(cbAutoI18n);
        guideLineColLabel.setLabelFor(guideLineColEditor);
        selectionBorderColLabel.setLabelFor(selectionBorderColEditor);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(generateComponetsLabel)
                    .add(variableModifierLabel)
                    .add(layoutStyleLabel)
                    .add(componentNamesLabel)
                    .add(listenerStyleLabel)
                    .add(autoI18nLabel)
                    .add(guideLineColLabel)
                    .add(selectionBorderColLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                    .add(rbGenerateLocals)
                    .add(rbGenerateFields)
                    .add(cbFold)
                    .add(cbAssistant)
                    .add(cbFQN)
                    .add(cbModifier, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cbLayoutStyle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cbComponentNames, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cbListenerStyle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cbAutoI18n, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(guideLineColEditor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(selectionBorderColEditor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(generateComponetsLabel)
                    .add(rbGenerateLocals))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(rbGenerateFields)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(variableModifierLabel)
                    .add(cbModifier))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(listenerStyleLabel)
                    .add(cbListenerStyle))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(autoI18nLabel)
                    .add(cbAutoI18n))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(layoutStyleLabel)
                    .add(cbLayoutStyle))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(componentNamesLabel)
                    .add(cbComponentNames))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(cbFold)
                .add(cbAssistant)
                .add(cbFQN)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.CENTER, guideLineColLabel)
                    .add(GroupLayout.CENTER, guideLineColEditor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.CENTER, selectionBorderColLabel)
                    .add(GroupLayout.CENTER, selectionBorderColEditor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .addContainerGap()
        );
        setBorder(new TitledBorder(loc("Code_Generation"))); // NOI18N

        cbFold.addActionListener (this);
        cbAssistant.addActionListener(this);
        cbFQN.addActionListener(this);
        cbLayoutStyle.addActionListener (this);
        cbComponentNames.addActionListener (this);
        cbListenerStyle.addActionListener (this);
        cbModifier.addActionListener (this);
        rbGenerateFields.addActionListener (this);
        rbGenerateLocals.addActionListener (this);
        cbAutoI18n.addActionListener(this);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (FormEditorCustomizer.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText((AbstractButton)c, loc(key));
        } else {
            Mnemonics.setLocalizedText((JLabel)c, loc(key));
        }
    }
    
    
    // other methods ...........................................................
    
    void update() {
        listen = false;
        FormLoaderSettings options = FormLoaderSettings.getInstance();
        try {
            selectionBorderColEditor.setProperty(
                    new PropertySupport.Reflection(
                    options,
                    java.awt.Color.class,
                    "selectionBorderColor")); // NOI18N
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        try {
            guideLineColEditor.setProperty(
                    new PropertySupport.Reflection(
                    options,
                    java.awt.Color.class,
                    "guidingLineColor")); // NOI18N
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        cbFold.setSelected(options.getFoldGeneratedCode());
        cbAssistant.setSelected(options.getAssistantShown());
        cbFQN.setSelected(options.getGenerateFQN());
        rbGenerateLocals.setSelected(options.getVariablesLocal());
        rbGenerateFields.setSelected(!options.getVariablesLocal());
        if ((options.getVariablesModifier() & Modifier.PUBLIC) > 0) {
            cbModifier.setSelectedIndex(0);
        } else if ((options.getVariablesModifier() & Modifier.PROTECTED) > 0) {
            cbModifier.setSelectedIndex(2);
        } else if ((options.getVariablesModifier() & Modifier.PRIVATE) > 0) {
            cbModifier.setSelectedIndex(3);
        } else {
            cbModifier.setSelectedIndex(1);
        }
        cbListenerStyle.setSelectedIndex(options.getListenerGenerationStyle());
        cbLayoutStyle.setSelectedIndex(options.getLayoutCodeTarget());
        cbComponentNames.setSelectedIndex(options.getAutoSetComponentName());
        cbAutoI18n.setSelectedIndex(options.getI18nAutoMode());
        listen = true;
        changed = false;
    }
    
    void applyChanges () {
        FormLoaderSettings options = FormLoaderSettings.getInstance ();
        
        options.setFoldGeneratedCode (cbFold.isSelected ());
        options.setAssistantShown(cbAssistant.isSelected());
        options.setGenerateFQN(cbFQN.isSelected());
        options.setListenerGenerationStyle (cbListenerStyle.getSelectedIndex ());
        options.setLayoutCodeTarget(cbLayoutStyle.getSelectedIndex ());
        options.setAutoSetComponentName(cbComponentNames.getSelectedIndex());
        options.setI18nAutoMode(cbAutoI18n.getSelectedIndex());
        options.setVariablesLocal (rbGenerateLocals.isSelected ());
        switch (cbModifier.getSelectedIndex ()) {
            case 0: options.setVariablesModifier (Modifier.PUBLIC);
                    break;
            case 1: options.setVariablesModifier (0);
                    break;
            case 2: options.setVariablesModifier (Modifier.PROTECTED);
                    break;
            case 3: options.setVariablesModifier (Modifier.PRIVATE);
                    break;
        }
        changed = false;
    }
    
    void cancel () {
        changed = false;
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        return changed;
    }
    
    @Override
    public void actionPerformed (ActionEvent e) {
        if (listen)
            changed = true;
    }
    
    @Override
    public void stateChanged (ChangeEvent e) {
        if (listen)
            changed = true;
    }
}
