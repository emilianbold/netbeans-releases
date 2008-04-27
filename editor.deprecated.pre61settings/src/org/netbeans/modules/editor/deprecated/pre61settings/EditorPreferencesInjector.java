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
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public final class EditorPreferencesInjector extends StorageFilter<String, TypedValue> {

    private static final Logger LOG = Logger.getLogger(EditorPreferencesInjector.class.getName());
    
    private final ThreadLocal<Map> currentSettingsMap = new ThreadLocal<Map>();
    
    public EditorPreferencesInjector() {
        super("Preferences"); //NOI18N
    }

    public void capturedSetValue(Class kitClass, String settingName, Object value) {
        Map map = currentSettingsMap.get();
        assert map != null : "The current settings map should not be null"; //NOI18N
        map.put(settingName, value);
    }
    
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
                continue;
            }

            String settingName = (String) key;
            if (preferences.containsKey(settingName)) {
                // legacy settings will never overwrite new-style settings
                continue;
            }

            Object value = map.get(settingName);
            String javaTypeKey = OrgNbEditorAccessor.get().Settings_JAVATYPE_KEY_PREFIX() + settingName;
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
            }
        }
    }

    @Override
    public void beforeSave(Map<String, TypedValue> map, MimePath mimePath, String profile, boolean defaults) throws IOException {
    }

} // End of EditorPreferencesInjector class
