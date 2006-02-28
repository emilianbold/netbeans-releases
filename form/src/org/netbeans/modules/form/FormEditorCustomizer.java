/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.lang.reflect.Modifier;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Stola, Jan Jancura
 */
public final class FormEditorCustomizer extends JPanel implements  ActionListener, ChangeListener {
    private JCheckBox cbFold = new JCheckBox ();
    private JCheckBox cbAssistant = new JCheckBox();
    private JComboBox cbModifier = new JComboBox ();
    private JRadioButton rbGenerateLocals = new JRadioButton ();
    private JRadioButton rbGenerateFields = new JRadioButton ();
    private JComboBox cbListenerStyle = new JComboBox ();

    private boolean changed = false;
    private boolean listen = false;

    public FormEditorCustomizer () {
        ButtonGroup group = new ButtonGroup ();
        loc(cbFold, "Fold"); // NOI18N
        cbFold.setBackground (Color.white);
        loc(cbAssistant, "Assistant"); // NOI18N
        cbAssistant.setBackground(Color.white);
        loc(rbGenerateLocals, "Generate_Locals"); // NOI18N
        rbGenerateLocals.setBackground (Color.white);
        group.add (rbGenerateLocals);
        loc(rbGenerateFields, "Generate_Fields"); // NOI18N
        rbGenerateFields.setBackground (Color.white);
        group.add (rbGenerateFields);
        cbModifier.addItem(loc("Public_Modifier")); // NOI18N
        cbModifier.addItem(loc("Default_Modifier")); // NOI18N
        cbModifier.addItem(loc("Protected_Modifier")); // NOI18N
        cbModifier.addItem(loc("Private_Modifier")); // NOI18N
        cbListenerStyle.addItem(loc("Anonymous")); // NOI18N
        cbListenerStyle.addItem(loc("InnerClass")); // NOI18N
        cbListenerStyle.addItem(loc("MainClass")); // NOI18N

        JLabel generateComponetsLabel = new JLabel(loc("Generate_Components")); // NOI18N
        JLabel variableModifierLabel = new JLabel(loc("Variable_Modifier")); // NOI18N
        JLabel listenerStyleLabel = new JLabel(loc("Listener_Style")); // NOI18N

        generateComponetsLabel.setToolTipText(loc("Generate_Components_Hint")); // NOI18N
        variableModifierLabel.setToolTipText(loc("HINT_VARIABLES_MODIFIER")); // NOI18N
        listenerStyleLabel.setToolTipText(loc("HINT_LISTENER_GENERATION_STYLE")); // NOI18N
        cbFold.setToolTipText(loc("HINT_FOLD_GENERATED_CODE")); // NOI18N
        cbAssistant.setToolTipText(loc("HINT_ASSISTANT_SHOWN")); // NOI18N

        variableModifierLabel.setLabelFor(cbModifier);
        listenerStyleLabel.setLabelFor(cbListenerStyle);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(generateComponetsLabel)
                    .add(variableModifierLabel)
                    .add(listenerStyleLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                    .add(rbGenerateLocals)
                    .add(rbGenerateFields)
                    .add(cbFold)
                    .add(cbAssistant)
                    .add(cbModifier, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cbListenerStyle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .add(cbFold)
                .add(cbAssistant)
                .addContainerGap()
        );
        setBorder(new TitledBorder(loc("Code_Generation"))); // NOI18N
        setBackground (Color.white);

        cbFold.addActionListener (this);
        cbAssistant.addActionListener(this);
        cbListenerStyle.addActionListener (this);
        cbModifier.addActionListener (this);
        rbGenerateFields.addActionListener (this);
        rbGenerateLocals.addActionListener (this);
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
    
    void update () {
        listen = false;
        FormLoaderSettings options = FormLoaderSettings.getInstance ();
        
        cbFold.setSelected (options.getFoldGeneratedCode ());
        cbAssistant.setSelected(options.getAssistantShown());
        rbGenerateLocals.setSelected (options.getVariablesLocal ());
        rbGenerateFields.setSelected (!options.getVariablesLocal ());
        if ((options.getVariablesModifier () & Modifier.PUBLIC) > 0)
            cbModifier.setSelectedIndex (0);
        else
        if ((options.getVariablesModifier () & Modifier.PROTECTED) > 0)
            cbModifier.setSelectedIndex (2);
        else
        if ((options.getVariablesModifier () & Modifier.PRIVATE) > 0)
            cbModifier.setSelectedIndex (3);
        else
            cbModifier.setSelectedIndex (1);
        cbListenerStyle.setSelectedIndex (options.getListenerGenerationStyle ());
        listen = true;
        changed = false;
    }
    
    void applyChanges () {
        FormLoaderSettings options = FormLoaderSettings.getInstance ();
        
        options.setFoldGeneratedCode (cbFold.isSelected ());
        options.setAssistantShown(cbAssistant.isSelected());
        options.setListenerGenerationStyle (cbListenerStyle.getSelectedIndex ());
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
    
    public void actionPerformed (ActionEvent e) {
        if (listen)
            changed = true;
    }
    
    public void stateChanged (ChangeEvent e) {
        if (listen)
            changed = true;
    }
}