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

package org.netbeans.modules.editor.settings.storage.api;

import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.swing.text.AttributeSet;
import org.netbeans.modules.editor.settings.storage.EditorSettingsImpl;


/**
 * This singleton class contains access methods for editor settings like
 * font & colors profiles and keymaps.
 *
 * @author Jan Jancura
 */
public abstract class EditorSettings {


    private static WeakReference<EditorSettings> editorSettings;

    /**
     * Returns default instance of EditorSettings.
     *
     * @return default instance of EditorSettings
     */
    public static synchronized EditorSettings getDefault () {
        EditorSettings es = editorSettings == null ? null : editorSettings.get();
        if (es == null) {
            es = new EditorSettingsImpl();
            editorSettings = new WeakReference<EditorSettings>(es);
        }
        return es;
    }
    
    /**
     * Returns set of mimetypes.
     *
     * @return set of mimetypes
     */
    public abstract Set<String> getMimeTypes ();
    
    /**
     * Returns name of language for given mime type.
     *
     * @return name of language for given mime type
     */
    public abstract String getLanguageName (String mimeType);

    
    // FontColors ..............................................................
    
    /** Property name constant. */
    public static final String PROP_CURRENT_FONT_COLOR_PROFILE = "currentFontColorProfile";
    
    /** 
     * The name of the property change event for 'All Languages' font and colors.
     * 
     * @deprecated This has always been meant for internal use only. As per
     * general contract clients should listen on mime type specific Lookup for
     * changes in editor settings.
     */
    public static final String PROP_DEFAULT_FONT_COLORS = EditorSettingsImpl.PROP_DEFAULT_FONT_COLORS;
    
    /** 
     * The name of the property change event for 'Highlighting' font and colors.
     * 
     * @deprecated This has always been meant for internal use only. As per
     * general contract clients should listen on mime type specific Lookup for
     * changes in editor settings.
     */
    public static final String PROP_EDITOR_FONT_COLORS = EditorSettingsImpl.PROP_EDITOR_FONT_COLORS;


    /**
     * Returns set of font & colors profiles.
     *
     * @return set of font & colors profiles
     */
    public abstract Set<String> getFontColorProfiles ();
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    public abstract boolean isCustomFontColorProfile (String profile);
    
    /**
     * Returns name of current font & colors profile.
     *
     * @return name of current font & colors profile
     */
    public abstract String getCurrentFontColorProfile ();
    
    /**
     * Sets current font & colors profile.
     *
     * @param profile a profile name
     */
    public abstract void setCurrentFontColorProfile (String profile);
    
    /**
     * Returns font & color defaults for given profile or null, if the profile
     * is unknown .
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     * 
     * @deprecated Use getFontColorSettings(new String[0]).getAllFontColors(profile) instead.
     */
    public abstract Collection<AttributeSet> getDefaultFontColors (
	String profile
    );
    
    /**
     * Returns default values for font & color defaults for given profile 
     * or null, if the profile is unknown.
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     * 
     * @deprecated Use getFontColorSettings(new String[0]).getAllFontColorsDefaults(profile) instead.
     */
    public abstract Collection<AttributeSet> getDefaultFontColorDefaults (
	String profile
    );
    
    /**
     * Sets font & color defaults for given profile.
     *
     * @param profile a profile name
     * @param fontColors font & color defaults to be used
     * 
     * @deprecated Use getFontColorSettings(new String[0]).setAllFontColors(profile, fontColors) instead.
     */
    public abstract void setDefaultFontColors (
	String profile,
	Collection<AttributeSet> fontColors
    );
    
    /**
     * Returns highlighting properties for given profile or null, if the 
     * profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    public abstract Map<String, AttributeSet> getHighlightings (
	String profile
    );
    
    /**
     * Returns defaults for highlighting properties for given profile,
     * or null if the profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    public abstract Map<String, AttributeSet> getHighlightingDefaults (
	String profile
    );
    
    /**
     * Sets highlighting properties for given profile.
     *
     * @param profile a profile name
     * @param highlightings a highlighting properties to be used
     */
    public abstract void setHighlightings (
	String profile,
	Map<String, AttributeSet> highlightings
    );
    
    /**
     * Returns FontColorSettings for given mimetypes.
     *
     * @return FontColorSettings for given mimetypes
     */
    public abstract FontColorSettingsFactory getFontColorSettings (String[] mimeTypes);
    
    
    // KeyMaps .................................................................
    
    /** Property name constant. */
    public static final String PROP_CURRENT_KEY_MAP_PROFILE = "currentKeyMapProfile";

    
    /**
     * Returns KeyBindingSettings for given mimetypes.
     *
     * @return KeyBindingSettings for given mimetypes
     */
    public abstract KeyBindingSettingsFactory getKeyBindingSettings (String[] mimeTypes);
    
    /**
     * Returns set of keymap profiles.
     *
     * @return set of font & colors profiles
     */
    public abstract Set<String> getKeyMapProfiles ();
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    public abstract boolean isCustomKeymapProfile (String profile);
    
    /**
     * Returns name of current keymap profile.
     *
     * @return name of current keymap profile
     */
    public abstract String getCurrentKeyMapProfile ();
    
    /**
     * Sets current keymap profile.
     *
     * @param profile a profile name
     */
    public abstract void setCurrentKeyMapProfile (String profile);

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
