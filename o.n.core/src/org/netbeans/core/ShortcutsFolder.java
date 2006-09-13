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

package org.netbeans.core;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Bridge to old layers based system.
 *
 * @author Jan Jancura
 */
class ShortcutsFolder {
    
    private static final String PROFILES_FOLDER = "Keymaps";
    private static final String SHORTCUTS_FOLDER = "Shortcuts";
    private static final String CURRENT_PROFILE_ATTRIBUTE = "currentKeymap";

    private static ShortcutsFolder  shortcutsFolder;
    private Listener                listener = new Listener ();
    private FileObject              profilesFileObject;
    private FileObject              shortcutsFileObject;
    private FileObject              currentFolder;
    private Logger debug = Logger.getLogger(ShortcutsFolder.class.getName ());
    
    
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
            Exceptions.printStackTrace(ex);
        }
        refresh ();
    }
    
    static void waitFinished () {
        shortcutsFolder.listener.task.waitFinished ();
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
        if (keymapName == null || "".equals (keymapName))
            keymapName = "NetBeans"; // NOI18N
        if (currentFolder != null) 
            currentFolder.removeFileChangeListener (listener);
        currentFolder = Repository.getDefault ().getDefaultFileSystem ().
            getRoot ().getFileObject (PROFILES_FOLDER + '/' + keymapName);
        if (currentFolder == null) {
            try {
                currentFolder = profilesFileObject.createFolder(keymapName);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        if (currentFolder != null) {
            readShortcuts (keymap, currentFolder);
            // add listener to current profile folder
            currentFolder.addFileChangeListener (listener);
        }
    }
    
    
    private void readShortcuts (NbKeymap keymap, FileObject fileObject) {
        debug.fine("\nreadShortcuts " + fileObject);
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
                debug.fine("  " + shortcuts + " : " + action);
                KeyStroke[] keyStrokes = Utilities.stringToKeys (shortcuts);
                if (keyStrokes != null) {
                    addShortcut(keymap, action, keyStrokes);
                } else { // see e.g. secondary exception in #74169
                    debug.warning("Unrecognized shortcut name from " + dataObject.getPrimaryFile().getPath()); // NOI18N
                }
            } catch (ClassNotFoundException x) {
                Logger.getLogger(ShortcutsFolder.class.getName()).log(Level.WARNING,
                        "{0} ignored; cannot load class {1}",
                        new Object[] {dataObject.getPrimaryFile().getPath(), ic.instanceName()});
            } catch (Exception ex) {
                Logger.global.log(Level.WARNING, null, ex);
            }
        }
    }
        
    private static void addShortcut (
        NbKeymap keymap, 
        Action action, 
        KeyStroke[] keyStrokes
    ) {
        Keymap currentKeymap = keymap;
        int i, k = keyStrokes.length - 1;
        for (i = 0; i < k; i++) {
            Action a = currentKeymap.getAction (keyStrokes [i]);
            if (a == null) {
                a = keymap.createMapAction 
                    (new NbKeymap.SubKeymap (null), keyStrokes [i]);
                currentKeymap.addActionForKeyStroke (keyStrokes [i], a);
            }
            if (!(a instanceof KeymapAction)) return;
            currentKeymap = ((KeymapAction) a).getSubMap ();
        }
        currentKeymap.addActionForKeyStroke (keyStrokes [k], action);
    }
    
    private class Listener extends FileChangeAdapter implements Runnable {
        
        private RequestProcessor.Task task = new RequestProcessor ("ShortcutsFolder").create (this);
        
        public void run () {
            refresh ();
        }
        
        public void fileDataCreated (FileEvent fe) {
            task.schedule (500);
        }

        public void fileChanged (FileEvent fe) {
            task.schedule (500);
        }

        public void fileDeleted (FileEvent fe) {
            task.schedule (500);
        }
        
        public void fileAttributeChanged (FileAttributeEvent fe) {
            if (fe.getName () != null &&
                !CURRENT_PROFILE_ATTRIBUTE.equals (fe.getName ())
            ) return;
            task.schedule (500);
        }
    }
}
