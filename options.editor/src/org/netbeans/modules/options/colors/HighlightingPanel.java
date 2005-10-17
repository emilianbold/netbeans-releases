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
//    private JTextField          tfFont = new JTextField ("");
//    private JButton             bFont = new JButton ("...");
    private ColorComboBox	foregroundColorChooser = new ColorComboBox ();
    private ColorComboBox	backgroundColorChooser = new ColorComboBox ();
    private JComboBox		cbEffects = new JComboBox ();
    private ColorComboBox	effectsColorChooser = new ColorComboBox ();
//    private JPanel              previewPanel;
//    private Preview             preview;
 
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
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (3);
        lCategories.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                refreshUI ();
            }
        });
        lCategories.setCellRenderer (new CategoryRenderer ());
//	tfFont.setEnabled (false);
//        bFont.addActionListener (this);
//        bFont.setMargin (new Insets (0, 0, 0, 0));
        foregroundColorChooser.addPropertyChangeListener (this);
        backgroundColorChooser.addPropertyChangeListener (this);
        cbEffects.addItem (loc ("CTL_Effects_None"));
        cbEffects.addItem (loc ("CTL_Effects_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Wave_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Strike_Through"));
        cbEffects.addActionListener (this);
        effectsColorChooser = new ColorComboBox ();
        effectsColorChooser.addPropertyChangeListener (this);
//        previewPanel = new JPanel (new BorderLayout ());
//        previewPanel.setBorder (new EtchedBorder ());

        // 2) define layout
	FormLayout layout = new FormLayout (
            "p:g, 10dlu, p, 3dlu, p:g, 1dlu, p", // cols
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, p:g"); //, 3dlu, p, 3dlu, f:130dlu:g");      // rows
        PanelBuilder builder = new PanelBuilder (layout);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();
        builder = new PanelBuilder (layout, this);
        builder.setDefaultDialogBorder ();
        builder.addLabel (loc ("CTL_Category"),         lc.xy   (1, 1),
	                  new JScrollPane (lCategories),cc.xywh (1, 3, 1, 8));
        builder.addLabel (loc ("CTL_Foreground_label"), lc.xy   (3, 3),
                          foregroundColorChooser,	cc.xyw  (5, 3, 3));
        builder.addLabel (loc ("CTL_Background_label"), lc.xy   (3, 5),
                          backgroundColorChooser,	cc.xyw  (5, 5, 3));
        builder.addLabel (loc ("CTL_Effects_label"),	lc.xy   (3, 7),
                          cbEffects,			cc.xyw  (5, 7, 3));
        builder.add (     effectsColorChooser,          cc.xyw  (5, 9, 3));
	
//        builder.addLabel (loc ("CTL_Preview"),	        lc.xyw (1, 11, 7),
//                          previewPanel,                 cc.xyw (1, 13, 7));
    }
 
    public void actionPerformed (ActionEvent evt) {
//        if (evt.getSource () == bFont) {
//            PropertyEditor pe = PropertyEditorManager.findEditor (Font.class);
//            Vector categories = getCategories (currentProfile);
//	    SimpleAttributeSet category = (SimpleAttributeSet) categories.get 
//		(lCategories.getSelectedIndex ());
//            Font f = category.getFont ();
//            if (f == null && category.getDefaultCategoryName () != null)
//                f = getDefault (category).getFont ();
//            pe.setValue (f);
//            DialogDescriptor dd = new DialogDescriptor (
//                pe.getCustomEditor (),
//                "Font Chooser"
//            );
//            DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
//            if (dd.getValue () == DialogDescriptor.OK_OPTION) {
//                f = (Font) pe.getValue ();
//                if (category.getDefaultCategoryName () != null &&
//		    f.equals (getDefault (category).getFont ())
//		)
//                    f = null;
//                categories.set (
//		    lCategories.getSelectedIndex (),
//		    new Category (
//                        category.getName (),
//			category.getDisplayName (),
//			category.getIcon (), 
//                        f,
//                        category.getForeground (), 
//		        category.getBackground (), 
//                        category.getUnderlineColor (), 
//		        category.getStrikeThroughColor (),
//                        category.getWaveUnderlineColor (),
//		        category.getDefaultCategoryName ()
//                    )
//                );
//                toBeSaved.add (currentProfile);
//                refreshUI ();
//                preview.setParameters (currentProfile, "", getCategories (currentProfile));
//            }
//            return;
//        }
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

    private void updateData () {
        if (lCategories.getSelectedIndex () < 0) return;
        Vector categories = getCategories (currentProfile);
	SimpleAttributeSet category = (SimpleAttributeSet) categories.get 
	    (lCategories.getSelectedIndex ());
        Color underline = null, 
              wave = null, 
              strikethrough = null;
        if (cbEffects.getSelectedIndex () == 1)
            underline = effectsColorChooser.getColor ();
        if (cbEffects.getSelectedIndex () == 2)
            wave = effectsColorChooser.getColor ();
        if (cbEffects.getSelectedIndex () == 3)
            strikethrough = effectsColorChooser.getColor ();
        
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
        
//        preview.setParameters (
//            ColorModel.HIGHLIGHTING_LANGUAGE,
//            fontAndColorsPanel.getDefaults (),
//            categories,
//            fontAndColorsPanel.getSyntaxColorings ()
//        );
        toBeSaved.add (currentProfile);
    }
    
    private void refreshUI () {
        int index = lCategories.getSelectedIndex ();
        if (index < 0) {
//	    tfFont.setText ("");
            cbEffects.setEnabled (false);
            foregroundColorChooser.setEnabled (false);
            backgroundColorChooser.setEnabled (false);
            effectsColorChooser.setEnabled (false);
            return;
        }
        cbEffects.setEnabled (true);
        foregroundColorChooser.setEnabled (true);
        backgroundColorChooser.setEnabled (true);
        
        Vector categories = getCategories (currentProfile);
	AttributeSet category = (AttributeSet) categories.get (index);
        
        // set values
        listen = false;
//        Font f = category.getFont ();
//	if (f != null) {
//            StringBuffer sb = new StringBuffer ();
//            sb.append (f.getName ()).
//                append (' ').
//                append (f.getSize ());
//            if (f.isBold ())
//                sb.append (' ').append (loc ("Bold"));
//            if (f.isItalic ())
//                sb.append (' ').append (loc ("Italic"));
//	    tfFont.setText (sb.toString ());
//        } else
//	    tfFont.setText (loc ("Default"));
	
        foregroundColorChooser.setColor (
            (Color) category.getAttribute (StyleConstants.Foreground)
        );
        backgroundColorChooser.setColor (
            (Color) category.getAttribute (StyleConstants.Background)
        );
        
        if (category.getAttribute (StyleConstants.Underline) != null) {
            cbEffects.setSelectedIndex (1);
            effectsColorChooser.setEnabled (true);
            effectsColorChooser.setColor (
                (Color) category.getAttribute (StyleConstants.Underline)
            );
        } else
        if (category.getAttribute (EditorStyleConstants.WaveUnderlineColor) != null) {
            cbEffects.setSelectedIndex (2);
            effectsColorChooser.setEnabled (true);
            effectsColorChooser.setColor (
                (Color) category.getAttribute (EditorStyleConstants.WaveUnderlineColor)
            );
        } else
        if (category.getAttribute (StyleConstants.StrikeThrough) != null) {
            cbEffects.setSelectedIndex (3);
            effectsColorChooser.setEnabled (true);
            effectsColorChooser.setColor 
                ((Color) category.getAttribute (StyleConstants.StrikeThrough));
        } else {
            cbEffects.setSelectedIndex (0);
            effectsColorChooser.setEnabled (false);
	    effectsColorChooser.setSelectedItem (new Value (null, null));
        }
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
