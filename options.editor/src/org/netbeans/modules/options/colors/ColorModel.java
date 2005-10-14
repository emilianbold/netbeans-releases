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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;

import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettings;
import org.netbeans.modules.options.OptionsPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author Administrator
 */
public class ColorModel {
    
    static final String         ALL_LANGUAGES = "All Languages";
    static final String         HIGHLIGHTING_LANGUAGE = "Highlighting";
    
    private EditorSettings      editorSettings = EditorSettings.getDefault ();
    
    
    
    // schemes .................................................................
    
    public Set /*<String>*/ getProfiles () {
        return editorSettings.getFontColorProfiles ();
    }
    
    public String getCurrentProfile () {
        return editorSettings.getCurrentFontColorProfile ();
    }
    
    public boolean isCustomProfile (String profile) {
        return editorSettings.isCustomFontColorProfile (profile);
    }
    
    public void setCurrentProfile (String profile) {
        editorSettings.setCurrentFontColorProfile (profile);
    }
    
    
    // annotations .............................................................
    
    public Collection /*<Category>*/ getAnnotations (String profile) {
        Iterator it = AnnotationTypes.getTypes ().getAnnotationTypeNames ();
        Collection annotations = new ArrayList ();
        while (it.hasNext ()) {
            String name = (String) it.next ();
            AnnotationType annotationType = AnnotationTypes.getTypes ().
                getType (name);
            if (!annotationType.isVisible ()) continue;

            URL iconURL = annotationType.getGlyph ();
            Image image = null;
            if (iconURL.getProtocol ().equals ("nbresloc")) { // NOI18N
                image = org.openide.util.Utilities.loadImage 
                    (iconURL.getPath ().substring (1));
            } else
                image = Toolkit.getDefaultToolkit ().getImage (iconURL);

            SimpleAttributeSet category = new SimpleAttributeSet ();
            category.addAttribute (
                EditorStyleConstants.DisplayName,
                annotationType.getDescription ()
            );
            category.addAttribute (
                StyleConstants.NameAttribute,
                annotationType.getDescription ()
            );
            if (image != null)
                category.addAttribute (
                    "icon",
                    new ImageIcon (image)
                );
            if (annotationType.isUseHighlightColor ())
                category.addAttribute (
                    StyleConstants.Background,
                    annotationType.getHighlight ()
                );
            if (!annotationType.isInheritForegroundColor ())
                category.addAttribute (
                    StyleConstants.Foreground,
                    annotationType.getForegroundColor ()
                );
            if (annotationType.isUseWaveUnderlineColor ())
                category.addAttribute (
                    EditorStyleConstants.WaveUnderlineColor,
                    annotationType.getWaveUnderlineColor ()
                );
            category.addAttribute (
                "annotationType",
                annotationType
            );
            annotations.add (category);
	}
	return annotations;
    }
    
    public void setAnnotations (
	String profile, 
	Collection /*<Category>*/ annotations
    ) {
	Iterator it = annotations.iterator ();
	//S ystem.out.println("ColorModelImpl.setAnnotations ");
	while (it.hasNext ()) {
	    AttributeSet category = (AttributeSet) it.next ();
	    AnnotationType annotationType = (AnnotationType) 
		category.getAttribute ("annotationType");
            
	    if (category.isDefined (StyleConstants.Background)) {
		annotationType.setUseHighlightColor (true);
		annotationType.setHighlight (
                    (Color) category.getAttribute (StyleConstants.Background)
                );
            } else
		annotationType.setUseHighlightColor (false);
	    if (category.isDefined (StyleConstants.Foreground)) {
		annotationType.setInheritForegroundColor (false);
		annotationType.setForegroundColor (
                    (Color) category.getAttribute (StyleConstants.Foreground)
                );
            } else
		annotationType.setInheritForegroundColor (true);
	    if (category.isDefined (EditorStyleConstants.WaveUnderlineColor)) {
                annotationType.setUseWaveUnderlineColor (true);
                annotationType.setWaveUnderlineColor (
                    (Color) category.getAttribute (EditorStyleConstants.WaveUnderlineColor)
                );
            } else
                annotationType.setUseWaveUnderlineColor (false);
	    //S ystem.out.println("  " + category.getDisplayName () + " : " + annotationType + " : " + annotationType.getHighlight() + " : " + annotationType.isUseHighlightColor());
	}
    }
    
    
    // editor categories .......................................................
    
    public Collection /*<Category>*/ getHighlightings (String profile) {
        Collection r = editorSettings.getHighlightings (profile);
        if (r == null) return null;
        return hideDummyCategories (r);
    }
    
    public Collection /*<Category>*/ getHighlightingDefaults (String profile) {
        Collection r = editorSettings.getHighlightingDefaults (profile);
        if (r == null) return null;
        return hideDummyCategories (r);
    }
    
    public void setHighlightings (
	String profile, 
	Collection /*<Category>*/ highlihgtings
    ) {
	editorSettings.setHighlightings (
	    profile, 
	    highlihgtings
	);
    }

    
    // syntax coloring .........................................................
    
    public Set /*<String>*/ getLanguages () {
	return getLanguageToMimeTypeMap ().keySet ();
    }
    
    public Collection /*<Category>*/ getCategories (
	String profile, 
	String language
    ) {
        if (language.equals (ALL_LANGUAGES))
            return editorSettings.getDefaultFontColors (profile);

        String mimeType = getMimeType (language);
	FontColorSettings fcs = (FontColorSettings) MimeLookup.
	    getMimeLookup (mimeType).lookup (FontColorSettings.class);
        return fcs.getAllFontColors (profile);
    }
    
    public Collection /*<Category>*/ getDefaults (
	String profile, 
	String language
    ) {
        if (language.equals (ALL_LANGUAGES))
            return editorSettings.getDefaultFontColorDefaults (profile);

        String mimeType = getMimeType (language);
	FontColorSettings fcs = (FontColorSettings) MimeLookup.
	    getMimeLookup (mimeType).lookup (FontColorSettings.class);
        return fcs.getAllFontColorDefaults (profile);
    }
    
    public void setCategories (
        String profile, 
        String language, 
        Collection categories
    ) {
        if (language.equals (ALL_LANGUAGES)) {
            editorSettings.setDefaultFontColors (
                profile,
                categories
            );
            return;
        }
        
        String mimeType = getMimeType (language);
        if (mimeType == null) {
            if (System.getProperty ("org.netbeans.optionsDialog") != null)
                System.out.println("ColorModelImpl.setCategories - unknown language " + language);
            return;
        }
	FontColorSettings fcs = (FontColorSettings) MimeLookup.
	    getMimeLookup (mimeType).lookup (FontColorSettings.class);
	fcs.setAllFontColors (
            profile,
	    categories
	);
    }
	
    public Component getEditorPreviewComponent () {
        return new Preview (HIGHLIGHTING_LANGUAGE);
    }
	
    public Component getSyntaxColoringPreviewComponent (
        String      language
    ) {
        return new Preview (language);
    }

    class Preview extends JPanel {
        
        private JEditorPane         editorPane;
        private FontColorSettings   fontColorSettings;
        
        
        Preview (
            final String      language
        ) {
            super (new BorderLayout ());
//            S ystem.out.println ("getPreviewComponent " + profile + " : " + language);
//            T hread.dumpStack ();
            
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    editorPane = new JEditorPane ();
                    updateMimeType (language);
                    if (language == HIGHLIGHTING_LANGUAGE) {
                        EditorUI editorUI = Utilities.getEditorUI (editorPane);
                        if (editorUI != null) {
                            editorUI.setLineNumberEnabled (true);
                            editorUI.getExtComponent ();
                            add (editorUI.getExtComponent (), BorderLayout.CENTER);
                            return;
                        }
//                        S ystem.out.println("no text ui " + editorPane);
                    }
                    add (editorPane, BorderLayout.CENTER);
                }
            });
        }
        
        private String currentLanguage;
        
        public void setParameters (
            final String      language,
            final Collection /*<Category>*/ defaults,
            final Collection /*<Category>*/ highlightings,
            final Collection /*<Category>*/ syntaxColorings
        ) {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    String internalMimeType = null;
                    if (!language.equals (currentLanguage)) {
                        updateMimeType (language);
                        currentLanguage = language;
                        internalMimeType = languageToInternalMimeType 
                            (language);
                        fontColorSettings = (FontColorSettings) 
                            getMimeLookup (internalMimeType).
                            lookup (FontColorSettings.class);
                    }
                    
                    if (internalMimeType == null)
                        internalMimeType = languageToInternalMimeType 
                            (language);
                    if (defaults != null)
                        editorSettings.setDefaultFontColors (
                            "test" + ColorModel.this.hashCode (),
                            defaults
                        );
                    if (highlightings != null)
                        editorSettings.setHighlightings (
                            "test" + ColorModel.this.hashCode (),
                            highlightings
                        );
                    if (syntaxColorings != null)
                        fontColorSettings.setAllFontColors (
                            "test" + ColorModel.this.hashCode (),
                            syntaxColorings
                        );
                }
            });
        }
        
        /**
         * Sets given mime type to preview and loads proper example text.
         */
        private void updateMimeType (String language) {
            String internalMimeType = languageToInternalMimeType (language);
            getMimeLookup (internalMimeType);
            Document document = editorPane.getDocument ();
            document.putProperty ("mimeType", internalMimeType);
            editorPane.setContentType (internalMimeType);
            document = editorPane.getDocument ();
            document.putProperty ("mimeType", internalMimeType);
            editorPane.setEditable (false);            
            String exampleName = language == ALL_LANGUAGES ?
                "JavaExample" :
                language + "Example";
            InputStream is = getClass ().getResourceAsStream (
                "/org/netbeans/modules/options/colors/" + exampleName
            );
            if (is == null) {
                assert true :
                       "Example for " + language + " language not found.";
                is = getClass ().getResourceAsStream 
                    ("/org/netbeans/modules/options/colors/JavaExample");
            }
            BufferedReader r = new BufferedReader (new InputStreamReader (is));
            StringBuffer sb = new StringBuffer ();
            try {
                String line = r.readLine ();
                while (line != null) {
                    sb.append (line).append ('\n');
                    line = r.readLine ();
                }
                editorPane.setText (new String (sb));
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
        
        private String languageToInternalMimeType (String language) {
            String mimeType = (
                language == HIGHLIGHTING_LANGUAGE || 
                language == ALL_LANGUAGES
            ) ? 
                "text/x-java" : // Highlighting & All Languages
                getMimeType (language);
            return "test" + ColorModel.this.hashCode () + "_" + mimeType;
        }
        
        private Map lookups = new HashMap ();
        private MimeLookup getMimeLookup (String mimeType) {
            if (!lookups.containsKey (mimeType))
                lookups.put (
                    mimeType,
                    MimeLookup.getMimeLookup (mimeType)
                );
            return (MimeLookup) lookups.get (mimeType);
        }
    }
    
    
    // private implementation ..................................................

    private String getMimeType (String language) {
        return (String) getLanguageToMimeTypeMap ().get (language);
    }
    
    private Map languageToMimeType;
    private Map getLanguageToMimeTypeMap () {
	if (languageToMimeType == null) {
	    languageToMimeType = new HashMap ();
	    Set mimeTypes = editorSettings.getMimeTypes ();
	    Iterator it = mimeTypes.iterator ();
	    while (it.hasNext ()) {
		String mimeType = (String) it.next ();
		languageToMimeType.put (
		    editorSettings.getLanguageName (mimeType),
		    mimeType
		);
	    }
            languageToMimeType.put (
                ALL_LANGUAGES, 
                "Defaults"
            );
	}
	return languageToMimeType;
    }
    
    private Set hiddenCategories = new HashSet ();
    {
//        hiddenCategories.add ("status-bar");
//        hiddenCategories.add ("status-bar-bold");
    }
    
    private Collection hideDummyCategories (
        Collection /*AttributeSet*/ categories
    ) {
        List result = new ArrayList ();
        Iterator it = categories.iterator ();
        while (it.hasNext ()) {
            AttributeSet as = (AttributeSet) it.next ();
            if (hiddenCategories.contains (
                as.getAttribute (StyleConstants.NameAttribute)
            )) continue;
            result.add (as);
        }
        return result;
    }
}
