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
import javax.swing.UIManager;
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
    
    private JComboBox		cbLanguages = new JComboBox ();
    private JList		lCategories = new JList ();
    private JTextField          tfFont = new JTextField ("");
    private JButton             bFont = new JButton ("...");
    private ColorComboBox	foregroundColorChooser = new ColorComboBox ();
    private ColorComboBox	backgroundColorChooser = new ColorComboBox ();
    private JComboBox		cbEffects = new JComboBox ();
    private ColorComboBox	effectsColorChooser = new ColorComboBox ();
    private JPanel              previewPanel = new JPanel ();
    private Preview             preview;
 
    private FontAndColorsPanel  fontAndColorsPanel;
    private ColorModel          colorModel = null;
    private String		currentLanguage;
    private String              currentProfile;
    /** cache Map (String (profile name) > Map (String (language name) > Vector (AttributeSet))). */
    private Map                 profiles = new HashMap ();
    /** Map (String (profile name) > Set (String (language name))) of names of changed languages. */
    private Map                 toBeSaved = new HashMap ();
    private boolean		listen = false;
    private boolean             changed = false;

    
    /** Creates new form FontAndColorsPanel */
    public SyntaxColoringPanel (FontAndColorsPanel fontAndColorsPanel) {
        this.fontAndColorsPanel = fontAndColorsPanel;
        
        // 1) init components
        cbLanguages.addActionListener (this);
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (3);
	lCategories.setCellRenderer (new CategoryRenderer ());
        lCategories.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                refreshUI ();
            }
        });
	tfFont.setEditable (false);
        bFont.addActionListener (this);
        bFont.setMargin (new Insets (0, 0, 0, 0));
        foregroundColorChooser.addPropertyChangeListener (this);

        backgroundColorChooser.addPropertyChangeListener (this);
        
        cbEffects.addItem (loc ("CTL_Effects_None"));
        cbEffects.addItem (loc ("CTL_Effects_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Wave_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Strike_Through"));
        cbEffects.addActionListener (this);
        effectsColorChooser = new ColorComboBox ();
        effectsColorChooser.addPropertyChangeListener (this);
        JLabel lCategory = new JLabel ();
        loc (lCategory, "CTL_Category");
        lCategory.setLabelFor (lCategories);
        JLabel lbFont = new JLabel ();
        loc (lbFont, "CTL_Font");
        lbFont.setLabelFor (bFont);

        previewPanel = new JPanel (new BorderLayout ());
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
        //layout.setColumnGroups (new int [][] {{1, 5}});
        builder = new PanelBuilder (layout, this);
        builder.setDefaultDialogBorder ();
	
	builder.add (pLanguages,                        cc.xyw (1, 1, 5));
        
        builder.add (lCategory,                         lc.xy  (1, 3));
	builder.add (new JScrollPane (lCategories),     cc.xywh(1, 5, 1, 9));
        
        builder.add (lbFont,                            lc.xy  (3, 5));
        builder.add (tfFont,	                        cc.xy  (5, 5));
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
    }
 
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
	if (evt.getSource () == cbEffects) {
	    effectsColorChooser.setEnabled (cbEffects.getSelectedIndex () > 0);
            changed = true;
	} else
	if (evt.getSource () == cbLanguages) {
	    setCurrentLanguage ((String) cbLanguages.getSelectedItem ());
	} else
        if (evt.getSource () == bFont) {
            PropertyEditor pe = PropertyEditorManager.findEditor (Font.class);
            AttributeSet category = getCurrentCategory ();
            Font f = getFont (category);
            pe.setValue (f);
            DialogDescriptor dd = new DialogDescriptor (
                pe.getCustomEditor (),
                loc ("CTL_Font_Chooser")                          // NOI18N
            );
            DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
            if (dd.getValue () == DialogDescriptor.OK_OPTION) {
                f = (Font) pe.getValue ();
                String fontName = f.getName ();
                Integer fontSize = new Integer (f.getSize ());
                Boolean bold = Boolean.valueOf (f.isBold ());
                Boolean italic = Boolean.valueOf (f.isItalic ());
                AttributeSet defaultCategory = getDefault (category);
                if (defaultCategory != null) {
                    if (fontName.equals (
                        getValue (defaultCategory, StyleConstants.FontFamily)
                    ))
                        fontName = null;
                    if (fontSize.equals (
                        getValue (defaultCategory, StyleConstants.FontSize)
                    ))
                        fontSize = null;
                    if (bold.equals 
                        (getValue (defaultCategory, StyleConstants.Bold)) ||
                        bold.equals (Boolean.FALSE)
                    )
                        bold = null;
                    if (italic.equals 
                        (getValue (defaultCategory, StyleConstants.Italic)) ||
                        italic.equals (Boolean.FALSE)
                    )
                        italic = null;
                }
                SimpleAttributeSet c = new SimpleAttributeSet (category);
                if (fontName != null)
                    c.addAttribute (
                        StyleConstants.FontFamily,
                        fontName
                    );
                else
                    c.removeAttribute (StyleConstants.FontFamily);
                if (fontSize != null)
                    c.addAttribute (
                        StyleConstants.FontSize,
                        fontSize
                    );
                else
                    c.removeAttribute (StyleConstants.FontSize);
                if (bold != null)
                    c.addAttribute (
                        StyleConstants.Bold,
                        bold
                    );
                else
                    c.removeAttribute (StyleConstants.Bold);
                if (italic != null)
                    c.addAttribute (
                        StyleConstants.Italic,
                        italic
                    );
                else
                    c.removeAttribute (StyleConstants.Italic);
                replaceCurrrentCategory (c);
                setToBeSaved (currentProfile, currentLanguage);
                refreshUI (); // refresh font viewer
                changed = true;
            }
        }
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
        currentLanguage = (String) colorModel.getLanguages ().
            iterator ().next ();
        Component component = colorModel.getSyntaxColoringPreviewComponent 
            (currentLanguage);
        preview = (Preview) component;
        previewPanel.removeAll ();
        previewPanel.add ("Center", component);
        listen = false;
        List languages = new ArrayList 
            (colorModel.getLanguages ());
        Collections.sort (languages, new LanguagesComparator ());
        Iterator it = languages.iterator ();
        cbLanguages.removeAllItems ();
        while (it.hasNext ())
            cbLanguages.addItem (it.next ());
        listen = true;
        cbLanguages.setSelectedIndex (0);
        changed = false;
    }
    
    void cancel () {
        toBeSaved = new HashMap ();
        profiles = new HashMap ();
        changed = false;
    }
    
    void applyChanges () {
        if (colorModel == null) return;
	Iterator it = toBeSaved.keySet ().iterator ();
	while (it.hasNext ()) {
	    String profile = (String) it.next ();
            Set toBeSavedLanguages = (Set) toBeSaved.get (profile);
            Map schemeMap = (Map) profiles.get (profile);
            Iterator it2 = toBeSavedLanguages.iterator ();
            while (it2.hasNext ()) {
                String languageName = (String) it2.next ();
                colorModel.setCategories (
                    profile,
                    languageName,
                    (Vector) schemeMap.get (languageName)
                );
            }
	}
        toBeSaved = new HashMap ();
        profiles = new HashMap ();
    }
    
    boolean isChanged () {
        return changed;
    }
    
    public void setCurrentProfile (String currentProfile) {
        String oldProfile = this.currentProfile;
        this.currentProfile = currentProfile;
        if (!colorModel.getProfiles ().contains (currentProfile))
            cloneScheme (oldProfile, currentProfile);
        Vector categories = getCategories (currentProfile, currentLanguage);
        lCategories.setListData (categories);
        lCategories.setSelectedIndex (0);
        refreshUI ();
    }

    void deleteProfile (String profile) {
        Iterator it = colorModel.getLanguages ().iterator ();
        Map m = new HashMap ();
        boolean custom = colorModel.isCustomProfile (profile);
        while (it.hasNext ()) {
            String language = (String) it.next ();
            if (custom)
                m.put (language, null);
            else
                m.put (language, getDefaults (profile, language));
        }
        profiles.put (profile, m);
        toBeSaved.put (profile, new HashSet (colorModel.getLanguages ()));
        if (!custom)
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
        profiles.put (newScheme, m);
    }
    
    Collection getDeafults () {
        return getCategories (currentProfile, ColorModel.ALL_LANGUAGES);
    }
    
    Collection getSyntaxColorings () {
        return getCategories (currentProfile, currentLanguage);
    }
    
    private void setCurrentLanguage (String language) {
	currentLanguage = language;
        
        // setup categories list
        lCategories.setListData (getCategories (currentProfile, currentLanguage));
        lCategories.setSelectedIndex (0);
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
        
        AttributeSet category = getCurrentCategory ();
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
        replaceCurrrentCategory (c);
        
        setToBeSaved (currentProfile, currentLanguage);
        updatePreview ();
    }
    
    private void updatePreview () {
        preview.setParameters (
            currentLanguage,
            getDeafults (),
            fontAndColorsPanel.getHighlights (),
            getSyntaxColorings ()
        );
    }
    
    /**
     * Called when current category, profile or language has been changed.
     * Updates all ui components.
     */
    private void refreshUI () {
        AttributeSet category = getCurrentCategory ();
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
        bFont.setEnabled (true);
        cbEffects.setEnabled (true);
        foregroundColorChooser.setEnabled (true);
        backgroundColorChooser.setEnabled (true);
        
        // set defaults
        foregroundColorChooser.setDefaultColor (
            (Color) getValue (category, StyleConstants.Foreground)
        );
        backgroundColorChooser.setDefaultColor (
            (Color) getValue (category, StyleConstants.Background)
        );
        
        listen = false;
        String font = fontToString (category);
        tfFont.setText (font);
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
        updatePreview ();
        listen = true;
    }
    
    private void setToBeSaved (String currentProfile, String currentLanguage) {
        Set s = (Set) toBeSaved.get (currentProfile);
        if (s == null) {
            s = new HashSet ();
            toBeSaved.put (currentProfile, s);
        }
        s.add (currentLanguage);
    }
    
    private Vector getCategories (String profile, String language) {
        if (colorModel == null) return null;
        Map m = (Map) profiles.get (profile);
        if (m == null) {
            m = new HashMap ();
            profiles.put (profile, m);
        }
        Vector v = (Vector) m.get (language);
        if (v == null) {
            Collection c = colorModel.getCategories (profile, language);
            List l = new ArrayList (c);
            Collections.sort (l, new CategoryComparator ());
            v = new Vector (l);
            m.put (language, v);
        }
        return v;
    }

    private Map defaults = new HashMap ();
    private Vector getDefaults (String profile, String language) {
        Map m = (Map) defaults.get (profile);
        if (m == null) {
            m = new HashMap ();
            defaults.put (profile, m);
        }
        Vector v = (Vector) m.get (language);
        if (v == null) {
            Collection c = colorModel.getDefaults (profile, language);
            List l = new ArrayList (c);
            Collections.sort (l, new CategoryComparator ());
            v = new Vector (l);
            m.put (language, v);
        }
        return new Vector (v);
    }
    
    private AttributeSet getCurrentCategory () {
        int i = lCategories.getSelectedIndex ();
        if (i < 0) return null;
        return (AttributeSet) getCategories (currentProfile, currentLanguage).get (i);
    }
    
    private void replaceCurrrentCategory (AttributeSet newValues) {
        int i = lCategories.getSelectedIndex ();
        getCategories (currentProfile, currentLanguage).set (i, newValues);
    }
    
    private AttributeSet getCategory (
        String profile, 
        String language, 
        String name
    ) {
        Vector v = getCategories (profile, language);
        Iterator it = v.iterator ();
        while (it.hasNext ()) {
            AttributeSet c = (AttributeSet) it.next ();
            if (c.getAttribute (StyleConstants.NameAttribute).equals (name)) 
                return c;
        }
        return null;
    }
    
    private Object getValue (AttributeSet category, Object key) {
	if (category == null) return null;
        Object result = category.getAttribute (key);
        if (result != null) return result;
        
        AttributeSet d = getDefault (category);
        if (d != null) return getValue (d, key);
        
        if ("default".equals (
            category.getAttribute (StyleConstants.NameAttribute)
        )) return null;
        AttributeSet defaultCategory = getCategory 
            (currentProfile, currentLanguage, "default"); // NOI18N
        return getValue (defaultCategory, key);
    }
    
    private AttributeSet getDefault (AttributeSet category) {
	String name = (String) category.getAttribute (EditorStyleConstants.Default);
	if (name == null) return null;

	// 1) search current language
	if (!name.equals (category.getAttribute (StyleConstants.NameAttribute))) {
	    AttributeSet result = getCategory 
                (currentProfile, currentLanguage, name);
            if (result != null) return result;
	}
	
	// 2) search default language
        return getCategory (currentProfile, ColorModel.ALL_LANGUAGES, name);
    }
    
    private Font getFont (AttributeSet category) {
        String name = (String) getValue (category, StyleConstants.FontFamily);
        if (name == null) name = "Monospaced";                        // NOI18N
        Integer size = (Integer) getValue (category, StyleConstants.FontSize);
        if (size == null)
            size = getDefaultFontSize ();
        Boolean bold = (Boolean) getValue (category, StyleConstants.Bold);
        if (bold == null) bold = Boolean.FALSE;
        Boolean italic = (Boolean) getValue (category, StyleConstants.Italic);
        if (italic == null) italic = Boolean.FALSE;
        int style = bold.booleanValue () ? Font.BOLD : Font.PLAIN;
        if (italic.booleanValue ()) style += Font.ITALIC;
        return new Font (name, style, size.intValue ());
    }
    
    private String fontToString (AttributeSet category) {
        boolean def = false;
        StringBuffer sb = new StringBuffer ();
        if (category.getAttribute (StyleConstants.FontFamily) != null)
            sb.append ('+').append (category.getAttribute (StyleConstants.FontFamily));
        else
            def = true;
        if (category.getAttribute (StyleConstants.FontSize) != null)
            sb.append ('+').append (category.getAttribute (StyleConstants.FontSize));
        else
            def = true;
        if (Boolean.TRUE.equals (category.getAttribute (StyleConstants.Bold)))
            sb.append ('+').append (loc ("Bold"));   // NOI18N
        if (Boolean.TRUE.equals (category.getAttribute (StyleConstants.Italic)))
            sb.append ('+').append (loc ("Italic"));
        
        if (def) {
            sb.insert (0, loc ("Default"));
            return sb.toString ();
        } else {
            String result = sb.toString ();
            return result.replace ('+', ' ');
        }
    }
    
    private static Integer defaultFontSize;
    private static Integer getDefaultFontSize () {
        if (defaultFontSize == null) {
            defaultFontSize = (Integer) UIManager.get 
                ("customFontSize");                                   // NOI18N
            if (defaultFontSize == null) {
                int s = UIManager.getFont ("TextField.font").getSize (); // NOI18N
                if (s < 12) s = 12;
                defaultFontSize = new Integer (s);
            }
        }
        return defaultFontSize;
    }
    
    private static class LanguagesComparator implements Comparator {
	public int compare (Object o1, Object o2) {
	    if (o1.equals (ColorModel.ALL_LANGUAGES)) 
		return o2.equals (ColorModel.ALL_LANGUAGES) ? 0 : -1;
	    return ((String) o1).compareTo ((String) o2);
	}
    }
}
