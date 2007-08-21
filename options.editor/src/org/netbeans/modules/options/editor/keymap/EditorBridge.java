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

package org.netbeans.modules.options.editor.keymap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.EditorKit;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.KeyBindingSettingsFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * @author Jan Jancura
 */
public final class EditorBridge extends KeymapManager {

    private static final Logger LOG = Logger.getLogger(EditorBridge.class.getName());

    private static final String EDITOR_BRIDGE = "EditorBridge"; //NOI18N

    public EditorBridge() {
        super(EDITOR_BRIDGE);
    }

    private Map<String, Set<ShortcutAction>> actions;

    public Map<String, Set<ShortcutAction>> getActions() {
        if (actions == null) {
            Map<String, String> categories = readCategories();
            actions = new HashMap<String, Set<ShortcutAction>>();
            for (EditorAction action : getEditorActionsMap().values()) {
                String category = categories.get(action.getId());
                if (category == null) {
                    category = NbBundle.getMessage(EditorBridge.class, "CTL_Other"); // NOI18N
                }
                Set<ShortcutAction> a = actions.get(category);
                if (a == null) {
                    a = new HashSet<ShortcutAction>();
                    actions.put(category, a);
                }
                a.add(action);
            }
            actions.remove("Hidden"); // NOI18N
        }
        return actions;
    }

    public void refreshActions() {
        editorActionsMap = null;
        actions = null;
        actionNameToMimeTypes = new HashMap<String, Set<String>>();
    }

    public String getCurrentProfile() {
        return getEditorSettings().getCurrentKeyMapProfile();
    }

    public void setCurrentProfile(String profile) {
        getEditorSettings().setCurrentKeyMapProfile(profile);
    }

    public boolean isCustomProfile(String profile) {
        return getEditorSettings().isCustomKeymapProfile(profile);
    }

    public Map<ShortcutAction, Set<String>> getKeymap(String profile) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>>();
        readKeymap(profile, null, false, result);
        for (String mimeType : getEditorSettings().getMimeTypes()) {
            readKeymap(profile, mimeType, false, result);
        }
        return Collections.unmodifiableMap(result);
    }

    public Map<ShortcutAction, Set<String>> getDefaultKeymap(String profile) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>>();
        readKeymap(profile, null, true, result);
        for (String mimeType : getEditorSettings().getMimeTypes()) {
            readKeymap(profile, mimeType, true, result);
        }
        return Collections.unmodifiableMap(result);
    }

    public void deleteProfile(String profile) {
        KeyBindingSettingsFactory kbs = getKeyBindingSettings(null);
        kbs.setKeyBindings(profile, null);
    }

    /**
     * Saves actionToShortcuts Map (GlobalAction > Set (String (shortcut)).
     * Ignores all non EditorAction actions.
     */
    public void saveKeymap(String profile, Map<ShortcutAction, Set<String>> actionToShortcuts) {

        // 1)
        // convert actionToShortcuts: Map (ShortcutAction > Set (String (shortcut AS-M)))
        // to mimeTypeToKeyBinding: Map (String (mimetype) > List (MultiKeyBinding)).
        Map<String, List<MultiKeyBinding>> mimeTypeToKeyBinding = new HashMap<String, List<MultiKeyBinding>>(); // editor shortcuts
        for(ShortcutAction action : actionToShortcuts.keySet()) {
            Set<String> shortcuts = actionToShortcuts.get(action);

            action = action.getKeymapManagerInstance(EDITOR_BRIDGE);
            if (!(action instanceof EditorAction)) {
                continue;
            }
            
            EditorAction editorAction = (EditorAction) action;
            Set<String> mimeTypes = getMimeTypes(editorAction);
            
            for (String shortcut : shortcuts) {
                MultiKeyBinding mkb = new MultiKeyBinding(stringToKeyStrokes2(shortcut), editorAction.getId());
                for (String mimeType : mimeTypes) {
                    List<MultiKeyBinding> l = mimeTypeToKeyBinding.get(mimeType);
                    if (l == null) {
                        l = new ArrayList<MultiKeyBinding>();
                        mimeTypeToKeyBinding.put(mimeType, l);
                    }
                    l.add(mkb);
                }
            }
        }

        // 2) save all shortcuts
        for (String mimeType : keyBindingSettings.keySet()) {
            KeyBindingSettingsFactory kbs = keyBindingSettings.get(mimeType);
            kbs.setKeyBindings(profile, mimeTypeToKeyBinding.get(mimeType));
        }
    }


    // private methods .........................................................
    /** Map (String (mimeType) > Set (String (action name))). */
    private Map<String, EditorAction> editorActionsMap;
    /** Map (ShortcutAction > Set (String (mimeType))). */
    private Map<String, Set<String>> actionNameToMimeTypes = new HashMap<String, Set<String>>();

    /**
     * Returns map of all editor actions.
     * Map (String (mimeType) > Set (String (action name)))
     */
    private Map<String, EditorAction> getEditorActionsMap() {
        if (editorActionsMap == null) {
            editorActionsMap = new HashMap<String, EditorAction>();
            for (String mimeType : getEditorSettings().getMimeTypes()) {
                initActionMap(mimeType);
            }
            initActionMap(null);
        }
        return editorActionsMap;
    }

    private Set<String> getMimeTypes(EditorAction a) {
        getEditorActionsMap(); // initialization
        return actionNameToMimeTypes.get(a.getId());
    }

    /**
     * Loads editor actions for given mimeType to editorActionsMap.
     */
    private void initActionMap(String mimeType) {

        // 1) get EditorKit
        EditorKit editorKit = null;
        if (mimeType == null) {
            editorKit = BaseKit.getKit(NbEditorKit.class);
        } else {
            Lookup mimeLookup = MimeLookup.getLookup(MimePath.parse(mimeType));
            editorKit = mimeLookup.lookup(EditorKit.class);
        }
        if (editorKit == null) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.warning("EditorKit not found for: " + mimeType); //NOI18N
            }
            return;
        }

        // 2) copy actions from EditorKit to actionMap
        Action[] as = editorKit.getActions();
        int i;
        int k = as.length;
        for (i = 0; i < k; i++) {
            Object isHidden = as[i].getValue(BaseAction.NO_KEYBINDING);
            if (isHidden instanceof Boolean && ((Boolean) isHidden).booleanValue()) {
                continue; // ignore hidden actions
            }
            EditorAction action = new EditorAction((TextAction) as [i]);
            String id = action.getId();
            editorActionsMap.put(id, action);
            Set<String> s = actionNameToMimeTypes.get(id);
            if (s == null) {
                s = new HashSet<String>();
                actionNameToMimeTypes.put(id, s);
            }
            s.add(mimeType);
        }
    }

    private EditorSettings editorSettings;

    private EditorSettings getEditorSettings() {
        if (editorSettings == null) {
            editorSettings = EditorSettings.getDefault();
        }
        return editorSettings;
    }

    private final Map<String, KeyBindingSettingsFactory> keyBindingSettings = new HashMap<String, KeyBindingSettingsFactory>();
    private static final String [] EMPTY = new String[0];
    
    private KeyBindingSettingsFactory getKeyBindingSettings(String mimeType) {
        KeyBindingSettingsFactory kbs = keyBindingSettings.get(mimeType);
        if (kbs == null) {
            kbs = EditorSettings.getDefault().getKeyBindingSettings(
                mimeType == null ? EMPTY : new String[] { mimeType });

            keyBindingSettings.put(mimeType, kbs);
            getListener().add(kbs);
        }
        return kbs;
    }

    private Listener listener;

    private Listener getListener() {
        if (listener == null) {
            listener = new Listener(this);
        }
        return listener;
    }

    private static class Listener implements PropertyChangeListener {

        private Reference<EditorBridge> model;
        private Set<KeyBindingSettingsFactory> factories = new HashSet<KeyBindingSettingsFactory>();

        Listener(EditorBridge model) {
            this.model = new WeakReference<EditorBridge>(model);
        }

        void add(KeyBindingSettingsFactory kbsf) {
            this.factories.add(kbsf);
            kbsf.addPropertyChangeListener(this);
        }

        private EditorBridge getModel() {
            EditorBridge m = model.get ();
            if (m != null) {
                return m;
            }
            for (KeyBindingSettingsFactory kbsf : factories) {
                kbsf.removePropertyChangeListener(this);
            }
            factories = new HashSet<KeyBindingSettingsFactory>();
            return null;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            EditorBridge m = getModel();
            if (m == null) {
                return;
                //model.keyMaps = new HashMap ();
            }
        }
    }

    /**
     * Reads keymap for given mimetype and profile to given map
     * Map (ShortcutAction > Set (String (shortcut)))
     */
    private void readKeymap(String profile, String mimeType, boolean defaults, Map<ShortcutAction, Set<String>> map) {
        // 1) get list of MultiKeyBindings
        KeyBindingSettingsFactory kbs = getKeyBindingSettings(mimeType);
        if (kbs == null) {
            return;
        }
        List<MultiKeyBinding> keyBindings = defaults ? kbs.getKeyBindingDefaults(profile) : kbs.getKeyBindings(profile);
        if (keyBindings == null) {
            return;
        }
        // 2) create Map (String (action name) > Set (String (shortcut)))
        Map<String, Set<String>> actionNameToShortcuts = convertKeymap(keyBindings);

        // 3) create Map (EditorAction > Set (String (shortcut)))
        for (String actionName : actionNameToShortcuts.keySet()) {
            Set<String> keyStrokes = actionNameToShortcuts.get(actionName);
            ShortcutAction action = (ShortcutAction) getEditorActionsMap ().get (actionName);
            if (action == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("action not found " + actionName); //NOI18N
                }
                continue;
            }
            Set<String> s = map.get(action);
            if (s == null) {
                map.put(action, keyStrokes);
            } else {
                s.addAll(keyStrokes);
            }
        }
    }

    /**
     * create Map (String (action name) > Set (String (shortcut AS-M)))
     *
     * @param keyBindings list of MultiKeyBindings
     */
    private static Map<String, Set<String>> convertKeymap(List<MultiKeyBinding> keyBindings) {
        Map<String, Set<String>> actionNameToShortcuts = new HashMap<String, Set<String>>();

        for (int i = 0; i < keyBindings.size(); i++) {
            MultiKeyBinding mkb = keyBindings.get(i);
            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < mkb.getKeyStrokeCount(); j++) {
                if (j > 0) {
                    sb.append(' '); //NOI18N
                }
                sb.append(Utilities.keyToString(mkb.getKeyStrokeList().get(j)));
            }

            Set<String> keyStrokes = actionNameToShortcuts.get(mkb.getActionName());
            if (keyStrokes == null) {
                keyStrokes = new HashSet<String>();
                actionNameToShortcuts.put(mkb.getActionName(), keyStrokes);
            }
            keyStrokes.add(sb.toString());
        }

        return actionNameToShortcuts;
    }

    private static Map<String, String> readCategories() {
        Map<String, String> result = new HashMap<String, String>();
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = fs.findResource("OptionsDialog/Actions"); //NOI18N
        if (fo == null) {
            return result;
        }
        FileObject[] categories = fo.getChildren();
        for (int i = 0; i < categories.length; i++) {
            String categoryName = categories[i].getName();
            String bundleName = (String) categories [i].getAttribute 
                ("SystemFileSystem.localizingBundle"); //NOI18N
            if (bundleName != null) {
                try {
                    categoryName = NbBundle.getBundle(bundleName).getString(categories[i].getPath());
                } catch (MissingResourceException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            FileObject[] actions = categories[i].getChildren();
            for (int j = 0; j < actions.length; j++) {
                if (actions[j].getExt().length() > 0) {
                    continue;
                }
                String actionName = actions[j].getName();
                result.put(actionName, categoryName);
            }
        }
        return result;
    }

    public List<String> getProfiles() {
        return null;
    }

    private static KeyStroke[] stringToKeyStrokes2(String key) {
        List<KeyStroke> result = new ArrayList<KeyStroke>();

        for (StringTokenizer st = new StringTokenizer(key, " "); st.hasMoreTokens();) { //NOI18N
            String ks = st.nextToken().trim();
            KeyStroke keyStroke = Utilities.stringToKey(ks);

            if (keyStroke == null) {
                LOG.warning("'" + ks + "' is not a valid keystroke"); //NOI18N
                return null;
            }

            result.add(keyStroke);
        }

        return result.toArray(new KeyStroke[result.size()]);
    }

    private static final class EditorAction implements ShortcutAction {

        private TextAction action;
        private String name;
        private String id;
        private String delegaitngActionId;

        public EditorAction(TextAction a) {
            action = a;
        }

        public String getDisplayName() {
            if (name == null) {
                name = (String) action.getValue (Action.SHORT_DESCRIPTION);
                if (name == null) {
                    LOG.warning("The action " + action + " doesn't provide short description, using its name."); //NOI18N
                    name = getId();
                }
                name = name.replaceAll("&", "").trim(); //NOI18N
            }
            return name;
        }

        public String getId() {
            if (id == null) {
                id = (String) action.getValue (Action.NAME);
                assert id != null : "Actions must have name, offending action: " + action; //NOI18N
            }
            return id;
        }

        public String getDelegatingActionId() {
            if (delegaitngActionId == null) {
                delegaitngActionId = (String) action.getValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
            }
            return delegaitngActionId;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EditorAction)) {
                return false;
            }
            return ((EditorAction) o).getId().equals(getId());
        }

        @Override
        public int hashCode() {
            return getId().hashCode();
        }

        @Override
        public String toString() {
            return "EditorAction[" + getDisplayName() + ":" + getId() + "]"; //NOI18N
        }

        public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
            if (EDITOR_BRIDGE.equals(keymapManagerName)) {
                return this;
            } else {
                return null;
            }
        }

        public TextAction getRealAction() {
            return action;
        }
    } // End of EditorAction
}
