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

package org.netbeans.core;

import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.text.Keymap;
import javax.swing.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;


/**
 * Working with shortcuts - retreiving, saving etc. The old .keys
 * files manipulation is also here for backward compatibility.
 * @author  dstrupl
 */
final class ShortcutsFolder extends FolderInstance {

    /** This reference is initialized in initShortcuts().*/
    static ShortcutsFolder shortcutsFolder = null;
    
    /** */
    public static final String PROPERTIES_FILE = "Shortcuts";   // NOI18N

    /** Used for backward compatibility - this file is no longer used.*/
    public static final String DEFAULT_KEYS_FILE = "Default"; // NOI18N
    
    /** Used for backward compatibility - this file is no longer used.*/
    public static final String USER_KEYS_FILE = "UserDefined"; // NOI18N

    /** Used for backward compatibility - this extension is no longer used.*/
    private static final String KEYS_EXT = "keys"; // NOI18N
    
    /** Folder name under the system folder.*/
    static final String SHORTCUTS_FOLDER = "Shortcuts"; // NOI18N

    /** Info for old XML file.*/
    private static final String XML_SHORTCUTS = "Shortcuts"; // NOI18N
    /** Info for old XML file.*/
    private static final String XML_DESCRIPTION = "Description"; // NOI18N
    /** Info for old XML file.*/
    private static final String XML_BINDING = "Binding"; // NOI18N

    /** Info for old XML file.*/
    private static final String ATTR_DESCRIPTION_NAME = "name"; // NOI18N
    /** Info for old XML file.*/
    private static final String ATTR_BINDING_KEY = "key"; // NOI18N
    /** Info for old XML file.*/
    private static final String ATTR_BINDING_ACTION = "action"; // NOI18N

    private static final String PROP_CURRENT_KEYFILE = "current"; // NOI18N
    
    /** Creates new ShortcutsFolder */
    public ShortcutsFolder(DataFolder f) {
        super(f);
        recreate();
    }
    
    /** Updates the NbKeyMap that represents the actually running
     * shortcuts.
     * @param cookies array of instance cookies for the folder
     * @return null [PENDING] - what should it return? (dstrupl)
     */
    protected Object createInstance(InstanceCookie[] cookies)
        throws IOException, ClassNotFoundException {
            
        //
        HashMap map = new HashMap(80);
        for (int i = 0; i < cookies.length; i++) {
                KeyStroke stroke = Utilities.stringToKey (cookies[i].instanceName());
                Action action = (Action)cookies[i].instanceCreate();
                map.put (stroke, action);
        }
        
        Keymap globalMap = TopManager.getDefault ().getGlobalKeymap ();
        globalMap.removeBindings();
        // globalMap is synchronized
        if (globalMap instanceof NbKeymap) {
            ((NbKeymap)globalMap).addActionForKeyStrokeMap(map);
        } else {
            // in the case we are working with unknown Keymap implementation,
            // we have to add the items one by one
            for (Iterator it = map.keySet().iterator(); it.hasNext (); ) {
                KeyStroke key = (KeyStroke)it.next ();
                globalMap.addActionForKeyStroke(key, (Action) map.get (key));
            }
        }
        
        // [PENDING] change this ???
        return null;
    }

    /** Overriden to to transform given DataObjet to KeyActionPair
     * if possible
     * @return KeyActionPair or null if it cannot be created
     */
    protected InstanceCookie acceptDataObject(DataObject dob) {
        InstanceCookie ic = super.acceptDataObject(dob);
        if (ic != null) {
            try {
                Object o = ic.instanceCreate();
                if (o instanceof Action) {
                    KeyActionPair pair = new KeyActionPair(dob.getName(), (Action)o);
                    return pair;
                }
            } catch (IOException x) {
                TopManager.getDefault().getErrorManager().notify(ErrorManager.WARNING, x);
            } catch (ClassNotFoundException x) {
                TopManager.getDefault().getErrorManager().notify(ErrorManager.WARNING, x);
            }
        }
        return null;
    }
    
    // -----------------------------------------------------------------------------
    // Static methods

    /** Takes the Action name and produces String without & ... etc.
     * It is used in ShortcutsPanel.
     */
    static String getKeyStrokeName (KeyStroke stroke) {
        Keymap map = TopManager.getDefault ().getGlobalKeymap ();
        Action action = map.getAction (stroke);
        if (action instanceof SystemAction) {
            String name = ((SystemAction)action).getName ();
            name = Utilities.replaceString (name, "&", ""); // remove mnemonics marker  // NOI18N
            name = Utilities.replaceString (name, "...", ""); // remove trailing "..."  // NOI18N
            return getKeyText (stroke) + " [" + name + "]"; // NOI18N
        } else {
            if (action == null) return getKeyText (stroke);
            else return getKeyText (stroke) + " [" + action.getValue (Action.NAME) + "]"; // NOI18N
        }
    }

    /**
     * Used in ActionsPanel.
     */
    static String getActionName (SystemAction action) {
        String name = Utilities.replaceString (action.getName (), "&", ""); // remove mnemonics marker // NOI18N
        name = Utilities.replaceString (name, "...", ""); // remove trailing "..." // NOI18N

        Keymap map = TopManager.getDefault ().getGlobalKeymap ();
        KeyStroke[] strokes = map.getKeyStrokesForAction(action);

        if (strokes.length > 0) {
            name = name + " ["; // NOI18N
            for (int i = 0; i < strokes.length; i++) {
                name = name +  getKeyText (strokes[i]);
                if (i != strokes.length - 1) {
                    name = name + ", "; // NOI18N
                }
            }
            return name + "]"; // NOI18N
        } else {
            return name;
        }
    }

    /**
     * @return textual representation of the key
     */
    static String getKeyText (int keyCode, int modifiers) {
        String modifText = java.awt.event.KeyEvent.getKeyModifiersText(modifiers);
        if ("".equals (modifText)) return java.awt.event.KeyEvent.getKeyText(keyCode); // NOI18N
        else {
            if ((keyCode == KeyEvent.VK_ALT) || (keyCode == KeyEvent.VK_ALT_GRAPH) || (keyCode == KeyEvent.VK_CONTROL) || (keyCode == KeyEvent.VK_SHIFT)) {
                return modifText + "+"; // in this case the keyCode text is also among the modifiers // NOI18N
            } else {
                return modifText + "+" + java.awt.event.KeyEvent.getKeyText(keyCode); // NOI18N
            }
        }
    }

    /**
     * @return textual representation of the key
     */
    static String getKeyText (KeyStroke stroke) {
        if (stroke == null) {
            return "";
        }
        String modifText = java.awt.event.KeyEvent.getKeyModifiersText(stroke.getModifiers ());
        if ("".equals (modifText)) return java.awt.event.KeyEvent.getKeyText(stroke.getKeyCode ()); // NOI18N
        else return modifText + "+" + java.awt.event.KeyEvent.getKeyText(stroke.getKeyCode ()); // NOI18N
    }

    /**
     * Parsing the old .keys file
     */
    static org.w3c.dom.Document parseKeysFile (java.net.URL url) throws org.xml.sax.SAXException, java.io.IOException {
        return XMLDataObject.parse(url, new org.xml.sax.ErrorHandler () {
                                       public void error (org.xml.sax.SAXParseException e) {
                                           // [PENDING]
                                       }
                                       public void warning (org.xml.sax.SAXParseException e) {
                                           // [PENDING]
                                       }
                                       public void fatalError (org.xml.sax.SAXParseException e) {
                                           // [PENDING]
                                       }
                                   }
                                  );
    }

    /**
     * Checks whether there are some old keys files present. If there
     * are some it transforms them to the new storing system and
     * deletes them.
     */
    private static void transformOldFiles() {
        // used to be installCurrentBindings
        try {
            org.openide.filesystems.FileSystem systemFS = TopManager.getDefault().getRepository().getDefaultFileSystem ();

            boolean defaultsUsed = false;
            HashMap moduleKeyFiles = new HashMap (11);

            FileObject mainFO = systemFS.find (SHORTCUTS_FOLDER, USER_KEYS_FILE, KEYS_EXT);
            if (mainFO == null) {
                defaultsUsed = true;
                // if no user-defined shortcuts found, try to use the default ones
                mainFO = systemFS.find (SHORTCUTS_FOLDER, DEFAULT_KEYS_FILE, KEYS_EXT);
            }

            // find all .keys files in the Shortcuts folder,
            FileObject shortcutsFolder = FileUtil.createFolder (systemFS.getRoot (), SHORTCUTS_FOLDER);
            FileObject[] files = shortcutsFolder.getChildren ();
            for (int i = 0; i < files.length; i++) {
                String fName = files[i].getName ();
                if (files[i].hasExt (KEYS_EXT)  && !fName.equals(DEFAULT_KEYS_FILE) && !fName.equals(USER_KEYS_FILE)) {
                    moduleKeyFiles.put (fName, files[i]);
                }
            }

            if (!defaultsUsed) {
                //
                // in the case, when the user customized the shortcuts,
                // we have to deal with the situation, that there might be modules
                // installed after the time the user performed the customization
                // and we now have to install their bindings as well
                //
                // the PROPERTIES_FILE file contains a list of all module keys files
                // existing at the point of customization, i.e. their shortcuts are now contained
                // in the USER_KEYS_FILE and should not be processed
                //
                FileObject propsFO = systemFS.find (SHORTCUTS_FOLDER, PROPERTIES_FILE, "properties"); // NOI18N
                if (propsFO != null) {
                    Properties props = new Properties ();
                    try {
                        props.load (propsFO.getInputStream ());
                    } catch (IOException e) {
                        TopManager.getDefault().getErrorManager().notify(e);
                    }

                    // remove all files with name specified in the properties file from the list of files to process (moduleKeyFiles)
                    for (Enumeration items = props.keys(); items.hasMoreElements (); ) {
                        moduleKeyFiles.remove(items.nextElement());
                    }
                }
            }

            HashMap allBindings = processKeysFiles (mainFO, moduleKeyFiles.values ());

            installBindings (allBindings);

        } catch (Exception e2) {
            TopManager.getDefault().getErrorManager().notify(e2);
        }
    }

    /** This method used to install the bindings into the global key map.
     * Now it goes through the HashMap and saves the bindings into
     * the shortcuts folder.
     */
    public static void installBindings (HashMap strokesMap) throws IOException { 
        FileObject fo = TopManager.getDefault().getRepository().getDefaultFileSystem ().findResource(SHORTCUTS_FOLDER);
        DataFolder f = DataFolder.findFolder(fo);
        for (Iterator it = strokesMap.keySet().iterator(); it.hasNext (); ) {
            KeyStroke key = (KeyStroke)it.next ();
            Action a = (Action) strokesMap.get (key);
            
            String name = Utilities.keyToString (key);
            Class clazz = a.getClass ();
            
            InstanceDataObject obj = InstanceDataObject.find (f, name, clazz);
            if (obj == null) {
                InstanceDataObject.create(f, name, clazz);
            }
//            map.addActionForKeyStroke(...); this used to be here
        }
    }

    /** Processes all files with shortcuts bindings and returns HashMap with all  <KeyStroke, Action> mappings.
     * NEW - it deletes the files after processing.
     * @param mainKeysFile the main keys file with default bindings, can be null if there is no such file
     * @param moduleKeyFiles collection of FileObjects with additional shortcuts
     * @return HashMap of <KeyStroke, Action> mapping of all shortcut bindings
     */
    private static HashMap processKeysFiles (FileObject mainKeysFile, Collection moduleKeysFiles) {
        HashMap bindings = new HashMap (79);
        FileLock lock = null;
        if (mainKeysFile != null) {
            try {
                lock = mainKeysFile.lock();
                addBindings (parseKeysFile (mainKeysFile.getURL ()), bindings);
                mainKeysFile.delete(lock);
            } catch (Exception x) {
                TopManager.getDefault().getErrorManager().notify(x);
            } finally {
                lock.releaseLock();
            }
        }
        for (Iterator it = moduleKeysFiles.iterator (); it.hasNext (); ) {
            try {
                FileObject fo = (FileObject)it.next ();
                lock = fo.lock();
                addBindings (parseKeysFile (fo.getURL ()), bindings);
                fo.delete(lock);
            } catch (Exception x) {
                TopManager.getDefault().getErrorManager().notify(x);
            } finally {
                lock.releaseLock();
            }
        }
        return bindings;
    }

    /**
     * Old .keys file reading. It goes through the doc and adds 
     * the items to the map.
     */
    private static void addBindings (org.w3c.dom.Document doc, HashMap map) {
        org.w3c.dom.NodeList nl = doc.getElementsByTagName (XML_BINDING);
        for (int i = 0; i < nl.getLength (); i++) {
            String act = "<unknown>"; // NOI18N
            try {
                String key = nl.item (i).getAttributes ().getNamedItem (ATTR_BINDING_KEY).getNodeValue ();
                act = nl.item (i).getAttributes ().getNamedItem (ATTR_BINDING_ACTION).getNodeValue ();

                KeyStroke stroke = Utilities.stringToKey (key);
                SystemAction action = SystemAction.get (Class.forName(act, true, TopManager.getDefault ().systemClassLoader ()));
                map.put (stroke, action);
	    } catch (ClassNotFoundException cnfe) {
		if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
		    System.err.println ("Warning: action " + act + " not found to add key binding for."); // NOI18N
            } catch (Exception e) { // NullPointer might be thrown if there is not the correct attribute present
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
            }
        }
    }

    /** Deletes the old .keys files and creates an instance of ShortcutsFolder.
     */
    public static void initShortcuts() {
        transformOldFiles();
        DataFolder f = NbPlaces.findSessionFolder(SHORTCUTS_FOLDER);
        shortcutsFolder = new ShortcutsFolder(f);
        shortcutsFolder.recreate();
    }

    /** This should update the global key map. */
    public static void refreshGlobalMap() {
        if (shortcutsFolder != null) {
            shortcutsFolder.recreate();
        }
    }
        
    
    /** Applies changes described by a sequence of ChangeRequests 
     * to the shortcuts folder.
     * @param List changes - the elements of the List are ChangeRequests
     */
    public static void applyChanges(java.util.List changes) {
        FileObject fo = TopManager.getDefault().getRepository().getDefaultFileSystem ().findResource(SHORTCUTS_FOLDER);
        DataFolder f = DataFolder.findFolder(fo);
        Iterator it = changes.listIterator();
        while (it.hasNext()) {
            ChangeRequest r = (ChangeRequest)it.next();
            try {
                if (r.add) {
                    if (InstanceDataObject.find (f, r.instanceName (), r.instanceClass ()) == null) {
                        InstanceDataObject.create(f, r.instanceName(), r.instanceClass());
                    }
                } else {
                    InstanceDataObject.remove(f, r.instanceName(), r.instanceClass());
                }
            } catch (ClassNotFoundException ex) {
                TopManager.getDefault().getErrorManager().notify(ex);
            } catch (IOException ex) {
                TopManager.getDefault().getErrorManager().notify(ex);
            }
        }
    }
    
    /** A holder for Name of the key and corresponding action.*/
    private static class KeyActionPair implements InstanceCookie {
        private String name;
        private Action action;
        public KeyActionPair(String name, Action action) {
            this.name = name;
            this.action = action;
        }
        public String instanceName () {
            return name;
        }
        public Class instanceClass () 
            throws java.io.IOException, ClassNotFoundException {
            return action.getClass();
        }
        public Object instanceCreate () 
            throws java.io.IOException, ClassNotFoundException {
            return action;
        }
    }
    
    /** Change request are enqueued for batch processing in ShortcutsEditor
     * instance. 
     * @see applyChanges(java.util.List)
     */
    static class ChangeRequest extends KeyActionPair {
        /** true if this is request for addidng the pair,
         * false if the request if for deleting.
         */
        public boolean add;
        public ChangeRequest(KeyStroke key, Action action, boolean add) {
            super(Utilities.keyToString(key), action);
            this.add = add;
        }
    }
}
