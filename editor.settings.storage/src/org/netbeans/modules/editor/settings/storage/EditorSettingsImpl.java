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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
public class EditorSettingsImpl extends EditorSettings {

    private PropertyChangeSupport   pcs;
    
    {pcs = new PropertyChangeSupport (this);}
    
    
    
    public Set /*<String>*/ getMimeTypes () {
	if (mimeToLanguage == null) init ();
	return mimeToLanguage.keySet ();
    }
    
    public String getLanguageName (String mimeType) {
	if (mimeToLanguage == null) init ();
	return (String) mimeToLanguage.get (mimeType);
    }

    
    // FontColors ..............................................................
    
    public Set /*<String>*/ getFontColorSchemes () {
	if (schemes == null)
	    init ();
	return schemes.keySet ();
    }
    
    private String currentScheme;
    
    public String getCurrentFontColorScheme () {
        if (currentScheme == null) {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource ("Editors");
            currentScheme = (String) fo.getAttribute ("currentScheme");
            if (currentScheme == null)
                currentScheme = "NetBeans";
        }
        if (!getFontColorSchemes ().contains (currentScheme)) {
            System.out.println("EditorColoringImpl.current scheme not found! " + currentScheme);
            currentScheme = "NetBeans";
        }
        return currentScheme;
    }
    
    public void setCurrentFontColorScheme (String scheme) {
        String oldScheme = getCurrentFontColorScheme ();
        if (oldScheme.equals (scheme)) return;
	FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
	FileObject fo = fs.findResource ("Editors");
        try {
            fo.setAttribute ("currentScheme", scheme);
            currentScheme = scheme;
	    //S ystem.out.println("EditorColoringImpl.setCurrentScheme " + scheme);
            pcs.firePropertyChange (PROP_CURRENT_FONT_COLOR_SCHEME, oldScheme, currentScheme);
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    private Map defaultColors = new HashMap ();
     
    public Collection /*<AttributeSet>*/ getDefaultFontColors (
	String scheme
    ) {
        // 1) translate scheme name
	String s = getOriginalScheme (scheme); // loc name > name
	if (s == null) {
            s = scheme; // create a new scheme!
            schemes.put (s, s);
        }
        
        if (!defaultColors.containsKey (s)) {
            
            // 2) load colorings
            Map m = ColoringStorage.loadColorings 
                (new String [0], s, "defaultColoring.xml");
            if (m != null) {
                Collection c = m.values ();
                defaultColors.put (s, c);
            } else
                defaultColors.put (s, null);
        }
	return (Collection) defaultColors.get (s);
    }
    
    public void setDefaultFontColors (
	String scheme,
	Collection /*<AttributeSet>*/ fontColors
    ) {
        // 1) translate name of scheme
	String s = getOriginalScheme (scheme); // loc name > name
	if (s == null)
            addScheme (s = scheme); // create a new scheme!

        // 2) save new values to cache
	Object oldColors = defaultColors.get (s);
        defaultColors.put (s, fontColors);
        
        // 3) save new values to disk
        ColoringStorage.saveColorings 
            (new String [0], s, "defaultColoring.xml", fontColors);
        
        // 4) update schemes
	pcs.firePropertyChange (PROP_DEFAULT_FONT_COLORS, oldColors, fontColors);
    }
    
    private Map editorFontColors = new HashMap ();
    
    public Collection /*<AttributeSet>*/ getEditorFontColors (
	String scheme
    ) {
        // 1) translate scheme name
	String s = getOriginalScheme (scheme);
        if (s == null) s = scheme; // no such scheme
        
        if (!editorFontColors.containsKey (s)) {
            
            Map m = ColoringStorage.loadColorings 
                (new String [0], s, "editorColoring.xml");
            if (m != null) {
                Collection c = m.values ();
                editorFontColors.put (s, c);
            } else
                editorFontColors.put (s, null);
        }
	return (Collection) editorFontColors.get (s);
    }
    
    public void setEditorFontColors (
	String scheme,
	Collection /*<AttributeSet>*/ fontColors
    ) {
        // 1) translate scheme name
	String s = (String) schemes.get (scheme);
	if (s == null)
            addScheme (s = scheme); // create a new scheme!
	
        // 2) save new values to cache
	Object oldColors = editorFontColors.get (s);
        editorFontColors.put (s, fontColors);
        
        // 3) save new values to disk
        ColoringStorage.saveColorings 
            (new String [0], s, "editorColoring.xml", fontColors);
        
	pcs.firePropertyChange (PROP_EDITOR_FONT_COLORS, oldColors, fontColors);
    }  
    
    
    // KeyMaps .................................................................
    
    public Set /*<String>*/ getKeyMapNames () {
	if (keyMaps == null)
	    init ();
	return keyMaps.keySet ();
    }
    
    private String currentKeyMapName;
    
    public String getCurrentKeyMapName () {
        if (currentKeyMapName == null) {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource ("Editors");
            currentScheme = (String) fo.getAttribute ("currentKeyMapName");
            if (currentKeyMapName == null)
                currentKeyMapName = "NetBeans";
        }
        return currentKeyMapName;
    }
    
    public void setCurrentKeyMapName (String keyMapName) {
        String oldKeyMap = getCurrentKeyMapName ();
        if (oldKeyMap.equals (keyMapName)) return;
	FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
	FileObject fo = fs.findResource ("Editors");
        try {
            fo.setAttribute ("currentKeyMapName", keyMapName);
            currentKeyMapName = keyMapName;
            pcs.firePropertyChange (PROP_CURRENT_KEY_MAP_NAME, oldKeyMap, currentKeyMapName);
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
    
    void addScheme (String scheme) {
        schemes.put (scheme, scheme);
    }
    
    private Map schemes;
    private Map keyMaps;
    private Map mimeToLanguage;

    private void init () {
	schemes = new HashMap ();
	schemes.put ("NetBeans", "NetBeans");
	keyMaps = new HashMap ();
	keyMaps.put ("NetBeans", "NetBeans");
	mimeToLanguage = new HashMap ();
	FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
	FileObject fo = fs.findResource ("Editors");
	Enumeration e = fo.getFolders (false);
	while (e.hasMoreElements ())
	    init1 ((FileObject) e.nextElement (), 1);
    }
    
    private void init1 (FileObject fo, int depth) {
	if (depth == 1) {
	    Enumeration e = fo.getFolders (false);
	    while (e.hasMoreElements ())
		init1 ((FileObject) e.nextElement (), 2);
	    return;
	}
	
	if (fo.getName ().equals ("Defaults") && fo.isFolder () &&
            fo.getFileObject ("editorColoring.xml") != null
        )
            addScheme (fo);
        else
        if (fo.getNameExt ().equals ("editorColoring.xml"))
            addScheme (fo);
        else
	if (fo.getName ().equals ("Defaults") && fo.isFolder () &&
            fo.getFileObject ("keybindings.xml") != null
        )
            addKeyMap (fo);
        else
        if (fo.getNameExt ().equals ("keybindings.xml"))
            addKeyMap (fo);
        else {
            FileObject fo1 = fo.getFileObject ("Defaults/coloring.xml");
            FileObject fo2 = fo.getFileObject ("coloring.xml");
            if (fo1 == null && fo2 == null) return;
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
    }
    
    private void addScheme (FileObject fo) {
        String scheme = fo.getParent ().getName ();
        String bundleName = (String) fo.getParent ().getAttribute 
            ("SystemFileSystem.localizingBundle");
        String locScheme = scheme;
        if (bundleName != null)
            try {
                locScheme = NbBundle.getBundle (bundleName).getString (scheme);
            } catch (MissingResourceException ex) {}
        schemes.put (locScheme, scheme);
    }
    
    private void addKeyMap (FileObject fo) {
        String keyMapName = fo.getParent ().getName ();
        String bundleName = (String) fo.getParent ().getAttribute 
            ("SystemFileSystem.localizingBundle");
        String locKeyMapName = keyMapName;
        if (bundleName != null)
            try {
                locKeyMapName = NbBundle.getBundle (bundleName).getString (keyMapName);
            } catch (MissingResourceException ex) {}
        keyMaps.put (locKeyMapName, keyMapName);
    }
    
    public String getOriginalScheme (String schemeName) {
	return (String) schemes.get (schemeName);
    }
}
