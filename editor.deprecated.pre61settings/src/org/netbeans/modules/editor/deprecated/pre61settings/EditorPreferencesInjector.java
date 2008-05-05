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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings.Initializer;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;

/**
 *
 * @author vita
 */
public final class EditorPreferencesInjector extends StorageFilter<String, TypedValue> implements PropertyChangeListener {

    public EditorPreferencesInjector() {
        super("Preferences"); //NOI18N
        // Settings uses WeakListenerList and holds all listeners by WeakReference
        OrgNbEditorAccessor.get().Settings_addPropertyChangeListener(this);
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
    public void afterLoad(Map<String, TypedValue> preferences, MimePath mimePath, String profile, boolean defaults) {
        Class kitClass = null;
        
        if (mimePath.size() > 0) {
            EditorKit kit = MimeLookup.getLookup(mimePath).lookup(EditorKit.class);
            if (kit != null) {
                kitClass = kit.getClass();
            }
        } else {
            kitClass = BaseKit.class;
        }

        if (kitClass == null) {
            // No kit, no settings
            return;
        }
        
        // Go through all the initializers and collect their settings
        Map map = new HashMap();
        currentSettingsMap.set(map);
        OrgNbEditorAccessor.get().Settings_interceptSetValue(this);
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
            OrgNbEditorAccessor.get().Settings_interceptSetValue(null);
            currentSettingsMap.remove();
        }
        
        for(Object key : map.keySet()) {
            if (!(key instanceof String)) {
                // wrong key, this should not normally happen, but who knows what crap the initializers may supply
                continue;
            }

            String settingName = (String) key;
            if (preferences.containsKey(settingName)) {
                // legacy settings will never overwrite new-style settings
                continue;
            }

            Object value = map.get(settingName);
            if (value == null) {
                // null value means the setting is not set
                continue;
            }
            
            if (value instanceof Boolean) {
                preferences.put(settingName, new TypedValue(
                        ((Boolean) value).toString(), Boolean.class.getName()));
                
            } else if (value instanceof Integer) {
                preferences.put(settingName, new TypedValue(
                        ((Integer) value).toString(), Integer.class.getName()));
                
            } else if (value instanceof Long) {
                preferences.put(settingName, new TypedValue(
                        ((Long) value).toString(), Long.class.getName()));
                
            } else if (value instanceof Float) {
                preferences.put(settingName, new TypedValue(
                        ((Float) value).toString(), Float.class.getName()));
                
            } else if (value instanceof Double) {
                preferences.put(settingName, new TypedValue(
                        ((Double) value).toString(), Double.class.getName()));
                
            } else if (value instanceof Insets) {
                preferences.put(settingName, new TypedValue(
                        SettingsConversions.insetsToString((Insets) value), Insets.class.getName()));
                
            } else if (value instanceof Dimension) {
                preferences.put(settingName, new TypedValue(
                        SettingsConversions.dimensionToString((Dimension) value), Dimension.class.getName()));
                
            } else if (value instanceof Color) {
                preferences.put(settingName, new TypedValue(
                        SettingsConversions.color2String((Color) value), Color.class.getName()));
                
            } else if (value instanceof String) {
                preferences.put(settingName, new TypedValue(
                        (String) value, String.class.getName()));
            } else {
                synchronized (COMPLEX) {
                    Map<String, Object> settings = COMPLEX.get(mimePath);
                    if (settings == null) {
                        settings = new HashMap<String, Object>();
                        COMPLEX.put(mimePath, settings);
                    }
                    settings.put(settingName, value);
                }
                preferences.put(settingName, new TypedValue(
                        getClass().getName() + ".getComplexSettingValue", "methodvalue")); //NOI18N
            }
        }
    }

    @Override
    public void beforeSave(Map<String, TypedValue> map, MimePath mimePath, String profile, boolean defaults) {
        // let's save everything we may have added
    }

    // ------------------------------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------------------------------
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || "initializers".equals(evt.getPropertyName())) { //NOI18N
            notifyChanges();
        }
    }

    // ------------------------------------------------------------------------
    // Complex value settings factory
    // ------------------------------------------------------------------------
    
    public static Object getComplexSettingValue(MimePath mimePath, String settingName) {
        List<String> paths = null;
        if (mimePath.size() > 0) {
            try {
                Method m = MimePath.class.getDeclaredMethod("getInheritedPaths", String.class, String.class); //NOI18N
                m.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<String> ret = (List<String>) m.invoke(mimePath, null, null);
                paths = ret;
                assert paths.size() > 1 : "Wrong getInheritedPaths result size: " + paths.size(); //NOI18N
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Can't call org.netbeans.api.editor.mimelookup.MimePath.getInheritedPaths method.", e); //NOI18N
            }
        }
        
        if (paths == null) {
            paths = Collections.singletonList(mimePath.getPath());
        }
        
        synchronized (COMPLEX) {
            for (String path : paths) {
                Map<String, Object> settings = COMPLEX.get(MimePath.parse(path));
                if (settings != null) {
                    Object value = settings.get(settingName);
                    if (value != null) {
                        return value;
                    }
               }
            }
            
            return null;
        }
    }
    
    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(EditorPreferencesInjector.class.getName());

    private static final Map<MimePath, Map<String, Object>> COMPLEX = new WeakHashMap<MimePath, Map<String, Object>>();
    private final ThreadLocal<Map> currentSettingsMap = new ThreadLocal<Map>();
    
} // End of EditorPreferencesInjector class
