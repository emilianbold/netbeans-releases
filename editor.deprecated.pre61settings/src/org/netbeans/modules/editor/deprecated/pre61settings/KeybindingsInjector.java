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

package org.netbeans.modules.editor.deprecated.pre61settings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.Settings.Initializer;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;

/**
 *
 * @author vita
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageFilter.class)
public final class KeybindingsInjector extends StorageFilter<Collection<KeyStroke>, MultiKeyBinding> implements PropertyChangeListener, SettingsChangeListener {

    public KeybindingsInjector() {
        super("Keybindings"); //NOI18N
        // Settings uses WeakListenerList and holds all listeners by WeakReference
        OrgNbEditorAccessor.get().Settings_addPropertyChangeListener(this);
        OrgNbEditorAccessor.get().Settings_addSettingsChangeListener(this);
    }

    public void capturedSetValue(Class kitClass, String settingName, Object value) {
        Map map = currentSettingsMap.get();
        assert map != null : "The current settings map should not be null"; //NOI18N
        map.put(settingName, value);
    }
    
    // ------------------------------------------------------------------------
    // StorageFilter implementation
    // ------------------------------------------------------------------------
    
    @Override
    public void afterLoad(Map<Collection<KeyStroke>, MultiKeyBinding> keybindings, MimePath mimePath, String profile, boolean defaults) {
        
        Class kitClass = null;
        
        if (mimePath.size() == 0) {
            kitClass = BaseKit.class;
        } else if (mimePath.size() == 1) {
            ignoreInitializerChanges.set(true);
            try {
                kitClass = KitsTracker.getInstance().findKitClass(mimePath.getPath());
            } finally {
                ignoreInitializerChanges.remove();
            }
        }

        if (kitClass == null) {
            // No kit, no settings
            return;
        }
        
        // Go through all the initializers and collect their settings
        Map map = new HashMap();
        currentSettingsMap.set(map);
//        OrgNbEditorAccessor.get().Settings_interceptSetValue(this);
        try {
            List [] lists = OrgNbEditorAccessor.get().Settings_getListsOfInitializers();
            for (int i = 0; i < lists.length; i++) {
                Iterator it = ((List) lists[i]).iterator();
                while (it.hasNext()) {
                    Initializer initializer = (Initializer)it.next();

                    // A call to initializer shouldn't break the whole updating
                    try {
                        initializer.updateSettingsMap(kitClass, map);
                    } catch (Throwable t) {
                        LOG.log(Level.WARNING, null, t);
                    }
                }
            }
        } finally {
//            OrgNbEditorAccessor.get().Settings_interceptSetValue(null);
            currentSettingsMap.remove();
        }
        
        @SuppressWarnings("unchecked")
        List<org.netbeans.editor.MultiKeyBinding> legacyKeybindings = (List<org.netbeans.editor.MultiKeyBinding>) map.get(SettingsNames.KEY_BINDING_LIST);

        if (legacyKeybindings != null) {
            for(org.netbeans.editor.MultiKeyBinding legacyMkb : legacyKeybindings) {
                if (legacyMkb.actionName == null || legacyMkb.actionName.length() == 0 ||
                    (legacyMkb.key == null && (legacyMkb.keys == null || legacyMkb.keys.length == 0))
                ) {
                    // ignore invalid keybinding
                    continue;
                }

                if (legacyMkb.keys != null) {
                    if (!keybindings.containsKey(Arrays.asList(legacyMkb.keys))) {
                        MultiKeyBinding mkb = new MultiKeyBinding(legacyMkb.keys, legacyMkb.actionName);
                        keybindings.put(mkb.getKeyStrokeList(), mkb);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Injecting: " + mkb.getKeyStrokeList() //NOI18N
                                    + " bound to '" + mkb.getActionName() //NOI18N
                                    + "' for '" + mimePath.getPath() //NOI18N
                                    + "', profile '" + profile + "'"); //NOI18N
                        }
                    }
                } else {
                    if (!keybindings.containsKey(Collections.singleton(legacyMkb.key))) {
                        MultiKeyBinding mkb = new MultiKeyBinding(legacyMkb.key, legacyMkb.actionName);
                        keybindings.put(mkb.getKeyStrokeList(), mkb);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Injecting: " + mkb.getKeyStrokeList() //NOI18N
                                    + " bound to '" + mkb.getActionName() //NOI18N
                                    + "' for '" + mimePath.getPath() //NOI18N
                                    + "', profile '" + profile + "'"); //NOI18N
                        }
                    }
                }
            }
        }
    }

    @Override
    public void beforeSave(Map<Collection<KeyStroke>, MultiKeyBinding> map, MimePath mimePath, String profile, boolean defaults) {
        // let's save everything we may have added
    }

    // ------------------------------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------------------------------
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || "initializers".equals(evt.getPropertyName())) { //NOI18N
            if (!ignoreInitializerChanges.get()) {
                LOG.fine("Settings.Initializers changed"); //NOI18N
                safeNotifyChanges();
            }
        }
    }

    // ------------------------------------------------------------------------
    // SettingsChangeListener implementation
    // ------------------------------------------------------------------------
    
    public void settingsChange(SettingsChangeEvent evt) {
        if (evt.getSettingName() == null || 
            SettingsNames.KEY_BINDING_LIST.equals(evt.getSettingName()) || 
            SettingsNames.CUSTOM_ACTION_LIST.equals(evt.getSettingName())
        ) {
            LOG.fine("settingsChange(" + evt.getSettingName() + ")"); //NOI18N
            safeNotifyChanges();
        }
    }
    
    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(EditorPreferencesInjector.class.getName());

    private final ThreadLocal<Map> currentSettingsMap = new ThreadLocal<Map>();
    private volatile int ourOwnChangeNotification = 0;
    
    // to prevent deadlocks caused by EditorKits that hook up settings initializers
    // from their constructor
    private final ThreadLocal<Boolean> ignoreInitializerChanges = new ThreadLocal<Boolean>() {
        protected @Override Boolean initialValue() {
            return false;
        }
    };

    private void safeNotifyChanges() {
        if (ourOwnChangeNotification == 0) {
            ourOwnChangeNotification++;
            try {
                Settings.update(new Runnable() {
                    public void run() {
                        notifyChanges();
                    }
                    public boolean asynchronous() {
                        return true;
                    }
                });
            } finally {
                ourOwnChangeNotification--;
            }
        }
    }
    
} // End of EditorPreferencesInjector class
