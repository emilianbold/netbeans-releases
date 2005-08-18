/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.settings.storage.api;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Set;


/**
 * 
 * @author Jan Jancura
 */
public abstract class EditorSettings {
    
    
    public abstract Set /*<String>*/ getMimeTypes ();
    
    public abstract String getLanguageName (String mimeType);

    
    // FontColors ..............................................................
    
    public static final String PROP_CURRENT_FONT_COLOR_SCHEME = "currentFontColorScheme";
    public static final String PROP_DEFAULT_FONT_COLORS = "defaultFontColors";
    public static final String PROP_EDITOR_FONT_COLORS = "editorFontColors";

    
    public abstract Set /*<String>*/ getFontColorSchemes ();
    
    public abstract String getCurrentFontColorScheme ();
    
    public abstract void setCurrentFontColorScheme (String scheme);
    
    public abstract Collection /*<AttributeSet>*/ getDefaultFontColors (
	String scheme
    );
    
    public abstract void setDefaultFontColors (
	String scheme,
	Collection /*<AttributeSet>*/ fontColors
    );
    
    public abstract Collection /*<AttributeSet>*/ getEditorFontColors (
	String scheme
    );
    
    public abstract void setEditorFontColors (
	String scheme,
	Collection /*<AttributeSet>*/ fontColors
    );
    
    
    // KeyMaps .................................................................
    
    public static final String PROP_CURRENT_KEY_MAP_NAME = "currentKeyMapName";
    
    public abstract Set /*<String>*/ getKeyMapNames ();
    
    public abstract String getCurrentKeyMapName ();
    
    public abstract void setCurrentKeyMapName (String keyMapName);

    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be registerred
     */
    public abstract void addPropertyChangeListener (
        PropertyChangeListener l
    );
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be unregisterred
     */
    public abstract void removePropertyChangeListener (
        PropertyChangeListener l
    );
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be registerred
     */
    public abstract void addPropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    );
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be unregisterred
     */
    public abstract void removePropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    );
}
