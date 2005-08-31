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
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.plain.PlainKit;
import org.netbeans.modules.options.colors.ColorComboBox.Value;
import org.netbeans.modules.options.colors.ColorModel.Category;
import org.netbeans.modules.options.colors.ColorModel.Preview;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;


/**
 *
 * @author  Jan Jancura
 */
public class SyntaxColoringPanel extends JPanel implements ActionListener, 
PropertyChangeListener {
    
    private ColorModel          colorModel = ColorModel.getDefault ();
    
    private JComboBox		cbLanguages = new JComboBox ();
    private JList		lCategories = new JList ();
    private JTextField          tfFont = new JTextField ("");
    private JButton             bFont = new JButton ("...");
    private ColorComboBox	foregroundColorChooser = new ColorComboBox ();
    private ColorComboBox	backgroundColorChooser = new ColorComboBox ();
    private JComboBox		cbEffects = new JComboBox ();
    private ColorComboBox	effectsColorChooser = new ColorComboBox ();
    private JPanel              previewPanel = new JPanel ();
 
    private String		currentLanguage;
    private String              currentScheme;
    private Map                 schemes = new HashMap ();
    private Map                 toBeSaved = new HashMap ();
    private boolean		listen = false;

    
    /** Creates new form FontAndColorsPanel */
    public SyntaxColoringPanel (String currentScheme) {
        this.currentScheme = currentScheme;
        currentLanguage = (String) colorModel.getLanguages ().iterator ().next ();

        // 1) init components
	List languages = new ArrayList 
	    (colorModel.getLanguages ());
	Collections.sort (languages, new LanguagesComparator ());
	Iterator it = languages.iterator ();
        while (it.hasNext ())
            cbLanguages.addItem (it.next ());
        cbLanguages.addActionListener (this);
	    
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (3);
        
	tfFont.setEditable (false);
        bFont.addActionListener (this);
        bFont.setMargin (new Insets (0, 0, 0, 0));
        foregroundColorChooser.addPropertyChangeListener (this);

        backgroundColorChooser.addPropertyChangeListener (this);
        
        cbEffects.addActionListener (this);
        effectsColorChooser = new ColorComboBox ();
        effectsColorChooser.addPropertyChangeListener (this);

        previewPanel = (JPanel) colorModel.getPreviewComponent 
            (currentScheme, currentLanguage, true);
        previewPanel.setBorder (new EtchedBorder ());

        // 2) define layout
        FormLayout layout = new FormLayout (
            "p, 3dlu, 120dlu",   // cols
            "p");           // rows
        PanelBuilder builder = new PanelBuilder (layout);
        CellConstraints cc = new CellConstraints ();
        CellConstraints lc = new CellConstraints ();
        builder.addLabel (loc ("CTL_Languages"),	lc.xy  (1, 1),
                          cbLanguages,			cc.xy  (3, 1));
	JPanel pLanguages = builder.getPanel ();
	
	layout = new FormLayout (
            "p:g, 10dlu, p, 3dlu, p:g, 1dlu, p", // cols
            "p, 10dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 5dlu, p, 3dlu, f:130dlu:g");      // rows
        layout.setColumnGroups (new int [][] {{1, 5}});
        builder = new PanelBuilder (layout, this);
        builder.setDefaultDialogBorder ();
	
	builder.add (pLanguages,                        cc.xyw (1, 1, 5));
        
        builder.addLabel (loc ("CTL_Category"),         lc.xy  (1, 3),
	                  new JScrollPane (lCategories),cc.xywh(1, 5, 1, 9));
        
        builder.addLabel (loc ("CTL_Font"),             lc.xy  (3, 5),
                          tfFont,	                cc.xy  (5, 5));
        builder.add (bFont,                             cc.xy  (7, 5));
        builder.addLabel (loc ("CTL_Foreground_label"), lc.xy  (3, 7),
                          foregroundColorChooser,	cc.xyw (5, 7, 3));
        builder.addLabel (loc ("CTL_Background_label"), lc.xy  (3, 9),
                          backgroundColorChooser,	cc.xyw (5, 9, 3));
        builder.addLabel (loc ("CTL_Effects_label"),	lc.xy  (3, 11),
                          cbEffects,			cc.xyw (5, 11, 3));
        builder.add (effectsColorChooser,		cc.xyw (5, 13, 3));
	
        builder.addLabel (loc ("CTL_Preview"),	        lc.xyw (1, 15, 7),
                          previewPanel,                 cc.xyw (1, 17, 7));
        
	lCategories.setCellRenderer (new CategoryRenderer ());
        lCategories.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                refreshUI ();
            }
        });
        
        cbEffects.addItem (loc ("CTL_Effects_None"));
        cbEffects.addItem (loc ("CTL_Effects_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Wave_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Strike_Through"));
	//setCurrentLanguage (currentLanguage);
        listen = true;
        cbLanguages.setSelectedIndex (0);
        lCategories.setSelectedIndex (0);
    }
 
    public void actionPerformed (ActionEvent evt) {
	if (evt.getSource () == cbEffects) {
	    effectsColorChooser.setEnabled (cbEffects.getSelectedIndex () > 0);
	} else
	if (evt.getSource () == cbLanguages) {
	    setCurrentLanguage ((String) cbLanguages.getSelectedItem ());
	} else
        if (evt.getSource () == bFont) {
            PropertyEditor pe = PropertyEditorManager.findEditor (Font.class);
            Category category = getCurrentCategory ();
            Font f = category.getFont ();
            Font defaultFont = getDefaultFont (category);
            if (f == null && category.getDefaultCategoryName () != null)
                f = defaultFont;
            pe.setValue (f);
            DialogDescriptor dd = new DialogDescriptor (
                pe.getCustomEditor (),
                "Font Chooser"
            );
            DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
            if (dd.getValue () == DialogDescriptor.OK_OPTION) {
                f = (Font) pe.getValue ();
                if (category.getDefaultCategoryName () != null &&
                    defaultFont != null &&
		    f.equals (defaultFont)
		)
                    f = null;
		getCategories (currentScheme, currentLanguage).set (
		    lCategories.getSelectedIndex (),
		    new Category (
		        category.getName (),
		        category.getDisplayName (),
		        category.getIcon (), 
                        f,
			category.getBackground (), 
                        category.getForeground (), 
                        category.getUnderlineColor (), 
			category.getStrikeThroughColor (),
                        category.getWaveUnderlineColor (),
		        category.getDefaultCategoryName ()
                    )
                );
                setToBeSaved (currentScheme, currentLanguage);
                refreshUI (); // refresh font viewer
            }
        }
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (!listen) return;
        if (evt.getPropertyName () != ColorComboBox.PROP_COLOR) return;
        updateData ();
    }
    
    public void applyChanges () {
	Iterator it = toBeSaved.keySet ().iterator ();
	while (it.hasNext ()) {
	    String scheme = (String) it.next ();
            Set toBeSavedLanguages = (Set) toBeSaved.get (scheme);
            Map schemeMap = (Map) schemes.get (scheme);
            Iterator it2 = toBeSavedLanguages.iterator ();
            while (it2.hasNext ()) {
                String languageName = (String) it2.next ();
                colorModel.setCategories (
                    scheme,
                    languageName,
                    (Vector) schemeMap.get (languageName)
                );
            }
	}
    }
    
    public void setCurrentScheme (String currentScheme) {
        String oldScheme = this.currentScheme;
        this.currentScheme = currentScheme;
        Vector v = getCategories (currentScheme, currentLanguage);
        if (v == null) {
            cloneScheme (oldScheme, currentScheme);
            v = getCategories (currentScheme, currentLanguage);
        }
        lCategories.setListData (v);
        refreshUI ();
    }
    
        
    // other methods ...........................................................
    
    private void cloneScheme (String oldScheme, String newScheme) {
        Map m = new HashMap ();
        Iterator it = colorModel.getLanguages ().iterator ();
        while (it.hasNext ()) {
            String language = (String) it.next ();
            Vector v = getCategories (oldScheme, language);
            m.put (language, new Vector (v));
            setToBeSaved (newScheme, language);
        }
        schemes.put (newScheme, m);
    }
    
    private void setCurrentLanguage (String language) {
	currentLanguage = language;
        
        // setup categories list
        lCategories.setListData (getCategories (currentScheme, currentLanguage));
        refreshUI ();
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

    /**
     * Called on user change.
     * Updates data structures and preview panel.
     */
    private void updateData () {
        int i = lCategories.getSelectedIndex ();
        if (i < 0) return;
        
        Category category = getCurrentCategory ();
        Color underline = null, 
              wave = null, 
              strikethrough = null;
        if (cbEffects.getSelectedIndex () == 1)
            underline = effectsColorChooser.getColor ();
        if (cbEffects.getSelectedIndex () == 2)
            wave = effectsColorChooser.getColor ();
        if (cbEffects.getSelectedIndex () == 3)
            strikethrough = effectsColorChooser.getColor ();
        getCategories (currentScheme, currentLanguage).set (
	    i,
	    new Category (
		category.getName (), 
		category.getDisplayName (), 
		category.getIcon (), 
		category.getFont (),
		backgroundColorChooser.getColor (),
		foregroundColorChooser.getColor (), 
		underline,
		strikethrough,
		wave,
	        category.getDefaultCategoryName ()
	    )
        );
        setToBeSaved (currentScheme, currentLanguage);
        updatePreview ();
    }
    
    private void updatePreview () {
        ((Preview) previewPanel).setParameters (
            currentScheme, 
            currentLanguage, 
            getCategories (currentScheme, currentLanguage)
        );
    }
    
    /**
     * Called when current category, scheme or language has been changed.
     * Updates all ui components.
     */
    private void refreshUI () {
        Category category = getCurrentCategory ();
        if (category == null) {
            // no category selected > disable all elements
	    tfFont.setText ("");
            bFont.setEnabled (false);
            cbEffects.setEnabled (false);
            foregroundColorChooser.setEnabled (false);
	    foregroundColorChooser.setSelectedItem (new Value (null, null));
            backgroundColorChooser.setEnabled (false);
	    backgroundColorChooser.setSelectedItem (new Value (null, null));
            effectsColorChooser.setEnabled (false);
	    effectsColorChooser.setSelectedItem (new Value (null, null));
            updatePreview ();
            return;
        }
        bFont.setEnabled (false);
        cbEffects.setEnabled (true);
        foregroundColorChooser.setEnabled (true);
        backgroundColorChooser.setEnabled (true);
        
        // set defaults
        foregroundColorChooser.setDefaultColor (getDefaultForeground (category));
        backgroundColorChooser.setDefaultColor (getDefaultBackground (category));
        
        listen = false;
        Font f = category.getFont ();
	if (f != null) {
            StringBuffer sb = new StringBuffer ();
            sb.append (f.getName ()).
                append (' ').
                append (f.getSize ());
            if (f.isBold ())
                sb.append (' ').append (loc ("Bold"));
            if (f.isItalic ())
                sb.append (' ').append (loc ("Italic"));
	    tfFont.setText (sb.toString ());
        } else
	    tfFont.setText (loc ("Default"));
        foregroundColorChooser.setColor (category.getForeground ());
        backgroundColorChooser.setColor (category.getBackground ());
        
        if (category.getUnderlineColor () != null) {
            cbEffects.setSelectedIndex (1);
            effectsColorChooser.setEnabled (true);
            effectsColorChooser.setColor (category.getUnderlineColor ());
        } else
        if (category.getWaveUnderlineColor () != null) {
            cbEffects.setSelectedIndex (2);
            effectsColorChooser.setEnabled (true);
            effectsColorChooser.setColor 
                (category.getWaveUnderlineColor ());
        } else
        if (category.getStrikeThroughColor () != null) {
            cbEffects.setSelectedIndex (3);
            effectsColorChooser.setEnabled (true);
            effectsColorChooser.setColor 
                (category.getStrikeThroughColor ());
        } else {
            cbEffects.setSelectedIndex (0);
            effectsColorChooser.setEnabled (false);
	    effectsColorChooser.setSelectedItem (new Value (null, null));
        }
        updatePreview ();
        listen = true;
    }
    
    private void setToBeSaved (String currentScheme, String currentLanguage) {
        Set s = (Set) toBeSaved.get (currentScheme);
        if (s == null) {
            s = new HashSet ();
            toBeSaved.put (currentScheme, s);
        }
        s.add (currentLanguage);
    }
    
    private Vector getCategories (String scheme, String language) {
        Map m = (Map) schemes.get (scheme);
        if (m == null) {
            m = new HashMap ();
            schemes.put (scheme, m);
        }
        Vector v = (Vector) m.get (language);
        if (v == null) {
            Collection c = colorModel.getCategories 
                (scheme, language);
            if (c == null) return null;
            List l = new ArrayList (c);
            Collections.sort (l, new CategoryComparator ());
            v = new Vector (l);
            m.put (language, v);
        }
        return v;
    }
    
    private Category getCurrentCategory () {
        int i = lCategories.getSelectedIndex ();
        if (i < 0) return null;
        return (Category) getCategories (currentScheme, currentLanguage).get (i);
    }
    
    private Font getDefaultFont (Category category) {
	String name = category.getDefaultCategoryName ();
	if (name == null) return null;
	return (Font) getDefault1 (category, 0);
    }
    
    private Color getDefaultForeground (Category category) {
	String name = category.getDefaultCategoryName ();
	if (name == null) return null;
	return (Color) getDefault1 (category, 1);
    }
    
    private Color getDefaultBackground (Category category) {
	String name = category.getDefaultCategoryName ();
	if (name == null) return null;
	return (Color) getDefault1 (category, 2);
    }
    
    private Object getDefault1 (Category category, int type) {
	if (category == null) return null;
        switch (type) {
            case 0:
                if (category.getFont () != null) return category.getFont ();
                break;
            case 1:
                if (category.getForeground () != null) return category.getForeground ();
                break;
            case 2:
                if (category.getBackground () != null) return category.getBackground ();
                break;
        }
	String name = category.getDefaultCategoryName ();
	if (name == null) return null;

	// 1) search current language
	if (!name.equals (category.getName ())) {
	    Vector v = getCategories (currentScheme, currentLanguage);
	    Iterator it = v.iterator ();
	    while (it.hasNext ()) {
		Category c = (Category) it.next ();
		if (c.getName ().equals (name)) 
		    return getDefault1 (c, type);
	    }
	}
	
	// 2) search default language
	Vector v = getCategories (currentScheme, ColorModel.ALL_LANGUAGES);
	if (v != null) {
	    Iterator it = v.iterator ();
	    while (it.hasNext ()) {
		Category c = (Category) it.next ();
		if (c.getName ().equals (name)) 
		    return getDefault1 (c, type);
	    }
	}
        return null;
    }
    
    private static class LanguagesComparator implements Comparator {
	public int compare (Object o1, Object o2) {
	    if (o1.equals (ColorModel.ALL_LANGUAGES)) 
		return o2.equals (ColorModel.ALL_LANGUAGES) ? 0 : -1;
	    return ((String) o1).compareTo ((String) o2);
	}
    }
}
