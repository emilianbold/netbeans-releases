/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.lib2.actions;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;


/**
 * Various utility methods for declarative editor action registrations.
 */
public final class EditorActionUtilities {

    // -J-Dorg.netbeans.modules.editor.lib2.actions.EditorActionUtilities.level=FINEST
    private static final Logger LOG = Logger.getLogger(EditorActionUtilities.class.getName());

    private static Map<String,Map<String,KeyStroke>> mimeType2actionName2KeyStroke;

    private static Map<String,Boolean> mimeType2ListenerPresent = new HashMap<String, Boolean>();

    private static Reference<EditorKit> globalActionsKitRef;

    private static LookupListener globalActionsKitListener;

    private static final Map<EditorKit,SearchableEditorKit> kit2searchable = new WeakHashMap<EditorKit,SearchableEditorKit>();

    private EditorActionUtilities() {
        // No instances
    }

    public static EditorKit getGlobalActionsKit() {
        synchronized (kit2searchable) {
            EditorKit globalKit = (globalActionsKitRef != null) ? globalActionsKitRef.get() : null;
            if (globalKit == null) {
                Lookup.Result<EditorKit> result = MimeLookup.getLookup("").lookupResult(EditorKit.class);
                Iterator<? extends EditorKit> instancesIterator = result.allInstances().iterator();
                globalKit = instancesIterator.hasNext() ? instancesIterator.next() : null;
                if (globalKit == null) {
                    globalKit = SearchableEditorKitImpl.createGlobalKit();
                }
                if (globalKit != null) {
                    globalActionsKitRef = new WeakReference<EditorKit>(globalKit);
                }
                if (globalActionsKitListener == null) {
                    globalActionsKitListener = new LookupListener() {
                        public void resultChanged(LookupEvent evt) {
                            synchronized (kit2searchable) {
                                globalActionsKitRef = null;
                            }
                        }
                    };
                    result.addLookupListener(globalActionsKitListener);
                }
            }
            return globalKit;
        }
    }

    public static EditorKit getKit(String mimeType) {
        Lookup.Result<EditorKit> result = MimeLookup.getLookup(mimeType).lookupResult(EditorKit.class);
        Iterator<? extends EditorKit> instancesIterator = result.allInstances().iterator();
        EditorKit kit = instancesIterator.hasNext() ? instancesIterator.next() : null;
        return kit;
    }

    /**
     * Register an instance of searchable kit explicitly for the given kit.
     * Used by BaseKit for explicit registration.
     * @param kit non-null kit.
     * @param searchableKit non-null searchable kit.
     */
    public static void registerSearchableKit(EditorKit kit, SearchableEditorKit searchableKit) {
        synchronized (kit2searchable) {
            kit2searchable.put(kit, searchableKit);
        }
    }

    /**
     * Get an editor action in a constant time (wrap a kit with a SearchableEditorKit if necessary).
     *
     * @param kit non-null kit.
     * @param actionName non-null action name.
     * @return action's instance or null.
     */
    public static Action getAction(EditorKit kit, String actionName) {
        return getSearchableKit(kit).getAction(actionName);
    }

    /**
     * Get searchable editor kit for the given kit.
     * @param kit non-null kit.
     * @return non-null searchable kit.
     */
    public static SearchableEditorKit getSearchableKit(EditorKit kit) {
        SearchableEditorKit searchableKit;
        if (kit instanceof SearchableEditorKit) {
            searchableKit = ((SearchableEditorKit)kit);
        } else {
            synchronized (kit2searchable) {
                searchableKit = kit2searchable.get(kit);
                if (searchableKit == null) {
                    searchableKit = new DefaultSearchableKit(kit);
                    registerSearchableKit(kit, searchableKit);
                }
            }
        }
        return searchableKit;
    }

    public static Lookup.Result<Action> createActionsLookupResult(String mimeType) {
        if (!MimePath.validate(mimeType)) {
            throw new IllegalArgumentException("Ïnvalid mimeType=\"" + mimeType + "\"");
        }
        Lookup lookup = Lookups.forPath(getPath(mimeType, "Actions"));
        return lookup.lookupResult(Action.class);
    }

    private static String getPath(String mimeType, String subFolder) {
        StringBuilder path = new StringBuilder(50);
        path.append("Editors/");
        if (mimeType.length() > 0) {
            path.append('/').append(mimeType);
        }
        if (subFolder.length() > 0) {
            path.append('/').append(subFolder);
        }
        return path.toString();
    }

    public static Preferences getGlobalPreferences() {
        Lookup globalMimeLookup = MimeLookup.getLookup(MimePath.EMPTY);
        return (globalMimeLookup != null) ? globalMimeLookup.lookup(Preferences.class) : null;
    }

    /**
     * Get single-key accelerator for a given declared action.
     * Only a single-key accelerators are supported.
     */
    public static KeyStroke getAccelerator(FileObject fo) {
        if (fo == null) {
            throw new IllegalArgumentException("Must be called with non-null fileObject"); // NOI18N
        }
        boolean fineLoggable = LOG.isLoggable(Level.FINE);
        String path = fo.getParent().getPath();
        String actionName = (String) fo.getAttribute(Action.NAME);
        KeyStroke ks = null;
        if (path.startsWith("Editors/")) {
            path = path.substring(7); // Leave ending '/' to support "Editors/Actions"
            if (path.endsWith("/Actions")) {
                path = path.substring(0, path.length() - 8);
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                String mimeType = path;
                if (!MimePath.validate(mimeType)) {
                    LOG.info("Invalid mime-type='" + mimeType + "' of action's fileObject=" + fo); // NOI18N
                }
                ks = getAccelerator(mimeType, actionName);
            } else if (fineLoggable) {
                LOG.fine("No \"/Actions\" at end of mime-type='" + path +
                    "' of action's fileObject=" + fo); // NOI18N
            }
        } else if (fineLoggable) {
            LOG.fine("No \"Editors/\" at begining of mime-type='" + path + // NOI18N
                    "' of action's fileObject=" + fo); // NOI18N
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Accelerator for action \"" + actionName + "\" is " + ks);
        }
        return ks;
    }

    /**
     * Get single-key accelerator for a given declared action.
     * <br/>
     * Unfortunately currently there's no easy way to display multi-keybinding in menu-item
     * (there's just JMenuItem.setAccelerator() and its impl is L&F-based)
     * so just display single-keystroke accelerators.
     */
    public static KeyStroke getAccelerator(String mimeType, String actionName) {
        KeyStroke ks = null;
        if (actionName != null) {
            synchronized (EditorActionUtilities.class) {
                if (mimeType2actionName2KeyStroke == null) {
                    mimeType2actionName2KeyStroke = new HashMap<String,Map<String,KeyStroke>>();
                }
                Map<String,KeyStroke> actionName2KeyStrokeList = mimeType2actionName2KeyStroke.get(mimeType);
                if (actionName2KeyStrokeList == null) {
                    actionName2KeyStrokeList = new HashMap<String,KeyStroke>();
                    Lookup.Result<KeyBindingSettings> result = MimeLookup.getLookup(mimeType).lookupResult(
                            KeyBindingSettings.class);
                    Collection<? extends KeyBindingSettings> instances = result.allInstances();
                    if (!instances.isEmpty()) {
                        KeyBindingSettings kbs = instances.iterator().next();
                        for (MultiKeyBinding kb : kbs.getKeyBindings()) {
                            if (!actionName2KeyStrokeList.containsKey(kb.getActionName())
                                && kb.getKeyStrokeCount() == 1)
                            {
                                actionName2KeyStrokeList.put(kb.getActionName(), kb.getKeyStroke(0));
                            }
                        }
                    }
                    mimeType2actionName2KeyStroke.put(mimeType, actionName2KeyStrokeList);
                    // Ensure listening on changes in keybinding settings
                    if (!Boolean.TRUE.equals(mimeType2ListenerPresent.get(mimeType))) {
                        mimeType2ListenerPresent.put(mimeType, true);
                        result.addLookupListener(KeyBindingSettingsListener.INSTANCE);
                    }
                }
                ks = actionName2KeyStrokeList.get(actionName);
            }
        }
        return ks;
    }

    private static final class KeyBindingSettingsListener implements LookupListener {
        
        static final KeyBindingSettingsListener INSTANCE = new KeyBindingSettingsListener();

        private KeyBindingSettingsListener() {
        }

        public void resultChanged(LookupEvent ev) {
            synchronized (EditorActionUtilities.class) {
                mimeType2actionName2KeyStroke = null;
                LOG.fine("mimeType2actionName2KeyStroke cleared."); // NOI18N
            }
        }

    }

    private static final class DefaultSearchableKit implements SearchableEditorKit {
        
        private final Map<String,Reference<Action>> name2actionRef = new WeakHashMap<String,Reference<Action>>();

        DefaultSearchableKit(EditorKit kit) {
            for (Action action : kit.getActions()) {
                if (action != null) {
                    name2actionRef.put((String)action.getValue(Action.NAME), new WeakReference<Action>(action));
                }
            }
        }

        public Action getAction(String actionName) {
            Reference<Action> actionRef = name2actionRef.get(actionName);
            return (actionRef != null) ? actionRef.get() : null;
        }

        public void addActionsChangeListener(ChangeListener listener) {
        }

        public void removeActionsChangeListener(ChangeListener listener) {
        }


    }

}
