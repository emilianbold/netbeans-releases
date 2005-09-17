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

package org.netbeans.core;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.core.NbKeymap.KeymapAction;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 * Bridge to old layers based system.
 *
 * @author Jan Jancura
 */
class ShortcutsFolder {
    
    private static final String PROFILES_FOLDER = "Profiles";
    private static final String SHORTCUTS_FOLDER = "Shortcuts";
    private static final String CURRENT_PROFILE_ATTRIBUTE = "currentProfile";

    private static ShortcutsFolder  shortcutsFolder;
    private Listener                listener = new Listener ();
    private FileObject              profilesFileObject;
    private FileObject              shortcutsFileObject;
    private FileObject              currentFolder;
    
    
    static void initShortcuts () {
        if (shortcutsFolder != null) return;
        shortcutsFolder = new ShortcutsFolder ();
    }
    
    private ShortcutsFolder () {
        try {
            FileObject root = Repository.getDefault ().
                getDefaultFileSystem ().getRoot ();
            profilesFileObject = root.getFileObject (PROFILES_FOLDER);
            if (profilesFileObject == null)
                profilesFileObject = root.createFolder (PROFILES_FOLDER);
            profilesFileObject.addFileChangeListener (listener);
            
            shortcutsFileObject = root.getFileObject (SHORTCUTS_FOLDER);
            if (shortcutsFileObject == null)
                shortcutsFileObject = root.createFolder (SHORTCUTS_FOLDER);
            shortcutsFileObject.addFileChangeListener (listener);            
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
        refresh ();
    }
    
    private void refresh () {
        
        // get keymap and delete old shortcuts
        NbKeymap keymap = (NbKeymap) Lookup.getDefault ().lookup (Keymap.class);
        keymap.removeBindings ();

        // update main shortcuts
        readShortcuts (keymap, shortcutsFileObject);
        
        // update shortcuts from profile
        String keymapName = (String) profilesFileObject.getAttribute
            (CURRENT_PROFILE_ATTRIBUTE);
        if (keymapName == null || keymapName.equals (""))
            keymapName = "NetBeans"; // NOI18N
        if (currentFolder != null) 
            currentFolder.removeFileChangeListener (listener);
        currentFolder = Repository.getDefault ().getDefaultFileSystem ().
            getRoot ().getFileObject (PROFILES_FOLDER + '/' + keymapName);
        if (currentFolder == null) return;
        readShortcuts (keymap, currentFolder);
        // add listener to current profile folder
        currentFolder.addFileChangeListener (listener);
    }
    
    private void readShortcuts (NbKeymap keymap, FileObject fileObject) {
        DataFolder folder = DataFolder.findFolder (fileObject);
        Enumeration en = folder.children (false);
        while (en.hasMoreElements ()) {
            DataObject dataObject = (DataObject) en.nextElement ();
            if (dataObject instanceof DataFolder) continue;
            InstanceCookie ic = (InstanceCookie) dataObject.getCookie 
                (InstanceCookie.class);
            if (ic == null) continue;
            try {
                Action action = (Action) ic.instanceCreate ();
                String shortcuts = dataObject.getName ();
                KeyStroke[] keyStrokes = Utilities.stringToKeys (shortcuts);
                addShortcut (keymap, action, keyStrokes);
            } catch (Exception ex) {
                ErrorManager.getDefault ().notify (ex);
            }
        }
    }
        
    private static void addShortcut (
        NbKeymap keymap, 
        Action action, 
        KeyStroke[] keyStrokes
    ) {
        Keymap currentKeymap = keymap;
        String shoutcutText = null;
        int i, k = keyStrokes.length - 1;
        for (i = 0; i < k; i++) {
            Action a = currentKeymap.getAction (keyStrokes [i]);
            if (shoutcutText == null) 
                shoutcutText = getKeyText (keyStrokes [i]);
            else
                shoutcutText += " " + getKeyText (keyStrokes [i]); // NOI18N
            if (a == null) {
                a = keymap.createMapAction 
                    (new NbKeymap.SubKeymap (null), shoutcutText);
                currentKeymap.addActionForKeyStroke (keyStrokes [i], a);
            }
            if (!(a instanceof KeymapAction)) return;
            currentKeymap = ((KeymapAction) a).getSubMap ();
        }
        currentKeymap.addActionForKeyStroke (keyStrokes [k], action);
    }
    
    private static String getKeyText (KeyStroke keyStroke) {
        if (keyStroke == null) return "";                       // NOI18N
        String modifText = KeyEvent.getKeyModifiersText 
            (keyStroke.getModifiers ());
        if ("".equals (modifText))                              // NOI18N   
            return KeyEvent.getKeyText (keyStroke.getKeyCode ());
        return modifText + "+" +                                // NOI18N
            KeyEvent.getKeyText (keyStroke.getKeyCode ()); 
    }
    
    private static DataFolder getDataFolder (String name) {
        FileObject root = Repository.getDefault ().
            getDefaultFileSystem ().getRoot ();
        root = root.getFileObject (name);
        if (root == null) return null;
        return DataFolder.findFolder (root);
    }
    
    private class Listener extends FileChangeAdapter implements Runnable {
        
        private Task task;
        
        public void run () {
            refresh ();
        }
        
        public void fileDataCreated (FileEvent fe) {
            if (task != null) task.cancel ();
            task = RequestProcessor.getDefault ().post (this, 500);
        }

        public void fileChanged (FileEvent fe) {
            if (task != null) task.cancel ();
            task = RequestProcessor.getDefault ().post (this, 500);
        }

        public void fileDeleted (FileEvent fe) {
            if (task != null) task.cancel ();
            task = RequestProcessor.getDefault ().post (this, 500);
        }
        
        public void fileAttributeChanged (FileAttributeEvent fe) {
            if (fe.getName () != null &&
                !fe.getName ().equals (CURRENT_PROFILE_ATTRIBUTE)
            ) return;
            if (task != null) task.cancel ();
            task = RequestProcessor.getDefault ().post (this, 500);
        }
    }
}
