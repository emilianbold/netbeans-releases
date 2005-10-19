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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;


/**
 *
 * @author  Administrator
 */
public class AnnotationsPanel extends JPanel implements ActionListener, 
PropertyChangeListener {
    
    private ColorModel          colorModel;
    
    private JList		lCategories = new JList ();
    private ColorComboBox	foregroundColorChooser = new ColorComboBox ();
    private ColorComboBox	backgroundColorChooser = new ColorComboBox ();
    private ColorComboBox	waveUnderlinedColorChooser = new ColorComboBox ();
 
    private boolean		listen = false;
    private String              currentScheme;
    private Map                 schemes = new HashMap ();
    private Set                 toBeSaved = new HashSet ();
    private boolean             changed = false;
    
    
    /** Creates new form FontAndColorsPanel */
    public AnnotationsPanel (FontAndColorsPanel fontAndColorsPanel) {
        
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
        foregroundColorChooser.addPropertyChangeListener (this);
        backgroundColorChooser.addPropertyChangeListener (this);
        waveUnderlinedColorChooser.addPropertyChangeListener (this);
        JLabel lCategory = new JLabel ();
        loc (lCategory, "CTL_Category");
        lCategory.setLabelFor (lCategories);

        // 2) define layout
	FormLayout layout = new FormLayout (
            "p:g, 10dlu, p, 3dlu, p:g", // cols
            "p, 3dlu, p, 3dlu, p, 3dlu, p, p:g");//, f:130dlu:g");      // rows
        PanelBuilder builder = new PanelBuilder (layout, this);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();
        builder.setDefaultDialogBorder ();
        builder.add (lCategory,                         lc.xy  (1, 1));
	builder.add (new JScrollPane (lCategories),     cc.xywh(1, 3, 1, 6));
        builder.addLabel (loc ("CTL_Foreground_label"), lc.xy  (3, 3),
                          foregroundColorChooser,	cc.xy  (5, 3));
        builder.addLabel (loc ("CTL_Background_label"), lc.xy  (3, 5),
                          backgroundColorChooser,	cc.xy  (5, 5));
        builder.addLabel (loc ("CTL_Wave_underlined_label"),	lc.xy  (3, 7),
                          waveUnderlinedColorChooser,   cc.xy  (5, 7));
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
    }
    
    void update (ColorModel colorModel) {
        this.colorModel = colorModel;
        listen = false;
        currentScheme = colorModel.getCurrentProfile ();
        lCategories.setListData (getAnnotations (currentScheme));
        lCategories.setSelectedIndex (0);
        refreshUI ();
        listen = true;
        changed = false;
    }
    
    void cancel () {
        toBeSaved = new HashSet ();
        schemes = new HashMap ();
        changed = false;
    }
    
    void applyChanges () {
        if (colorModel == null) return;
        Iterator it = toBeSaved.iterator ();
        while (it.hasNext ()) {
            String scheme = (String) it.next ();
            colorModel.setAnnotations (scheme, getAnnotations (scheme));
        }
        toBeSaved = new HashSet ();
        schemes = new HashMap ();
    }
    
    boolean isChanged () {
        return changed;
    }
    
    public void setCurrentProfile (String currentScheme) {
        String oldScheme = this.currentScheme;
        this.currentScheme = currentScheme;
        Vector v = getAnnotations (currentScheme);
        if (v == null) {
            // clone scheme
            v = getAnnotations (oldScheme);
            schemes.put (currentScheme, new Vector (v));
            toBeSaved.add (currentScheme);
            v = getAnnotations (currentScheme);
        }
        lCategories.setListData (v);
        lCategories.setSelectedIndex (0);
        refreshUI ();
    }
    
    void deleteProfile (String scheme) {
    }
        
    
    // other methods ...........................................................
    
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
        Vector annotations = getAnnotations (currentScheme);
	SimpleAttributeSet c = (SimpleAttributeSet) annotations.get 
	    (lCategories.getSelectedIndex ());
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
        if (waveUnderlinedColorChooser.getColor () != null)
            c.addAttribute (
                EditorStyleConstants.WaveUnderlineColor,
                waveUnderlinedColorChooser.getColor ()
            );
        else
            c.removeAttribute (EditorStyleConstants.WaveUnderlineColor);
        toBeSaved.add (currentScheme);
    }
    
    private void refreshUI () {
        int index = lCategories.getSelectedIndex ();
        if (index < 0) {
	    // no category selected
            foregroundColorChooser.setEnabled (false);
            backgroundColorChooser.setEnabled (false);
            waveUnderlinedColorChooser.setEnabled (false);
            return;
        }
        foregroundColorChooser.setEnabled (true);
        backgroundColorChooser.setEnabled (true);
        waveUnderlinedColorChooser.setEnabled (true);
        listen = false;
        Vector annotations = getAnnotations (currentScheme);
        AttributeSet c = (AttributeSet) annotations.get (index);
        foregroundColorChooser.setColor ((Color) c.getAttribute (StyleConstants.Foreground));
        backgroundColorChooser.setColor ((Color) c.getAttribute (StyleConstants.Background));
	waveUnderlinedColorChooser.setColor ((Color) c.getAttribute (EditorStyleConstants.WaveUnderlineColor));
        listen = true;
    }
    
    private Vector getAnnotations (String scheme) {
        if (!schemes.containsKey (scheme)) {
            Collection c = colorModel.getAnnotations (currentScheme);
            if (c == null) return null;
            List l = new ArrayList (c);
            Collections.sort (l, new CategoryComparator ());
            schemes.put (scheme, new Vector (l));
        }
        return (Vector) schemes.get (scheme);
    }
}
