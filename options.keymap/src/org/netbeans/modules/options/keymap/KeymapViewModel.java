/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.options.keymap;


import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    
    private Vector<TreeModelListener> listeners = new Vector<TreeModelListener> ();
    private String              currentProfile;
    private KeymapModel         model = new KeymapModel ();
    // Map (String ("xx/yy") > List (Object (action)))
    // tree of actions in folders
    private Map<String, List<Object>> categoryToActionsCache = 
            new HashMap<String, List<Object>> ();
    // Profile name to map of action to set of shortcuts
    private Map<String, Map<ShortcutAction, Set<String>>> modifiedProfiles = 
            new HashMap<String, Map<ShortcutAction, Set<String>>> ();
    // Set (String (profileName)).
    private Set<String> deletedProfiles = new HashSet<String> ();
    // Map (String (keymapName) > Map (ShortcutAction > Set (String (shortcut Ctrl+F)))).
    private Map<String, Map<ShortcutAction, Set<String>>> shortcutsCache = 
            new HashMap<String, Map<ShortcutAction, Set<String>>> ();
    
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
                TreeModelEvent tme = new TreeModelEvent (this, new TreePath(getRoot()));
                int i, k = v.size ();
                for (i = 0; i < k; i++) {
                    ((TreeModelListener) v.get (i)).treeStructureChanged (tme);
                }
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
    private Map<String, List<String>> categories;
    
    
    /**
     * Returns map of categories and subcategories.
     * Root: getCategories ().get ("")
     * Subcategories: getCategories ().get (category)
     *
     * Map (String (category name) > List (String (category name))).
     */
    public Map<String, List<String>> getCategories () {
        if (categories == null) {
            categories = new TreeMap<String, List<String>> ();
            List<String> c = new ArrayList<String> (model.getActionCategories ());
            Collections.sort (c);
            for (String cn: c) {
                String folderName = "";
                StringTokenizer st = new StringTokenizer (cn, "/");
                while (st.hasMoreTokens ()) {
                    String name = st.nextToken ();
                    List<String> asd = categories.get (folderName);
                    if (asd == null) {
                        asd = new ArrayList<String> ();
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
    public List<Object/*Union2<String,ShortcutAction>*/> getItems (String category) {
        List<Object> result = categoryToActionsCache.get (category);
        if (result == null) {
            result = new ArrayList<Object> ();
            List<String> ll = getCategories ().get (category);
            if (ll != null)
                result.addAll (ll);
            List<ShortcutAction> l = new ArrayList<ShortcutAction> (model.getActions (category));
            Collections.<ShortcutAction>sort (l, new ActionsComparator ());
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
        Set<String> result = new HashSet<String> (model.getProfiles ());
        result.addAll (modifiedProfiles.keySet ());
        List<String> r = new ArrayList<String> (result);
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
            Map<ShortcutAction, Set<String>> m = model.getKeymapDefaults (profile);
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
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>> ();
        cloneProfile ("", result);
        modifiedProfiles.put (newProfileName, result);
    }
    
    private void cloneProfile (
        String category,        // name of currently resolved category
        Map<ShortcutAction, Set<String>> result
    ) {
        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof String) {
                cloneProfile ((String) o, result);
            } else {
                String[] shortcuts = getShortcuts ((ShortcutAction) o);
                result.put ((ShortcutAction)o, new HashSet<String> (Arrays.asList (shortcuts)));
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
            Map<ShortcutAction, Set<String>> actionToShortcuts = modifiedProfiles.
                get (currentProfile);
            if (actionToShortcuts.containsKey (action)) {
                Set<String> s = actionToShortcuts.get (action);
                return s.toArray (new String [s.size ()]);
            }
        }
        
        if (!shortcutsCache.containsKey (currentProfile)) {
            // read profile and put it to cache
            Map<ShortcutAction, Set<String>> profileMap = convertFromEmacs (model.getKeymap (currentProfile));
            shortcutsCache.put (
                currentProfile, 
                profileMap
             );
        }
        Map<ShortcutAction, Set<String>> profileMap = shortcutsCache.get (currentProfile);
        Set<String> shortcuts = profileMap.get (action);
        if (shortcuts == null) {
            return new String [0];
        }
        return shortcuts.toArray (new String [shortcuts.size ()]);
    }
    
    void addShortcut (TreePath path, String shortcut) {
        // delete old shortcut
        ShortcutAction action = findActionForShortcut (shortcut);
        if (action != null) {
            removeShortcut (action, shortcut);
        }
        action = (ShortcutAction) path.getLastPathComponent ();
        Set<String> s = new HashSet<String> ();
        s.add (shortcut);
        s.addAll (Arrays.asList (getShortcuts (action)));
        setShortcuts (action, s);
        nodeChanged (path);
    }
    
    public void setShortcuts (ShortcutAction action, Set<String> shortcuts) {
        Map<ShortcutAction, Set<String>> actionToShortcuts = modifiedProfiles.get (currentProfile);
        if (actionToShortcuts == null) {
            actionToShortcuts = new HashMap<ShortcutAction, Set<String>> ();
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
        Set<String> s = new HashSet<String> (Arrays.asList (getShortcuts (action)));
        s.remove (shortcut);
        setShortcuts(action, s);
    }
    
    public void refreshActions () {
        categoryToActionsCache = new HashMap<String, List<Object>> ();
        model.refreshActions ();
    }
    
    public void apply () {
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                for (String profile: modifiedProfiles.keySet()) {
                    Map<ShortcutAction, Set<String>> actionToShortcuts = modifiedProfiles.get (profile);
                    actionToShortcuts = convertToEmacs (actionToShortcuts);
                    model.changeKeymap (
                        profile, 
                        actionToShortcuts
                    );
                }
                for (String profile: deletedProfiles) {
                    model.deleteProfile (profile);
                }
                model.setCurrentProfile (currentProfile);
                modifiedProfiles = new HashMap<String, Map<ShortcutAction, Set<String>>> ();
                deletedProfiles = new HashSet<String> ();
                shortcutsCache = new HashMap<String, Map<ShortcutAction, Set<String>>> ();
                model = new KeymapModel ();
            }
        });
    }
    
    public boolean isChanged () {
        return (!modifiedProfiles.isEmpty ()) || !deletedProfiles.isEmpty ();
    }
    
    public void cancel () {
        modifiedProfiles = new HashMap<String, Map<ShortcutAction, Set<String>>> ();
        deletedProfiles = new HashSet<String> ();
        shortcutsCache = new HashMap<String, Map<ShortcutAction, Set<String>>> ();
        setCurrentProfile (model.getCurrentProfile ());
        model = new KeymapModel ();
    }
    
    /**
     *
     */
    public String showShortcutsDialog() {
        final ShortcutsDialog d = new ShortcutsDialog ();
        d.init(this);
        final DialogDescriptor descriptor = new DialogDescriptor (
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
        descriptor.setValid(d.isShortcutValid());
        d.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null || ShortcutsDialog.PROP_SHORTCUT_VALID.equals(evt.getPropertyName())) {
                    descriptor.setValid(d.isShortcutValid());
                }
            }
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
    private static Map<ShortcutAction, Set<String>> convertToEmacs (Map<ShortcutAction, Set<String>> shortcuts) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>> ();
        for (Map.Entry<ShortcutAction, Set<String>> entry: shortcuts.entrySet()) {
            ShortcutAction action = entry.getKey();
            Set<String> newSet = new HashSet<String> ();
            for (String s: entry.getValue()) {
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
    private static Map<ShortcutAction, Set<String>> convertFromEmacs (Map<ShortcutAction, Set<String>> emacs) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>> ();
        for (Map.Entry<ShortcutAction, Set<String>> entry: emacs.entrySet()) {
            ShortcutAction action = entry.getKey();
            Set<String> shortcuts = new HashSet<String> ();
            for (String emacsShortcut: entry.getValue()) {
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
        List<KeyStroke> result = new ArrayList<KeyStroke> ();
        while (st.hasMoreTokens ()) {
            String ks = st.nextToken ().trim ();
            KeyStroke keyStroke = Utils.getKeyStroke (ks);
            if (keyStroke == null) return null; // text is not parsable 
            result.add (keyStroke);
        }
        return result.toArray (new KeyStroke [result.size ()]);
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
