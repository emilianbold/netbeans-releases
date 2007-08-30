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
import java.io.IOException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
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

    private static final Logger LOG = Logger.getLogger(ColorModel.class.getName());
    
    public static final String ALL_LANGUAGES = NbBundle.getMessage(ColorModel.class, "CTL_All_Languages"); //NOI18N
    private static final String [] EMPTY_MIMEPATH = new String[0];
    
    // schemes .................................................................
    
    public Set<String> getProfiles() {
        return EditorSettings.getDefault().getFontColorProfiles();
    }
    
    public String getCurrentProfile () {
        return EditorSettings.getDefault().getCurrentFontColorProfile ();
    }
    
    public boolean isCustomProfile (String profile) {
        return EditorSettings.getDefault().isCustomFontColorProfile (profile);
    }
    
    public void setCurrentProfile (String profile) {
        EditorSettings.getDefault().setCurrentFontColorProfile (profile);
    }
    
    
    // annotations .............................................................
    
    public Collection<AttributeSet> getAnnotations(String profile) {
        List<AttributeSet> annotations = new ArrayList<AttributeSet>();
        for(Iterator it = AnnotationTypes.getTypes().getAnnotationTypeNames(); it.hasNext(); ) {
            String name = (String) it.next ();
            
            AnnotationType annotationType = AnnotationTypes.getTypes().getType(name);
            if (!annotationType.isVisible()) {
                continue;
            }

            String description = annotationType.getDescription();
            if (description == null) {
                continue;
            }

            SimpleAttributeSet category = new SimpleAttributeSet();
            category.addAttribute(EditorStyleConstants.DisplayName, description);
            category.addAttribute(StyleConstants.NameAttribute, description);
            
            URL iconURL = annotationType.getGlyph ();
            Image image = null;
            if (iconURL.getProtocol ().equals ("nbresloc")) { // NOI18N
                image = org.openide.util.Utilities.loadImage(iconURL.getPath().substring(1));
            } else {
                image = Toolkit.getDefaultToolkit ().getImage (iconURL);
            }
            if (image != null) {
                category.addAttribute("icon", new ImageIcon(image)); //NOI18N
            }
            
            Color bgColor = annotationType.getHighlight();
            if (annotationType.isUseHighlightColor() && bgColor != null) {
                category.addAttribute(StyleConstants.Background, bgColor);
            }
            
            Color fgColor = annotationType.getForegroundColor();
            if (!annotationType.isInheritForegroundColor() && fgColor != null) {
                category.addAttribute(StyleConstants.Foreground, fgColor);
            }

            Color underColor = annotationType.getWaveUnderlineColor();
            if (annotationType.isUseWaveUnderlineColor() && underColor != null) {
                category.addAttribute(EditorStyleConstants.WaveUnderlineColor, underColor);
            }
            
            category.addAttribute("annotationType", annotationType); //NOI18N
            annotations.add(category);
	}
        
	return annotations;
    }
    
    public void setAnnotations (
	String profile, 
	Collection<AttributeSet> annotations
    ) {
	//S ystem.out.println("ColorModelImpl.setAnnotations ");
	for(AttributeSet category : annotations) {
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
    public Collection<AttributeSet> getHighlightings (String profile) {
        Map<String, AttributeSet> m = EditorSettings.getDefault().getHighlightings(profile);
        if (m == null) {
            return null;
        }
        return hideDummyCategories(m.values());
    }
    
    public Collection<AttributeSet> getHighlightingDefaults (String profile) {
        Collection<AttributeSet> r = EditorSettings.getDefault().getHighlightingDefaults(profile).values();
        if (r == null) return null;
        return hideDummyCategories (r);
    }
    
    public void setHighlightings(String profile, Collection<AttributeSet> highlihgtings) {
        EditorSettings.getDefault().setHighlightings(profile, toMap(highlihgtings));
    }
    
    // syntax coloring .........................................................
    
    public Set<String> getLanguages() {
        return getLanguageToMimeTypeMap().keySet();
    }
    
    public Collection<AttributeSet> getCategories (
	String profile, 
	String language
    ) {
        String [] mimePath = getMimePath(language);
        FontColorSettingsFactory fcs = EditorSettings.getDefault().getFontColorSettings(mimePath);
        return fcs.getAllFontColors(profile);
    }
    
    public Collection<AttributeSet> getDefaults (
	String profile, 
	String language
    ) {
        String [] mimePath = getMimePath(language);
        FontColorSettingsFactory fcs = EditorSettings.getDefault().getFontColorSettings(mimePath);
        return fcs.getAllFontColorDefaults(profile);
    }
    
    public void setCategories (
        String profile, 
        String language, 
        Collection<AttributeSet> categories
    ) {
        String [] mimePath = getMimePath(language);
        FontColorSettingsFactory fcs = EditorSettings.getDefault().getFontColorSettings(mimePath);
        fcs.setAllFontColors(profile, categories);
    }
	
    public Component getSyntaxColoringPreviewComponent(String language) {
        String mimeType = getMimeType(language);
        return new Preview("test" + hashCode(), mimeType); //NOI18N
    }

    final class Preview extends JPanel {
        
        static final String         PROP_CURRENT_ELEMENT = "currentAElement";
        
        private String testProfileName;
        private String currentMimeType;
        private JEditorPane editorPane;
        private FontColorSettingsFactory fontColorSettings;
        
        
        public Preview (String testProfileName, final String mimeType) {
            super (new BorderLayout ());
            this.testProfileName = testProfileName;
            
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    updateMimeType(mimeType);
                }
            });
            setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        }
        
        public void setParameters(
            String      language,
            final Collection<AttributeSet> defaults,
            final Collection<AttributeSet> highlightings,
            final Collection<AttributeSet> syntaxColorings
        ) {
            final String mimeType = getMimeType(language);
            
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    if (!mimeType.equals(currentMimeType)) {
                        updateMimeType(mimeType);
                    }
                    
                    if (defaults != null) {
                        EditorSettings.getDefault().getFontColorSettings(EMPTY_MIMEPATH).setAllFontColors(
                            testProfileName,
                            defaults
                        );
                    }
                    if (highlightings != null) {
                        EditorSettings.getDefault().setHighlightings (
                            testProfileName,
                            toMap (highlightings)
                        );
                    }
                    if (syntaxColorings != null && currentMimeType.length() != 0) {
                        FontColorSettingsFactory fcs = EditorSettings.getDefault().getFontColorSettings(new String[] { currentMimeType });
                        fcs.setAllFontColors (
                            testProfileName,
                            syntaxColorings
                        );
                    }
                }
            });
        }
        
        /**
         * Sets given mime type to preview and loads proper example text.
         */
        private void updateMimeType(String mimeType) {
            currentMimeType = mimeType;
            
            String [] ret = loadPreviewExample(mimeType);
            String exampleText = ret[0];
            String exampleMimeType = ret[1];
            
            // XXX: There is several hacks in the few lines of code below.
            // First, the 'mimeType' property on a Document is abused for
            // injecting the name of a profile used for previewing changes in
            // colors. This by itself causes several problems in other parts of
            // the IDE that had to be worked around. Second, Document properties
            // are normally not supposed to be changed during a lifetime of a Document
            // and there is no way how to listen for those changes. Which means
            // that we have to fire a property change on the JTextComponent containing
            // the Document, so that the layers can get recalculated.

            String hackMimeType = hackMimeType(exampleMimeType);

            // Replace the whole component, see #113608
            removeAll();
            editorPane = new JEditorPane();
            add(editorPane, BorderLayout.CENTER);
            
            Document document = editorPane.getDocument ();
            document.putProperty ("mimeType", hackMimeType);
            editorPane.setEditorKit (CloneableEditorSupport.getEditorKit(hackMimeType));
            document = editorPane.getDocument ();
            document.putProperty ("mimeType", hackMimeType);
            editorPane.firePropertyChange(null, 0, 1);
            
            editorPane.addCaretListener (new CaretListener () {
                public void caretUpdate (CaretEvent e) {
                    int offset = e.getDot ();
                    String elementName = null;
                    
                    TokenHierarchy<Document> th = TokenHierarchy.get(editorPane.getDocument());
                    if (th != null) {
                        elementName = findLexerElement(th, offset);
                    } else {
                        SyntaxSupport ss = Utilities.getSyntaxSupport(editorPane);
                        if (ss instanceof ExtSyntaxSupport) {
                            elementName = findSyntaxElement((ExtSyntaxSupport) ss, offset);
                        }
                    }

                    if (elementName != null) {
                        firePropertyChange(PROP_CURRENT_ELEMENT, null, elementName);
                    }
                }
                
                private String findLexerElement(TokenHierarchy<Document> hierarchy, int offset) {
                    String elementName = null;
                    List<TokenSequence<? extends TokenId>> sequences = hierarchy.embeddedTokenSequences(offset, false);
                    TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);
                    seq.move(offset);
                    if (seq.moveNext()) {
                        elementName = seq.token().id().primaryCategory();
                        if (elementName == null) {
                            elementName = seq.token().id().name();
                        }
                    }
                    return elementName;
                }

                private String findSyntaxElement(ExtSyntaxSupport syntax, int offset) {
                    try {
                        TokenItem tokenItem = syntax.getTokenChain(offset, offset + 1);
                        if (tokenItem == null) {
                            return null;
                        }
                        String elementName = tokenItem.getTokenContextPath().getNamePrefix();
                        if (tokenItem.getTokenID().getCategory() != null) {
                            elementName += tokenItem.getTokenID().getCategory().getName();
                        } else {
                            elementName += tokenItem.getTokenID().getName();
                        }
                        return elementName;
                    } catch (BadLocationException ble) {
                        LOG.log(Level.WARNING, null, ble);
                        return null;
                    }
                }
            });
            
            editorPane.setEnabled(false);
            editorPane.setText(exampleText);
        }
        
        private String [] loadPreviewExample(String mimeType) {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject exampleFile = null;
            String exampleMimeType = null;
            
            if (mimeType == null || mimeType.length() == 0) {
                FileObject f = fs.findResource("OptionsDialog/PreviewExamples"); //NOI18N
                if (f != null && f.isFolder()) {
                    FileObject [] ff = f.getChildren();
                    for(int i = 0 ; i < ff.length; i++) {
                        if (ff[i].isData()) {
                            exampleFile = ff[i];
                            break;
                        }
                    }
                }
                if (exampleFile != null) {
                    if (exampleFile.getMIMEType().equals("content/unknown")) { //NOI18N
                        exampleMimeType = "text/x-java"; //NOI18N
                    } else {
                        exampleMimeType = exampleFile.getMIMEType();
                    }
                }
            } else {
                exampleFile = fs.findResource("OptionsDialog/PreviewExamples/" + mimeType); //NOI18N
                exampleMimeType = mimeType;
            }
            
            if (exampleFile != null) {
                StringBuilder sb = new StringBuilder((int)exampleFile.getSize());
                
                try {
                    InputStreamReader is = new InputStreamReader(exampleFile.getInputStream());
                    char [] buffer = new char[1024];
                    int size;
                    try {
                        while(0 < (size = is.read(buffer, 0, buffer.length))) {
                            sb.append(buffer, 0, size);
                        }
                    } finally {
                        is.close();
                    }
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, "Can't read font & colors preview example", ioe); //NOI18N
                }
                
                return new String [] { sb.toString(), exampleMimeType };
            } else {
                return new String [] { "", "text/plain" }; //NOI18N
            }            
        }

        private String hackMimeType(String mimeType) {
            return testProfileName + "_" + mimeType; //NOI18N
        }
    }
    
    
    // private implementation ..................................................

    private String getMimeType(String language) {
        if (language.equals(ALL_LANGUAGES)) {
            return ""; //NOI18N
        } else {
            String mimeType = getLanguageToMimeTypeMap().get(language);
            assert mimeType != null : "Invalid language '" + language + "'"; //NOI18N
            return mimeType;
        }
    }
    
    private String [] getMimePath(String language) {
        if (language.equals(ALL_LANGUAGES)) {
            return EMPTY_MIMEPATH;
        } else {
            String mimeType = getLanguageToMimeTypeMap().get(language);
            assert mimeType != null : "Invalid language '" + language + "'"; //NOI18N
            return new String [] { mimeType };
        }
    }
    
    private Map<String, String> languageToMimeType;
    private Map<String, String> getLanguageToMimeTypeMap() {
        if (languageToMimeType == null) {
            languageToMimeType = new HashMap<String, String>();
            Set<String> mimeTypes = EditorSettings.getDefault().getMimeTypes();
            for(String mimeType : mimeTypes) {
                languageToMimeType.put(
                    EditorSettings.getDefault().getLanguageName(mimeType),
                    mimeType
                );
            }
            languageToMimeType.put(
                    ALL_LANGUAGES,
                    "Defaults" //NOI18N
                    );
        }
        return languageToMimeType;
    }
    
    private Set<AttributeSet> hiddenCategories = new HashSet<AttributeSet>();
    {
//        hiddenCategories.add ("status-bar");
//        hiddenCategories.add ("status-bar-bold");
    }
    
    private Collection<AttributeSet> hideDummyCategories(Collection<AttributeSet> categories) {
        List<AttributeSet> result = new ArrayList<AttributeSet>();
        for(AttributeSet as : categories) {
            if (hiddenCategories.contains(as.getAttribute(StyleConstants.NameAttribute))) {
                continue;
            }
            result.add(as);
        }
        return result;
    }
    
    private static Map<String, AttributeSet> toMap(Collection<AttributeSet> categories) {
        if (categories == null) return null;
        Map<String, AttributeSet> result = new HashMap<String, AttributeSet>();
        for(AttributeSet as : categories) {
            result.put((String) as.getAttribute(StyleConstants.NameAttribute), as);
        }
        return result;
    }
}
