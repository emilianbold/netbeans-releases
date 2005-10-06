/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyEditorManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.spi.options.OptionsCategory;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class FormEditorCustomizer extends JPanel implements 
ActionListener, ChangeListener {
    
    private JTextField tfPropertyEditorSearchPath = new JTextField ();
    
    private JCheckBox cbBigIcons = new JCheckBox ();
    private JCheckBox cbShowNames = new JCheckBox ();
    private JCheckBox cbApplyGrid = new JCheckBox ();
    private JSpinner sBorder = new JSpinner ();
    
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
        loc (cbBigIcons, "Big_Icons");
        cbBigIcons.setBackground (Color.white);
        loc (cbShowNames, "Show_Names");
        cbShowNames.setBackground (Color.white);
        loc (cbApplyGrid, "Apply_Grid");
        cbApplyGrid.setBackground (Color.white);
        ButtonGroup group = new ButtonGroup ();
        loc (cbFold, "Fold");
        cbFold.setBackground (Color.white);
        group = new ButtonGroup ();
        loc (rbGenerateLocals, "Generate_Locals");
        rbGenerateLocals.setBackground (Color.white);
        group.add (rbGenerateLocals);
        loc (rbGenerateFields, "Generate_Fields");
        rbGenerateFields.setBackground (Color.white);
        group.add (rbGenerateFields);
        cbModifier.addItem (loc ("Public_Modifier"));
        cbModifier.addItem (loc ("Default_Modifier"));
        cbModifier.addItem (loc ("Protected_Modifier"));
        cbModifier.addItem (loc ("Private_Modifier"));
        cbListenerStyle.addItem (loc ("Anonymous"));
        cbListenerStyle.addItem (loc ("InnerClass"));
        cbListenerStyle.addItem (loc ("MainClass"));
        loc (cbShowMnemonicsDialog, "Show_Mnemonics_Dialog");
        cbShowMnemonicsDialog.setBackground (Color.white);
        loc (cbGenerateMnemonics, "Generate_Mnemonics");
        cbGenerateMnemonics.setBackground (Color.white);
        
        FormLayout layout = new FormLayout (
            "p:g", // cols
            "p, p, p, p"
        );      // rows
        PanelBuilder builder = new PanelBuilder (layout, this);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();
        
            layout = new FormLayout (
                "p", // cols
                "p, 3dlu, p, 3dlu, p"
            );      // rows
            PanelBuilder builder2 = new PanelBuilder (layout);
            builder2.add (cbBigIcons,                cc.xy (1, 1));
            builder2.add (cbShowNames,               cc.xy (1, 3));
            builder2.add (cbApplyGrid,               cc.xy (1, 5));
            JPanel p = builder2.getPanel ();
            p.setBorder (new TitledBorder (loc ("UI_Options")));
            p.setBackground (Color.white);
        builder.add (p,                              cc.xy (1, 1));
        
            layout = new FormLayout (
                "p, 5dlu, 40dlu, 100dlu, p:g", // cols
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p" + 
                ", 3dlu, p, 3dlu, p, 3dlu, p"
            );      // rows
            builder2 = new PanelBuilder (layout);
            builder2.addLabel (loc ("Generate_Components"),lc.xy (1, 5));
            builder2.add (rbGenerateLocals,          cc.xyw (3, 5, 3));
            builder2.add (rbGenerateFields,          cc.xyw (3, 7, 3));
            builder2.add (cbFold,                    cc.xyw (3, 9, 3));
            builder2.addLabel (loc ("Variable_Modifier"),
                                                     lc.xy (1, 11), 
                          cbModifier,                cc.xyw (3, 11, 2));
            builder2.addLabel (loc ("Listener_Style"), lc.xy (1, 13), 
                          cbListenerStyle,           cc.xyw (3, 13, 2));
            builder2.addLabel (loc ("Variable_Name"), lc.xy (1, 15), 
                          tfVariableName,            cc.xyw (3, 15, 2));
            builder2.addLabel (loc ("Grid_X"),       lc.xy (1, 17), 
                          sGridX,                    cc.xy (3, 17));
            builder2.addLabel (loc ("Grid_Y"),       lc.xy (1, 19), 
                          sGridY,                    cc.xy (3, 19));
            p = builder2.getPanel ();
            p.setBorder (new TitledBorder (loc ("Code_Generation")));
            p.setBackground (Color.white);
        builder.add (p,                              cc.xy (1, 2));
        
            layout = new FormLayout (
                "p", // cols
                "p, 3dlu, p"
            );      // rows
            builder2 = new PanelBuilder (layout);
            builder2.add (cbGenerateMnemonics,       cc.xy (1, 1));
            builder2.add (cbShowMnemonicsDialog,     cc.xy (1, 3));
            p = builder2.getPanel ();
            p.setBorder (new TitledBorder (loc ("Mnemonics_Options")));
            p.setBackground (Color.white);
        builder.add (p,                              cc.xy (1, 3));

        cbApplyGrid.addActionListener (this);
        cbBigIcons.addActionListener (this);
        cbFold.addActionListener (this);
        cbGenerateMnemonics.addActionListener (this);
        cbListenerStyle.addActionListener (this);
        cbModifier.addActionListener (this);
        cbShowMnemonicsDialog.addActionListener (this);
        cbShowNames.addActionListener (this);
        rbGenerateFields.addActionListener (this);
        rbGenerateLocals.addActionListener (this);
        sBorder.addChangeListener (this);
        sGridX.addChangeListener (this);
        sGridY.addChangeListener (this);
        tfPropertyEditorSearchPath.addActionListener (this);
        tfVariableName.addActionListener (this);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (FormEditorCustomizer.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
    }
    
    
    // other methods ...........................................................
    
    void update () {
        FormLoaderSettings options = FormLoaderSettings.getInstance ();
        
        cbApplyGrid.setSelected (options.getApplyGridToPosition ());
        cbFold.setSelected (options.getFoldGeneratedCode ());
        rbGenerateLocals.setSelected (options.getVariablesLocal ());
        rbGenerateFields.setSelected (!options.getVariablesLocal ());
        if ((options.getVariablesModifier () | Modifier.PUBLIC) > 0)
            cbModifier.setSelectedIndex (0);
        else
        if ((options.getVariablesModifier () | Modifier.PROTECTED) > 0)
            cbModifier.setSelectedIndex (2);
        else
        if ((options.getVariablesModifier () | Modifier.PRIVATE) > 0)
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
        
        options.setApplyGridToPosition (cbApplyGrid.isSelected ());
        options.setApplyGridToSize (cbApplyGrid.isSelected ());
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