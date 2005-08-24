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
    
    public abstract Set /*<String>*/ getSchemeNames ();
    
    public abstract String getCurrentScheme ();
    
    public abstract void setCurrentScheme (String s);
    
    
    // annotations .............................................................
    
    public abstract Collection /*<Category>*/ getAnnotations (String scheme);
    
    public abstract void setAnnotations (
	String scheme, 
	Collection /*<Category>*/ annotations
    );
    
    
    // editor categories .......................................................
    
    public abstract Collection /*<Category>*/ getEditorCategories (String scheme);
    
    public abstract void setEditorCategories (
	String scheme, 
	Collection /*<Category>*/ editorCategories
    );

    
    // syntax coloring .........................................................
    
    public abstract Set /*<String>*/ getLanguages ();
    
    public abstract Collection /*<Category>*/ getCategories (String scheme, String language);
    
//    public abstract Category getCategory (String scheme, String language, String name);
    
    public abstract void setCategories (
        String scheme,
        String language, 
        Collection /*<Category>*/ categories
    );
    
    public abstract Component getPreviewComponent (
        String      scheme,
        String      language,
        boolean     plain
    );
       
    
    // innerclasses ............................................................
    
    public static interface Preview {
        
        public void setParameters (
            String      scheme,
            String      language,
            Collection /*<Category>*/ categories
        );
    } 
    
    public static class Category {
	private String	name;
	private String	displayName;
	private Icon	icon;
	private Font	font;
	private Color	background;
	private Color	foreground;
	private Color	underline;
	private Color	strikeThrough;
	private Color	waveUnderline;
	private String  defaultCategory;

	
	public Category (
	    String	name,
	    String	displayName,
	    Icon	icon,
	    Font	font,
	    Color	background,
	    Color	foreground,
	    Color	underline,
	    Color	strikeThrough,
	    Color	waveUnderline,
	    String      defaultCategory
	) {
	    this.name = name;
            if (name == null) throw new NullPointerException ();
	    this.displayName = displayName;
            if (displayName == null) throw new NullPointerException ();
	    this.icon = icon;
	    this.font = font;
	    this.background = background;
	    this.foreground = foreground;
	    this.underline = underline;
	    this.strikeThrough = strikeThrough;
	    this.waveUnderline = waveUnderline;
	    this.defaultCategory = defaultCategory;
	}

	public String getName () {
	    return name;
	}

	public String getDisplayName () {
	    return displayName;
	}

	public Icon getIcon () {
	    return icon;
	}

	public Font getFont () {
	    return font;
	}

	public Color getForeground () {
	    return foreground;
	}

	public Color getBackground () {
	    return background;
	}

	public Color getUnderlineColor () {
	    return underline;
	}

	public Color getStrikeThroughColor () {
	    return strikeThrough;
	}

	public Color getWaveUnderlineColor () {
	    return waveUnderline;
	}
	
	public String getDefaultCategoryName () {
	    return defaultCategory;
	}
    }
}
