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
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.core.NbKeymap.KeymapAction;
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
    private String                  listenerFolder = null;
    
    
    static void initShortcuts () {
        if (shortcutsFolder != null) return;
        shortcutsFolder = new ShortcutsFolder ();
    }
    
    private ShortcutsFolder () {
        refresh ();
        FileObject root = Repository.getDefault ().
            getDefaultFileSystem ().getRoot ();
        root.getFileObject (SHORTCUTS_FOLDER).addFileChangeListener (listener);
        FileObject profilesFolder = root.getFileObject(PROFILES_FOLDER);
        if (profilesFolder != null) {
            profilesFolder.addFileChangeListener(listener);
        }
    }
    
    private void refresh () {
        
        // get keymap and delete old shortcuts
        NbKeymap keymap = (NbKeymap) Lookup.getDefault ().lookup (Keymap.class);
        keymap.removeBindings ();

        // update main shortcuts
        readShortcuts (keymap, SHORTCUTS_FOLDER);
        
        // update shortcuts from profile
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject profilesFolder = root.getFileObject(PROFILES_FOLDER);
        if (profilesFolder != null) {
            String keymapName = (String) profilesFolder.getAttribute("currentProfile"); // NOI18N
            if (keymapName == null) {
                keymapName = "NetBeans"; // NOI18N
            }
            String folderName = PROFILES_FOLDER + '/' + keymapName;
            FileObject profileFolder = root.getFileObject(folderName);
            if (profileFolder != null) {
                readShortcuts(keymap, folderName);
                // add listener to current profile folder
                if (listenerFolder != null) {
                    FileObject formerProfileFolder = root.getFileObject(listenerFolder);
                    if (formerProfileFolder != null) {
                        formerProfileFolder.removeFileChangeListener(listener);
                    }
                }
                listenerFolder = folderName;
                profileFolder.addFileChangeListener(listener);
            }
        }
    }
    
    private void readShortcuts (NbKeymap keymap, String folderName) {
        DataFolder folder = getDataFolder (folderName);
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
                ex.printStackTrace ();
                System.out.println("dataObject: " + dataObject);
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
            if (!fe.getName ().equals (CURRENT_PROFILE_ATTRIBUTE)) return;
            if (task != null) task.cancel ();
            task = RequestProcessor.getDefault ().post (this, 500);
        }
    }
}
