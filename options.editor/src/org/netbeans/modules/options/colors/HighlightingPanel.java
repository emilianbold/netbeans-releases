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
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;

import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.options.colors.ColorComboBox.Value;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;


/**
 *
 * @author  Administrator
 */
public class HighlightingPanel extends JPanel implements ActionListener, 
PropertyChangeListener {
    
    
    private JList		lCategories = new JList ();
    private ColorComboBox	foregroundColorChooser = new ColorComboBox ();
    private ColorComboBox	backgroundColorChooser = new ColorComboBox ();
 
    private ColorModel          colorModel = null;
    private boolean		listen = false;
    private String              currentProfile;
    /** cache Map (String (profile name) > Vector (AttributeSet)). */
    private Map                 profileToCategories = new HashMap ();
    /** Set (String (profile name)) of changed profile names. */
    private Set                 toBeSaved = new HashSet ();
    private boolean             changed = false;

    
    /** Creates new form FontAndColorsPanel */
    public HighlightingPanel (FontAndColorsPanel fontAndColorsPanel) {

        // 1) init components
        lCategories.getAccessibleContext ().setAccessibleName (loc ("AN_Categories"));
        lCategories.getAccessibleContext ().setAccessibleDescription (loc ("AD_Categories"));
        foregroundColorChooser.getAccessibleContext ().setAccessibleName (loc ("AN_Foreground_Chooser"));
        foregroundColorChooser.getAccessibleContext ().setAccessibleDescription (loc ("AD_Foreground_Chooser"));
        backgroundColorChooser.getAccessibleContext ().setAccessibleName (loc ("AN_Background_Chooser"));
        backgroundColorChooser.getAccessibleContext ().setAccessibleDescription (loc ("AD_Background_Chooser"));
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (3);
        lCategories.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                refreshUI ();
            }
        });
        lCategories.setCellRenderer (new CategoryRenderer ());
        foregroundColorChooser.addPropertyChangeListener (this);
        backgroundColorChooser.addPropertyChangeListener (this);
        JLabel lCategory = new JLabel ();
        loc (lCategory, "CTL_Category");
        lCategory.setLabelFor (lCategories);

        // 2) define layout
	FormLayout layout = new FormLayout (
            "p:g, 10dlu, p, 3dlu, p:g, 1dlu, p", // cols
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, p:g"); //, 3dlu, p, 3dlu, f:130dlu:g");      // rows
        PanelBuilder builder = new PanelBuilder (layout);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();
        builder = new PanelBuilder (layout, this);
        builder.setDefaultDialogBorder ();
        builder.add (lCategory,                         lc.xy   (1, 1));
	builder.add (new JScrollPane (lCategories),     cc.xywh (1, 3, 1, 8));
        builder.addLabel (loc ("CTL_Foreground_label"), lc.xy   (3, 3),
                          foregroundColorChooser,	cc.xyw  (5, 3, 3));
        builder.addLabel (loc ("CTL_Background_label"), lc.xy   (3, 5),
                          backgroundColorChooser,	cc.xyw  (5, 5, 3));
    }
 
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
        updateData ();
        changed = true;
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (!listen) return;
        if (evt.getPropertyName () != ColorComboBox.PROP_COLOR) return;
        updateData ();
        changed = true;
    }
    
    void update (ColorModel colorModel) {
        this.colorModel = colorModel;
        currentProfile = colorModel.getCurrentProfile ();
        listen = false;
        setCurrentProfile (currentProfile);
        lCategories.setListData (getCategories (currentProfile));
        lCategories.setSelectedIndex (0);
        refreshUI ();	
        listen = true;
        changed = false;
    }
    
    void cancel () {
        toBeSaved = new HashSet ();
        profileToCategories = new HashMap ();        
        changed = false;
    }
    
    public void applyChanges () {
        if (colorModel == null) return;
        Iterator it = toBeSaved.iterator ();
        while (it.hasNext ()) {
            String profile = (String) it.next ();
            colorModel.setHighlightings (profile, getCategories (profile));
        }
        toBeSaved = new HashSet ();
        profileToCategories = new HashMap ();
    }
    
    boolean isChanged () {
        return changed;
    }
    
    void setCurrentProfile (String currentProfile) {
        String oldScheme = this.currentProfile;
        this.currentProfile = currentProfile;
        if (!colorModel.getProfiles ().contains (currentProfile)) {
            // clone profile
            Vector categories = getCategories (oldScheme);
            profileToCategories.put (currentProfile, new Vector (categories));
            toBeSaved.add (currentProfile);
        }
        refreshUI ();
    }

    void deleteProfile (String profile) {
        if (colorModel.isCustomProfile (profile))
            profileToCategories.put (profile, null);
        else {
            profileToCategories.put (profile, getDefaults (profile));
            refreshUI ();
        }
        toBeSaved.add (profile);
    }
    
    // other methods ...........................................................
    
    Collection getHighlightings () {
        return getCategories (currentProfile);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (SyntaxColoringPanel.class, key);
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

    private void updateData () {
        if (lCategories.getSelectedIndex () < 0) return;
        Vector categories = getCategories (currentProfile);
	AttributeSet category = (AttributeSet) categories.get 
	    (lCategories.getSelectedIndex ());
        Color underline = null, 
              wave = null, 
              strikethrough = null;
        
        SimpleAttributeSet c = new SimpleAttributeSet (category);
        if (backgroundColorChooser.getColor () != null)
            c.addAttribute (
                StyleConstants.Background,
                backgroundColorChooser.getColor ()
            );
        else
            c.removeAttribute (StyleConstants.Background);
        if (foregroundColorChooser.getColor () != null)
            c.addAttribute (
                StyleConstants.Foreground,
                foregroundColorChooser.getColor ()
            );
        else
            c.removeAttribute (StyleConstants.Foreground);
        if (underline != null)
            c.addAttribute (
                StyleConstants.Underline,
                underline
            );
        else
            c.removeAttribute (StyleConstants.Underline);
        if (strikethrough != null)
            c.addAttribute (
                StyleConstants.StrikeThrough,
                strikethrough
            );
        else
            c.removeAttribute (StyleConstants.StrikeThrough);
        if (wave != null)
            c.addAttribute (
                EditorStyleConstants.WaveUnderlineColor,
                wave
            );
        else
            c.removeAttribute (EditorStyleConstants.WaveUnderlineColor);
        int i = lCategories.getSelectedIndex ();
        categories.set (i, c);
        
        toBeSaved.add (currentProfile);
    }
    
    private void refreshUI () {
        int index = lCategories.getSelectedIndex ();
        if (index < 0) {
            foregroundColorChooser.setEnabled (false);
            backgroundColorChooser.setEnabled (false);
            return;
        }
        foregroundColorChooser.setEnabled (true);
        backgroundColorChooser.setEnabled (true);
        
        Vector categories = getCategories (currentProfile);
	AttributeSet category = (AttributeSet) categories.get (index);
        
        // set values
        listen = false;
        foregroundColorChooser.setColor (
            (Color) category.getAttribute (StyleConstants.Foreground)
        );
        backgroundColorChooser.setColor (
            (Color) category.getAttribute (StyleConstants.Background)
        );
        listen = true;
    }
    
    private Vector getCategories (String profile) {
        if (colorModel == null) return null;
        if (!profileToCategories.containsKey (profile)) {
            Collection c = colorModel.getHighlightings (profile);
            List l = new ArrayList (c);
            Collections.sort (l, new CategoryComparator ());
            profileToCategories.put (profile, new Vector (l));
        }
        return (Vector) profileToCategories.get (profile);
    }

    /** cache Map (String (profile name) > Vector (AttributeSet)). */
    private Map profileToDefaults = new HashMap ();
    
    private Vector getDefaults (String profile) {
        if (!profileToDefaults.containsKey (profile)) {
            Collection c = colorModel.getHighlightingDefaults (profile);
            List l = new ArrayList (c);
            Collections.sort (l, new CategoryComparator ());
            profileToDefaults.put (profile, new Vector (l));
        }
        return (Vector) profileToDefaults.get (profile);
    }
}
