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
import javax.swing.JSpinner;
import javax.swing.JTextField;
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
    private JComboBox cbModifier = new JComboBox ();
    private JRadioButton rbGenerateLocals = new JRadioButton ();
    private JRadioButton rbGenerateFields = new JRadioButton ();
    private JComboBox cbListenerStyle = new JComboBox ();
    private JTextField tfVariableName = new JTextField ();
    private JSpinner sGridX = new JSpinner ();
    private JSpinner sGridY = new JSpinner ();

    private JCheckBox cbShowMnemonicsDialog = new JCheckBox ();
    private JCheckBox cbGenerateMnemonics = new JCheckBox ();

    private boolean changed = false;

    public FormEditorCustomizer () {
        ButtonGroup group = new ButtonGroup ();
        loc(cbFold, "Fold"); // NOI18N
        cbFold.setBackground (Color.white);
        group = new ButtonGroup ();
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
        loc(cbShowMnemonicsDialog, "Show_Mnemonics_Dialog"); // NOI18N
        cbShowMnemonicsDialog.setBackground (Color.white);
        loc(cbGenerateMnemonics, "Generate_Mnemonics"); // NOI18N
        cbGenerateMnemonics.setBackground (Color.white);

        // Code Generation panel
        JPanel codeGeneration = new JPanel();
        JLabel generateComponetsLabel = new JLabel(loc("Generate_Components")); // NOI18N
        JLabel variableModifierLabel = new JLabel(loc("Variable_Modifier")); // NOI18N
        JLabel listenerStyleLabel = new JLabel(loc("Listener_Style")); // NOI18N
        JLabel variableNameLabel = new JLabel(loc("Variable_Name")); // NOI18N
        JLabel gridXLabel = new JLabel(loc("Grid_X")); // NOI18N
        JLabel gridYLabel = new JLabel(loc("Grid_Y")); // NOI18N

        variableModifierLabel.setLabelFor(cbModifier);
        listenerStyleLabel.setLabelFor(cbListenerStyle);
        variableNameLabel.setLabelFor(tfVariableName);
        gridXLabel.setLabelFor(sGridX);
        gridYLabel.setLabelFor(sGridY);

        GroupLayout layout = new GroupLayout(codeGeneration);
        codeGeneration.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(generateComponetsLabel)
                    .add(variableModifierLabel)
                    .add(listenerStyleLabel)
                    .add(variableNameLabel)
                    .add(gridXLabel)
                    .add(gridYLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                    .add(rbGenerateLocals)
                    .add(rbGenerateFields)
                    .add(cbFold)
                    .add(cbModifier, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cbListenerStyle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(tfVariableName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(sGridX, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                    .add(sGridY, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
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
                .add(cbFold)
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
                    .add(variableNameLabel)
                    .add(tfVariableName))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(gridXLabel)
                    .add(sGridX))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(gridYLabel)
                    .add(sGridY))
                .addContainerGap()
        );
        codeGeneration.setBorder(new TitledBorder(loc("Code_Generation"))); // NOI18N
        codeGeneration.setBackground (Color.white);

       // Mnemonics panel
        JPanel mnemonics = new JPanel();
        layout = new GroupLayout(mnemonics);
        mnemonics.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(cbShowMnemonicsDialog)
                    .add(cbGenerateMnemonics))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .add(cbShowMnemonicsDialog)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(cbGenerateMnemonics)
                .addContainerGap()
        );
        mnemonics.setBorder(new TitledBorder(loc("Mnemonics_Options"))); // NOI18N
        mnemonics.setBackground (Color.white);
        
        // This panel
        layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
                .add(codeGeneration)
                .add(mnemonics)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .add(codeGeneration)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(mnemonics)
        );

        cbFold.addActionListener (this);
        cbGenerateMnemonics.addActionListener (this);
        cbListenerStyle.addActionListener (this);
        cbModifier.addActionListener (this);
        cbShowMnemonicsDialog.addActionListener (this);
        rbGenerateFields.addActionListener (this);
        rbGenerateLocals.addActionListener (this);
        sGridX.addChangeListener (this);
        sGridY.addChangeListener (this);
        tfVariableName.addActionListener (this);
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
        FormLoaderSettings options = FormLoaderSettings.getInstance ();
        
        cbFold.setSelected (options.getFoldGeneratedCode ());
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
        cbShowMnemonicsDialog.setSelected (options.getShowMnemonicsDialog ());
        cbGenerateMnemonics.setSelected (options.getGenerateMnemonicsCode ());
        tfVariableName.setText (options.getEventVariableName ());
        sGridX.setValue (new Integer (options.getGridX ()));
        sGridY.setValue (new Integer (options.getGridY ()));
        changed = false;
    }
    
    void applyChanges () {
        FormLoaderSettings options = FormLoaderSettings.getInstance ();
        
        options.setEventVariableName (tfVariableName.getText ());
        options.setFoldGeneratedCode (cbFold.isSelected ());
        options.setGenerateMnemonicsCode (cbGenerateMnemonics.isSelected ());
        options.setGridX (((Integer) sGridX.getValue ()).intValue ());
        options.setGridY (((Integer) sGridY.getValue ()).intValue ());
        options.setListenerGenerationStyle (cbListenerStyle.getSelectedIndex ());
        options.setShowMnemonicsDialog (cbShowMnemonicsDialog.isSelected ());
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
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        return changed;
    }
    
    public void actionPerformed (ActionEvent e) {
        changed = true;
    }
    
    public void stateChanged (ChangeEvent e) {
        changed = true;
    }
}