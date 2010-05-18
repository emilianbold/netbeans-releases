/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public final class KitchenSink {

    private static final Logger LOG = Logger.getLogger(KitchenSink.class.getName());
    
    public static final String JAVATYPE_KEY_PREFIX = "nbeditor-javaType-for-legacy-setting_"; //NOI18N
    
    /**
     * Gets a value of a setting from editor Preferences by guessing its type.
     * 
     * @param settingName
     * @param prefs
     * 
     * @return The value or <code>null</code>
     */
    public static Object getValueFromPrefs(String settingName, Preferences prefs, MimePath mimePath) {
        Object value = null;
        
        if (prefs != null && null != prefs.get(settingName, null)) {
            // try guessing the type
            Class type = null;
            String javaType = prefs.get(JAVATYPE_KEY_PREFIX + settingName, null);
            if (javaType != null) {
                type = typeFromString(javaType);
            }

            if (type != null) {
                if (type.equals(SettingsConversions.class)) {
                    value = SettingsConversions.callFactory(prefs, mimePath, settingName, null);
                } else if (type.equals(Boolean.class)) {
                    value = prefs.getBoolean(settingName, false);
                } else if (type.equals(Integer.class)) {
                    value = prefs.getInt(settingName, 0);
                } else if (type.equals(Long.class)) {
                    value = prefs.getLong(settingName, 0L);
                } else if (type.equals(Float.class)) {
                    value = prefs.getFloat(settingName, 0.0F);
                } else if (type.equals(Double.class)) {
                    value = prefs.getDouble(settingName, 0.0D);
                } else if (type.equals(Insets.class)) {
                    value = SettingsConversions.parseInsets(prefs.get(settingName, null));
                } else if (type.equals(Dimension.class)) {
                    value = SettingsConversions.parseDimension(prefs.get(settingName, null));
                } else if (type.equals(Color.class)) {
                    value = SettingsConversions.parseColor(prefs.get(settingName, null));
                } else if (type.equals(String.class)) {
                    value = prefs.get(settingName, null);
                } else {
                    LOG.log(Level.WARNING, "Can't load setting '" + settingName + "' with value '" + prefs.get(settingName, null) //NOI18N
                        + "' through org.netbeans.editor.Settings! Unsupported value conversion to " + type, new Throwable("Stacktrace")); //NOI18N
                }
            } else {
                // unknown setting type, treat it as String
                LOG.warning("Can't determine type of '" + settingName + "' editor setting. If you supplied this setting" //NOI18N
                    + " through the editor implementation of java.util.prefs.Preferences you should use the 'javaType'" //NOI18N
                    + " attribute and specify the class representing values of this setting. There seem to be legacy" //NOI18N
                    + " clients accessing your setting through the old org.netbeans.editor.Settings."); //NOI18N
            }
        }
        
        return value;
    }

    /**
     * Sets a setting's value to Preferences by guessing the type from the value.
     * 
     * @param settingName
     * @param newValue
     * @param prefs
     * 
     * @return <code>true</code> if the value was set successfully, otherwise <code>false</code>.
     */
    public static boolean setValueToPreferences(String settingName, Object value, Preferences prefs, MimePath mimePath) {
        if (value != null) {
            if (value instanceof Boolean) {
                prefs.putBoolean(settingName, (Boolean) value);
            } else if (value instanceof Integer) {
                prefs.putInt(settingName, (Integer) value);
            } else if (value instanceof Long) {
                prefs.putLong(settingName,(Long) value);
            } else if (value instanceof Float) {
                prefs.putFloat(settingName, (Float) value);
            } else if (value instanceof Double) {
                prefs.putDouble(settingName, (Double) value);
            } else if (value instanceof Insets) {
                prefs.put(settingName, SettingsConversions.insetsToString((Insets) value));
                prefs.put(JAVATYPE_KEY_PREFIX + settingName, Insets.class.getName());
            } else if (value instanceof Dimension) {
                prefs.put(settingName, SettingsConversions.dimensionToString((Dimension) value));
                prefs.put(JAVATYPE_KEY_PREFIX + settingName, Dimension.class.getName());
            } else if (value instanceof Color) {
                prefs.put(settingName, SettingsConversions.color2String((Color) value));
                prefs.put(JAVATYPE_KEY_PREFIX + settingName, Color.class.getName());
            } else if (value instanceof String) {
                prefs.put(settingName, (String) value);
            } else {
                LOG.log(Level.FINE, "Can't save setting '" + settingName + "' with value '" + value //NOI18N
                    + "' through org.netbeans.editor.Settings; unsupported value conversion!", new Throwable("Stacktrace")); //NOI18N
                return false;
            }
        } else {
            prefs.remove(settingName);
        }

        return true;
    }
    
    private static Class typeFromString(String javaType) {
        if ("methodvalue".equals(javaType)) { //NOI18N
            return SettingsConversions.class;
        } else {
            try {
                ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
                return classLoader == null ? null : classLoader.loadClass(javaType);
            } catch (ClassNotFoundException cnfe) {
                LOG.log(Level.WARNING, null, cnfe);
                return null;
            }
        }
    }

    // -----------------------------------------------------------------------
    // 'indentEngine' setting
    // -----------------------------------------------------------------------

    /** This factory method is here to produce value for 'indentEngine' settings,
     * which is still supplied (thru BaseOptions.getDefaultIndentEngineClass) by some modules.
     */
    public static final Object getIndentEngineValue(MimePath mimePath, String settingName) {
        assert settingName.equals(NbEditorDocument.INDENT_ENGINE) : "The getIndentEngineValue factory called for '" + settingName + "'"; //NOI18N
        BaseOptions bo = MimeLookup.getLookup(mimePath).lookup(BaseOptions.class);
        return bo != null ? bo.getIndentEngine() : null;
    }
}
