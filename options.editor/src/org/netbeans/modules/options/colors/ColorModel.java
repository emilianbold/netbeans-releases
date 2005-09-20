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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.editor.Coloring;

import org.netbeans.modules.options.editorimpl.ColorModelImpl;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
public abstract class ColorModel {
    
    public static final String ALL_LANGUAGES = 
        NbBundle.getMessage (ColorModel.class, "CTL_All_Languages");
    
    private static ColorModel model;
    
    public static ColorModel getDefault () {
        if (model == null)
            model = new ColorModelImpl ();
        return model;
    } 
    
    
    // schemes .................................................................
    
    public abstract Set /*<String>*/ getProfiles ();
    
    public abstract String getCurrentProfile ();
    
    public abstract boolean isCustomProfile (String profile);
    
    public abstract void setCurrentProfile (String profile);
    
    
    // annotations .............................................................
    
    public abstract Collection /*<Category>*/ getAnnotations (String scheme);
    
    public abstract void setAnnotations (
	String profile, 
	Collection /*<Category>*/ annotations
    );
    
    
    // editor categories .......................................................
    
    public abstract Collection /*<Category>*/ getHighlightings (String profile);
    
    public abstract Collection /*<Category>*/ getHighlightingDefaults 
        (String profile);
    
    public abstract void setHighlightings (
	String profile, 
	Collection /*<Category>*/ highlihgtings
    );

    
    // syntax coloring .........................................................
    
    public abstract Set /*<String>*/ getLanguages ();
    
    public abstract Collection /*<Category>*/ getCategories (String profile, String language);

    public abstract Collection /*<Category>*/ getDefaults (
	String profile, 
	String language
    );

    public abstract void setCategories (
        String profile,
        String language, 
        Collection /*<Category>*/ categories
    );
    
    public abstract Component getEditorPreviewComponent ();
	
    public abstract Component getSyntaxColoringPreviewComponent (
        String      language
    );
    
    
    // innerclasses ............................................................
    
    public static interface Preview {
        
        public void setParameters (
            String      language,
            final Collection /*<Category>*/ defaults,
            Collection /*<Category>*/ highlightings,
            Collection /*<Category>*/ syntaxColorings
        );
    } 
}
