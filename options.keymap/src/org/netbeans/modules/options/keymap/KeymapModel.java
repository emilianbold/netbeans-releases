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
import java.util.logging.Logger;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Jancura
 */
public class KeymapModel {
    
    private static final Logger LOG = Logger.getLogger(KeymapModel.class.getName ());
                                    
    private static ArrayList<KeymapManager> al = new ArrayList<KeymapManager>();
    
    /**
     * @return All the registered implementations.
     */
    public static Collection<? extends KeymapManager> getKeymapManagerInstances() {
        if (!al.isEmpty()) {
            return al;
        }
        al.addAll(Lookup.getDefault().lookupAll(KeymapManager.class));
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
        }
        return categoryToActions.get(category);
    }

    /**
     * Clear action caches.
     */
    public void refreshActions () {
        categoryToActions = new HashMap<String,Set<ShortcutAction>>();
        for (KeymapManager m : getKeymapManagerInstances()) {
            m.refreshActions();
        }
    }
    
    // keymaps .................................................................
    
    public String getCurrentProfile () {
        for (KeymapManager m : getKeymapManagerInstances()) {
            String res = m.getCurrentProfile();
            if (res != null) {
                return res;
            }
        }
        return "NetBeans";
    }
    
    public void setCurrentProfile (String profile) {
        for (KeymapManager m : getKeymapManagerInstances()) {
            m.setCurrentProfile(profile);
        }
    }
    
    public List<String> getProfiles () {
        for (KeymapManager m : getKeymapManagerInstances()) {
            List<String> l = m.getProfiles();
            if (l != null) {
                return l;
            }
        }
        return null;
    }
    
    public boolean isCustomProfile (String profile) {
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
        if (!keyMaps.containsKey(profile)) {
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
        if (!keyMapDefaults.containsKey (profile)) {
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
        for (KeymapManager m : getKeymapManagerInstances()) {
            m.deleteProfile(profile);
        }
    }
    
    /**
     * Defines new shortcuts for some actions in given keymap.
     * Map (ShortcutAction > Set (String (shortcut AS-M P)).
     */
    public void changeKeymap(String profile, 
                Map<ShortcutAction,Set<String>> actionToShortcuts) {
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
            LOG.fine("  " + item);
        }
    }
    
    private Map<ShortcutAction,CompoundAction> sharedActions = 
            new HashMap<ShortcutAction,CompoundAction>();
    
    /**
     * Merges editor actions and layers actions. Creates CompoundAction for
     * actions like Copy, registerred to both contexts.
     */
    Set<ShortcutAction> mergeActions (
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
    
    Map<ShortcutAction,Set<String>> mergeShortcuts (
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
        }
        return res;
    }

    {
        // HACK - loads all actions. othervise during second open of Options
        // Dialog (after cancel) map of sharedActions is not initialized.
        Iterator it = getActionCategories ().iterator ();
        while (it.hasNext ())
            getActions ((String) it.next ());
    }
}
