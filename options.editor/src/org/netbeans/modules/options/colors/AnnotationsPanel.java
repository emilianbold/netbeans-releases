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

import org.netbeans.modules.options.colors.ColorModel.Category;

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
    
    private ColorModel          colorModel = ColorModel.getDefault ();
    
    private JList		lCategories = new JList ();
    private ColorComboBox	foregroundColorChooser = new ColorComboBox ();
    private ColorComboBox	backgroundColorChooser = new ColorComboBox ();
    private ColorComboBox	waveUnderlinedColorChooser = new ColorComboBox ();
//    private JPanel              previewPanel = new JPanel ();
 
    private boolean		listen = false;
    private String              currentScheme;
    private Map                 schemes = new HashMap ();
    private Set                 toBeSaved = new HashSet ();

    
    /** Creates new form FontAndColorsPanel */
    public AnnotationsPanel (String currentScheme) {
        this.currentScheme = currentScheme;

        // 1) init components
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (3);
        
        foregroundColorChooser.addPropertyChangeListener (this);
        backgroundColorChooser.addPropertyChangeListener (this);
        waveUnderlinedColorChooser.addPropertyChangeListener (this);

//        previewPanel.setLayout (new BorderLayout ());
//        previewPanel.setBorder (new EtchedBorder ());

        // 2) define layout
	FormLayout layout = new FormLayout (
            "p:g, 10dlu, p, 3dlu, p:g", // cols
            "p, 3dlu, p, 3dlu, p, 3dlu, p, p:g");//, f:130dlu:g");      // rows
        //layout.setColumnGroups (new int [][] {{1, 5}});
        PanelBuilder builder = new PanelBuilder (layout, this);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();
        builder.setDefaultDialogBorder ();
	
        builder.addLabel (loc ("CTL_Category"),         lc.xy  (1, 1),
	                  new JScrollPane (lCategories),cc.xywh(1, 3, 1, 6));
        
        builder.addLabel (loc ("CTL_Foreground_label"), lc.xy  (3, 3),
                          foregroundColorChooser,	cc.xy  (5, 3));
        builder.addLabel (loc ("CTL_Background_label"), lc.xy  (3, 5),
                          backgroundColorChooser,	cc.xy  (5, 5));
        builder.addLabel (loc ("CTL_Wave_underlined_label"),	lc.xy  (3, 7),
                          waveUnderlinedColorChooser,   cc.xy  (5, 7));
	
//        builder.add (previewPanel,                      cc.xyw (1, 9, 5));
        
        lCategories.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                refreshUI ();
            }
        });
        
//	previewPanel.removeAll ();
//	previewPanel.add (
//	    "Center", 
//	    colorModel.getPreviewComponent ("Defaults")
//	);
        
        // setup categories list
        lCategories.setListData (getAnnotations (currentScheme));
	lCategories.setCellRenderer (new CategoryRenderer ());
        refreshUI ();
        listen = true;
    }
 
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
        updateData ();
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (!listen) return;
        if (evt.getPropertyName () != ColorComboBox.PROP_COLOR) return;
        updateData ();
    }
    
    public void applyChanges () {
        Iterator it = toBeSaved.iterator ();
        while (it.hasNext ()) {
            String scheme = (String) it.next ();
            colorModel.setAnnotations (scheme, getAnnotations (scheme));
        }
    }
    
    public void setCurrentScheme (String currentScheme) {
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
        refreshUI ();
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
	Category c = (Category) annotations.get 
	    (lCategories.getSelectedIndex ());
	annotations.set (
	    lCategories.getSelectedIndex (),
	    new Category (
		c.getName (),
		c.getDisplayName (),
		c.getIcon (),
		null,
		backgroundColorChooser.getColor (),  
		foregroundColorChooser.getColor (), 
		null,
		null,
	        waveUnderlinedColorChooser.getColor (),
	        null
	    )
	);
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
        
        Vector annotations = getAnnotations (currentScheme);
        Category c = (Category) annotations.get (index);
        foregroundColorChooser.setColor (c.getForeground ());
        backgroundColorChooser.setColor (c.getBackground ());
	waveUnderlinedColorChooser.setColor (c.getWaveUnderlineColor ());
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
    
    private String fontToString (Font f) {
	StringBuffer sb = new StringBuffer ();
	sb.append (f.getName ()).
	    append (' ').
	    append (f.getSize ());
	if (f.isBold ())
	    sb.append (' ').append (loc ("Bold"));
	if (f.isItalic ())
	    sb.append (' ').append (loc ("Italic"));
	return sb.toString ();
    }
}
