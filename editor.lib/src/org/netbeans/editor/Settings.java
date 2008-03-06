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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.api.editor.settings.CodeTemplateSettings;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Configurable settings that editor uses. All the methods are static
 * The editor is configurable mainly by using the following static
 * method in Settings class:
 *
 *   org.netbeans.editor.Settings.setValue(Class kitClass, String settingName, Object newValue);
 *
 * kitClass - this is the class of the editor kit for which the setting is changed.
 *   The current hierarchy of editor kits starts
 *   with the <tt>org.netbeans.editor.BaseKit</tt> kit, the begining of the whole
 *   kit hierarchy. There should be a different editor kit for each mime-type.
 *
 *   When the particular setting is not set foar a given kit, then the superclass of
 *   the given kit class is retrieved and the search for the setting value is performed.
 *   Example: If the java document calls Settings.getValue() to retrieve the value
 *   for TAB_SIZE setting and it passes JavaKit.class as the kitClass
 *   parameter and the setting has no value on this level, then the super class
 *   of the JavaKit is retrieved (by using Class.getSuperclass() call) which is BaseKit
 *   in this case and the search for the value of TAB_SIZE setting
 *   is performed again. It is finished by reaching the null value for the kitClass.
 *   The null value can be also used as the kitClass parameter value.
 *   In a more general look not only the kit-class hierarchy could be used
 *   in <tt>Settings</tt>. Any class inheritance hierarchy could be used here
 *   having the null as the common root.
 *
 *   This way the inheritance of the setting values is guaranteed. By changing
 *   the setting value on the BaseKit level (or even on the null level),
 *   all the kit classes that don't
 *   override the particular setting are affected.
 *
 * settingName - name of the setting to change. The base setting names
 *   are defined as public String constants in <tt>SettingsNames</tt> class.
 *   The additional packages that extend the basic editor functionality
 *   can define additional setting names.
 *
 * newValue - new value for the setting. It must be always an object even
 *   if the setting is logicaly the basic datatype such as int (java.lang.Integer
 *   would be used in this case). A particular class types that can be used for
 *   the value of the settings are documented for each setting.
 *
 * WARNING! Please read carefully the description for each option you're
 *   going to change as you can make the editor stop working if you'll
 *   change the setting in a wrong way.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class Settings {

    private static final Logger LOG = Logger.getLogger(Settings.class.getName());
    private static final boolean LOG_STACTRACES = Boolean.getBoolean(Settings.class.getName() + ".stacktraces"); //NOI18N
    
    /** Core level used by the settings initializers. This is the level used
     * for the base and ext editor packages initializers only.
     */
    public static final int CORE_LEVEL = 0;

    /** System level used by the settings initializers. This is the (default)
     * first level.
     * It should be used by all the modules that install the new kits
     * into the editor.
     */
    public static final int SYSTEM_LEVEL = 1;

    /** Extension level used by the settings initializers. This is the second
     * level. It should be used by all the modules that want to extend
     * or modify the settings but they don't install their own kits.
     * The example can be a module extending the popup menu of an existing
     * kit.
     */
    public static final int EXTENSION_LEVEL = 2;

    /** Option level used by the settings initializers. This is the third
     * level. It should be used by the visual options created by the IDE.
     */
    public static final int OPTION_LEVEL = 3;

    /** User level used by the settings initializers. This is the fourth level.
     * All the initializers with this level will be called AFTER
     * all the initializers at the system level. All the user custom
     * initializers should be added at this level to guarantee
     * they will overwrite the settings added by the system.
     */
    public static final int USER_LEVEL = 4;

    /** List of Initializers */
    private static final ArrayList initializerLists = new ArrayList();
    private static long initializerListsVersion = 0;
    private static List [] listsOfInitializers = null;
    private static long listsOfInitializersVersion = -1;

    /** Current initializer sorter. */
    private static InitializerSorter currentInitializerSorter;

    /** List of Filters */
    private static final Filter [] NULL_FILTERS = new Filter[0];
    private static final String FILTERS_LOCK = new String("Settings.FILTERS_LOCK"); //NOI18N
    private static volatile Filter [] filters = NULL_FILTERS;

    /** [kit-class, map-of-settings] pairs */
    private static final Map kit2Maps = new HashMap();

    /** Support for firing change events */
    private static final WeakEventListenerList listenerList = new WeakEventListenerList();

    private static volatile int firingEnabled = 0;

    /** Save repetitive creation of the empty maps using this variable.
     * [kit-class, map-of-settings] pairs
     */
    private static final HashMap emptyMaps = new HashMap();

    private static final RequestProcessor PROCESSOR = new RequestProcessor("org.netbeans.editor.Settings.PROCESSOR"); //NOI18N
    private static final RequestProcessor.Task RESET_TASK = PROCESSOR.create(new Runnable() {
        public void run() {
            synchronized (Settings.class) {
                kit2Maps.clear();
            }
            fireSettingsChange(null, null, null, null);
        }
    });

    private Settings() {
        // no instances allowed
    }

    /** Add the initializer at the system level and perform reset. */
    public static void addInitializer(Initializer i) {
        addInitializer(i, SYSTEM_LEVEL);
        reset();
    }

    /** Add initializer instance to the list of current initializers.
     * You can call reset() after adding the initializer to make sure
     * it will update the current settings with its values.
     * However all the changes
     * that were made explicitly by calling setValue() will be lost
     * in this case.
     *
     * @param i initializer to add to the current list of initializers
     * @param level initializer level. It defines in which order
     *  the initializers will be called. There are currently three levels
     *  <tt>CORE_LEVEL</tt>, <tt>SYSTEM_LEVEL</tt> and <tt>USER_LEVEL</tt>.
     *  It's guaranteed that initializers with the particular level
     *  will be called in the order shown above.
     *  The order of the initializers at the same
     *  level is given by the order of their addition.
     */
    public static void addInitializer(Initializer i, int level) {
        synchronized (initializerLists) {
            int size = initializerLists.size();
            for (int j = size; j <= level; j++) {
                initializerLists.add(new ArrayList());
            }
            ((List)initializerLists.get(level)).add(i);

            // Sort the initializers if there's a valid sorter
            if (currentInitializerSorter != null) {
                currentInitializerSorter.sort(initializerLists);
            }

            initializerListsVersion++;
        }
    }

    /** Remove the initializer of the given name from all the levels
     * where it occurs.
     * @param name name of the initializer sorter to remove.
     */
    public static void removeInitializer(String name) {
        synchronized (initializerLists) {
            Iterator itit = initializerLists.iterator();
            while (itit.hasNext()) {
                Iterator it = ((List)itit.next()).iterator();
                while (it.hasNext()) {
                    if (name.equals(((Initializer)it.next()).getName())) {
                        it.remove();
                    }
                }
            }

            // Sort the initializers if there's a valid sorter
            if (currentInitializerSorter != null) {
                currentInitializerSorter.sort(initializerLists);
            }

            initializerListsVersion++;
        }
    }

    /** Get the current initializer sorter. */
    public static InitializerSorter getInitializerSorter() {
        synchronized (initializerLists) {
            return currentInitializerSorter;
        }
    }

    /** Set the current initializer sorter. */
    public static void setInitializerSorter(InitializerSorter initializerSorter) {
        synchronized (initializerLists) {
            currentInitializerSorter = initializerSorter;
        }
    }

    private static List [] getListsOfInitializers() {
        synchronized (initializerLists) {
            if (listsOfInitializersVersion != initializerListsVersion) {
                List [] lists = (List []) initializerLists.toArray(new List[initializerLists.size()]);

                // copy & immutize
                for (int i = 0; i < lists.length; i++) {
                    lists[i] = Collections.unmodifiableList(new ArrayList(lists[i]));
                }

                listsOfInitializers = lists;
                listsOfInitializersVersion = initializerListsVersion;
            }

            return listsOfInitializers;
        }
    }

    /** Add filter instance to the list of current filters.
     * If there are already existing editor components,
     * and you want to apply the changes that this filter makes
     * to these existing
     * components, you can call reset(). However all the changes
     * that were made explicitly by calling setValue() will be lost
     * in this case.
     *
     * @param f filter to add to the list of the filters
     */
    public static void addFilter(Filter f) {
        synchronized (FILTERS_LOCK) {
            if (filters.length == 0) {
                filters = new Filter [] { f };
            } else {
                Filter [] tmp = new Filter [filters.length + 1];
                System.arraycopy(filters, 0, tmp, 0, filters.length);
                tmp[filters.length] = f;

                filters = tmp;
            }
        }
    }

    public static void removeFilter(Filter f) {
        synchronized (FILTERS_LOCK) {
            if (filters.length == 0) {
                return;
            } else if (filters.length == 1 && filters[0] == f) {
                filters = NULL_FILTERS;
            } else {
                int idx = -1;
                for (int i = 0; i < filters.length; i++) {
                    if (filters[i] == f) {
                        idx = i;
                        break;
                    }
                }

                if (idx != -1) {
                    Filter [] tmp = new Filter [filters.length - 1];
                    System.arraycopy(filters, 0, tmp, 0, idx);
                    if (idx < tmp.length) {
                        System.arraycopy(filters, idx + 1, tmp, idx, tmp.length - idx);
                    }

                    filters = tmp;
                }
            }
        }
    }

    /** Get the value and evaluate the evaluators. */
    public static Object getValue(Class kitClass, String settingName) {
        return getValue(kitClass, settingName, true);
    }

    /** Get the property by searching the given kit class settings and if not
     * found then the settings for super class and so on.
     * @param kitClass editor kit class for which the value of setting should
     *   be retrieved. The null can be used as the root of the whole hierarchy.
     * @param settingName name of the setting for which the value should
     *   be retrieved
     * @return the value of the setting
     */
    public static Object getValue(Class kitClass, String settingName, boolean evaluateEvaluators) {
        String mimeType = BaseKit.kitsTracker_FindMimeType(kitClass);
        MimePath mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);
        
        // Get the value
        Object value = getValueEx(mimePath, kitClass, settingName, evaluateEvaluators);
       
        // filter the value if necessary
        Filter [] currentFilters = filters;
        for (int i = 0; i < currentFilters.length; i++) {
            value = currentFilters[i].filterValue(kitClass, settingName, value);
        }
        
        return value;
    }
    
    private static Object getValueEx(MimePath mimePath, Class kitClass, String settingName, boolean evaluateEvaluators) {
        Object value = null;
        boolean hasValue = false;

        // read the value according to the guessed type of the setting
        if (settingName != null && SettingsNames.ABBREV_MAP.equals(settingName)) {
            Object abbrevsFromInitializers = getValueOld(kitClass, SettingsNames.ABBREV_MAP, true);
            Map<String, String> abbrevs = findCodeTemplates(mimePath);
            
            if (abbrevsFromInitializers instanceof Map) {
                value = new HashMap((Map)abbrevsFromInitializers);
                ((Map) value).putAll(abbrevs);
            } else {
                value = abbrevs;
            }
            
            hasValue = true;
        } else if (settingName != null && SettingsNames.KEY_BINDING_LIST.equals(settingName)) {
            Object keybindingsFromInitializers = getValueOld(kitClass, SettingsNames.KEY_BINDING_LIST, true);
            List<MultiKeyBinding> keybindings = findKeyBindings(mimePath);
            
            if (keybindingsFromInitializers instanceof List) {
                value = new ArrayList((List) keybindingsFromInitializers);
                ((List) value).addAll(keybindings);
            } else {
                value = keybindings;
            }
            
            hasValue = true;
        } else if (settingName != null && SettingsNames.MACRO_MAP.equals(settingName)) {
            Object macrosFromInitializers = getValueOld(kitClass, SettingsNames.MACRO_MAP, true);
            Map<String, String> macros = findMacros(mimePath);

            if (macrosFromInitializers instanceof Map) {
                value = new HashMap((Map)macrosFromInitializers);
                ((Map) value).putAll(macros);
            } else {
                value = macros;
            }
            
            hasValue = true;
        } else {
            // Check if the requested setting is a coloring
            if (settingName != null && HIGHLIGHT_COLOR_NAMES.contains(settingName)) {
                value = findColor(settingName, mimePath);
                hasValue = true;
            } else if (settingName != null && HIGHLIGHT_COLORING_NAMES.contains(settingName)) {
                value = findColoring(settingName, mimePath, false, true);
                hasValue = true;
            } else {
                String coloringName = translateOldTokenColoringName(settingName);
                if (coloringName != null) {
                    value = findColoring(coloringName, mimePath, true, true);
                    hasValue = true;
                }
            }

            // Try getting it from editor Preferences
            if (!hasValue) {
                Preferences prefs = findPreferences(mimePath);

                // check if there is actually some value
                if (prefs != null && null != prefs.get(settingName, null)) {
                    // try guessing the type
                    Class type = null;
                    String javaType = prefs.get(JAVATYPE_KEY_PREFIX + settingName, null);
                    if (javaType != null) {
                        type = typeFromString(javaType);
                    }
                    
                    if (type != null) {
                        if (type.equals(Boolean.class)) {
                            value = prefs.getBoolean(settingName, false);
                            hasValue = true;
                        } else if (type.equals(Integer.class)) {
                            value = prefs.getInt(settingName, 0);
                            hasValue = true;
                        } else if (type.equals(Long.class)) {
                            value = prefs.getLong(settingName, 0L);
                            hasValue = true;
                        } else if (type.equals(Float.class)) {
                            value = prefs.getFloat(settingName, 0.0F);
                            hasValue = true;
                        } else if (type.equals(Double.class)) {
                            value = prefs.getDouble(settingName, 0.0D);
                            hasValue = true;
                        } else if (type.equals(Insets.class)) {
                            value = parseInsets(prefs.get(settingName, null));
                            hasValue = true;
                        } else if (type.equals(Dimension.class)) {
                            value = parseDimension(prefs.get(settingName, null));
                            hasValue = true;
                        } else if (type.equals(Color.class)) {
                            value = parseColor(prefs.get(settingName, null));
                            hasValue = true;
                        } else if (type.equals(String.class)) {
                            value = prefs.get(settingName, null);
                            hasValue = true;
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
            }
        }
        
        // Fallback on to the kitmaps
        if (!hasValue) {
            value = getValueOld(kitClass, settingName, evaluateEvaluators);
        }

        return value;
    }

    private static Object getValueOld(Class kitClass, String settingName, boolean evaluateEvaluators) {
        Object value = null;
        List allKitMaps = getAllKitMaps(kitClass);
        assert allKitMaps.size() % 2 == 0 : "allKitMaps should contain pairs of [kitClass, settingsMap]."; //NOI18N

        for(int i = 0; i < allKitMaps.size() / 2; i++) {
            Class kc = (Class) allKitMaps.get(2 * i);
            Map map = (Map) allKitMaps.get(2 * i + 1);
            value = map.get(settingName);
            if (evaluateEvaluators && value instanceof Evaluator) {
                value = ((Evaluator)value).getValue(kc, settingName);
            }
            if (value != null) {
                break;
            }
        }
        
        return value;
    }
    
    /** Get the value hierarchy and evaluate the evaluators */
    public static KitAndValue[] getValueHierarchy(Class kitClass, String settingName) {
        return getValueHierarchy(kitClass, settingName, true);
    }

    /** Get array of KitAndValue objects sorted from the given kit class to its
     * deepest superclass and the last member can be filled whether there
     * is global setting (kit class of that member would be null).
     * This method is useful for objects like keymaps that
     * need to create all the parent keymaps to work properly.
     * The method can either evaluate evaluators or leave them untouched
     * which can become handy in some cases.
     * @param kitClass editor kit class for which the value of setting should
     *   be retrieved. The null can be used as the root of the whole hierarchy.
     * @param settingName name of the setting for which the value should
     *   be retrieved
     * @param evaluateEvaluators whether the evaluators should be evaluated or not
     * @return the array containing KitAndValue instances describing the particular
     *   setting's value on the specific kit level.
     */
    public static KitAndValue[] getValueHierarchy(Class kitClass, String settingName, boolean evaluateEvaluators) {
        ArrayList<KitAndValue> kavList = new ArrayList<KitAndValue>();
        boolean superclass = false;
        
        try {
            for (Class kc = kitClass; kc != null; kc = kc.getSuperclass()) {
                String mimeType = BaseKit.kitsTracker_FindMimeType(kc);
                MimePath mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);

                Object value = getValueEx(mimePath, kc, settingName, evaluateEvaluators);
                if (value != null) {
                    kavList.add(new KitAndValue(kc, value));
                }

                if (mimePath == MimePath.EMPTY) {
                    break;
                }

                if (!superclass) {
                    superclass = true;
                    BaseKit.kitsTracker_setContextMimeType(""); //NOI18N
                }
            }
        } finally {
            if (superclass) {
                BaseKit.kitsTracker_setContextMimeType(null);
            }
        }
        
        KitAndValue[] kavArray = kavList.toArray(new KitAndValue[kavList.size()]);

        // filter the value if necessary
        Filter [] currentFilters = filters;
        for (int i = 0; i < currentFilters.length; i++) {
            kavArray = currentFilters[i].filterValueHierarchy(kitClass, settingName, kavArray);
        }

        return kavArray;
    }

    /** Set the new value for property on kit level. The old and new values
     * are compared and if they are equal the setting is not changed and
     * nothing is fired.
     *
     * @param kitClass editor kit class for which the value of setting should
     *   be set. The null can be used as the root of the whole hierarchy.
     * @param settingName the string used for searching the value
     * @param newValue new value to set for the property; the value can
     *   be null to clear the value for the specified kit
     */
    public static void setValue(Class kitClass, String settingName, Object newValue) {
        if (settingName != null && SettingsNames.ABBREV_MAP.equals(settingName)) {
            LOG.log(Level.WARNING, "Can't save 'SettingsNames.ABBREV_MAP' setting through org.netbeans.editor.Settings!", new Throwable("Stacktrace")); //NOI18N
        } else if (settingName != null && SettingsNames.KEY_BINDING_LIST.equals(settingName)) {
            LOG.log(Level.WARNING, "Can't save 'SettingsNames.KEY_BINDING_LIST' setting through org.netbeans.editor.Settings!", new Throwable("Stacktrace")); //NOI18N
        } else if (settingName != null && SettingsNames.MACRO_MAP.equals(settingName)) {
            LOG.log(Level.WARNING, "Can't save 'SettingsNames.MACRO_MAP' setting through org.netbeans.editor.Settings!", new Throwable("Stacktrace")); //NOI18N
        } else {
            boolean coloring = false;
            
            // Check if the requested setting is a coloring
            if (settingName != null && HIGHLIGHT_COLOR_NAMES.contains(settingName)) {
                coloring = true;
            } else if (settingName != null && HIGHLIGHT_COLORING_NAMES.contains(settingName)) {
                coloring = true;
            } else {
                String coloringName = translateOldTokenColoringName(settingName);
                if (coloringName != null) {
                    coloring = true;
                }
            }

            if (coloring) {
                LOG.log(Level.WARNING, "Can't save coloring '" + settingName + "' through org.netbeans.editor.Settings!", new Throwable("Stacktrace")); //NOI18N
            } else {
                boolean useKitMaps = false;
                String mimeType = BaseKit.kitsTracker_FindMimeType(kitClass);
                MimePath mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);
                Preferences prefs = findPreferences(mimePath);
                
                if (prefs != null) {
                    if (newValue != null) {
                        if (newValue instanceof Boolean) {
                            prefs.putBoolean(settingName, (Boolean) newValue);
                        } else if (newValue instanceof Integer) {
                            prefs.putInt(settingName, (Integer) newValue);
                        } else if (newValue instanceof Long) {
                            prefs.putLong(settingName,(Long) newValue);
                        } else if (newValue instanceof Float) {
                            prefs.putFloat(settingName, (Float) newValue);
                        } else if (newValue instanceof Double) {
                            prefs.putDouble(settingName, (Double) newValue);
                        } else if (newValue instanceof Insets) {
                            prefs.put(settingName, insetsToString((Insets) newValue));
                            prefs.put(JAVATYPE_KEY_PREFIX + settingName, Insets.class.getName());
                        } else if (newValue instanceof Dimension) {
                            prefs.put(settingName, dimensionToString((Dimension) newValue));
                            prefs.put(JAVATYPE_KEY_PREFIX + settingName, Dimension.class.getName());
                        } else if (newValue instanceof Color) {
                            prefs.put(settingName, color2String((Color) newValue));
                            prefs.put(JAVATYPE_KEY_PREFIX + settingName, Color.class.getName());
                        } else if (newValue instanceof String) {
                            prefs.put(settingName, (String) newValue);
                        } else {
                            LOG.log(Level.FINE, "Can't save setting '" + settingName + "' with value '" + newValue //NOI18N
                                + "' through org.netbeans.editor.Settings; unsupported value conversion!", new Throwable("Stacktrace")); //NOI18N
                            useKitMaps = true;
                        }
                    } else {
                        prefs.remove(settingName);
                    }
                } else {
                    useKitMaps = true;
                }
                
                if (useKitMaps) {
                    // no prefs implementation, fall back on to the kit maps
                    synchronized (Settings.class) {
                        Map map = getKitMap(kitClass, true);
                        Object oldValue = map.get(settingName);
                        if (oldValue == null && newValue == null || (oldValue != null && oldValue.equals(newValue))) {
                            return; // no change                    
                        }
                        if (newValue != null) {
                            map.put(settingName, newValue);
                        } else {
                            map.remove(settingName);
                        }
                    }
                }
            }
        }
        
        fireSettingsChange(kitClass, settingName, null, newValue);
    }

    /** Don't change the value of the setting, but fire change
     * event. This is useful when there's internal change in the value object
     * of some setting.
     */
    public static void touchValue(Class kitClass, String settingName) {
        fireSettingsChange(kitClass, settingName, null, null); // kit class currently not used
    }

    /** Set the value for the current kit and propagate it to all
     * the children of the given kit by removing
     * the possible values for the setting from the children kit setting maps.
     * Note: This call only affects the settings for the kit classes for which
     * the kit setting map with the setting values currently exists, i.e. when
     * there was at least one getValue() or setValue() call performed for any
     * setting on that particular kit class level. Other kit classes maps
     * will be initialized by the particular initializer(s) as soon as
     * the first getValue() or setValue() will be performed for them.
     * However that future process will not be affected by the current
     * propagateValue() call.
     * This method is useful for the visual options that always set
     * the value on all the kit levels without regard whether it's necessary or not.
     * If the value is then changed for the base kit, it's not propagated
     * naturally as there's a special setting
     * This method enables
     *
     * The current implementation always fires the change regardless whether
     * there was real change in setting value or not.
     * @param kitClass editor kit class for which the value of setting should
     *   be set.  The null can be used as the root of the whole hierarchy.
     * @param settingName the string used for searching the value
     * @param newValue new value to set for the property; the value can
     *   be null to clear the value for the specified kit
     */
    public static void propagateValue(Class kitClass, String settingName, Object newValue) {
        synchronized (Settings.class) {
            Map map = getKitMap(kitClass, true);
            if (newValue != null) {
                map.put(settingName, newValue);
            } else {
                map.remove(settingName);
            }
            // resolve kits
            Iterator it = kit2Maps.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry me = (Map.Entry)it.next();
                Class kc = (Class)me.getKey();
                if (kitClass != kc && (kitClass == null || kitClass.isAssignableFrom(kc))) {
                    ((Map)me.getValue()).remove(settingName);
                }
            }
        }

        fireSettingsChange(null, settingName, null, null);
    }

    /** Run the given runnable. All the changes in the settings are not fired until
     * the whole runnable completes. Nesting of <tt>update()</tt> call is allowed.
     * Only one firing is performed after the whole runnable completes
     * using the 'null triple'.
     */
    public static void update(final Runnable r) {
        // Just a backdoor for BaseOptions and OptionSupport to be able
        // to serialize settings related tasks.
        if (isAsyncTask(r)) {
            PROCESSOR.post(r, getTaskDelay(r));
        } else {
            boolean fire = false;

            synchronized (Settings.class) {
                firingEnabled++;
                try {
                    r.run();
                } finally {
                    firingEnabled--;
                    fire = firingEnabled == 0;
                }
            }

            if (fire) {
                fireSettingsChange(null, null, null, null);
            }
        }
    }

    private static boolean isAsyncTask(Runnable r) {
        try {
            Method m = r.getClass().getDeclaredMethod("asynchronous"); //NOI18N
            m.setAccessible(true);
            return (Boolean) m.invoke(r);
        } catch (Exception e) {
            return false;
        }
    }
    
    private static int getTaskDelay(Runnable r) {
        try {
            Method m = r.getClass().getDeclaredMethod("delay"); //NOI18N
            m.setAccessible(true);
            return (Integer) m.invoke(r);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /** Reset all the settings and fire the change of the settings
     * so that all the listeners will be notified and will reload
     * the settings.
     * The settings that were changed using setValue() and propagateValue()
     * are lost. Initializers will be asked for the settings values when
     * necessary.
     */
    public static void reset() {
        RESET_TASK.schedule(1);
    }

    /** Debug the current initializers */
    public static String initializersToString() {
        StringBuffer sb = new StringBuffer();
        List [] lists = getListsOfInitializers();
        for (int i = 0; i < lists.length; i++) {
            // debug the level
            switch (i) {
                case CORE_LEVEL:
                    sb.append("CORE_LEVEL"); // NOI18N
                    break;
                
                case SYSTEM_LEVEL:
                    sb.append("SYSTEM_LEVEL"); // NOI18N
                    break;
                
                case EXTENSION_LEVEL:
                    sb.append("EXTENSION_LEVEL"); // NOI18N
                    break;
                
                case OPTION_LEVEL:
                    sb.append("OPTION_LEVEL"); // NOI18N
                    break;
                
                case USER_LEVEL:
                    sb.append("USER_LEVEL"); // NOI18N
                    break;
                
                default:
                    sb.append("level " + i); // NOI18N
                    break;
            }
            sb.append(":\n"); // NOI18N
            
            // debug the initializers
            sb.append(EditorDebug.debugList((List)lists[i]));
            sb.append('\n'); //NOI18N
        }

        return sb.toString();
    }

    /** Add weak listener to listen to change of any property. The caller must
     * hold the listener object in some instance variable to prevent it
     * from being garbage collected.
     */
    public static void addSettingsChangeListener(SettingsChangeListener l) {
        listenerList.add(SettingsChangeListener.class, l);
    }

    /** Remove listener for changes in properties */
    public static void removeSettingsChangeListener(SettingsChangeListener l) {
        listenerList.remove(SettingsChangeListener.class, l);
    }

    private static void fireSettingsChange(Class kitClass, String settingName, Object oldValue, Object newValue) {
        if (firingEnabled == 0) {
            SettingsChangeListener[] listeners = (SettingsChangeListener[])
                    listenerList.getListeners(SettingsChangeListener.class);
            SettingsChangeEvent evt = new SettingsChangeEvent(Settings.class,
                    kitClass, settingName, oldValue, newValue);
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].settingsChange(evt);
            }
        }
    }

    /** 
     * Gets (and possibly creates) kit map for particular kit. This always runs
     * under the Settings.class lock and because it calls initializers that
     * can do whatever ever they like, it is important not to fire any events
     * (eg. when an initializer calls setValue or something similar). See issue #118763.
     */
    private static Map getKitMap(Class kitClass, boolean forceCreation) {
        firingEnabled++;
        try {
            return getKitMapWithEvent(kitClass, forceCreation);
        } finally {
            firingEnabled--;
        }
    }
    
    private static Map getKitMapWithEvent(Class kitClass, boolean forceCreation) {
        Map kitMap = (Map)kit2Maps.get(kitClass);
        if (kitMap == null) {
            Map emptyMap = (Map) emptyMaps.get(kitClass);
            if (emptyMap != null) {
                // recursive initialization, return what we have collected so far
                return emptyMap;
            }

            if (emptyMap == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    emptyMap = new LoggingMap(kitClass, Level.FINE);
                } else {
                    emptyMap = new HashMap();
                }
                emptyMaps.put(kitClass, emptyMap);
            }

            // Go through all the initializers
            List [] lists = getListsOfInitializers();
            for (int i = 0; i < lists.length; i++) {
                Iterator it = ((List) lists[i]).iterator();
                while (it.hasNext()) {
                    Initializer initializer = (Initializer)it.next();

                    // A call to initializer shouldn't break the whole updating
                    try {
                        initializer.updateSettingsMap(kitClass, emptyMap);
                    } catch (Throwable t) {
                        LOG.log(Level.WARNING, null, t);
                    }
                }
            }

            kitMap = emptyMap;
            
            kit2Maps.put(kitClass, kitMap);
            emptyMaps.remove(kitClass);
        }

        return kitMap;
    }

    private static List getAllKitMaps(Class kitClass) {
        synchronized (Settings.class) {
            // Collect the kit classes that we need to ask for settings
            List<Class> classes = new ArrayList<Class>();
            for (Class kc = kitClass; kc != null; kc = kc.getSuperclass()) {
                classes.add(kc);
            }
            
            // Load the kit maps starting with BaseKit.class and going to kitClass.
            // This allows an initializer for a superclass to add more initializers
            // for subclasses. See for example NbEditorSettingsInitializer.updateSettingsMap.
            List list = new ArrayList();
            for (int i = classes.size() - 1; i >= 0; i--) {
                Class kc = classes.get(i);
                Map map = getKitMap(kc, false);
                if (map != null) {
                    list.add(map);
                    list.add(kc);
                }
            }
            
            Collections.reverse(list);
            return list;
        }
    }

    /** Kit class and value pair */
    public static class KitAndValue {

        public Class kitClass;

        public Object value;

        public KitAndValue(Class kitClass, Object value) {
            this.kitClass = kitClass;
            this.value = value;
        }
        
    }

    /** Initializer of the settings updates the map filled
     * with settings for the particular kit class when asked.
     * If the settings are being initialized all the initializers registered
     * by the <tt>Settings.addInitializer()</tt> are being asked to update
     * the settings-map through calling their <tt>updateSettingsMap()</tt>.
     */
    public static interface Initializer {

        /** Each initializer must have a name. The name should be unique.
         * The name is used for identifying the initializer during removal
         * and sort operations and for debuging purposes.
         */
        public String getName();

        /** Update map filled with the settings.
         * @param kitClass kit class for which the settings are being updated.
         *    It can be null which means the root of the whole kit class hierarchy.
         * @param settingsMap map holding [setting-name, setting-value] pairs.
         *   The map can be empty if this is the first initializer
         *   that updates it or if no previous initializers updated it.
         */
        public void updateSettingsMap(Class kitClass, Map settingsMap);
        
    }

    /** Abstract implementation of the initializer dealing with the name. */
    public static abstract class AbstractInitializer implements Initializer {

        private String name;

        public AbstractInitializer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public @Override String toString() {
            return getName();
        }
        
    } // End of AbstractInitializer class

    /** Sort the settings initializers that were added to the settings.
     * There can be only one sorter for the Settings, but it can delegate
     * to previously registered sorter.
     */
    public static interface InitializerSorter {

        public void sort(List initializersList);
        
    }

    /** Initializer sorter that delegates to another sorter. */
    public static abstract class FilterInitializerSorter {

        private InitializerSorter delegate;

        public FilterInitializerSorter(InitializerSorter delegate) {
            this.delegate = delegate;
        }

        public void sort(List initializersList) {
            if (delegate != null) {
                delegate.sort(initializersList);
            }
        }
        
    }



    /** Evaluator can be used in cases when value of some setting
     * depends on the value for other setting and it allows to compute
     * the value dynamically based on the other setting(s) value.
     * The <tt>Evaluator</tt> instance can be used as the value
     * in the <tt>Settings.setValue()</tt> call. In that case the call
     * to the <tt>Settings.getValue()</tt> call will 'evaluate' the Evaluator
     * by calling its <tt>getValue()</tt>.
     */
    public static interface Evaluator {

        /** Compute the particular setting's value.
         * @param kitClass kit class for which the setting is being retrieved.
         * @param settingName name of the setting to retrieve. Although the Evaluator
         *   are usually constructed only for the concrete setting, this parameter
         *   allows creation of the Evaluator for multiple settings.
         * @return the value for the requested setting. The substitution
         *   is not attempted again, so the return value cannot be another
         *   Evaluator instance. If the returned value is null, the same
         *   action is taken as if there would no value set on the particular
         *   kit level.
         *
         */
        public Object getValue(Class kitClass, String settingName);
        
    }


    /** Filter is applied on every value or KitAndValue pairs returned from getValue().
     * The filter can be registered by calling <tt>Settings.addFilter()</tt>.
     * Each call to <tt>Settings.getValue()</tt> will first retrieve the value and
     * then call the <tt>Filter.filterValue()</tt> to get the final value. Each call
     * to <tt>Settings.getValueHierarchy()</tt> will first retrieve the kit-and-value
     * array and then call the <tt>Filter.filterValueHierarchy()</tt>.
     * If more filters are registered they are all used in the order they were added.
     */
    public static interface Filter {

        /** Filter single value. The value can be substituted here.
         * @param kitClass class of the kit for which the value is retrieved
         * @param settingName name of the retrieved setting
         * @param value value to be optionally filtered
         */
        public Object filterValue(Class kitClass, String settingName, Object value);

        /** Filter array of kit-class and value pairs. The pairs can be completely
         * substituted with an array with different length and different members.
         * @param kitClass class of the kit for which the value is retrieved
         * @param settingName name of the retrieved setting
         * @param kavArray kit-class and value array to be filtered
         */
        public KitAndValue[] filterValueHierarchy(Class kitClass, String settingName,
                KitAndValue[] kavArray);
        
    }

    // This is just for debugging and should not normally be used.
    private static final class LoggingMap extends HashMap {

        private Class kitClass;
        private Level logLevel;

        private static final Set<String> DEPRECATED_SETTINGS =
            Collections.synchronizedSet(Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String [] {
                // Fonts & Colors related settings
                SettingsNames.BLOCK_SEARCH_COLORING,
                SettingsNames.CARET_COLOR_INSERT_MODE,
                SettingsNames.CARET_COLOR_OVERWRITE_MODE,
                SettingsNames.CODE_FOLDING_COLORING,
                SettingsNames.COLORING_NAME_LIST,
                SettingsNames.COLORING_NAME_PRINT_SUFFIX,
                SettingsNames.COLORING_NAME_SUFFIX,
                SettingsNames.DEFAULT_COLORING,
                SettingsNames.GUARDED_COLORING,
                SettingsNames.HIGHLIGHT_SEARCH_COLORING,
                SettingsNames.INC_SEARCH_COLORING,
                SettingsNames.LINE_NUMBER_COLORING,
                SettingsNames.SELECTION_COLORING,
                SettingsNames.STATUS_BAR_BOLD_COLORING,
                SettingsNames.STATUS_BAR_COLORING,
                SettingsNames.TEXT_LIMIT_LINE_COLOR,

                // Keybindings related settings
                SettingsNames.KEY_BINDING_LIST,
        }))));
        
        public LoggingMap(Class kitClass, Level logLevel) {
            super();
            this.kitClass = kitClass;
            this.logLevel = logLevel;
        }

        public @Override Object put(Object key,Object value) {
            if (key != null &&
                    (key.equals(SettingsNames.RENDERING_HINTS) ||
                    key.equals("textAntialiasing") //NOI18N
                    ))
            {
                String msg = "Settings map: put('" + key + "' to '" + value + "') for kitClass=" + kitClass; //NOI18N
                if (LOG_STACTRACES) {
                    LOG.log(logLevel, null, new Throwable(msg));
                } else {
                    LOG.log(logLevel, msg);
                }
            }

            logDeprecatedKey(key);
            return super.put(key, value);
        }

        public @Override Object get(Object key) {
            logDeprecatedKey(key);
            return super.get(key);
        }

        public @Override boolean containsKey(Object key) {
            logDeprecatedKey(key);
            return super.containsKey(key);
        }

        public @Override Object remove(Object key) {
            logDeprecatedKey(key);
            return super.remove(key);
        }

        private void logDeprecatedKey(Object key) {
            if (LOG.isLoggable(logLevel)) {
                if (key != null && DEPRECATED_SETTINGS.contains(key)) {
                    String msg = "The editor setting '" + key + "' is deprecated. Please use Editor Settings API instead."; //NOI18N
                    if (LOG_STACTRACES) {
                        LOG.log(logLevel, null, new Throwable(msg));
                    } else {
                        LOG.log(logLevel, msg);
                    }
                }
            }
        }
    } // End of LoggingMap class

    // ----------------------------------------------------------
    // Fonts & Colors bridge to Editor Settings API
    // ----------------------------------------------------------

    private static final Map<MimePath, ChangesTrackingLookupResult<FontColorSettings>> FCS_CACHE = 
        new WeakHashMap<MimePath, Settings.ChangesTrackingLookupResult<FontColorSettings>>();

    private static final Set<String> HIGHLIGHT_COLOR_NAMES = new HashSet<String>();
    private static final Set<String> HIGHLIGHT_COLORING_NAMES = new HashSet<String>();
    static {
        HIGHLIGHT_COLOR_NAMES.add(SettingsNames.CARET_COLOR_INSERT_MODE);
        HIGHLIGHT_COLOR_NAMES.add(SettingsNames.CARET_COLOR_OVERWRITE_MODE);
        HIGHLIGHT_COLOR_NAMES.add(SettingsNames.TEXT_LIMIT_LINE_COLOR);

        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.LINE_NUMBER_COLORING);
        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.GUARDED_COLORING);
        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.CODE_FOLDING_COLORING);
        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.CODE_FOLDING_BAR_COLORING);
        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.SELECTION_COLORING);
        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.HIGHLIGHT_SEARCH_COLORING);
        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.INC_SEARCH_COLORING);
        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.BLOCK_SEARCH_COLORING);
        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.STATUS_BAR_COLORING);
        HIGHLIGHT_COLORING_NAMES.add(SettingsNames.STATUS_BAR_BOLD_COLORING);
    }

    private static Coloring findColoring(String coloringName, MimePath mimePath, boolean token, boolean highlight) {
        AttributeSet attribs = findAttribs(coloringName, mimePath, token, highlight);
        return attribs == null ? null : Coloring.fromAttributeSet(attribs);
    }

    private static Color findColor(String coloringName, MimePath mimePath) {
        AttributeSet attribs = findAttribs(coloringName, mimePath, false, true);
        return attribs == null ? null : (Color) attribs.getAttribute(StyleConstants.Foreground);
    }

    private static AttributeSet findAttribs(String coloringName, MimePath mimePath, boolean token, boolean highlight) {
        synchronized (FCS_CACHE) {
            ChangesTrackingLookupResult<FontColorSettings> ctlr = FCS_CACHE.get(mimePath);

            if (ctlr == null) {
                Lookup.Result<FontColorSettings> lookupResult = MimeLookup.getLookup(mimePath).lookupResult(FontColorSettings.class);
                ctlr = new ChangesTrackingLookupResult<FontColorSettings>(lookupResult, null);
                FCS_CACHE.put(mimePath, ctlr);
            }

            AttributeSet attribs = null;
            Collection<? extends FontColorSettings> allFcs = ctlr.getLookupResult().allInstances();
            FontColorSettings fcs = allFcs.isEmpty() ? null : allFcs.iterator().next();

            if (fcs != null) {
                if (token && !highlight) {
                    attribs = fcs.getTokenFontColors(coloringName);
                } else if (!token && highlight) {
                    attribs = fcs.getFontColors(coloringName);
                } else {
                    attribs = fcs.getFontColors(coloringName);
                    if (attribs == null) {
                        attribs = fcs.getTokenFontColors(coloringName);
                    }
                }
            }

            return attribs;
        }
    }

    private static String translateOldTokenColoringName(String name) {
        String translated = null;

        if (name != null && name.endsWith(SettingsNames.COLORING_NAME_SUFFIX)) {
            translated = name.substring(0, name.length() - SettingsNames.COLORING_NAME_SUFFIX.length());
        }

        if (name != null && name.endsWith(SettingsNames.COLORING_NAME_PRINT_SUFFIX)) {
            translated = name.substring(0, name.length() - SettingsNames.COLORING_NAME_PRINT_SUFFIX.length());
        }

        return translated;
    }

    // ----------------------------------------------------------
    // KeyBindings bridge to Editor Settings API
    // ----------------------------------------------------------

    private static final Map<MimePath, ChangesTrackingLookupResult<KeyBindingSettings>> KBS_CACHE =
        new WeakHashMap<MimePath, ChangesTrackingLookupResult<KeyBindingSettings>>();

    private static List<MultiKeyBinding> findKeyBindings(MimePath mimePath) {
        synchronized (KBS_CACHE) {
            ChangesTrackingLookupResult<KeyBindingSettings> ctlr = KBS_CACHE.get(mimePath);

            if (ctlr == null) {
                Lookup.Result<KeyBindingSettings> lookupResult = MimeLookup.getLookup(mimePath).lookupResult(KeyBindingSettings.class);
                ctlr = new ChangesTrackingLookupResult<KeyBindingSettings>(lookupResult, SettingsNames.KEY_BINDING_LIST);
                KBS_CACHE.put(mimePath, ctlr);
            }

            @SuppressWarnings("unchecked")
            List<MultiKeyBinding> list = (List<MultiKeyBinding>)ctlr.getCustomData();
            if (list == null) {
                Collection<? extends KeyBindingSettings> allKbs = ctlr.getLookupResult().allInstances();
                KeyBindingSettings kbs = allKbs.isEmpty() ? null : allKbs.iterator().next();

                list = new ArrayList<MultiKeyBinding>();

                if (kbs != null) {
                    for(org.netbeans.api.editor.settings.MultiKeyBinding mkb : kbs.getKeyBindings()) {
                        List<KeyStroke> keyStrokes = mkb.getKeyStrokeList();
                        list.add(new MultiKeyBinding(
                            keyStrokes.toArray(new KeyStroke[keyStrokes.size()]),
                            mkb.getActionName()
                        ));
                    }

                    ctlr.setCustomData(list);
                }
            }

            return list;
        }
    }
    
    // ----------------------------------------------------------
    // CodeTemplates bridge to Editor Settings API
    // ----------------------------------------------------------

    private static final Map<MimePath, ChangesTrackingLookupResult<CodeTemplateSettings>> CTS_CACHE = 
        new WeakHashMap<MimePath, ChangesTrackingLookupResult<CodeTemplateSettings>>();
    
    private static Map<String, String> findCodeTemplates(MimePath mimePath) {
        synchronized (CTS_CACHE) {
            ChangesTrackingLookupResult<CodeTemplateSettings> ctlr = CTS_CACHE.get(mimePath);

            if (ctlr == null) {
                Lookup.Result<CodeTemplateSettings> lookupResult = MimeLookup.getLookup(mimePath).lookupResult(CodeTemplateSettings.class);
                ctlr = new ChangesTrackingLookupResult<CodeTemplateSettings>(lookupResult, SettingsNames.ABBREV_MAP);
                CTS_CACHE.put(mimePath, ctlr);
            }

            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) ctlr.getCustomData();
            if (map == null) {
                Collection<? extends CodeTemplateSettings> allCts = ctlr.getLookupResult().allInstances();
                CodeTemplateSettings cts = allCts.isEmpty() ? null : allCts.iterator().next();

                map = new HashMap<String, String>();

                if (cts != null) {
                    for(CodeTemplateDescription ctd : cts.getCodeTemplateDescriptions()) {
                        map.put(ctd.getAbbreviation(), ctd.getParametrizedText());
                    }

                    ctlr.setCustomData(map);
                }
            }

            return map;
        }
    }

    private static final class ChangesTrackingLookupResult<T> implements LookupListener {

        private final Lookup.Result<T> lookupResult;
        private final String settingName;
        private Object customData = null;

        public ChangesTrackingLookupResult(Lookup.Result<T> lookupResult, String settingName) {
            this.lookupResult = lookupResult;
            this.lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, this.lookupResult));
            this.settingName = settingName;
        }

        public Lookup.Result<T> getLookupResult() {
            return lookupResult;
        }

        public Object getCustomData() {
            return customData;
        }

        public void setCustomData(Object data) {
            this.customData = data;
        }

        public void resultChanged(LookupEvent ev) {
            this.customData = null;
            fireSettingsChange(null, settingName, null, null);
        }
    } // End of TrackingResult class

    // ----------------------------------------------------------
    // Preferences bridge to Editor Settings API
    // ----------------------------------------------------------

    private static final Map<MimePath, PreferenceChangesTracker> PREFS_CACHE = 
        new WeakHashMap<MimePath, PreferenceChangesTracker>();
    
    private static Preferences findPreferences(MimePath mimePath) {
        synchronized (PREFS_CACHE) {
            PreferenceChangesTracker tracker = PREFS_CACHE.get(mimePath);

            if (tracker == null) {
                Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
                
                // in some tests there is no MimeLookup at all
                if (prefs != null) {
                    tracker = new PreferenceChangesTracker(prefs);
                    PREFS_CACHE.put(mimePath, tracker);
                }
            }

            return tracker == null ? null : tracker.getPreferences();
        }
    }
    
    private static final class PreferenceChangesTracker implements PreferenceChangeListener {
        private final Preferences prefs;
        
        public PreferenceChangesTracker(Preferences prefs) {
            this.prefs = prefs;
            this.prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, this.prefs));
        }

        public Preferences getPreferences() {
            return prefs;
        }
        
        public void preferenceChange(PreferenceChangeEvent evt) {
            fireSettingsChange(null, evt.getKey(), null, null);
        }
    } // End of TrackingResult class
    
    // ----------------------------------------------------------
    // Macros to Editor Settings API
    // ----------------------------------------------------------

    // XXX: rewrite this to not use reflection. It will require dependency on editor/macros
    // and editor/settings/storage. It should also listen on changes fired from EditorSettingsStorage
    // and call fireSettingsChange(null, SettingsName.MACRO_MAP, null, null). Should be done
    // after the Settings & co. is factored out to its own deprecated module so that we don't
    // introduce additional dependencies in editor/lib.
    private static Map<String, String> findMacros(MimePath mimePath) {
        Map macros = new HashMap();
        
        ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            Class essClass = classLoader.loadClass("org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage"); //NOI18N
            Method findMethod = essClass.getDeclaredMethod("find", String.class); //NOI18N
            Object macrosEss = findMethod.invoke(null, "Macros"); //NOI18N

            if (macrosEss != null) {
                Class mdClass = classLoader.loadClass("org.netbeans.modules.editor.macros.storage.MacroDescription"); //NOI18N
                Method getCodeMethod = mdClass.getDeclaredMethod("getCode"); //NOI18N

                Method loadMethod = essClass.getDeclaredMethod("load", MimePath.class, String.class, Boolean.TYPE); //NOI18N
                Map macroDescriptions = (Map) loadMethod.invoke(macrosEss, mimePath, null, false);
                for(Object key : macroDescriptions.keySet()) {
                    String macroName = (String) key;
                    Object macroDescription = macroDescriptions.get(key);

                    String macroCode = (String) getCodeMethod.invoke(macroDescription);
                    macros.put(macroName, macroCode);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        
        macros.put(null, findKeyBindings(mimePath));
        return macros;
    }

    /** Coverts Insets to String representation */
    private static String insetsToString(Insets ins) {
        StringBuilder sb = new StringBuilder();
        sb.append(ins.top);
        sb.append(','); //NOI18N

        sb.append(ins.left);
        sb.append(','); //NOI18N

        sb.append(ins.bottom);
        sb.append(','); //NOI18N

        sb.append(ins.right);

        return sb.toString();
    }

    /** Converts textual representation of Insets */
    private static Insets parseInsets(String s) {
        StringTokenizer st = new StringTokenizer(s, ","); //NOI18N

        int arr[] = new int[4];
        int i = 0;
        while (st.hasMoreElements()) {
            if (i > 3) {
                return null;
            }
            try {
                arr[i] = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {
                LOG.log(Level.WARNING, null, nfe);
                return null;
            }
            i++;
        }
        if (i != 4) {
            return null;
        } else {
            return new Insets(arr[0], arr[1], arr[2], arr[3]);
        }
    }
    
    private static String dimensionToString(Dimension dim) {
        StringBuilder sb = new StringBuilder();
        sb.append(dim.width);
        sb.append(','); //NOI18N
        sb.append(dim.height);
        return sb.toString();
    }

    private static Dimension parseDimension(String s) {
        StringTokenizer st = new StringTokenizer(s, ","); // NOI18N

        int arr[] = new int[2];
        int i = 0;
        while (st.hasMoreElements()) {
            if (i > 1) {
                return null;
            }
            try {
                arr[i] = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {
                LOG.log(Level.WARNING, null, nfe);
                return null;
            }
            i++;
        }
        if (i != 2) {
            return null;
        } else {
            return new Dimension(arr[0], arr[1]);
        }
    }
    
    private static String wrap(String s) {
        return (s.length() == 1) ? "0" + s : s; // NOI18N
    }
    
    /** Converts Color to hexadecimal String representation */
    private static String color2String(Color c) {
        StringBuilder sb = new StringBuilder();
        sb.append('#'); // NOI18N
        sb.append(wrap(Integer.toHexString(c.getRed()).toUpperCase()));
        sb.append(wrap(Integer.toHexString(c.getGreen()).toUpperCase()));
        sb.append(wrap(Integer.toHexString(c.getBlue()).toUpperCase()));
        return sb.toString();
    }
    
    /** Converts a String to an integer and returns the specified opaque Color. */
    private static Color parseColor(String s) {
        try {
            return Color.decode(s);
        } catch (NumberFormatException nfe) {
            LOG.log(Level.WARNING, null, nfe);
            return null;
        }
    }
    
//    private static final Map<String, Class> typesCache = new HashMap<String, Class>();
//    private static final class NO_TYPE {};
//    private static Class typeFromName(String settingName) {
//        synchronized (typesCache) {
//            Class settingType = typesCache.get(settingName);
//            
//            if (settingType == null) {
//                EditorSettingClass esc = null;
//
//                try {
//                    for (Field f : SettingsNames.class.getDeclaredFields()) {
//                        Object value;
//                        try {
//                            if ((f.getModifiers() & Modifier.STATIC) == Modifier.STATIC &&
//                                null != (value = f.get(null)) &&
//                                value.equals(settingName)
//                            ) {
//                                esc = f.getAnnotation(EditorSettingClass.class);
//                                break;
//                            }
//                        } catch (Exception e) {
//                            LOG.log(Level.FINE, null, e);
//                        }
//                    }
//                } catch (SecurityException se) {
//                    LOG.log(Level.FINE, null, se);
//                }
//                
//                settingType = esc == null ? NO_TYPE.class : esc.value();
//                typesCache.put(settingName, settingType);
//            }
//            
//            return settingType == NO_TYPE.class ? null : settingType;
//        }
//    }
    
    private static final String JAVATYPE_KEY_PREFIX = "nbeditor-javaType-for-legacy-setting_"; //NOI18N
    private static Class typeFromString(String javaType) {
        try {
            ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
            return classLoader == null ? null : classLoader.loadClass(javaType);
        } catch (ClassNotFoundException cnfe) {
            LOG.log(Level.WARNING, null, cnfe);
            return null;
        }
    }
}
