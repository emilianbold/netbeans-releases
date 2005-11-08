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

package org.netbeans.modules.options.generaleditor;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;


import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class GeneralEditorPanel extends JPanel implements 
ActionListener {

    private JCheckBox       cbShowCodeFolding = new JCheckBox ();
    private JCheckBox       cbFoldMethods = new JCheckBox ();
    private JCheckBox       cbFoldInnerClasses = new JCheckBox ();
    private JCheckBox       cbFoldImports = new JCheckBox ();
    private JCheckBox       cbFoldJavaDocComments = new JCheckBox ();
    private JCheckBox       cbFoldInitialComment = new JCheckBox ();
    
    private JCheckBox       cbAutoPopup = new JCheckBox ();
    private JCheckBox       cbInsertSingleProposals = new JCheckBox ();
    private JCheckBox       cbCaseSensitive = new JCheckBox ();
    private JCheckBox       cbShowDeprecated = new JCheckBox ();
    private JCheckBox       cbPairCharacterCompletion = new JCheckBox ();

    private boolean         changed = false;
    private boolean         listen = false;
    
    
    public GeneralEditorPanel () {
        
        loc (cbShowCodeFolding, "Use_Folding");
        loc (cbFoldMethods, "Fold_Methods");
        loc (cbFoldInnerClasses, "Fold_Classes");
        loc (cbFoldImports, "Fold_Imports");
        loc (cbFoldJavaDocComments, "Fold_JavaDoc");
        loc (cbFoldInitialComment, "Fold_Licence");

        loc (cbAutoPopup, "Auto_Popup_Completion_Window");
        loc (cbInsertSingleProposals, "Insert_Single_Proposals_Automatically");
        loc (cbCaseSensitive, "Case_Sensitive_Code_Completion");
        loc (cbShowDeprecated, "Show_Deprecated_Members");
        loc (cbPairCharacterCompletion, "Pair_Character_Completion");

        FormLayout layout = new FormLayout (
            "5dlu, p, 5dlu, p:g", // cols
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p," + 
            "5dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"// rows
        );      
        
        PanelBuilder builder = new PanelBuilder (layout, this);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();
        builder.addSeparator (loc ("Code_Folding"),     cc.xyw (1,  1, 4));
        builder.addLabel (loc ("Code_Folding_Section"), lc.xy  (2,  3),
                              cbShowCodeFolding,        cc.xy  (4,  3));
        builder.addLabel (loc ("Fold_by_Default"),      lc.xy  (2,  5),
                          cbFoldMethods,                cc.xy  (4,  5));
        builder.add (         cbFoldInnerClasses,       cc.xy  (4,  7));
        builder.add (         cbFoldImports,            cc.xy  (4,  9));
        builder.add (         cbFoldJavaDocComments,    cc.xy  (4,  11));
        builder.add (         cbFoldInitialComment,     cc.xy  (4,  13));
        
        builder.addSeparator (loc ("Code_Completion"),  cc.xyw (1,  15, 4));
        builder.addLabel (    loc ("Code_Completion_Section"),     
                                                        lc.xy  (2,  17),
                              cbAutoPopup,              cc.xy  (4,  17));
        builder.add (         cbInsertSingleProposals,  cc.xy  (4,  19));
        builder.add (         cbCaseSensitive,          cc.xy  (4,  21));
        builder.add (         cbShowDeprecated,         cc.xy  (4,  23));
        builder.add (         cbPairCharacterCompletion,cc.xy  (4,  25));
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (GeneralEditorPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key));
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key));
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc ("CTL_" + key)
            );
        } else {
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc ("CTL_" + key)
            );
        }
    }
    
    private Model model;
    
    void update () {
        listen = false;
        if (model == null) {
            model = new Model ();
            cbShowCodeFolding.addActionListener (this);
            cbFoldMethods.addActionListener (this);
            cbFoldInnerClasses.addActionListener (this);
            cbFoldImports.addActionListener (this);
            cbFoldJavaDocComments.addActionListener (this);
            cbFoldInitialComment.addActionListener (this);
            cbAutoPopup.addActionListener (this);
            cbInsertSingleProposals.addActionListener (this);
            cbCaseSensitive.addActionListener (this);
            cbShowDeprecated.addActionListener (this);
            cbPairCharacterCompletion.addActionListener (this);
        }
        
        // init code folding
        cbShowCodeFolding.setSelected (model.isShowCodeFolding ());
        cbFoldImports.setSelected (model.isFoldImports ());
        cbFoldInitialComment.setSelected (model.isFoldInitialComment ());
        cbFoldInnerClasses.setSelected (model.isFoldInnerClasses ());
        cbFoldJavaDocComments.setSelected (model.isFoldJavaDocComments ());
        cbFoldMethods.setSelected (model.isFoldMethods ());
        updateEnabledState ();
        
        // code completion options
        cbPairCharacterCompletion.setSelected 
            (model.isPairCharacterCompletion ());
        cbAutoPopup.setSelected 
            (model.isCompletionAutoPopup ());
        cbShowDeprecated.setSelected 
            (model.isShowDeprecatedMembers ());
        cbInsertSingleProposals.setSelected 
            (model.isCompletionInstantSubstitution ());
        cbCaseSensitive.setSelected
            (model.isCompletionCaseSensitive ());
        
        listen = true;
    }
    
    void applyChanges () {
        
        if (model == null) return;
        // code folding options
        model.setFoldingOptions (
            cbShowCodeFolding.isSelected (),
            cbFoldImports.isSelected (),
            cbFoldInitialComment.isSelected (),
            cbFoldInnerClasses.isSelected (),
            cbFoldJavaDocComments.isSelected (),
            cbFoldMethods.isSelected ()
        );
        
        // code completion options
        model.setCompletionOptions (
            cbPairCharacterCompletion.isSelected (),
            cbAutoPopup.isSelected (),
            cbShowDeprecated.isSelected (),
            cbInsertSingleProposals.isSelected (),
            cbCaseSensitive.isSelected ()
        );
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
        if (!listen) return;
        if (e.getSource () == cbShowCodeFolding)
            updateEnabledState ();
        changed = true;
    }
    
    
    // other methods ...........................................................
    
    private void updateEnabledState () {
        boolean useCodeFolding = cbShowCodeFolding.isSelected ();
        cbFoldImports.setEnabled (useCodeFolding);
        cbFoldInitialComment.setEnabled (useCodeFolding);
        cbFoldInnerClasses.setEnabled (useCodeFolding);
        cbFoldJavaDocComments.setEnabled (useCodeFolding);
        cbFoldMethods.setEnabled (useCodeFolding);
    }
}