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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;
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
    
    private static final String EDITOR_BRIDGE = "EditorBridge";
    
    public EditorBridge() {
        super(EDITOR_BRIDGE);
    }
    
    private Map actions;
    
    public Map getActions () {
        if (actions == null) {
            Map categories = readCategories ();
            actions = new HashMap ();
            Iterator it = getEditorActionsMap ().values ().iterator ();
            while (it.hasNext ()) {
                ShortcutAction action = (ShortcutAction) it.next ();
                String category = (String) categories.get (action.getId ());
                if (category == null) 
                    category = NbBundle.getMessage 
                        (EditorBridge.class, "CTL_Other");             // NOI18N
                Set a = (Set) actions.get (category);
                if (a == null) {
                    a = new HashSet ();
                    actions.put (category, a);
                }
                a.add (action);
            }
            actions.remove ("Hidden");                                 // NOI18N
        }
        return actions;
    }
    
    public void refreshActions () {
        editorActionsMap = null;
        actions = null;
        actionNameToMimeTypes = new HashMap ();
    }

    public String getCurrentProfile () {
        return getEditorSettings ().getCurrentKeyMapProfile ();
    }
    
    public void setCurrentProfile (String profile) {
        getEditorSettings ().setCurrentKeyMapProfile (profile);
    }
    
    public boolean isCustomProfile (String profile) {
        return getEditorSettings ().isCustomKeymapProfile (profile);
    }
    
    public Map getKeymap (String profile) {
        Map result = new HashMap ();
        readKeymap (profile, "text/base", false, result);
        Iterator it = getEditorSettings ().getMimeTypes ().iterator ();
        while (it.hasNext ())
            readKeymap (profile, (String) it.next (), false, result);
        return Collections.unmodifiableMap (result);
    }
    
    Map readKeymapDefaults (String profile) {
        Map result = new HashMap ();
        readKeymap (profile, "text/base", true, result);
        Iterator it = getEditorSettings ().getMimeTypes ().iterator ();
        while (it.hasNext ())
            readKeymap (profile, (String) it.next (), true, result);
        return Collections.unmodifiableMap (result);
    }

    public void deleteProfile (String profile) {
        KeyBindingSettingsFactory kbs = getKeyBindingSettings ("text/base");
        kbs.setKeyBindings (profile, null);
    }
    
    /**
     * Saves actionToShortcuts Map (GlobalAction > Set (String (shortcut)).
     * Ignores all non EditorAction actions.
     */
    public void saveKeymap (String profile, Map actionToShortcuts) {
        
        // 1) 
        // convert actionToShortcuts: Map (ShortcutAction > Set (String (shortcut AS-M)))
        // to mimeTypeToKeyBinding: Map (String (mimetype) > List (MultiKeyBinding)).
        Map mimeTypeToKeyBinding = new HashMap (); // editor shortcuts
        Iterator it = actionToShortcuts.keySet ().iterator ();
        while (it.hasNext ()) {
            ShortcutAction action = (ShortcutAction) it.next ();
            Set shortcuts = (Set) actionToShortcuts.get (action);

            action = action.getKeymapManagerInstance(EDITOR_BRIDGE);
            if (!(action instanceof EditorAction)) continue;
            EditorAction editorAction = (EditorAction) action;
            Set mimeTypes = getMimeTypes (editorAction);

            Iterator it2 = shortcuts.iterator ();
            while (it2.hasNext ()) {
                String shortcut = (String) it2.next ();
                MultiKeyBinding mkb = new MultiKeyBinding (
                    stringToKeyStrokes2 (shortcut),
                    editorAction.getId ()
                );
                Iterator it3 = mimeTypes.iterator ();
                while (it3.hasNext ()) {
                    String mimeType = (String) it3.next ();
                    List l = (List) mimeTypeToKeyBinding.get (mimeType);
                    if (l == null) {
                        l = new ArrayList ();
                        mimeTypeToKeyBinding.put (mimeType, l);
                    }
                    l.add (mkb);
                }
            }
        }
        
        // 2) save all shortcuts
        it = keyBindingSettings.keySet ().iterator ();
        while (it.hasNext ()) {
            String mimeType = (String) it.next ();
            KeyBindingSettingsFactory kbs = (KeyBindingSettingsFactory) keyBindingSettings.
                get (mimeType);
            //log ("changeKeymap.editorShortcuts " + mimeType, (List) mimeTypeToKeyBinding.get (mimeType));
            kbs.setKeyBindings (profile, (List) mimeTypeToKeyBinding.get (mimeType));
        }
    }
    
    
    // private methods .........................................................
    
    /** Map (String (mimeType) > Set (String (action name))). */
    private Map editorActionsMap;
    /** Map (ShortcutAction > Set (String (mimeType))). */
    private Map actionNameToMimeTypes = new HashMap ();
    
    /**
     * Returns map of all editor actions.
     * Map (String (mimeType) > Set (String (action name)))
     */
    private Map getEditorActionsMap () {
        if (editorActionsMap == null) {
            editorActionsMap = new HashMap ();
            Iterator it = getEditorSettings ().getMimeTypes ().iterator ();
            while (it.hasNext ())
                initActionMap ((String) it.next ());
            initActionMap ("text/base");
        }
        return editorActionsMap;
    }
    
    private Set getMimeTypes (EditorAction a) {
        getEditorActionsMap (); // initialization
        return (Set) actionNameToMimeTypes.get (a.getId ());
    }
    
    /**
     * Loads editor actions for given mimeType to editorActionsMap.
     */
    private void initActionMap (String mimeType) {
        
        // 1) get EditorKit
        EditorKit editorKit = null;
        if (mimeType.equals ("text/base")) {
            editorKit = BaseKit.getKit (NbEditorKit.class);
        } else {
            Lookup mimeLookup = MimeLookup.getLookup (MimePath.parse(mimeType));
            editorKit = (EditorKit) mimeLookup.lookup (EditorKit.class);
        }
        if (editorKit == null) {
            if (System.getProperty ("org.netbeans.optionsDialog") != null)
                System.out.println 
                    ("KeymapModel EditorKit not found for: " + mimeType);
            return;
        }
        
        // 2) copy actions from EditorKit to actionMap
        Action[] as = editorKit.getActions ();
        int i, k = as.length;
        for (i = 0; i < k; i++) {
            Object isHidden = as [i].getValue (BaseAction.NO_KEYBINDING);
            if (isHidden instanceof Boolean &&
                ((Boolean) isHidden).booleanValue ()
            )
                continue; // ignore hidden actions
            
            EditorAction action = new EditorAction ((TextAction) as [i]);
            String id = action.getId ();
            editorActionsMap.put (id, action);
            if (mimeType.equals ("text/base")) {
                actionNameToMimeTypes.put (id, Collections.singleton ("text/base"));
                continue;
            }
            Set s = (Set) actionNameToMimeTypes.get (id);
            if (s == null) {
                s = new HashSet ();
                actionNameToMimeTypes.put (id, s);
            }
            s.add (mimeType);
        }
    }
    
    private EditorSettings editorSettings;
    
    private EditorSettings getEditorSettings () {
        if (editorSettings == null)
            editorSettings = EditorSettings.getDefault ();
        return editorSettings;
    }
    
    private Map keyBindingSettings = new HashMap ();
    private KeyBindingSettingsFactory getKeyBindingSettings (String mimeType) {
        if (keyBindingSettings.containsKey (mimeType))
            return (KeyBindingSettingsFactory) keyBindingSettings.get (mimeType);
        KeyBindingSettingsFactory kbs = EditorSettings.getDefault ().
            getKeyBindingSettings (new String[] {mimeType});
        keyBindingSettings.put (mimeType, kbs);
        getListener ().add (kbs);
        return kbs;
    }
    
    private Listener listener;
    private Listener getListener () {
        if (listener == null) 
            listener = new Listener (this);
        return listener;
    }
    
    private static class Listener implements PropertyChangeListener {
        
        private WeakReference model;
        private Set settings = new HashSet ();
        
        Listener (EditorBridge model) {
            this.model = new WeakReference (model);
        }
        
        void add (KeyBindingSettingsFactory settings) {
            this.settings.add (settings);
            settings.addPropertyChangeListener (this);
        }
        
        private EditorBridge getModel () {
            EditorBridge m = (EditorBridge) model.get ();
            if (m != null) return m;
            Iterator it = settings.iterator ();
            while (it.hasNext ()) 
                ((KeyBindingSettingsFactory) it.next ()).
                    removePropertyChangeListener (this);
            settings = new HashSet ();
            return null;
        }
        
        public void propertyChange (PropertyChangeEvent evt) {
            EditorBridge model = getModel ();
            if (model == null) return;
            //model.keyMaps = new HashMap ();
        }
    }
    
    /**
     * Reads keymap for given mimetype and profile to given map 
     * Map (ShortcutAction > Set (String (shortcut)))
     */
    private void readKeymap (
        String profile, 
        String mimeType, 
        boolean defaults,
        Map map
    ) {
        // 1) get list of MultiKeyBindings
        KeyBindingSettingsFactory kbs = getKeyBindingSettings (mimeType);
        if (kbs == null) return;
        List keyBindings = defaults ? 
            kbs.getKeyBindingDefaults (profile) :
            kbs.getKeyBindings (profile);
        if (keyBindings == null) return;

        // 2) create Map (String (action name) > Set (String (shortcut)))
        Map actionNameToShortcuts = convertKeymap (keyBindings);

        // 3) create Map (EditorAction > Set (String (shortcut)))
        Iterator it = actionNameToShortcuts.keySet ().iterator ();
        while (it.hasNext ()) {
            String actionName = (String) it.next ();
            Set keyStrokes = (Set) actionNameToShortcuts.get 
                (actionName);
            ShortcutAction action = (ShortcutAction) getEditorActionsMap ().get (actionName);
            if (action == null) {
                if (System.getProperty ("org.netbeans.optionsDialog") != null)
                    System.out.println ("action not found " + actionName);
                continue;
            }
            Set s = (Set) map.get (action);
            if (s == null)
                map.put (action, keyStrokes);
            else
                s.addAll (keyStrokes);
        }
    }
    
    /**
     * create Map (String (action name) > Set (String (shortcut AS-M)))
     *
     * @param keyBindings list of MultiKeyBindings
     */
    private static Map convertKeymap (List keyBindings) {
        Map actionNameToShortcuts = new HashMap ();
        int i, k = keyBindings.size ();
        for (i = 0; i < k; i++) {
            MultiKeyBinding mkb = (MultiKeyBinding) keyBindings.get (i);
            String keyStroke = "";
            Iterator it = mkb.getKeyStrokeList ().iterator ();
            if (it.hasNext ()) {
                StringBuffer sb = new StringBuffer 
                    (Utilities.keyToString ((KeyStroke) it.next ()));
                while (it.hasNext ())
                    sb.append (' ').append 
                        (Utilities.keyToString ((KeyStroke) it.next ()));
                keyStroke = sb.toString ();
            }

            Set keyStrokes = (Set) actionNameToShortcuts.get 
                (mkb.getActionName ());
            if (keyStrokes == null) {
                keyStrokes = new HashSet ();
                actionNameToShortcuts.put (mkb.getActionName (), keyStrokes);
            }
            keyStrokes.add (keyStroke);
            //S ystem.out.println("  " + mkb.actionName + " : " + keyStroke);
        }
        return actionNameToShortcuts;
    }

    private static Map readCategories () {
        Map result = new HashMap ();
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        FileObject fo = fs.findResource ("OptionsDialog/Actions");
        if (fo == null) return result;
        FileObject[] categories = fo.getChildren ();
        int i, k = categories.length;
        for (i = 0; i < k; i++) {
            String categoryName = categories [i].getName ();
            String bundleName = (String) categories [i].getAttribute 
                ("SystemFileSystem.localizingBundle");
            if (bundleName != null)
                try {
                    categoryName = NbBundle.getBundle (bundleName).getString (
                        categories [i].getPath ()
                    );
                } catch (MissingResourceException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            FileObject[] actions = categories [i].getChildren ();
            int j, jj = actions.length;
            for (j = 0; j < jj; j++) {
                if (actions [j].getExt ().length () > 0) continue;
                String actionName = actions [j].getName ();
                result.put (actionName, categoryName);
            }
        }
        return result;
    }

    public List getProfiles() {
        return null;
    }
    
    static KeyStroke[] stringToKeyStrokes2 (String key) {
        StringTokenizer st = new StringTokenizer (key, " ");
        List result = new ArrayList ();
        key = null;
        while (st.hasMoreTokens ()) {
            String ks = st.nextToken ().trim ();
            KeyStroke keyStroke = Utilities.stringToKey (ks);
            //S ystem.out.println("1 " + ks + ">" + keyStroke);
            if (keyStroke == null) {
                if (System.getProperty ("org.netbeans.optionsDialog") != null)
                    System.out.println("no key stroke for:" + key);
                return null;
            }
            result.add (keyStroke);
        }
        return (KeyStroke[]) result.toArray 
            (new KeyStroke [result.size ()]);
    }
   
    public static class EditorAction implements ShortcutAction {
        private TextAction action;
        private String name;
        private String id;
        private String delegaitngActionId;
        
        public EditorAction (TextAction a) {
            action = a;
        }
        
        public String getDisplayName () {
            if (name == null) {
                name = (String) action.getValue (Action.SHORT_DESCRIPTION);
                name = name.replaceAll ("&", "").trim ();
            }
            return name;
        }
        
        public String getId () {
            if (id == null)
                id = (String) action.getValue (Action.NAME);
            return id;
        }
        
        public String getDelegatingActionId () {
            if (delegaitngActionId == null)
                delegaitngActionId = (String) action.getValue 
                    (NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
            return delegaitngActionId;
        }
        
        public boolean equals (Object o) {
            if (!(o instanceof EditorAction)) return false;
            return ((EditorAction) o).getId ().equals (getId ());
        }
        
        public int hashCode () {
            return getId ().hashCode ();
        }
        
        public String toString () {
            return "EditorAction[" + getDisplayName() + ":" + getId() + "]";
        }
    
        public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
            if (EDITOR_BRIDGE.equals(keymapManagerName)) {
                return this;
            }
            return null;
        }
}
}
