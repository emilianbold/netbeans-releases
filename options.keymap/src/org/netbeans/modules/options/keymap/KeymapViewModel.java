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

package org.netbeans.modules.options.keymap;


import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.netbeans.modules.options.keymap.KeymapModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 *
 * @author Jan Jancura
 */
public class KeymapViewModel implements TreeModel, ShortcutsFinder {
    
    private Vector              listeners = new Vector ();
    private String              currentProfile;
    private KeymapModel         model = new KeymapModel ();
    // Map (String ("xx/yy") > List (Object (action)))
    // tree of actions in folders
    private Map                 categoryToActionsCache = new HashMap ();
    // Map (String (keymapName) > Map (ShortcutAction > Set (String (shortcut Ctrl+F)))).
    // contains modified shortcuts only
    private Map                 modifiedProfiles = new HashMap ();
    // Set (String (profileName)).
    private Set                 deletedProfiles = new HashSet ();
    // Map (String (keymapName) > Map (ShortcutAction > Set (String (shortcut Ctrl+F)))).
    private Map                 shortcutsCache = new HashMap ();
    
    static final ActionsComparator actionsComparator = new ActionsComparator ();
    
    
    /** 
     * Creates a new instance of KeymapModel 
     */
    public KeymapViewModel () {
        currentProfile = model.getCurrentProfile ();
    }

    
    // TreeModel ...............................................................

    public Object getRoot () {
        return "";
    }
    
    public Object getChild (Object parent, int index) {
        return getItems ((String) parent).get (index);
    }

    public int getChildCount (Object parent) {
        if (parent instanceof String)
            return getItems ((String) parent).size ();
        return 0;
    }

    public boolean isLeaf (Object node) {
        return !(node instanceof String);
    }

    public void valueForPathChanged (TreePath path, Object newValue) {}

    public int getIndexOfChild (Object parent, Object child) {
        return getItems ((String) parent).indexOf (child);
    }

    public void addTreeModelListener (TreeModelListener l) {
        listeners.add (l);
    }

    public void removeTreeModelListener (TreeModelListener l) {
        listeners.remove (l);
    }
    
    private void treeChanged () {
        final Vector v = (Vector) listeners.clone ();
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                TreeModelEvent tme = new TreeModelEvent (this, new Object[] {""});
                int i, k = v.size ();
                for (i = 0; i < k; i++)
                    ((TreeModelListener) v.get (i)).treeNodesChanged (tme);
            }
        });
    }
    
    private void nodeChanged (final TreePath path) {
        final Vector v = (Vector) listeners.clone ();
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                TreeModelEvent tme = new TreeModelEvent (this, path);
                int i, k = v.size ();
                for (i = 0; i < k; i++)
                    ((TreeModelListener) v.get (i)).treeNodesChanged (tme);
            }
        });
    }

    
    // ListModel ...............................................................

    // Map (String ("xx/yy") > Map ...)
    private Map                 categories;
    
    
    /**
     * Returns map of categories and subcategories.
     * Root: getCategories ().get ("")
     * Subcategories: getCategories ().get (category)
     *
     * Map (String (category name) > List (String (category name))).
     */
    public Map getCategories () {
        if (categories == null) {
            categories = new TreeMap ();
            List c = new ArrayList (model.getActionCategories ());
            Collections.sort (c);
            Iterator it = c.iterator ();
            while (it.hasNext ()) {
                String cn = (String) it.next ();
                String folderName = "";
                StringTokenizer st = new StringTokenizer (cn, "/");
                while (st.hasMoreTokens ()) {
                    String name = st.nextToken ();
                    List asd = (List) categories.get (folderName);
                    if (asd == null) {
                        asd = new ArrayList ();
                        categories.put (folderName, asd);
                    }
                    folderName = folderName.length () == 0 ?
                        name : folderName + '/' + name;
                    if (asd.isEmpty () || 
                        !asd.get (asd.size () - 1).equals (folderName)
                    )
                        asd.add (folderName);
                }
            }
        }
        return categories;
    }
    
    /**
     * Returns list of subcategories (String) for given category merged 
     * together with actions for give category.
     */
    public List getItems (String category) {
        List result = (List) categoryToActionsCache.get (category);
        if (result == null) {
            result = new ArrayList ();
            List ll = (List) getCategories ().get (category);
            if (ll != null)
                result.addAll (ll);
            List l = new ArrayList (model.getActions (category));
            Collections.sort (l, new ActionsComparator ());
            result.addAll (l);
            categoryToActionsCache.put (category, result);
            //S ystem.out.println("getItems " + category + " : " + result);
        }
        return result;
    }

//    public ListCellRenderer getListCellRenderer () {
//        return new KeymapListRenderer (this);
//    }
    
    
    // other methods ...........................................................

    List getProfiles () {
        Set result = new HashSet (model.getProfiles ());
        result.addAll (modifiedProfiles.keySet ());
        List r = new ArrayList (result);
        Collections.sort (r);
        return r;
    }
    
    boolean isCustomProfile (String profile) {
        return model.isCustomProfile (profile);
    }
    
    void deleteProfile (String profile) {
        if (model.isCustomProfile (profile)) {
            deletedProfiles.add (profile);
            modifiedProfiles.remove (profile);
        } else {
            Map m = model.getKeymapDefaults (profile);
            m = convertFromEmacs (m);
            modifiedProfiles.put (profile, m);
            treeChanged ();
        }
    }
    
    String getCurrentProfile () {
        return currentProfile;
    }
    
    void setCurrentProfile (String currentKeymap) {
        this.currentProfile = currentKeymap;
        treeChanged ();
    }
    
    void cloneProfile (String newProfileName) {
        Map result = new HashMap ();
        cloneProfile ("", result);
        modifiedProfiles.put (newProfileName, result);
    }
    
    private void cloneProfile (
        String category,        // name of currently resolved category
        Map result              // Map (ShortcutAction > Set (String (shortcut))) 
    ) {
        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof String)
                cloneProfile ((String) o, result);
            else {
                String[] shortcuts = getShortcuts ((ShortcutAction) o);
                result.put (o, new HashSet (Arrays.asList (shortcuts)));
            }
        }
    }
    
    public ShortcutAction findActionForShortcut (String shortcut) {
        return findActionForShortcut (shortcut, "");
    }
    
    private ShortcutAction findActionForShortcut (String shortcut, String category) {
        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof String) {
                ShortcutAction result = findActionForShortcut (shortcut, (String) o);
                if (result != null) return result;
                continue;
            }
            ShortcutAction action = (ShortcutAction) o;
            String[] shortcuts = getShortcuts (action);
            int i, k = shortcuts.length;
            for (i = 0; i < k; i++) {
                if (shortcuts [i].equals (shortcut)) return action;
                if (shortcuts [i].equals (shortcut + " ")) return action;
            }
        }
        return null;
    }

    public ShortcutAction findActionForId (final String actionId) {
        if (SwingUtilities.isEventDispatchThread ())
            return findActionForId (actionId, "");
        
        final ShortcutAction[] result = new ShortcutAction [1];
        try {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {
                    result [0] = findActionForId (actionId, "");
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault ().notify (ex);
        }
        return result [0];
    }
    
    private ShortcutAction findActionForId (String actionId, String category) {
        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof String) {
                ShortcutAction result = findActionForId (actionId, (String) o);
                if (result != null) return result;
                continue;
            }
            String id = ((ShortcutAction) o).getId ();
            if (actionId.equals (id)) 
                return (ShortcutAction) o;
        }
        return null;
    }
    
    public String[] getShortcuts (ShortcutAction action) {
        if (modifiedProfiles.containsKey (currentProfile)) {
            // find it in modified shortcuts
            Map actionToShortcuts = (Map) modifiedProfiles.
                get (currentProfile);
            if (actionToShortcuts.containsKey (action)) {
                Set s = (Set) actionToShortcuts.get (action);
                return (String[]) s.toArray (new String [s.size ()]);
            }
        }
        
        if (!shortcutsCache.containsKey (currentProfile)) {
            // read profile and put it to cache
            Map profileMap = convertFromEmacs (model.getKeymap (currentProfile));
            shortcutsCache.put (
                currentProfile, 
                profileMap
             );
        }
        Map profileMap = (Map) shortcutsCache.get (currentProfile);
        Set shortcuts = (Set) profileMap.get (action);
        if (shortcuts == null) return new String [0];
        return (String[]) shortcuts.toArray (new String [shortcuts.size ()]);
    }
    
    void addShortcut (TreePath path, String shortcut) {
        // delete old shortcut
        ShortcutAction action = findActionForShortcut (shortcut);
        if (action != null)
            removeShortcut (action, shortcut);
        action = (ShortcutAction) path.getLastPathComponent ();
        Set s = new HashSet ();
        s.add (shortcut);
        s.addAll (Arrays.asList (getShortcuts (action)));
        setShortcuts (action, s);
        nodeChanged (path);
    }
    
    public void setShortcuts (ShortcutAction action, Set shortcuts) {
        Map actionToShortcuts = (Map) modifiedProfiles.get (currentProfile);
        if (actionToShortcuts == null) {
            actionToShortcuts = new HashMap ();
            modifiedProfiles.put (currentProfile, actionToShortcuts);
        }
        actionToShortcuts.put (action, shortcuts);
    }
    
    void removeShortcut (TreePath path, String shortcut) {
        ShortcutAction action = (ShortcutAction) path.getLastPathComponent ();
        removeShortcut (action, shortcut);
        nodeChanged (path);
    }
    
    private void removeShortcut (ShortcutAction action, String shortcut) {
        Set s = new HashSet (Arrays.asList (getShortcuts (action)));
        s.remove (shortcut);
        Map actionToShortcuts = (Map) modifiedProfiles.get (currentProfile);
        if (actionToShortcuts == null) {
            actionToShortcuts = new HashMap ();
            modifiedProfiles.put (currentProfile, actionToShortcuts);
        }
        actionToShortcuts.put (action, s);
    }
    
    public void refreshActions () {
        categoryToActionsCache = new HashMap ();
        model.refreshActions ();
    }
    
    public void apply () {
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                Iterator it = modifiedProfiles.keySet ().iterator ();
                while (it.hasNext ()) {
                    String profile = (String) it.next ();
                    Map actionToShortcuts = (Map) modifiedProfiles.get (profile);
                    actionToShortcuts = convertToEmacs (actionToShortcuts);
                    model.changeKeymap (
                        profile, 
                        actionToShortcuts
                    );
                }
                it = deletedProfiles.iterator ();
                while (it.hasNext ()) {
                    String profile = (String) it.next ();
                    model.deleteProfile (profile);
                }
                model.setCurrentProfile (currentProfile);
                modifiedProfiles = new HashMap ();
                deletedProfiles = new HashSet ();
                shortcutsCache = new HashMap ();
                model = new KeymapModel ();
            }
        });
    }
    
    public boolean isChanged () {
        return (!modifiedProfiles.isEmpty ()) || !deletedProfiles.isEmpty ();
    }
    
    public void cancel () {
        modifiedProfiles = new HashMap ();
        deletedProfiles = new HashSet ();
        shortcutsCache = new HashMap ();
        setCurrentProfile (model.getCurrentProfile ());
        model = new KeymapModel ();
    }
    
    /**
     *
     */
    public String showShortcutsDialog() {
        final ShortcutsDialog d = new ShortcutsDialog ();
        d.init(this);
        DialogDescriptor descriptor = new DialogDescriptor (
            d,
            loc ("Add_Shortcut_Dialog"),
            true,
            new Object[] {
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.CANCEL_OPTION
            },
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            null, 
            d.getListener()
        );
        descriptor.setClosingOptions (new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        });
        descriptor.setAdditionalOptions (new Object [] {
            d.getBClear(), d.getBTab()
        });
        DialogDisplayer.getDefault ().notify (descriptor);
        if (descriptor.getValue () == DialogDescriptor.OK_OPTION)
            return d.getTfShortcut().getText ();
        return null;
    }
    
    /**
     * Converts Map (ShortcutAction > Set (String (shortcut Alt+Shift+P))) to 
     * Map (ShortcutAction > Set (String (shortcut AS-P))).
     */
    private static Map convertToEmacs (Map shortcuts) {
        Map result = new HashMap ();
        Iterator it = shortcuts.keySet ().iterator ();
        while (it.hasNext ()) {
            Object action = it.next ();
            Set sh = (Set) shortcuts.get (action);
            Set newSet = new HashSet ();
            Iterator it2 = sh.iterator ();
            while (it2.hasNext ()) {
                String s = (String) it2.next ();
                if (s.length () == 0) continue;
                KeyStroke[] ks = getKeyStrokes (s, " ");
                if (ks == null) 
                    continue; // unparsable shortcuts ignorred
                StringBuffer sb = new StringBuffer (
                    Utilities.keyToString (ks [0])
                );
                int i, k = ks.length;
                for (i = 1; i < k; i++)
                    sb.append (' ').append (Utilities.keyToString (ks [i]));
                newSet.add (sb.toString ());
            }
            result.put (action, newSet);
        }
        return result;
    }
    
    /**
     * Converts Map (ShortcutAction > Set (String (shortcut AS-P))) to 
     * Map (ShortcutAction > Set (String (shortcut Alt+Shift+P))).
     */
    private static Map convertFromEmacs (Map emacs) {
        Map result = new HashMap ();
        Iterator it = emacs.keySet ().iterator ();
        while (it.hasNext ()) {
            ShortcutAction action = (ShortcutAction) it.next ();
            Set emacsShortcuts = (Set) emacs.get (action);
            Iterator it2 = emacsShortcuts.iterator ();
            Set shortcuts = new HashSet ();
            while (it2.hasNext ()) {
                String emacsShortcut = (String) it2.next ();
                KeyStroke[] keyStroke = Utilities.stringToKeys (emacsShortcut);
                shortcuts.add (Utils.getKeyStrokesAsText (keyStroke, " "));
            }
            result.put (action, shortcuts);
        }
        return result;
    }
    
    /** 
     * Returns multi keystroke for given text representation of shortcuts
     * (like Alt+A B). Returns null if text is not parsable, and empty array
     * for empty string.
     */
    private static KeyStroke[] getKeyStrokes (String keyStrokes, String delim) {
        if (keyStrokes.length () == 0) return new KeyStroke [0];
        StringTokenizer st = new StringTokenizer (keyStrokes, delim);
        List result = new ArrayList ();
        while (st.hasMoreTokens ()) {
            String ks = st.nextToken ().trim ();
            KeyStroke keyStroke = Utils.getKeyStroke (ks);
            if (keyStroke == null) return null; // text is not parsable 
            result.add (keyStroke);
        }
        return (KeyStroke[]) result.toArray (new KeyStroke [result.size ()]);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (KeymapPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
    }
    
    
    // innerclasses ............................................................

    static class ActionsComparator implements Comparator {
        
        public int compare (Object o1, Object o2) {
            if (o1 instanceof String)
                if (o2 instanceof String)
                    return ((String) o1).compareTo ((String) o2);
                else
                    return 1;
            else
                if (o2 instanceof String)
                    return -1;
                else
                    return ((ShortcutAction) o1).getDisplayName ().compareTo (
                        ((ShortcutAction) o2).getDisplayName ()
                    );
        }
    }
}
