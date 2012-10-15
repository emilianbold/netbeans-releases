/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.openide.ErrorManager;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Bridge to old layers based system.
 *
 * @author Jan Jancura
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.core.options.keymap.spi.KeymapManager.class)
public class LayersBridge extends KeymapManager {
    
    /**
     * Extension for DataObjects, which cause an action to be removed from the parent (general) keymap.
     */
    private static final String EXT_REMOVED = "removed"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(LayersBridge.class.getName());
    
    static final String         KEYMAPS_FOLDER = "Keymaps";
    private static final String SHORTCUTS_FOLDER = "Shortcuts";
    
    private static final String LAYERS_BRIDGE = "LayersBridge";
    
    /** Map (GlobalAction > DataObject). */
    private Map<GlobalAction, DataObject> actionToDataObject = 
            new HashMap<GlobalAction, DataObject> ();
    /** Map (String (folderName) > Set (GlobalAction)). */
    private Map<String, Set<ShortcutAction>> categoryToActions;
    /** Set (GlobalAction). */
    private Set<GlobalAction> actions = new HashSet<GlobalAction> ();
    
    public LayersBridge() {
        super(LAYERS_BRIDGE);
    }
    
    /**
     * Returns Map (String (folderName) > Set (GlobalAction)).
     */
    public synchronized Map<String, Set<ShortcutAction>> getActions () {
        if (categoryToActions == null) {
            categoryToActions = new HashMap<String, Set<ShortcutAction>> ();
            initActions ("Actions", null);               // NOI18N
            categoryToActions.remove ("Hidden");                       // NOI18N
            categoryToActions = Collections.unmodifiableMap (categoryToActions);
        }
        return categoryToActions;
    }

    private void initActions (String folder, String category) {
        FileObject fo = FileUtil.getConfigFile(folder);
        if (fo == null) return;
        DataFolder root = DataFolder.findFolder (fo);
        Enumeration<DataObject> en = root.children ();
        while (en.hasMoreElements ()) {
            DataObject dataObject = en.nextElement ();
            if (dataObject instanceof DataFolder)
                initActions ((DataFolder) dataObject, null, category);
        }
    }
    
    private void initActions (
        DataFolder folder, 
        String folderName, 
        String category
    ) {
        
        // 1) reslove name
        String name = folder.getName ();
        if (category != null)
            name = category;
        else {
            String bundleName = (String) folder.getPrimaryFile ().getAttribute 
                ("SystemFileSystem.localizingBundle");
            if (bundleName != null)
                try {
                    name = NbBundle.getBundle (bundleName).getString (
                        folder.getPrimaryFile ().getPath ()
                    );
                } catch (MissingResourceException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            if (folderName != null) 
                name = folderName + '/' + name;
        }
        
        Enumeration en = folder.children ();
        while (en.hasMoreElements ()) {
            DataObject dataObject = (DataObject) en.nextElement ();
            if (dataObject instanceof DataFolder) {
                initActions ((DataFolder) dataObject, name, category);
                continue;
            }
            GlobalAction action = createAction (dataObject, name);
            if (action == null) continue;
            if (actions.contains (action)) continue;
            actions.add (action);
            
            // add to actions (Map (String (folderName) > Set (GlobalAction))).
            Set<ShortcutAction> a = categoryToActions.get (name);
            if (a == null) {
                a = new HashSet<ShortcutAction> ();
                categoryToActions.put (name, a);
            }
            a.add (action);
            
            while (dataObject instanceof DataShadow)
                dataObject = ((DataShadow) dataObject).getOriginal ();
            
            actionToDataObject.put (action, dataObject);
        }
    }
    
    private List<String> keymapNames;
    private Map<String, String> keymapDisplayNames;

    private void refreshKeymapNames() {
        DataFolder root = getRootFolder(KEYMAPS_FOLDER, null);
        Enumeration en = root.children(false);
        keymapNames = new ArrayList<String>();
        keymapDisplayNames = new HashMap<String, String>();
        while (en.hasMoreElements()) {
            FileObject f = ((DataObject) en.nextElement()).getPrimaryFile();
            if (f.isFolder()) {
                String name = f.getNameExt();
                String displayName;

                try {
                    displayName = f.getFileSystem().getStatus().annotateName(name, Collections.singleton(f));
                } catch (FileStateInvalidException fsie) {
                    // ignore
                    displayName = name;
                }
                keymapNames.add(name);
                keymapDisplayNames.put(name, displayName);
            }
        }
        if (keymapNames.isEmpty()) {
            keymapNames.add("NetBeans"); //NOI18N
        }
    }

    public List<String> getProfiles() {
        if (keymapNames == null) {
            refreshKeymapNames();
        }
        return Collections.unmodifiableList(keymapNames);
    }
    
    public @Override String getProfileDisplayName(String profileName) {
        String displayName = keymapDisplayNames.get(profileName);
        return displayName == null ? profileName : displayName;
    }
    
    /** Profile to Map of GlobalAction to set of shortcuts. */
    private Map<String, Map<ShortcutAction, Set<String>>> keymaps = 
            new HashMap<String, Map<ShortcutAction, Set<String>>> ();
    
    /**
     * The base keymap, shared for all profiles. Used as a baseline when generating
     * 'removed' instructions for a profile.
     */
    private volatile Map<ShortcutAction, Set<String>> baseKeyMap;
    
    /**
     * Returns Map (GlobalAction > Set (String (shortcut))).
     */
    public Map<ShortcutAction, Set<String>> getKeymap (String profile) {
        if (!keymaps.containsKey (profile)) {
            DataFolder root = getRootFolder (SHORTCUTS_FOLDER, null);
            Map<ShortcutAction, Set<String>> m = readKeymap (root);
            root = getRootFolder (KEYMAPS_FOLDER, profile);
            overrideWithKeyMap(m, readKeymap(root), profile);
            m.remove(REMOVED);
            keymaps.put (profile, m);
        }
        return Collections.unmodifiableMap (keymaps.get (profile));
    }
        
    private Map<ShortcutAction, Set<String>> getBaseKeyMap() {
        if (baseKeyMap == null) {
            DataFolder root = getRootFolder (SHORTCUTS_FOLDER, null);
            Map<ShortcutAction, Set<String>> m = readKeymap (root);
            baseKeyMap = m;
        }
        return baseKeyMap;
    }
    
    /**
     * Overrides the base shortcut map with contents of the Keymap. If keymap specifies
     * a shortcut which is already used in the 'base', the shortcut mapping is removed from the base
     * and only the keymap mapping will prevail.
     * 
     * @param base base keymap
     * @param keyMap override keymap, from the profile
     * @return 
     */
    private Map<ShortcutAction, Set<String>> overrideWithKeyMap(Map<ShortcutAction, Set<String>> base,
            Map<ShortcutAction, Set<String>> keyMap, String profile) {
        Set<String> overrideKeyStrokes = new HashSet<String>();
        Map<String, ShortcutAction> shortcuts = null;
        
        for (ShortcutAction a : keyMap.keySet()) {
            overrideKeyStrokes.addAll(keyMap.get(a));
        }
        for (Iterator<Map.Entry<ShortcutAction,Set<String>>> it = base.entrySet().iterator();
                it.hasNext();) {
            Map.Entry<ShortcutAction,Set<String>> en = it.next();
            Set<String> keys = en.getValue();

            if (LOG.isLoggable(Level.FINER)) {
                for (String s : keys) {
                    if (overrideKeyStrokes.contains(s)) {
                        if (shortcuts == null) {
                            shortcuts = shortcutToAction(keyMap);
                        }
                        ShortcutAction sa = shortcuts.get(s);
                        if (!sa.getId().equals(en.getKey().getId())) {
                            LOG.finer("[" + profile + "] change keybinding " + s + " from " + en.getKey().getId() + " to " + sa.getId());
                        }
                    }
                }
            }
            
            keys.removeAll(overrideKeyStrokes);
            if (keys.isEmpty()) {
                it.remove();
            }
        }
        base.putAll(keyMap);
        return base;
    }
    
    /** Map (String (profile) > Map (GlobalAction > Set (String (shortcut)))). */
    private Map<String, Map<ShortcutAction, Set<String>>> keymapDefaults = 
            new HashMap<String, Map<ShortcutAction, Set<String>>> ();
    
    /**
     * Returns Map (GlobalAction > Set (String (shortcut))).
     */
    public synchronized Map<ShortcutAction, Set<String>> getDefaultKeymap (String profile) {
        if (!keymapDefaults.containsKey (profile)) {
            DataFolder root = getRootFolder (SHORTCUTS_FOLDER, null);
            System.err.println(Arrays.asList(root.getChildren()));
            Map<ShortcutAction, Set<String>> m = readKeymap (root);
            root = getRootFolder (KEYMAPS_FOLDER, profile);
            overrideWithKeyMap(m, readKeymap(root), profile);
            m.remove(REMOVED);
            keymapDefaults.put (profile, m);
        }
        return Collections.unmodifiableMap (keymapDefaults.get (profile));
    }
    
    DataObject getDataObject (Object action) {
        return actionToDataObject.get (action);
    }
    
    /**
     * Placeholder, which indicates shortcut(s) that should be removed. Must be used
     * only internally !
     */
    private static final GlobalAction REMOVED = new GlobalAction(null, null, "<removed>") {
        { 
            name = ""; // NOI18N
        }
    };
    
    /**
     * Read keymap from one folder Map (GlobalAction > Set (String (shortcut))).
     */
    private Map<ShortcutAction, Set<String>> readKeymap (DataFolder root) {
        LOG.log(Level.FINEST, "Reading keymap from: {0}", root);
        Map<ShortcutAction, Set<String>> keymap = 
                new HashMap<ShortcutAction, Set<String>> ();
        if (root == null) return keymap;
        Enumeration<DataObject> en = root.children (false);
        while (en.hasMoreElements ()) {
            DataObject dataObject = en.nextElement ();
            if (dataObject instanceof DataFolder) continue;
            GlobalAction action = createAction (dataObject, null);
            if (action == null) continue;
            String shortcut = dataObject.getPrimaryFile().getName();
            
            LOG.log(Level.FINEST, "Action {0}: {1}, by {2}", new Object[] {
                action.getDisplayName(),
                shortcut,
                dataObject.getPrimaryFile().getPath()
            });
            Set<String> s = keymap.get (action);
            if (s == null) {
                s = new HashSet<String> ();
                keymap.put (action, s);
            }
            s.add (shortcut);
        }
        return keymap;
    }

    @Override
    public void deleteProfile (String profile) {
        FileObject root = FileUtil.getConfigFile(KEYMAPS_FOLDER);
        if (root == null) return;
        root = root.getFileObject (profile);
        if (root == null) return;
        try {
            root.delete ();
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    // actionToShortcuts Map (GlobalAction > Set (String (shortcut))
    @Override
    public void saveKeymap (String profile, Map<ShortcutAction, Set<String>> actionToShortcuts) {
        // discard our cached copy first
        keymaps.remove(profile);
        keymapDefaults.remove(profile);
        // 1) get / create Keymaps/Profile folder
        DataFolder defaultFolder = getRootFolder(SHORTCUTS_FOLDER, null);
        DataFolder folder = getRootFolder (KEYMAPS_FOLDER, profile);
        if (folder == null) {
            folder = getRootFolder (KEYMAPS_FOLDER, null);
            try {
                folder = DataFolder.create (folder, profile);
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
                return;
            }
        }
        saveKeymap (defaultFolder, folder, actionToShortcuts);
    }
    
    private void saveKeymap (DataFolder defaultMap, DataFolder folder, Map<ShortcutAction, Set<String>> actionToShortcuts) {
        LOG.log(Level.FINEST, "Saving keymap to: {0}", folder.getPrimaryFile().getPath());
        // hack: initialize the actions map first
  	getActions();
        // 2) convert to: Map (String (shortcut AC-C X) > GlobalAction)
        Map<String, ShortcutAction> shortcutToAction = shortcutToAction (actionToShortcuts);
        
        Set<String> definedShortcuts = new HashSet<String>(shortcutToAction.keySet());
        
        // 3) delete obsolete DataObjects
        FileObject targetDir = folder.getPrimaryFile();

        Enumeration en = folder.children ();
        while (en.hasMoreElements ()) {
            DataObject dataObject = (DataObject) en.nextElement ();
            if (dataObject.getPrimaryFile().getExt().equals(EXT_REMOVED)) {
                continue;
            }
            GlobalAction a1 = (GlobalAction) shortcutToAction.get (dataObject.getName ());
            if (a1 != null) {
                GlobalAction action = createAction (dataObject, null);
                if (action == null) {
                    LOG.log(Level.FINEST, "Broken action shortcut will be removed: {0}, will replace by {1}", new Object[] { dataObject.getName(), a1.getId() });
                } else if (action.equals (a1)) {
                    // shortcut already saved
                    LOG.log(Level.FINEST, "Found same binding: {0} -> {1}", new Object[] { dataObject.getName(), action.getId()});
                    shortcutToAction.remove (dataObject.getName ());
                    continue;
                }
            }
            // obsolete shortcut. 
            try {
                LOG.log(Level.FINEST, "Removing obsolete binding: {0}", dataObject.getName());
                dataObject.delete ();
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
        
        // 4) add new shortcuts
        en = defaultMap.children();
        while (en.hasMoreElements()) {
            DataObject dataObject = (DataObject)en.nextElement();
            GlobalAction ga = (GlobalAction)shortcutToAction.get(dataObject.getName());
            if (ga == null) {
                continue;
            }
            GlobalAction action = createAction(dataObject, null);
            if (ga.equals(action)) {
                LOG.log(Level.FINEST, "Leaving default shortcut: {0}", dataObject.getName());
                shortcutToAction.remove(dataObject.getName());
            }
        }
        
        Iterator it = shortcutToAction.keySet ().iterator ();
        while (it.hasNext ()) {
            String shortcut = (String) it.next ();
            // check whether the DO does not already exist:
            GlobalAction action = (GlobalAction) shortcutToAction.get (shortcut);
            DataObject dataObject = actionToDataObject.get (action);
            if (dataObject == null) {
                 if (System.getProperty ("org.netbeans.optionsDialog") != null)
                     System.out.println ("No original DataObject specified! Not possible to create shadow1. " + action);
                 continue;
            }
            try {
                DataShadow.create (folder, shortcut, dataObject);
                // remove the '.remove' file, if it exists:
                FileObject f = targetDir.getFileObject(shortcut, EXT_REMOVED);
                if (f != null) {
                    f.delete();
                }
            } catch (IOException ex) {
                ex.printStackTrace ();
                continue;
            }
        }

        // 5, mask out DataObjects from the global keymap, which are NOT present in this profile:
        if (defaultMap != null) {
            en = defaultMap.children();
            while (en.hasMoreElements()) {
                DataObject dataObject = (DataObject) en.nextElement ();
                if (definedShortcuts.contains(dataObject.getName())) {
                    continue;
                }
                try {
                    FileObject pf = dataObject.getPrimaryFile();
                    // If the shortcut is ALSO defined in 'parent' folder,
                    // we cannot just 'delete' it, but also mask the parent by adding 'removed' file.
                    if (targetDir.getFileObject(pf.getName(), EXT_REMOVED) == null) {
                        LOG.log(Level.FINEST, "Masking out binding: {0}", pf.getName());
                        folder.getPrimaryFile().createData(pf.getName(), EXT_REMOVED);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace ();
                }
            }
        }
        
    }    

    private static DataFolder getRootFolder (String name1, String name2) {
        FileObject root = FileUtil.getConfigRoot ();
        FileObject fo1 = root.getFileObject (name1);
        try {
            if (fo1 == null) fo1 = root.createFolder (name1);
            if (fo1 == null) return null;
            if (name2 == null) return DataFolder.findFolder (fo1);
            FileObject fo2 = fo1.getFileObject (name2);
            if (fo2 == null) fo2 = fo1.createFolder (name2);
            if (fo2 == null) return null;
            return DataFolder.findFolder (fo2);
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
            return null;
        }
    }

    /**
     * Returns instance of GlobalAction encapsulating action, or null.
     */
    private GlobalAction createAction (DataObject dataObject, String prefix) {
        InstanceCookie ic = dataObject.getCookie(InstanceCookie.class);
        // handle any non-IC file as instruction to remove the action
        if (ic == null) {
            FileObject pf = dataObject.getPrimaryFile();
            if (!EXT_REMOVED.equals(pf.getExt())) {
                LOG.log(Level.WARNING, "Invalid shortcut: {0}", dataObject);
                return null;
            }
            // ignore the 'remove' file, if there's a shadow (= real action) present
            if (FileUtil.findBrother(pf, "shadow") != null) {
                // handle redefinition + removal: ignore the removal.
                return null;
            }
            return REMOVED;
        }
        try {
            Object action = ic.instanceCreate ();
            if (action == null) return null;
            if (!(action instanceof Action)) return null;
            return createAction((Action) action, prefix, dataObject.getPrimaryFile().getName());
        } catch (Exception ex) {
            ex.printStackTrace ();
            return null;
        }
    }

    // hack: hardcoded OpenIDE impl class name + field
    private static final String OPENIDE_DELEGATE_ACTION = "org.openide.awt.GeneralAction$DelegateAction"; // NOI18N
    private static Field KEY_FIELD;

    /**
     * Hack, which allows to somehow extract actionId from OpenIDE actions. Public API
     * does not exist for this.
     * 
     * @param a
     * @param prefix
     * @param name
     * @return 
     */
    private static GlobalAction createAction(Action a, String prefix, String name) {
        String id = name;
        
        try {
            if (a.getClass().getName().equals(OPENIDE_DELEGATE_ACTION)) {
                if (KEY_FIELD == null) {
                    Class c = a.getClass();
                    KEY_FIELD = c.getSuperclass().getDeclaredField("key"); // NOI18N
                    KEY_FIELD.setAccessible(true);
                }
                String key = (String)KEY_FIELD.get(a);
                if (key != null) {
                    id = key;
                }
            }
        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return new GlobalAction(a, prefix, id);
    }
    
    /**
     * converts: actionToShortcuts: Map (ShortcutAction > Set (String (shortcut AC-C X)))
     * to: Map (String (shortcut AC-C X) > GlobalAction).
     * removes all non GlobalAction actions.
     */
    static Map<String, ShortcutAction> shortcutToAction (Map<ShortcutAction, Set<String>> actionToShortcuts) {
        Map<String, ShortcutAction> shortcutToAction = new HashMap<String, ShortcutAction> ();
        for (Map.Entry<ShortcutAction, Set<String>> entry: actionToShortcuts.entrySet()) {
            ShortcutAction action = entry.getKey();
            Set<String> shortcuts = entry.getValue();
            action = action != null ? action.getKeymapManagerInstance(LAYERS_BRIDGE) : null; // #161164
            if (!(action instanceof GlobalAction)) continue;
            for (String multiShortcut: shortcuts) {
                shortcutToAction.put (multiShortcut, action);
            }
        }
        return shortcutToAction;
    }
    
    public void refreshActions() {
        refreshKeymapNames();
    }

    public String getCurrentProfile() {
        return null;
    }

    public void setCurrentProfile(String profileName) {
    }

    public boolean isCustomProfile(String profileName) {
        // TODO:
        return false;
    }
    
    /* package */ static String getOrigActionClass(ShortcutAction sa) {
        if (!(sa instanceof GlobalAction)) {
            return null;
        }
        GlobalAction ga = (GlobalAction)sa;
        return ga.action == null ? null : ga.action.getClass().getName();
    }
    
    private static class GlobalAction implements ShortcutAction {
        private Action action;
        String name;
        private String id;
        
        /**
         * 
         * @param a the action to be delegated to
         * @param prefix prefix for the name, e.g. category where the action is defined
         * @param n name / id of the action, usually a declaring filename
         */
        private GlobalAction (Action a, String prefix, String n) {
            action = a;
            /*
            if (prefix != null) {
                this.id = prefix + "/" + n; // NOI18N
            } else {
                this.id = n;
            }
            */
            this.id = n;
        }
        
        public String getDisplayName () {
            if (name == null) {
                name = (String) action.getValue (Action.NAME);
                if (name == null) {
                    name = ""; // #185619: not intended for presentation in this dialog
                }
                name = name.replaceAll ("&", "").trim (); // NOI18N
            }
            return name;
        }
        
        public String getId () {
            if (id == null) 
                id = action.getClass ().getName ();
            return id;
        }
        
        public String getDelegatingActionId () {
            return null;
        }
        
        @Override
        public boolean equals (Object o) {
            if (!(o instanceof GlobalAction)) return false;
            return ((GlobalAction) o).action == action || ((GlobalAction) o).action.equals (action);
        }
        
        @Override
        public int hashCode () {
            return action == null ? 111 : action.hashCode ();
        }
        
        @Override
        public String toString () {
            return "GlobalAction[" + getDisplayName()+ ":" + id + "]";
        }
    
        public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
            if (LAYERS_BRIDGE.equals(keymapManagerName)) {
                return this;
            }
            return null;
        }
    }
}
