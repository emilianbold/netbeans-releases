/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.colors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

public final class ColorModel {
    
    /* package */ static final String ALL_LANGUAGES = NbBundle.getMessage(ColorModel.class, "CTL_All_Languages"); //NOI18N
    private static final String HIGHLIGHTING_LANGUAGE = "Highlighting"; //NOI18N
    private static final String [] EMPTY_MIMEPATH = new String[0];
    
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
            String description = annotationType.getDescription ();
            if (description == null) continue;
            category.addAttribute (
                EditorStyleConstants.DisplayName,
                description
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
    
    /**
     * Returns Collection of AttributeSets or null, if the profile does 
     * not exists.
     *
     * @param profile a profile name
     * @return Collection of AttributeSets or null
     */
    public Collection /*<Category>*/ getHighlightings (String profile) {
        Map m = editorSettings.getHighlightings(profile);
        if (m == null) {
            return null;
        }
        return hideDummyCategories(m.values());
    }
    
    public Collection /*<Category>*/ getHighlightingDefaults (String profile) {
        Collection r = editorSettings.getHighlightingDefaults (profile).values ();
        if (r == null) return null;
        return hideDummyCategories (r);
    }
    
    public void setHighlightings (
	String profile, 
	Collection /*<Category>*/ highlihgtings
    ) {
	editorSettings.setHighlightings (
	    profile, 
	    toMap (highlihgtings)
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
        if (language.equals(ALL_LANGUAGES)) {
            return editorSettings.getFontColorSettings(EMPTY_MIMEPATH).getAllFontColors(profile);
        }
        
        String mimeType = getMimeType (language);
	FontColorSettingsFactory fcs = EditorSettings.getDefault ().
            getFontColorSettings (new String[] {mimeType});
        return fcs.getAllFontColors (profile);
    }
    
    public Collection /*<Category>*/ getDefaults (
	String profile, 
	String language
    ) {
        if (language.equals(ALL_LANGUAGES)) {
            return editorSettings.getFontColorSettings(EMPTY_MIMEPATH).getAllFontColorDefaults(profile);
        }
        
        String mimeType = getMimeType (language);
	FontColorSettingsFactory fcs = EditorSettings.getDefault ().
            getFontColorSettings (new String[] {mimeType});
        return fcs.getAllFontColorDefaults (profile);
    }
    
    public void setCategories (
        String profile, 
        String language, 
        Collection categories
    ) {
        if (language.equals (ALL_LANGUAGES)) {
            editorSettings.getFontColorSettings(EMPTY_MIMEPATH).setAllFontColors(profile, categories);
            return;
        }
        
        String mimeType = getMimeType (language);
        if (mimeType == null) {
            if (System.getProperty ("org.netbeans.optionsDialog") != null)
                System.out.println("ColorModelImpl.setCategories - unknown language " + language);
            return;
        }
	FontColorSettingsFactory fcs = EditorSettings.getDefault ().
            getFontColorSettings (new String[] {mimeType});
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
        
        static final String         PROP_CURRENT_ELEMENT = "currentAElement";
        private JEditorPane         editorPane;
        private FontColorSettingsFactory   fontColorSettings;
        
        
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
            setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
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
                        internalMimeType = languageToInternalMimeType(language, false);
                        fontColorSettings = EditorSettings.getDefault ().
                            getFontColorSettings (new String[] {internalMimeType});
                    }
                    
                    if (internalMimeType == null) {
                        internalMimeType = languageToInternalMimeType(language, false);
                    }
                    if (defaults != null) {
                        editorSettings.getFontColorSettings(EMPTY_MIMEPATH).setAllFontColors(
                            "test" + ColorModel.this.hashCode(),
                            defaults
                        );
                    }
                    if (highlightings != null)
                        editorSettings.setHighlightings (
                            "test" + ColorModel.this.hashCode (),
                            toMap (highlightings)
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
            String internalMimeType = languageToInternalMimeType(language, true);
            Document document = editorPane.getDocument ();
            document.putProperty ("mimeType", internalMimeType);
            editorPane.setEditorKit (CloneableEditorSupport.getEditorKit(internalMimeType));
            document = editorPane.getDocument ();
            document.putProperty ("mimeType", internalMimeType);
            editorPane.addCaretListener (new CaretListener () {
                public void caretUpdate (CaretEvent e) {
                    int position = e.getDot ();
                    EditorUI editorUI = Utilities.getEditorUI (editorPane);
                    if (editorUI == null) return;
                    SyntaxSupport ss = Utilities.getSyntaxSupport 
                        (editorUI.getComponent ());
                    if (!(ss instanceof ExtSyntaxSupport)) return;
                    try {
                        TokenItem tokenItem = ((ExtSyntaxSupport) ss).
                            getTokenChain (position, position + 1);
                        if (tokenItem == null) return;
                        String elementName = tokenItem.getTokenContextPath ().
                                getNamePrefix ();
                        if (tokenItem.getTokenID ().getCategory () != null)
                            elementName += tokenItem.getTokenID ().
                                getCategory ().getName ();
                        else
                            elementName += tokenItem.getTokenID ().getName ();
                        firePropertyChange (PROP_CURRENT_ELEMENT, null, elementName);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace ();
                    }
                }
            });
            editorPane.setEnabled (false);
            InputStream is = loadPreviewExample (language);
            if (is == null) {
                assert true :
                       "Example for " + language + " language not found.";
                is = loadPreviewExample ("Java");
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
        
        private InputStream loadPreviewExample (String language) {
            String mimeType = getMimeType (language);
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject exampleFile = fs.findResource 
                ("OptionsDialog/PreviewExamples/" + mimeType);
            try {
                return exampleFile != null ? 
                    exampleFile.getInputStream () : null;
            } catch (FileNotFoundException fnfe) {
                return null;
            }
        }

        private String languageToInternalMimeType (String language, boolean encodeTestProfileName) {
            String mimeType = (
                language == HIGHLIGHTING_LANGUAGE || 
                language == ALL_LANGUAGES
            ) ? 
                "text/x-java" : //NOI18N      Highlighting & All Languages
                getMimeType (language);
            
            if (encodeTestProfileName) {
                return "test" + ColorModel.this.hashCode () + "_" + mimeType; //NOI18N
            } else {
                return mimeType;
            }
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
    
    private static Map toMap (Collection categories) {
        if (categories == null) return null;
        Map result = new HashMap ();
        Iterator it = categories.iterator ();
        while (it.hasNext ()) {
            AttributeSet as = (AttributeSet) it.next ();
            result.put (
                as.getAttribute (StyleConstants.NameAttribute),
                as
            );
        }
        return result;
    }
}
