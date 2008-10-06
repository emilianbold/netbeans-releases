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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Jancura
 */
public class KeymapModel {
    
    private static final Logger LOG = Logger.getLogger(KeymapModel.class.getName ());
    private static final Logger UI_LOG = Logger.getLogger("org.netbeans.ui.options"); // NOI18N
                                    
    private static ArrayList<KeymapManager> al = new ArrayList<KeymapManager>();
    
    /**
     * @return All the registered implementations.
     */
    public static Collection<? extends KeymapManager> getKeymapManagerInstances() {
        if (!al.isEmpty()) {
            return al;
        }
        al.addAll(Lookup.getDefault().lookupAll(KeymapManager.class));

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Dumping registered KeymapManagers: ");
            for(KeymapManager m : al) {
                LOG.fine("    KeymapManager: " + s2s(m));
            }
            LOG.fine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }

        return al;
    }
    
    // actions .................................................................
    
    public Set<String> getActionCategories () {
        Set<String> result = new HashSet<String>();
        for (KeymapManager m : getKeymapManagerInstances()) {
            result.addAll(m.getActions().keySet());
        }
        return Collections.unmodifiableSet (result);
    }
    
    /**
     * Map (String (category name) > Set (ShortcutAction)).
     */
    private Map<String,Set<ShortcutAction>> categoryToActions = 
            new HashMap<String,Set<ShortcutAction>>();

    /**
     * Returns List (ShortcutAction) of all global and editor actions.
     */
    public Set<ShortcutAction> getActions(String category) {
        if (!categoryToActions.containsKey (category)) {
            Set<ShortcutAction> actions = new HashSet<ShortcutAction>();
            for (KeymapManager m : getKeymapManagerInstances()) {
                Set<ShortcutAction> s = m.getActions().get(category);
                if (s != null) {
                    actions = mergeActions(actions, s, m.getName());
                }
            }
            categoryToActions.put(category, actions);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Category '" + category + "' actions (" + actions.size() + "), KeymapModel=" + this + ":"); //NOI18N
                for(ShortcutAction sa : actions) {
                    LOG.fine("    id='" + sa.getId() + "', did='" + sa.getDelegatingActionId() + ", " + s2s(sa)); //NOI18N
                }
                LOG.fine("---------------------------"); //NOI18N
            }
        }
        return categoryToActions.get(category);
    }

    /**
     * Clear action caches.
     */
    public void refreshActions () {
        categoryToActions = new HashMap<String,Set<ShortcutAction>>();
        sharedActions = new HashMap<ShortcutAction, CompoundAction>();
        keyMaps = new HashMap<String, Map<ShortcutAction, Set<String>>>();
        keyMapDefaults = new HashMap<String, Map<ShortcutAction, Set<String>>>();

        for (KeymapManager m : getKeymapManagerInstances()) {
            m.refreshActions();
        }
    }
    
    // keymaps .................................................................
    
    public String getCurrentProfile () {
        String profileName = null;
        for (KeymapManager m : getKeymapManagerInstances()) {
            String res = m.getCurrentProfile();
            if (res != null) {
                profileName = res;
                break;
            }
        }
        
        if (profileName == null) {
            profileName = "NetBeans"; //NOI18N
        }
        
        Map<String, String> map = getProfilesMap();
        for(Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(profileName)) {
                return entry.getKey();
            }
        }
        
        return profileName;
    }
    
    public void setCurrentProfile (String profile) {
        String prev = getCurrentProfile();
        if (!prev.equals(profile)) {
            LogRecord rec = new LogRecord(Level.CONFIG, "KEYMAP_SET_PROFILE"); // NOI18N
            rec.setParameters(new Object[]{ profile, prev });
            rec.setResourceBundle(NbBundle.getBundle(KeymapModel.class));
            rec.setResourceBundleName(KeymapModel.class.getPackage().getName() + ".Bundle");
            rec.setLoggerName(UI_LOG.getName());
            UI_LOG.log(rec);
        }
        
        profile = displayNameToName(profile);
        for (KeymapManager m : getKeymapManagerInstances()) {
            m.setCurrentProfile(profile);
        }
    }
    
    public List<String> getProfiles () {
        return new ArrayList<String>(getProfilesMap().keySet());
    }
    
    public boolean isCustomProfile (String profile) {
        profile = displayNameToName(profile);
        for (KeymapManager m : getKeymapManagerInstances()) {
            boolean res = m.isCustomProfile(profile);
            if (res) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Map (String (profile) > Map (ShortcutAction > Set (String (shortcut AS-M)))).
     */
    private Map<String, Map<ShortcutAction,Set<String>>> keyMaps = 
            new HashMap<String, Map<ShortcutAction,Set<String>>>();
    
    /**
     * Returns Map (ShortcutAction > Set (String (shortcut))).
     */
    public Map<ShortcutAction,Set<String>> getKeymap (String profile) {
        profile = displayNameToName(profile);
        if (!keyMaps.containsKey(profile)) {
            ensureActionsLoaded();
            Map<ShortcutAction,Set<String>> res = new 
                    HashMap<ShortcutAction,Set<String>>();
            for (KeymapManager m : getKeymapManagerInstances()) {
                Map<ShortcutAction,Set<String>> mm = m.getKeymap(profile);
                res = mergeShortcuts(res, mm);
            }
            keyMaps.put(profile, res);
        }
        return keyMaps.get(profile);
    }
    
    /**
     * Map (String (keymap name) > Map (ShortcutAction > Set (String (shortcut AS-M)))).
     */
    private Map<String,Map<ShortcutAction,Set<String>>> keyMapDefaults = 
            new HashMap<String,Map<ShortcutAction,Set<String>>>();
    
    /**
     * Returns Map (ShortcutAction > Set (String (shortcut))).
     */
    public Map<ShortcutAction, Set<String>> getKeymapDefaults(String profile) {
        profile = displayNameToName(profile);
        if (!keyMapDefaults.containsKey (profile)) {
            ensureActionsLoaded();
            Map<ShortcutAction,Set<String>> res = new 
                    HashMap<ShortcutAction,Set<String>>();
            for (KeymapManager m : getKeymapManagerInstances()) {
                Map<ShortcutAction,Set<String>> mm = m.getDefaultKeymap(profile);
                res = mergeShortcuts(res, mm);
            }
            keyMapDefaults.put(profile, res);
        }
        return keyMapDefaults.get(profile);
    }
    
    public void deleteProfile(String profile) {
        profile = displayNameToName(profile);
        for (KeymapManager m : getKeymapManagerInstances()) {
            m.deleteProfile(profile);
        }
    }
    
    /**
     * Defines new shortcuts for some actions in given keymap.
     * Map (ShortcutAction > Set (String (shortcut AS-M P)).
     */
    public void changeKeymap(String profile, Map<ShortcutAction,Set<String>> actionToShortcuts) {
        profile = displayNameToName(profile);
        
        log ("changeKeymap.actionToShortcuts", actionToShortcuts.entrySet ());
        
        // 1) mix changes with current keymap and put them to cached current shortcuts
        Map<ShortcutAction,Set<String>> m = 
                new HashMap<ShortcutAction,Set<String>>(getKeymap(profile));
        m.putAll (actionToShortcuts);
        keyMaps.put(profile, m);
        log ("changeKeymap.m", m.entrySet ());
        for (KeymapManager km : getKeymapManagerInstances()) {
            km.saveKeymap(profile, m);
        }
    }
    
    
    // private methods .........................................................
    
    private void log(String name, Collection items) {
        if (!LOG.isLoggable(Level.FINE)) return;
        
        LOG.fine(name);
        for(Iterator i = items.iterator(); i.hasNext(); ) {
            Object item = i.next();
            LOG.fine("  " + item); //NOI18N
        }
    }
    
    private Map<ShortcutAction,CompoundAction> sharedActions = 
            new HashMap<ShortcutAction,CompoundAction>();
    
    /**
     * Merges editor actions and layers actions. Creates CompoundAction for
     * actions like Copy, registerred to both contexts.
     */
    /* package */ Set<ShortcutAction> mergeActions (
        Collection<ShortcutAction> res, Collection<ShortcutAction> adding, String name) {
        
        Set<ShortcutAction> result = new HashSet<ShortcutAction>();
        Map<String,ShortcutAction> idToAction = new HashMap<String,ShortcutAction>();
        Map<String,ShortcutAction> delegateIdToAction = new HashMap<String,ShortcutAction>();
        for (ShortcutAction action: res) {
            String id = action.getId();
            idToAction.put(id, action);
            String delegate = action.getDelegatingActionId();
            if (delegate != null) {
                delegateIdToAction.put(delegate, action);
            }
        }
        
        for (ShortcutAction action : adding) {
            String id = action.getId();

            if (delegateIdToAction.containsKey(id)) {
                ShortcutAction origAction = delegateIdToAction.remove(id);
                idToAction.remove(origAction.getId());
                KeymapManager origActionKeymapManager = findOriginator(origAction);
                Map<String, ShortcutAction> ss = new HashMap<String, ShortcutAction>();
                ss.put(origActionKeymapManager.getName(), origAction);
                ss.put(name,action);
                CompoundAction compoundAction = new CompoundAction(ss);
                result.add(compoundAction);
                sharedActions.put(origAction, compoundAction);
                sharedActions.put(action, compoundAction);
                result.add(compoundAction);
            }
            String delegatingId = action.getDelegatingActionId();
            if (idToAction.containsKey(delegatingId)) {
                ShortcutAction origAction = idToAction.remove(delegatingId);
                KeymapManager origActionKeymapManager = findOriginator(origAction);
                Map<String, ShortcutAction> ss = new HashMap<String, ShortcutAction>();
                ss.put(origActionKeymapManager.getName(), origAction);
                ss.put(name,action);
                CompoundAction compoundAction = new CompoundAction(ss);
                result.add(compoundAction);
                sharedActions.put(origAction, compoundAction);
                sharedActions.put(action, compoundAction);
                result.add(compoundAction);
            }
            if (!sharedActions.containsKey(action)) {
                result.add(action);
            }
        }
        result.addAll(idToAction.values());
        return result;
    }
    
    /**
     * Tries to determince where the action originates.
     */
    private KeymapManager findOriginator(ShortcutAction a) {
        for (KeymapManager km : getKeymapManagerInstances()) {
            if (a.getKeymapManagerInstance(km.getName()) != null) {
                return km;
            }
        }
        return null;
    }
    
    private Map<ShortcutAction,Set<String>> mergeShortcuts (
        Map<ShortcutAction,Set<String>> res,
        Map<ShortcutAction,Set<String>> adding) {

        for (ShortcutAction action : adding.keySet()) {
            Set<String> shortcuts = adding.get(action);
            if (shortcuts.isEmpty()) {
                continue;
            }
            if (sharedActions.containsKey (action)) {
                action = sharedActions.get(action);
            }
            res.put(action, shortcuts);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Action='" + action.getId() + "' (" + s2s(action) + ") shortcuts: " + shortcuts);
            }
        }
        return res;
    }

    private void ensureActionsLoaded() {
        for(String c : getActionCategories()) {
            getActions(c);
        }
    }

    private String displayNameToName(String keymapDisplayName) {
        String name = getProfilesMap().get(keymapDisplayName);
        return name == null ? keymapDisplayName : name;
    }

    private Map<String, String> profilesMap;
    private Map<String, String> getProfilesMap() {
        if (profilesMap == null) {
            for (KeymapManager m : getKeymapManagerInstances()) {
                List<String> l = m.getProfiles();
                if (l != null) {
                    profilesMap = new HashMap<String, String>();
                    for(String name : l) {
                        profilesMap.put(m.getProfileDisplayName(name), name);
                    }
                    break;
                }
            }
        }
        return profilesMap;
    }
    
    public KeymapModel() {
//        System.out.println("\n\n\n~~~ Dumping all actions in all categories:");
//        TreeSet<String> categories = new TreeSet<String>(getActionCategories());
//        for(String category : categories) {
//            System.out.println("Category='" + category + "'");
//            TreeMap<String, ShortcutAction> actions = new TreeMap<String, ShortcutAction>();
//            for(ShortcutAction sa : getActions(category)) {
//                assert sa != null : "ShortcutAction must not be null";
//                assert sa.getId() != null : "Action Id must not be null";
//
//                if (actions.containsKey(sa.getId())) {
//                    System.out.println("! Duplicate action detected: '" + sa.getId()
//                        + "', delegatingId='" + sa.getDelegatingActionId()
//                        //+ "', displayName='" + sa.getDisplayName()
//                        + "', " + s2s(sa));
//                }
//
//                actions.put(sa.getId(), sa);
//            }
//
//            for(String id : actions.keySet()) {
//                ShortcutAction sa = actions.get(id);
//                System.out.println("Id='" + sa.getId()
//                    + "', delegatingId='" + sa.getDelegatingActionId()
//                    //+ "', displayName='" + sa.getDisplayName()
//                    + "', " + s2s(sa));
//            }
//        }
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n\n");

        // HACK - loads all actions. othervise during second open of Options
        // Dialog (after cancel) map of sharedActions is not initialized.
        Iterator it = getActionCategories ().iterator ();
        while (it.hasNext ())
            getActions ((String) it.next ());
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }
}
