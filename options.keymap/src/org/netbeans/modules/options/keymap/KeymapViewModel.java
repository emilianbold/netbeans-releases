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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Jancura
 * @author Max Sauer
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.core.options.keymap.api.ShortcutsFinder.class)
public class KeymapViewModel extends DefaultTableModel implements ShortcutsFinder {
    
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
    private String searchText = "";
    
    
    /** 
     * Creates a new instance of KeymapModel 
     */
    public KeymapViewModel () {
        super(new String[]{
                    NbBundle.getMessage(KeymapViewModel.class, "Actions"), //NOI18N
                    NbBundle.getMessage(KeymapViewModel.class, "Shortcut"), //NOI18N
                    NbBundle.getMessage(KeymapViewModel.class, "Category"), //NOI18N
//                    NbBundle.getMessage(KeymapViewModel.class, "Scope") //NOI18N
                }, 0);
        currentProfile = model.getCurrentProfile ();
    }


    // DefaultTableModel
    @Override
    public Class getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return ActionHolder.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) //shotcuts cells editable
            return true;
        else
            return false;
    }

    
    void setSearchText(String searchText) {
        this.searchText = searchText;
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

    
    // other methods ...........................................................

    void update() {
        boolean caseSensitiveSearch = false;
        String searchTxt;

        if (searchText.matches(".*[A-Z].*")) { //NOI18N
            caseSensitiveSearch = true;
            searchTxt = searchText;
        } else {
            searchTxt = searchText.toLowerCase();
        }

        getDataVector().removeAllElements();
        for (String category : getCategories().get("")) {
            for (Object o : getItems(category)) {
                if (o instanceof ShortcutAction) {
                    ShortcutAction sca = (ShortcutAction) o;
                    String[] shortcuts = getShortcuts(sca);
                    String displayName = sca.getDisplayName();
//                    System.out.println("### " + sca.getDisplayName() + " " + searched(displayName.toLowerCase()));
                    if (searched(caseSensitiveSearch ? displayName : displayName.toLowerCase(), searchTxt)) {
                        if (shortcuts.length == 0)
                            addRow(new Object[]{new ActionHolder(sca, false), "", category}); // NOI18N
                        else
                            for (int i = 0; i < shortcuts.length; i++) {
                                String shortcut = shortcuts[i];
//                                String shownDisplayName = i == 0 ? displayName : displayName + " (alternative shortcut)";
                                addRow(new Object[]{
                                            i == 0 ? new ActionHolder(sca, false) : new ActionHolder(sca, true),
                                            shortcut, category,
                                        });
                            }
                    }
                }
            }
        }
        fireTableDataChanged();
    }

    private boolean searched(String displayName, String searchText) {
        if (displayName.length() == 0 || displayName.startsWith(searchText) || displayName.contains(searchText))
            return true;
        else
            return false;
    }

    List<String> getProfiles () {
        Set<String> result = new HashSet<String> (model.getProfiles ());
        result.addAll (modifiedProfiles.keySet ());
        List<String> r = new ArrayList<String> (result);
        Collections.sort (r);
        return r;
    }
    
    boolean isCustomProfile (String profile) {
        return model.isCustomProfile (profile);
    }
    
    void deleteOrRestoreProfile (String profile) {
        if (model.isCustomProfile (profile)) {
            deletedProfiles.add (profile);
            modifiedProfiles.remove (profile);
        } else {
            Map<ShortcutAction, Set<String>> m = model.getKeymapDefaults (profile);
            m = convertFromEmacs (m);
            modifiedProfiles.put (profile, m);
            update();
        }
    }
    
    String getCurrentProfile () {
        return currentProfile;
    }
    
    void setCurrentProfile (String currentKeymap) {
        this.currentProfile = currentKeymap;
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
        return findActionForShortcut (shortcut, "", false, null, "");
    }

    /**
     * Finds action with conflicting shortcut (or a prefix, for a multi-keybinding)
     * for a shortcut
     * @param shortcut the shortcut to look for
     * @return action with same shortcut, or shortcutprefix. If the prefix is same
     * but the rest of multi-keybinding is different, returns <code>null</code> (no conflict).
     */
    Set<ShortcutAction> findActionForShortcutPrefix(String shortcut) {
        Set<ShortcutAction> set = new HashSet<ShortcutAction>();
        if (shortcut.length() == 0) {
            return set;
        }
        //has to work with multi-keybinding properly,
        //ie. not allow 'Ctrl+J' and 'Ctrl+J X' at the same time
        if (shortcut.contains(" ")) {
            findActionForShortcut(shortcut.substring(0, shortcut.indexOf(' ')), "", true, set, shortcut);
        } else {
            findActionForShortcut(shortcut, "", true, set, shortcut);
        }
        return set;
    }

    private ShortcutAction findActionForShortcut (String shortcut, String category, boolean prefixSearch, Set<ShortcutAction> set, String completeMultikeySC) {
        //search in modified profiles first
        Map<ShortcutAction, Set<String>> map = modifiedProfiles.get(currentProfile);
        if (map != null)
            for (Entry<ShortcutAction, Set<String>> entry : map.entrySet()) {
                for (String sc : entry.getValue()) {
                    if (prefixSearch) {
                        if (sc.equals(shortcut) || (sc.startsWith(completeMultikeySC) && shortcut.equals(completeMultikeySC) && sc.contains(" "))) {
                            set.add(entry.getKey());
                        }
                    } else if (sc.equals(shortcut)) {
                        return entry.getKey();
                    }
                }
            }

        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof String) {
                ShortcutAction result = findActionForShortcut (shortcut, (String) o, prefixSearch, set, completeMultikeySC);
                if (result != null) {
                    if (!prefixSearch) {
                        return result;
                    }
                }
                continue;
            }
            ShortcutAction action = (ShortcutAction) o;
            String[] shortcuts = getShortcuts (action);
            int i, k = shortcuts.length;
            for (i = 0; i < k; i++) {
                if (prefixSearch) {
                    if (shortcuts[i].equals(shortcut) || (shortcuts[i].startsWith(completeMultikeySC) && shortcut.equals(completeMultikeySC) && shortcuts[i].contains(" "))) {
                        set.add(action);
                    }
                } else if (shortcuts[i].equals(shortcut)) {
                    return action;
                }
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
        
        Map<ShortcutAction, Set<String>> profileMap = getProfileMap(currentProfile);
        Set<String> shortcuts = profileMap.get (action);
        if (shortcuts == null) {
            return new String [0];
        }
        return shortcuts.toArray (new String [shortcuts.size ()]);
    }

    /**
     * Provides mapping of actions to their (non modified) shortcuts for a profile
     * @param profile given profile
     * @return the mapping
     */
    private Map<ShortcutAction, Set<String>> getProfileMap(String profile) {
        if (!shortcutsCache.containsKey (profile)) {
            // read profile and put it to cache
            Map<ShortcutAction, Set<String>> profileMap = convertFromEmacs (model.getKeymap (profile));
            shortcutsCache.put (
                profile,
                profileMap
             );
        }
        return shortcutsCache.get (profile);
    }

    /**
     * Set of all shortcuts used by current profile (including modifications)
     * In case there is a multikey keybinding used, its prefix is included
     * @return set of shortcuts
     */
    public Set<String> getAllCurrentlyUsedShortcuts() {
        Set<String> set = new LinkedHashSet<String>();
        //add modified shortcuts, if any
        Map<ShortcutAction, Set<String>> modMap = modifiedProfiles.get(currentProfile);
        if (modMap != null)
            for (Entry<ShortcutAction, Set<String>> entry : modMap.entrySet()) {
                for (String sc : entry.getValue()) {
                    set.add(sc);
                    if (sc.contains(" ")) {
                        set.add(sc.substring(0, sc.indexOf(' ')));
                    }
                }
            }
        //add default shortcuts
        for (Entry<ShortcutAction, Set<String>> entry : getProfileMap(currentProfile).entrySet()) {
            for (String sc : entry.getValue()) {
                    set.add(sc);
                    if (sc.contains(" ")) {
                        set.add(sc.substring(0, sc.indexOf(' ')));
                    }
                }
        }

        return set;
    }

    void addShortcut (ShortcutAction action, String shortcut) {
        // delete old shortcut
        ShortcutAction act = findActionForShortcut (shortcut);
        if (act != null && act != action) {
            removeShortcut (act, shortcut);
            this.fireTableDataChanged();
            update();
        }
        Set<String> s = new LinkedHashSet<String> ();
        s.addAll (Arrays.asList (getShortcuts (action)));
        s.add (shortcut);
        setShortcuts (action, s);
    }

    void revertShortcutsToDefault(ShortcutAction action) {
        Map<ShortcutAction, Set<String>> m = model.getKeymapDefaults (currentProfile);
        m = convertFromEmacs(m);
        Set<String> shortcuts = m.get(action);
        if (shortcuts == null)
            shortcuts = Collections.<String>emptySet(); //this action has no default shortcut
        //lets search for conflicting SCs
        Set<ShortcutAction> conflictingActions = new HashSet<ShortcutAction>();
        for(String sc : shortcuts) {
            ShortcutAction ac = findActionForShortcut(sc);
            if (ac != null && !ac.equals(action)) {
                conflictingActions.add(ac);
            }
        }
        if(conflictingActions.size() > 0) {
            if(overrideAll(conflictingActions)) {
                for (String sc : shortcuts) {
                    ShortcutAction sca = findActionForShortcut(sc);
                    removeShortcut(sca, sc);
                }
            } else {
                return;
            }
        }

        setShortcuts(action, shortcuts);
        update();
    }

    private boolean overrideAll(Set<ShortcutAction> actions) {
        JPanel innerPane = new JPanel();
        StringBuffer display = new StringBuffer();
        for(ShortcutAction sc : actions) {
            display.append(" '" + sc.getDisplayName() + "'<br>"); //NOI18N
        }

        innerPane.add(new JLabel(NbBundle.getMessage(KeymapViewModel.class, "Override_All", display))); //NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                innerPane,
                NbBundle.getMessage(KeymapViewModel.class, "Conflicting_Shortcut_Dialog"), //NOI18N
                true,
                DialogDescriptor.YES_NO_OPTION,
                null,
                null);
        DialogDisplayer.getDefault().notify(descriptor);

        if (descriptor.getValue().equals(DialogDescriptor.YES_OPTION))
            return true;
        else return false;
    }

    public void setShortcuts (ShortcutAction action, Set<String> shortcuts) {
        Map<ShortcutAction, Set<String>> actionToShortcuts = modifiedProfiles.get (currentProfile);
        if (actionToShortcuts == null) {
            actionToShortcuts = new HashMap<ShortcutAction, Set<String>> ();
            modifiedProfiles.put (currentProfile, actionToShortcuts);
        }
        actionToShortcuts.put (action, shortcuts);
    }

    public void removeShortcut (ShortcutAction action, String shortcut) {
        Set<String> s = new LinkedHashSet<String> (Arrays.asList (getShortcuts (action)));
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

    Map<String, Map<ShortcutAction, Set<String>>> getModifiedProfiles() {
        return modifiedProfiles;
    }

    Set<String> getDeletedProfiles() {
        return deletedProfiles;
    }

    void setModifiedProfiles(Map<String, Map<ShortcutAction, Set<String>>> mp) {
        this.modifiedProfiles = mp;
    }

    void setDeletedProfiles(Set<String> dp) {
        this.deletedProfiles = dp;
    }
    
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
            Set<String> shortcuts = new LinkedHashSet<String> ();
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
