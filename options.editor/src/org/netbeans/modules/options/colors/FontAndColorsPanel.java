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

package org.netbeans.modules.options.colors;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.spi.options.OptionsCategory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class FontAndColorsPanel extends JPanel implements 
ActionListener {
    
    private JComboBox		    cbProfiles;
    private JButton		    bDelete;
    private JButton		    bClone;
    private JTabbedPane		    tabbedPane;
    private SyntaxColoringPanel	    syntaxColoringPanel;
    private HighlightingPanel       highlightingPanel;
    private AnnotationsPanel	    annotationsPanel;

    private ColorModel		    colorModel;
    private String		    currentProfile;
    private boolean		    listen = false;
    
 
    /** Creates new form FontAndColorsPanel */
    public FontAndColorsPanel () {
        
        // init components
        syntaxColoringPanel = new SyntaxColoringPanel (this);
        highlightingPanel = new HighlightingPanel (this);
        annotationsPanel = new AnnotationsPanel (this);
        cbProfiles = new JComboBox ();
        cbProfiles.addItemListener (new ItemListener () {
            public void itemStateChanged (ItemEvent evt) {
                if (!listen) return;
                setCurrentProfile ((String) cbProfiles.getSelectedItem ());
            }
        });
        JPanel pButtons = new JPanel (new GridLayout (1, 2, 3, 3));
        loc (bClone = new JButton (), "CTL_Create_New");
        bClone.addActionListener (this);
        pButtons.add (bClone);
        loc (bDelete = new JButton (), "CTL_Delete");
        bDelete.addActionListener (this);
        pButtons.add (bDelete);
        tabbedPane = new JTabbedPane ();

        // init layout
        FormLayout layout = new FormLayout (
            "p, 3dlu, p:g, 5dlu, p", // cols
            "p, 5dlu, f:p:g");      // rows
        PanelBuilder builder = new PanelBuilder (layout, this);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();
        builder.addLabel (     loc ("CTL_Color_Profile_Name"), lc.xy (1, 1), 
                               cbProfiles,                     cc.xy (3, 1));
        builder.add (          pButtons,                      cc.xy (5, 1, "l,d"));
        builder.add (          tabbedPane,                    cc.xyw (1, 3, 5));
	tabbedPane.addTab (loc ("Syntax_coloring_tab"), syntaxColoringPanel);
	tabbedPane.addTab (loc ("Editor_tab"), highlightingPanel);
	tabbedPane.addTab (loc ("Annotations_tab"), annotationsPanel);
        tabbedPane.setMnemonicAt (0, loc ("Syntax_coloring_tab_mnemonic").charAt (0));
        tabbedPane.setMnemonicAt (1, loc ("Editor_tab_mnemonic").charAt (0));
        tabbedPane.setMnemonicAt (2, loc ("Annotations_tab_mnemonic").charAt (0));
    }
    
    private void setCurrentProfile (String profile) {
        if (colorModel.isCustomProfile (profile))
            loc (bDelete, "CTL_Delete");                              // NOI18N
        else
            loc (bDelete, "CTL_Restore");                             // NOI18N
        currentProfile = profile;
        highlightingPanel.setCurrentProfile (currentProfile);
        syntaxColoringPanel.setCurrentProfile (currentProfile);
        annotationsPanel.setCurrentProfile (currentProfile);
    }
    
    private void deleteCurrentProfile () {
        String currentProfile = (String) cbProfiles.getSelectedItem ();
        highlightingPanel.deleteProfile (currentProfile);
        syntaxColoringPanel.deleteProfile (currentProfile);
        annotationsPanel.deleteProfile (currentProfile);
        if (colorModel.isCustomProfile (currentProfile)) {
            cbProfiles.removeItem (currentProfile);
            cbProfiles.setSelectedIndex (0);
        }
    }
    
    
    // other methods ...........................................................
    
    void update () {
        highlightingPanel.update ();
        syntaxColoringPanel.update ();
        annotationsPanel.update ();
        
        colorModel = new ColorModel ();
        currentProfile = colorModel.getCurrentProfile ();

        // init schemes
        listen = false;
        Iterator it = colorModel.getProfiles ().iterator ();
        cbProfiles.removeAllItems ();
        while (it.hasNext ())
            cbProfiles.addItem (it.next ());
        listen = true;
        cbProfiles.setSelectedItem (currentProfile);
    }
    
    
    
    void applyChanges () {
        highlightingPanel.applyChanges ();
        syntaxColoringPanel.applyChanges ();
        annotationsPanel.applyChanges ();
        if (colorModel == null) return;
        colorModel.setCurrentProfile (currentProfile);
    }
    
    void cancel () {
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        if (currentProfile != null &&
            colorModel != null &&
            !currentProfile.equals (colorModel.getCurrentProfile ())
        ) return true;
        if (highlightingPanel.isChanged ()) return true;
        if (syntaxColoringPanel.isChanged ()) return true;
        if (annotationsPanel.isChanged ()) return true;
        return false;
    }
   
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        if (e.getSource () == bClone) {
            InputLine il = new InputLine (
                loc ("CTL_Create_New_Profile_Message"),                // NOI18N
                loc ("CTL_Create_New_Profile_Title")                   // NOI18N
            );
            il.setInputText (currentProfile);
            DialogDisplayer.getDefault ().notify (il);
            if (il.getValue () == NotifyDescriptor.OK_OPTION) {
                String newScheme = il.getInputText ();
                Iterator it = colorModel.getProfiles ().iterator ();
                while (it.hasNext ())
                    if (newScheme.equals (it.next ())) {
                        Message md = new Message (
                            loc ("CTL_Duplicate_Profile_Name"),        // NOI18N
                            Message.ERROR_MESSAGE
                        );
                        DialogDisplayer.getDefault ().notify (md);
                        return;
                    }
                setCurrentProfile (newScheme);
                listen = false;
                cbProfiles.addItem (il.getInputText ());
                cbProfiles.setSelectedItem (il.getInputText ());
                listen = true;
            }
            return;
        }
        if (e.getSource () == bDelete) {
            deleteCurrentProfile ();
            return;
        }
    }
    
    Collection getDefaults () {
        return syntaxColoringPanel.getDeafults ();
    }
    
    Collection getHighlights () {
        return highlightingPanel.getHighlightings ();
    }
    
    Collection getSyntaxColorings () {
        return syntaxColoringPanel.getSyntaxColorings ();
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (FontAndColorsPanel.class, key);
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
}
