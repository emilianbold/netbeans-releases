/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Collection;
import java.util.List;
import javax.swing.text.Keymap;
import javax.swing.*;

import org.openide.*;
import org.openide.awt.Actions;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;


/**
 * Working with shortcuts - retreiving, saving etc. The old .keys
 * files manipulation is also here for backward compatibility.
 * @author  dstrupl
 */
final class ShortcutsFolder extends FolderInstance {

    /** This reference is initialized in initShortcuts().*/
    static ShortcutsFolder shortcutsFolder = null;
    
    /** Folder name under the system folder.*/
    private static final String SHORTCUTS_FOLDER = "Shortcuts"; // NOI18N

    /** Key for value in action keeping path to original file, when shortcut is saved as '.shadow' file. */
    private static final String KEY_ORIGINAL_FILE_PATH = "originalFilePath"; // NOI18N
    

    /** Creates new ShortcutsFolder - package private for unit tests only */
    ShortcutsFolder(DataFolder f) {
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
            String keyname = cookies[i].instanceName();
            KeyStroke stroke = Utilities.stringToKey (keyname);
            if (stroke == null) {
                ErrorManager.getDefault ().
                    getInstance ("org.netbeans.core.ShortcutsFolder"). // NOI18N
                    log (ErrorManager.WARNING, "Warning: unparsable keystroke: " + keyname); // NOI18N
                continue;
            }
            Action action = (Action)cookies[i].instanceCreate();
            map.put (stroke, action);
        }
        
        Keymap globalMap = (Keymap)Lookup.getDefault().lookup(Keymap.class);
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
    protected InstanceCookie acceptDataObject(final DataObject dob) {
        InstanceCookie ic = super.acceptDataObject(dob);
        if (ic != null) {
            try {
                final Object o = ic.instanceCreate();
                if (o instanceof Action) {
                    // XXX #37306
                    if(dob instanceof DataShadow) {
                        // bugfix #41500, replan puting to EQ
                        Mutex.EVENT.writeAccess (new Runnable () {
                            public void run () {
                                ((Action)o).putValue(KEY_ORIGINAL_FILE_PATH, ((DataShadow)dob).getOriginal().getPrimaryFile().getPath());
                            }
                        });
                    }
                    KeyActionPair pair = new KeyActionPair(dob.getName(), (Action)o);
                    return pair;
                }
            } catch (IOException x) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, x);
            } catch (ClassNotFoundException x) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, x);
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
        Keymap map = (Keymap)Lookup.getDefault().lookup(Keymap.class);
        Action action = map.getAction (stroke);
        if (action != null) {
            return getKeyText (stroke) + " [" + getActionBasicName(action) + "]"; // NOI18N
        } else {
            return getKeyText(stroke);
        }
    }
    
    private static String getActionBasicName(Action action) {
        String name = (String)action.getValue(Action.NAME);
        if (name == null) {
            return "???"; // NOI18N
        }
        name = Actions.cutAmpersand(name);
        return Utilities.replaceString (name, "...", ""); // remove trailing "..."  // NOI18N
    }

    /**
     * Used in ActionsPanel.
     */
    static String getActionName (Action action) {
        String name = getActionBasicName(action);
        
        Keymap map = (Keymap)Lookup.getDefault().lookup(Keymap.class);
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
            return ""; // NOI18N
        }
        String modifText = java.awt.event.KeyEvent.getKeyModifiersText(stroke.getModifiers ());
        if ("".equals (modifText)) return java.awt.event.KeyEvent.getKeyText(stroke.getKeyCode ()); // NOI18N
        else return modifText + "+" + java.awt.event.KeyEvent.getKeyText(stroke.getKeyCode ()); // NOI18N
    }

    /** Creates an instance of ShortcutsFolder.
     */
    public static void initShortcuts() {
        DataFolder f = NbPlaces.getDefault().findSessionFolder(SHORTCUTS_FOLDER);
        shortcutsFolder = new ShortcutsFolder(f);
    }

    /** This should update the global key map. */
    public static void refreshGlobalMap() {
        if (shortcutsFolder != null) {
            shortcutsFolder.recreate();
        }
    }

    public static void waitShortcutsFinished () {
        if (shortcutsFolder != null) {
            shortcutsFolder.waitFinished ();
        }
    }
    
    /** Applies changes described by a sequence of ChangeRequests 
     * to the shortcuts folder.
     * @param List changes - the elements of the List are ChangeRequests
     */
    public static void applyChanges(java.util.List changes) {
        FileObject fo = Repository.getDefault().getDefaultFileSystem()
            .getRoot().getFileObject(SHORTCUTS_FOLDER);
        
        DataFolder f = DataFolder.findFolder(fo);
        Iterator it = changes.listIterator();
        while (it.hasNext()) {
            ChangeRequest r = (ChangeRequest)it.next();
            try {
                // XXX #37306 Added special handling of '.shadow' files.
                Action action = (Action)r.instanceCreate();
                String originalFilePath = (String)action.getValue(KEY_ORIGINAL_FILE_PATH);
                if(originalFilePath == null) { // It is '.instance' file
                    if (r.add) {
                        if (InstanceDataObject.find (f, r.instanceName (), r.instanceClass ()) == null) {
                            // bugfix #37064, bind the actual object instead of a default instance
                            //Bugfix #37637 Create data shadow instead of IDO .settings file
                            DataObject actionDO = findForAction(null, action);
                            if (actionDO != null) {
                                //DO for action found create DataShadow
                                DataObject shadow = actionDO.createShadow(f);
                                //Rename to shortcut code
                                shadow.rename(r.instanceName());
                            } else {
                                //create .instance file
                                InstanceDataObject.create(f, r.instanceName(), r.instanceCreate().getClass().getName());
                            }
                        }
                    } else {
                        String instanceName = r.instanceName();
                        ArrayList arr = new ArrayList ();
                        arr.add (instanceName);
                        arr.addAll (Arrays.asList (getPermutations (instanceName, (Utilities.getOperatingSystem() & Utilities.OS_MAC) !=0)));
                        for (Iterator iter = arr.iterator(); iter.hasNext(); ) {
                            String name = (String)iter.next ();
                            DataObject[] ch = f.getChildren();
                            for (int j = 0; j < ch.length; j++) {
                                if (ch[j].getName ().equals (name)) {
                                    ch[j].delete ();
                                    break;
                                }
                            }
                        }
                    }
                } else { // It is '.shadow' file
                    FileObject root = f.getPrimaryFile();
                    if (r.add) {
                        FileObject foAdd = root.getFileObject(r.instanceName(), "shadow"); // NOI18N
                        if(foAdd == null) {
                            foAdd = FileUtil.createData(root, r.instanceName() + ".shadow"); // NOI18N
                        }
                        foAdd.setAttribute("originalFile", originalFilePath); // NOI18N
                    } else {
                        FileObject foRemove = root.getFileObject(r.instanceName(), "shadow"); // NOI18N
                        
                        if(foRemove != null) {
                            foRemove.delete();
                        }
                        String[] permutations = getPermutations(r.instanceName(), (Utilities.getOperatingSystem() & Utilities.OS_MAC) != 0);
                        //We may be deleting a wildcard keystroke, and/or the
                        //order defined in the layer may not be the same as
                        //what we were fed by the shortcuts editor, so search
                        //all the possible variants
                        for (int i=0; i < permutations.length; i++) {
                            foRemove = root.getFileObject(permutations[i], "shadow");
                            if (foRemove != null) {
                                foRemove.delete();
                                break;
                            }
                            
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    /**
     * There is no required ordering of key modifiers (C, M, S, A), and the
     * D (default) wildcard character can map to either C or M depending on 
     * the platform.  So when we need to delete a keybinding, the editor has
     * given us one possible ordering, but not necessarily the correct one; it
     * has also given us a hard keybinding, but the key may really be bound to
     * D.  So, for "MAS-F5" (meta-alt-shift F5) on the pc, we need to check
     * MSA-F5, SMA-F5, SAM-F5, AMS-F5, ASM-F5; on the mac, we also need to check
     * the same permutations of DAS-F5, since it may be registered with the 
     * wildcard character.
     * <p>
     * Finally, for each permutation, it is legal to separate characters with
     * dashes - so for each permutation, we must also check for a hyphenated
     * variant - i.e. for MAS-F5, we must check M-A-S-F5.  Note that mixed
     * hyphenation (M-AS-F5) is not supported.  It either is or it isn't.
     *
     */
    static String[] getPermutations (String name, boolean mac) {
        //IMPORTANT: THERE IS A COPY OF THE SAME CODE IN 
        //org.netbeans.modules.editor.options.KeyBindingsMIMEOptionFile
        //ANY CHANGES MADE HERE SHOULD ALSO BE MADE THERE!
        String key = mac ?
            "M" : "C"; //NOI18N
        
        String ctrlWildcard = "D"; //NOI18N
        
        String altKey = mac ?
            "C" : "A"; //NOI18N
        
        String altWildcard = "O"; //NOI18N
        
        
        int pos = name.lastIndexOf ("-"); //NOI18N
        if (pos == -1) {
            //#49590 - key bindings such as F11 have no modifiers
            return new String[] { name };
        }
        String keyPart = name.substring (pos);
        String modsPart = Utilities.replaceString (name.substring (0, pos), "-", ""); //NOI18N
        if (modsPart.length() > 1) {
            Collection perms = new HashSet(modsPart.length() * modsPart.length());
            int idx = name.indexOf(key);
            if (idx != -1) {
                //First, try with the wildcard key.  Remove all hyphens - we'll
                //put them back later
                StringBuffer sb = new StringBuffer(modsPart);
                sb.replace(idx, idx+1, ctrlWildcard);
                perms.add (sb.toString() + keyPart);
                getAllPossibleOrderings (sb.toString(), keyPart, perms);
                createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                idx = name.indexOf (altKey);
                if (idx != -1) {
                    sb.replace (idx, idx+1, altWildcard);
                    perms.add (sb.toString() + keyPart);
                    getAllPossibleOrderings (sb.toString(), keyPart, perms);
                    createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                } else {
                    idx = name.indexOf(altWildcard);
                    if (idx != -1) {
                        sb.replace (idx, idx+1, altKey);
                        perms.add (sb.toString() + keyPart);
                        getAllPossibleOrderings (sb.toString(), keyPart, perms);
                        createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                    }
                }                
            } else {
                idx = name.indexOf (ctrlWildcard); //NOI18N
                if (idx != -1) {
                    StringBuffer sb = new StringBuffer(modsPart);
                    sb.replace(idx, idx+1, key);
                    perms.add (sb.toString() + keyPart);
                    getAllPossibleOrderings (sb.toString(), keyPart, perms);
                    createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                    idx = name.indexOf (altKey);
                    if (idx != -1) {
                        sb.replace (idx, idx+1, altWildcard);
                        perms.add (sb.toString() + keyPart);
                        getAllPossibleOrderings (sb.toString(), keyPart, perms);
                    } else {
                        idx = name.indexOf(altWildcard);
                        if (idx != -1) {
                            sb.replace (idx, idx+1, altKey);
                            perms.add (sb.toString() + keyPart);
                            getAllPossibleOrderings (sb.toString(), keyPart, perms);
                            createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                        }
                    }                    
                }
            }
            
            idx = name.indexOf (altKey);
            if (idx != -1) {
                StringBuffer sb = new StringBuffer(modsPart);
                sb.replace (idx, idx+1, altWildcard);
                perms.add (sb.toString() + keyPart);
                getAllPossibleOrderings (sb.toString(), keyPart, perms);
                createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
            } else {
                StringBuffer sb = new StringBuffer(modsPart);
                idx = name.indexOf(altWildcard);
                if (idx != -1) {
                    sb.replace (idx, idx+1, altKey);
                    perms.add (sb.toString() + keyPart);
                    getAllPossibleOrderings (sb.toString(), keyPart, perms);
                    createHyphenatedPermutation(sb.toString().toCharArray(), perms, keyPart);
                }
            }
            
            getAllPossibleOrderings (modsPart, keyPart, perms);
            createHyphenatedPermutation(modsPart.toCharArray(), perms, keyPart);
            return (String[]) perms.toArray(new String[perms.size()]);
        } else {
            return key.equals (modsPart) ?
                new String[] {ctrlWildcard + keyPart} : altKey.equals(modsPart) ?
                    new String[]{altWildcard + keyPart} : altWildcard.equals(modsPart) ? 
                    new String[] {altKey + keyPart} : 
                    new String[0];
        }
    }
    
    /**
     * Retrieves all the possible orders for the passed in string, and puts them
     * in the passed collection, appending <code>toAppend</code> to each.
     */
    static void getAllPossibleOrderings (String s, String toAppend, final Collection store) {
        //IMPORTANT: THERE IS A COPY OF THE SAME CODE IN 
        //org.netbeans.modules.editor.options.KeyBindingsMIMEOptionFile
        //ANY CHANGES MADE HERE SHOULD ALSO BE MADE THERE!
        char[] c = s.toCharArray();
        mutate (c, store, 0, toAppend);
        String[] result = (String[]) store.toArray(new String[store.size()]);
    }   
    
    /**
     * Recursively generates all possible orderings of the passed char array
     */
    private static void mutate(char[] c, Collection l, int n, String toAppend) {
        //IMPORTANT: THERE IS A COPY OF THE SAME CODE IN 
        //org.netbeans.modules.editor.options.KeyBindingsMIMEOptionFile
        //ANY CHANGES MADE HERE SHOULD ALSO BE MADE THERE!
        if (n == c.length) {
            l.add (new String(c) + toAppend);
            createHyphenatedPermutation(c, l, toAppend);
            return;
        }
        //XXX could be optimized to eliminate duplicates
        for (int i=0; i < c.length; i++) {
            char x = c[i];
            c[i] = c[n];
            c[n] = x;
            if (n < c.length) { 
                mutate (c, l, n+1, toAppend);
            } 
        }
    }
    
    /**
     * Inserts "-" characters between each character in the char array and
     * adds the result + toAppend to the collection.
     */
    static void createHyphenatedPermutation (char[] c, Collection l, String toAppend) {
        //IMPORTANT: THERE IS A COPY OF THE SAME CODE IN 
        //org.netbeans.modules.editor.options.KeyBindingsMIMEOptionFile
        //ANY CHANGES MADE HERE SHOULD ALSO BE MADE THERE!
        if (c.length == 1) {
            return;
        }
        StringBuffer sb = new StringBuffer (new String(c));
        for (int i=c.length-1; i >= 1; i-=1) {
            sb.insert (i, '-');
        }
        sb.append (toAppend);
        l.add (sb.toString());
    }
        

    private static DataObject findForAction (DataFolder actionsFolder, Action a) {
        if (actionsFolder == null) {
            actionsFolder = NbPlaces.getDefault().actions ();
        }
        DataObject[] actionsChildren = actionsFolder.getChildren ();
        for (int i = 0; i < actionsChildren.length; i++) {
            if (actionsChildren[i] instanceof DataFolder) {
                DataObject obj = findForAction ((DataFolder)actionsChildren[i], a);
                if (obj != null) {
                    return obj;
                }
            } else {
                InstanceCookie ic = (InstanceCookie)actionsChildren[i].getCookie(InstanceCookie.class);
                if (ic != null) {
                    Object obj = null;
                    try {
                        obj = ic.instanceCreate();
                    } catch (java.io.IOException exc) {
                    } catch (ClassNotFoundException exc) {
                    }
                    if ((obj != null) && a.equals(obj)) {
                        return actionsChildren[i];
                    }
                }
            }
        }
        return null;
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
