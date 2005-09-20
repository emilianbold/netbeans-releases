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

package org.netbeans.modules.editor.settings.storage;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
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
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;


/**
 * This class contains access methods for editor settings like font & colors
 * profiles and keymap profiles. 
 *
 * @author Jan Jancura
 */
public class EditorSettingsImpl extends EditorSettings {

    private PropertyChangeSupport   pcs;
    
    {pcs = new PropertyChangeSupport (this);}
    
    static final String HIGHLIGHTING_FILE_NAME = "editorColoring.xml"; // NOI18N
    static final String KEYBINDING_FILE_NAME = "keybindings.xml";      // NOI18N
    static final String ALL_LANGUAGES_FILE_NAME = "defaultColoring.xml"; // NOI18N
    static final String CURRENT_FONT_COLOR_PROFILE = "currentFontColorProfile"; // NOI18N
    static final String CURRENT_KEYMAP_PROFILE = "currentKeymap";      // NOI18N
    static final String KEYMAPS_FOLDER = "Keymaps";                    // NOI18N

    
    /**
     * Returns set of mimetypes.
     *
     * @return set of mimetypes
     */
    public Set /*<String>*/ getMimeTypes () {
	if (mimeToLanguage == null) init ();
	return Collections.unmodifiableSet (mimeToLanguage.keySet ());
    }
    
    /**
     * Returns name of language for given mime type.
     *
     * @return name of language for given mime type
     */
    public String getLanguageName (String mimeType) {
	if (mimeToLanguage == null) init ();
	return (String) mimeToLanguage.get (mimeType);
    }

    
    // FontColors ..............................................................
    
    /**
     * Returns set of font & colors profiles.
     *
     * @return set of font & colors profiles
     */
    public Set /*<String>*/ getFontColorProfiles () {
	if (fontColorProfiles == null)
	    init ();
        Set result = new HashSet ();
        Iterator it = fontColorProfiles.keySet ().iterator ();
        while (it.hasNext ()) {
            String profile = (String) it.next ();
            if (!profile.startsWith ("test"))
                result.add (profile);
        }
	return result;
    }
    
    private Set systemFontColorProfiles;
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    public boolean isCustomFontColorProfile (String profile) {
        if (systemFontColorProfiles == null) init ();
        return !systemFontColorProfiles.contains (profile);
    }
    
    private String currentFontColorProfile;
    
    /**
     * Returns name of current font & colors profile.
     *
     * @return name of current font & colors profile
     */
    public String getCurrentFontColorProfile () {
        if (currentFontColorProfile == null) {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource ("Editors");
            currentFontColorProfile = (String) fo.getAttribute 
                (CURRENT_FONT_COLOR_PROFILE);
            if (currentFontColorProfile == null)
                currentFontColorProfile = "NetBeans";
        }
        if (!getFontColorProfiles ().contains (currentFontColorProfile)) {
            currentFontColorProfile = "NetBeans";
        }
        return currentFontColorProfile;
    }
    
    /**
     * Sets current font & colors profile.
     *
     * @param profile a profile name
     */
    public void setCurrentFontColorProfile (String profile) {
        String oldProfile = getCurrentFontColorProfile ();
        if (oldProfile.equals (profile)) return;
	FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
	FileObject fo = fs.findResource ("Editors");
        try {
            fo.setAttribute (CURRENT_FONT_COLOR_PROFILE, profile);
            currentFontColorProfile = profile;
            pcs.firePropertyChange (PROP_CURRENT_FONT_COLOR_PROFILE, oldProfile, currentFontColorProfile);
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    private Map defaultColors = new HashMap ();
     
    /**
     * Returns font & color defaults for given profile or null, if the profile
     * is unknown .
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     */
    public Collection /*<AttributeSet>*/ getDefaultFontColors (
	String profile
    ) {
        // 1) translate profile name
	String s = getOriginalProfile (profile); // loc name > name

        if (!defaultColors.containsKey (s)) {
            
            // 2) init profile for test mime types
            if (s.startsWith ("test")) {
                defaultColors.put (
                    s,
                    getDefaultFontColors ("NetBeans")
                );
            } else {

                // 3) load colorings
                Map m = ColoringStorage.loadColorings 
                    (new String [0], s, ALL_LANGUAGES_FILE_NAME, false);
                if (m != null) {
                    Collection c = m.values ();
                    defaultColors.put (s, c);
                } else
                    defaultColors.put (s, null);
            }
        }
        
        if (defaultColors.get (s) == null) return null;
	return Collections.unmodifiableCollection (
            (Collection) defaultColors.get (s)
        );
    }
    
    private Map defaultColorDefaults = new HashMap ();
     
    /**
     * Returns default values for font & color defaults for given profile 
     * or null, if the profile is unknown.
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     */
    public Collection /*<AttributeSet>*/ getDefaultFontColorDefaults (
	String profile
    ) {
        // 1) translate profile name
	String s = getOriginalProfile (profile); // loc name > name

        // 2) get data from cache or disk
        if (!defaultColorDefaults.containsKey (s)) {
            Map m = ColoringStorage.loadColorings 
                (new String [0], s, ALL_LANGUAGES_FILE_NAME, true);
            if (m != null) {
                Collection c = m.values ();
                defaultColorDefaults.put (s, c);
            } else
                defaultColorDefaults.put (s, null);
        }
        
        if (defaultColorDefaults.get (s) == null) return null;
	return Collections.unmodifiableCollection (
            (Collection) defaultColorDefaults.get (s)
        );
    }
    
    /**
     * Sets font & color defaults for given profile.
     *
     * @param profile a profile name
     * @param fontColors font & color defaults to be used
     */
    public void setDefaultFontColors (
	String profile,
	Collection /*<AttributeSet>*/ fontColors
    ) {
        // 1) translate name of profile
	String s = getOriginalProfile (profile); // loc name > name
        
        if (fontColors == null) {
            // 2) remove coloring / revert to defaults
            ColoringStorage.deleteColorings
                (new String [0], s, ALL_LANGUAGES_FILE_NAME);
            defaultColors.remove (s);
            init ();
            pcs.firePropertyChange (PROP_DEFAULT_FONT_COLORS, null, null);
            return;
        }
        
        // 2) save new values to cache
	Object oldColors = defaultColors.get (s);
        defaultColors.put (s, fontColors);
        
        // 3) save new values to disk
        if (!profile.startsWith ("test"))
            ColoringStorage.saveColorings 
                (new String [0], s, ALL_LANGUAGES_FILE_NAME, fontColors);
        
        // 4) update profiles
	pcs.firePropertyChange (PROP_DEFAULT_FONT_COLORS, null, null);
    }
    
    private Map editorFontColors = new HashMap ();
    
    /**
     * Returns highlighting properties for given profile or null, if the 
     * profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    public Collection /*<AttributeSet>*/ getHighlightings (
	String profile
    ) {
        // 1) translate profile name
	String s = getOriginalProfile (profile);

        // 2) init profile for test mime types
        if (s.startsWith ("test")) {
            int i = s.indexOf ('_');
            editorFontColors.put (
                s,
                getHighlightings (s.substring (i + 1))
            );
        }

        // 3) read data form disk or cache
        if (!editorFontColors.containsKey (s)) {
            Map m = ColoringStorage.loadColorings 
                (new String [0], s, HIGHLIGHTING_FILE_NAME, false);
            if (m != null) {
                Collection c = m.values ();
                editorFontColors.put (s, c);
            } else
                editorFontColors.put (s, null);
        }
        
        if (editorFontColors.get (s) == null) return null;
	return Collections.unmodifiableCollection (
            (Collection) editorFontColors.get (s)
        );
    }
    
    private Map editorFontColorDefaults = new HashMap ();
    
    /**
     * Returns defaults for highlighting properties for given profile,
     * or null if the profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    public Collection /*<AttributeSet>*/ getHighlightingDefaults (
	String profile
    ) {
        // 1) translate profile name
	String s = getOriginalProfile (profile);

        // 2) read data form disk or cache
        if (!editorFontColorDefaults.containsKey (s)) {
            Map m = ColoringStorage.loadColorings 
                (new String [0], s, HIGHLIGHTING_FILE_NAME, true);
            if (m != null) {
                Collection c = m.values ();
                editorFontColorDefaults.put (s, c);
            } else
                editorFontColorDefaults.put (s, null);
        }
        if (editorFontColorDefaults.get (s) == null) return null;
	return Collections.unmodifiableCollection (
            (Collection) editorFontColorDefaults.get (s)
        );
    }
    
    /**
     * Sets highlighting properties for given profile.
     *
     * @param profile a profile name
     * @param highlighting a highlighting properties to be used
     */
    public void setHighlightings (
	String profile,
	Collection /*<AttributeSet>*/ fontColors
    ) {
        // 1) translate profile name
	String s = (String) fontColorProfiles.get (profile);
	
        if (fontColors == null) {
            // 2) remove coloring / revert to defaults
            ColoringStorage.deleteColorings
                (new String [0], s, HIGHLIGHTING_FILE_NAME);
            editorFontColors.remove (s);
            init ();
            pcs.firePropertyChange (PROP_EDITOR_FONT_COLORS, null, null);
            return;
        }
        
        // 2) save new values to cache
	Object oldColors = editorFontColors.get (s);
        editorFontColors.put (s, fontColors);
        
        // 3) save new values to disk
        if (!profile.startsWith ("test"))
            ColoringStorage.saveColorings 
                (new String [0], s, HIGHLIGHTING_FILE_NAME, fontColors);
        
	pcs.firePropertyChange (PROP_EDITOR_FONT_COLORS, null, null);
    }  
    
    
    // KeyMaps .................................................................
    
    /**
     * Returns set of keymap profiles.
     *
     * @return set of font & colors profiles
     */
    public Set /*<String>*/ getKeyMapProfiles () {
	if (keyMapProfiles == null) init ();
	return Collections.unmodifiableSet (keyMapProfiles.keySet ());
    }
    
    private Set systemKeymapProfiles;
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    public boolean isCustomKeymapProfile (String profile) {
        if (systemKeymapProfiles == null) init ();
        return !systemKeymapProfiles.contains (profile);
    }
    
    private String currentKeyMapProfile;
    
    /**
     * Returns name of current keymap profile.
     *
     * @return name of current keymap profile
     */
    public String getCurrentKeyMapProfile () {
        if (currentKeyMapProfile == null) {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource (KEYMAPS_FOLDER);
            currentKeyMapProfile = (String) fo.getAttribute (CURRENT_KEYMAP_PROFILE);
            if (currentKeyMapProfile == null)
                currentKeyMapProfile = "NetBeans";
        }
        return currentKeyMapProfile;
    }
    
    /**
     * Sets current keymap profile.
     *
     * @param profile a profile name
     */
    public void setCurrentKeyMapProfile (String keyMapName) {
        String oldKeyMap = getCurrentKeyMapProfile ();
        if (oldKeyMap.equals (keyMapName)) return;
        try {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource (KEYMAPS_FOLDER);
            if (fo == null)
                fo = fs.getRoot ().createFolder (KEYMAPS_FOLDER);
            fo.setAttribute (CURRENT_KEYMAP_PROFILE, keyMapName);
            currentKeyMapProfile = keyMapName;
            pcs.firePropertyChange (PROP_CURRENT_KEY_MAP_PROFILE, oldKeyMap, currentKeyMapProfile);
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be registerred
     */
    public void addPropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be unregisterred
     */
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be registerred
     */
    public void addPropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (propertyName, l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be unregisterred
     */
    public void removePropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (propertyName, l);
    }
    

    // support methods .........................................................
    
    private Map fontColorProfiles;
    private Map keyMapProfiles;
    private Map mimeToLanguage;

    private void init () {
	fontColorProfiles = new HashMap ();
	keyMapProfiles = new HashMap ();
	keyMapProfiles.put ("NetBeans", "NetBeans");
	mimeToLanguage = new HashMap ();
        systemFontColorProfiles = new HashSet ();
        systemKeymapProfiles = new HashSet ();
	FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
	FileObject fo = fs.findResource ("Editors");
	Enumeration e = fo.getFolders (false);
	while (e.hasMoreElements ())
	    init1 ((FileObject) e.nextElement ());
    }
    
    private void init1 (FileObject fo) {
        Enumeration e = fo.getChildren (false);
        while (e.hasMoreElements ())
            init2 ((FileObject) e.nextElement ());
    }
	
    private void init2 (FileObject fo) {
        if (fo.getName ().equals ("Defaults") && fo.isFolder () &&
            fo.getFileObject (HIGHLIGHTING_FILE_NAME) != null
        )
            addFontColorsProfile (fo, true); // Editors/ProfileName/Defaults/editorColoring.xml
        else
        if (fo.getNameExt ().equals (HIGHLIGHTING_FILE_NAME))
            addFontColorsProfile (fo, false); // Editors/ProfileName/editorColoring.xml
        else
        if (fo.getFileObject ("NetBeans/Defaults/coloring.xml") != null)
            addMimeType (fo); // Editors/XXX/YYY/NetBeans/Defaults/coloring.xml
        else
        if (fo.getPath ().endsWith ("text/base") && fo.isFolder ()) {
            if (fo.getFileObject ("Defaults/" + KEYBINDING_FILE_NAME) != null)
                addKeyMapProfile (fo, true); // Editors/text/base/Defaults/keybindings.xml
            else
            if (fo.getFileObject (KEYBINDING_FILE_NAME) != null)
                addKeyMapProfile (fo, false); // Editors/text/base/keybindings.xml
            Enumeration e = fo.getChildren (false);
            while (e.hasMoreElements ())
                init3 ((FileObject) e.nextElement ());
        }
    }
        
    private void init3 (FileObject fo) {
        if (fo.getFileObject ("Defaults/" + KEYBINDING_FILE_NAME) != null)
            addKeyMapProfile (fo, true); // Editors/text/base/ProfileName/Defaults/keybindings.xml
        else
        if (fo.getFileObject (KEYBINDING_FILE_NAME) != null)
            addKeyMapProfile (fo, false); // Editors/text/base/ProfileName/keybindings.xml
    }

    private void addMimeType (FileObject fo) {
        String mimeType = fo.getPath ();
        mimeType = mimeType.substring (8);
        String bundleName = (String) fo.getAttribute 
            ("SystemFileSystem.localizingBundle");
        String languageName = mimeType;
        if (bundleName != null)
            try {
                languageName = NbBundle.getBundle (bundleName).getString (mimeType);
            } catch (MissingResourceException ex) {}
        mimeToLanguage.put (mimeType, languageName);
    }
    
    private void addFontColorsProfile (FileObject fo, boolean systemProfile) {
        String profile = fo.getParent ().getName ();
        String bundleName = (String) fo.getParent ().getAttribute 
            ("SystemFileSystem.localizingBundle");
        String locProfile = profile;
        if (bundleName != null)
            try {
                locProfile = NbBundle.getBundle (bundleName).getString (profile);
            } catch (MissingResourceException ex) {}
        if (systemProfile) systemFontColorProfiles.add (locProfile);
        fontColorProfiles.put (locProfile, profile);
    }
    
    private void addKeyMapProfile (FileObject fo, boolean systemProfile) {
        String profile = fo.getName ();
        if (profile.equals ("base")) profile = "NetBeans";
        String bundleName = (String) fo.getAttribute 
            ("SystemFileSystem.localizingBundle");
        String locProfile = profile;
        if (bundleName != null)
            try {
                locProfile = NbBundle.getBundle (bundleName).getString (profile);
            } catch (MissingResourceException ex) {}
        if (systemProfile) systemKeymapProfiles.add (locProfile);
        keyMapProfiles.put (locProfile, profile);
    }
    
    String getOriginalProfile (String profile) {
	if (fontColorProfiles == null)
	    init ();
	String result = (String) fontColorProfiles.get (profile);
        if (result != null) return result;
        fontColorProfiles.put (profile, profile);
        return profile;
    }
}
