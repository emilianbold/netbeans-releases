/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Project configurations maintenance class
 * 
 * Getter/Setter naming conventions:
 * "Property" in method name -> method deals with single properties in configuration given by parameter config
 * "Default" in method name -> method deals with properties in default configuration
 * "Active" in method name -> method deals with properties in currently chosen configuration
 * "Transparent" in method name -> method deals with property in configuration fiven by parameter config if
 *     exists, or with property in default configuration otherwise. This is to provide simple access to
 *     union of default and non-default properties that are to be presented to users in non-default configurations
 * "Param" in method name -> metod deals with properties representing sets of application parameters
 *
 * @author Petr Somol
 */
public class JFXProjectConfigurations {
    
    private static final Logger LOG = Logger.getLogger(JFXProjectConfigurations.class.getName());

    public static final String APPLICATION_ARGS = ProjectProperties.APPLICATION_ARGS;
    public static final String APP_PARAM_PREFIX = "javafx.param."; // NOI18N
    public static final String APP_PARAM_SUFFIXES[] = new String[] { "name", "value" }; // NOI18N
    // folders and files
    public static final String PROJECT_CONFIGS_DIR = "nbproject/configs"; // NOI18N
    public static final String PROJECT_PRIVATE_CONFIGS_DIR = "nbproject/private/configs"; // NOI18N
    public static final String PROPERTIES_FILE_EXT = "properties"; // NOI18N
    // the following should be J2SEConfigurationProvider.CONFIG_PROPS_PATH which is now inaccessible from here
    public static final String CONFIG_PROPERTIES_FILE = "nbproject/private/config.properties"; // NOI18N    

    public static String getSharedConfigFilePath(final @NonNull String config)
    {
        return PROJECT_CONFIGS_DIR + "/" + config + "." + PROPERTIES_FILE_EXT; // NOI18N
    }

    public static String getPrivateConfigFilePath(final @NonNull String config)
    {
        return PROJECT_PRIVATE_CONFIGS_DIR + "/" + config + "." + PROPERTIES_FILE_EXT; // NOI18N
    }

    private Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> RUN_CONFIGS;
    private Set<String> ERASED_CONFIGS;
    private Map<String/*|null*/,List<Map<String,String/*|null*/>>/*|null*/> APP_PARAMS;
    private BoundedPropertyGroups groups = new BoundedPropertyGroups();
    private String active;
    
    private FileObject projectDir;

    // list of all properties related to project configurations (excluding application parameter properties that are handled separately)
    private List<String> PROJECT_PROPERTIES = new ArrayList<String>();
    // list of those properties that should be stored in private.properties instead of project.properties
    private List<String> PRIVATE_PROPERTIES = new ArrayList<String>();
    // list of properties that, if set, should later not be overriden by changes in default configuration
    // (useful for keeping pre-defined configurations that do not change unexpectedly after changes in default config)
    // Note that the standard behavior is: when setting a default property, the property is checked in all configs
    // and reset if its value in any non-def config is equal to that in default config
    private List<String> STATIC_PROPERTIES = new ArrayList<String>();
    // defaults if missing - on read, substitute missing property values by those registered here
    private Map<String, String> DEFAULT_IF_MISSING = new HashMap<String, String>();
    // on save remove the following props from file if they are empty
    private List<String> CLEAN_EMPTY_PROJECT_PROPERTIES = new ArrayList<String>();
    private List<String> CLEAN_EMPTY_PRIVATE_PROPERTIES = new ArrayList<String>();

    private Comparator<String> getComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1 != null ? (s2 != null ? s1.compareTo(s2) : 1) : (s2 != null ? -1 : 0);
            }
        };
    }

    JFXProjectConfigurations(final @NonNull FileObject projectDirFO) {
        projectDir = projectDirFO;
        reset();
    }

    public void registerProjectProperties(String[] props) {
        if(props != null) {
            PROJECT_PROPERTIES.addAll(Arrays.asList(props));
        }
    }            
    public void registerPrivateProperties(String[] props) {
        if(props != null) {
            PRIVATE_PROPERTIES.addAll(Arrays.asList(props));
        }
    }
    public void registerStaticProperties(String[] props) {
        if(props != null) {
            STATIC_PROPERTIES.addAll(Arrays.asList(props));
        }
    }
    public void registerDefaultsIfMissing(Map<String, String> defaults) {
        if(defaults != null) {
            DEFAULT_IF_MISSING.putAll(defaults);
        }
    }
    public void registerCleanEmptyProjectProperties(String[] props) {
        if(props != null) {
            CLEAN_EMPTY_PROJECT_PROPERTIES.addAll(Arrays.asList(props));
        }
    }
    public void registerCleanEmptyPrivateProperties(String[] props) {
        if(props != null) {
            CLEAN_EMPTY_PRIVATE_PROPERTIES.addAll(Arrays.asList(props));
        }
    }
    public void resetProjectProperties() {
        PROJECT_PROPERTIES.clear();
    }
    public void resetPrivateProperties() {
        PRIVATE_PROPERTIES.clear();
    }
    public void resetStaticProperties() {
        STATIC_PROPERTIES.clear();
    }
    public void resetDefaultsIfMissing() {
        DEFAULT_IF_MISSING.clear();
    }
    public void resetCleanEmptyProjectProperties() {
        CLEAN_EMPTY_PROJECT_PROPERTIES.clear();
    }
    public void resetCleanEmptyPrivateProperties() {
        CLEAN_EMPTY_PRIVATE_PROPERTIES.clear();
    }

    private void reset() {
        RUN_CONFIGS = new TreeMap<String,Map<String,String>>(getComparator());
        ERASED_CONFIGS = null;
        APP_PARAMS = new TreeMap<String,List<Map<String,String>>>(getComparator());
    }

    private boolean configNameWrong(String config) {
        return config !=null && config.contains("default"); //NOI18N
    }

    public final void defineGroup(String groupName, Collection<String> props) {
        groups.defineGroup(groupName, props);
    }

    public final void clearGroup(String groupName) {
        groups.clearGroup(groupName);
    }

    public final void clearAllGroups() {
        groups.clearAllGroups();
    }

    public boolean isBound(String prop) {
        return groups.isBound(prop);
    }

    public Collection<String> getBoundedProperties(String prop) {
        return groups.getBoundedProperties(prop);
    }

    //==========================================================

    public String getActive() {
        return active;
    }
    public void setActive(String config) {
        assert !configNameWrong(config);
        active = config;
    }

    //==========================================================

    public boolean hasConfig(String config) {
        assert !configNameWrong(config);
        return RUN_CONFIGS.containsKey(config);
    }

    public boolean isConfigEmpty(String config) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap != null) {
            return configMap.isEmpty();
        }
        return true;
    }

    public boolean isDefaultConfigEmpty() {
        return isConfigEmpty(null);
    }

    public boolean isActiveConfigEmpty() {
        return isConfigEmpty(getActive());
    }

    //----------------------------------------------------------

    public Set<String> getConfigNames() {
        return Collections.unmodifiableSet(RUN_CONFIGS.keySet());
    }

    private Map<String,String/*|null*/> getConfigUnmodifyable(String config) {
        assert !configNameWrong(config);
        return Collections.unmodifiableMap(RUN_CONFIGS.get(config));
    }

    private Map<String,String/*|null*/> getDefaultConfigUnmodifyable() {
        return getConfigUnmodifyable(null);
    }

    private Map<String,String/*|null*/> getActiveConfigUnmodifyable() {
        return getConfigUnmodifyable(getActive());
    }

    private Map<String,String/*|null*/> getConfig(String config) {
        assert !configNameWrong(config);
        return RUN_CONFIGS.get(config);
    }

    private Map<String,String/*|null*/> getDefaultConfig() {
        return getConfig(null);
    }

    private Map<String,String/*|null*/> getActiveConfig() {
        return getConfig(getActive());
    }

    private Map<String,String/*|null*/> getConfigNonNull(String config) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap == null) {
            configMap = new TreeMap<String,String>(getComparator());
            RUN_CONFIGS.put(config, configMap);
        }
        return configMap;
    }

    private Map<String,String/*|null*/> getDefaultConfigNonNull() {
        return getConfigNonNull(null);
    }

    private Map<String,String/*|null*/> getActiveConfigNonNull() {
        return getConfigNonNull(getActive());
    }

    //----------------------------------------------------------

    /**
     * Adds new and replaces existing properties
     * @param config
     * @param props 
     */
    public void addToConfig(String config, Map<String,String/*|null*/> props) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap == null) {
            configMap = new TreeMap<String,String>(getComparator());
            RUN_CONFIGS.put(config, configMap);
        }
        configMap.putAll(props);
    }

    public void addToDefaultConfig(Map<String,String/*|null*/> props) {
        addToConfig(null, props);
    }

    public void addToActiveConfig(Map<String,String/*|null*/> props) {
        addToConfig(getActive(), props);
    }

    public void addToConfig(String config, EditableProperties props) {
        assert !configNameWrong(config);
        addToConfig(config, new HashMap<String,String>(props));
    }

    public void addToDefaultConfig(EditableProperties props) {
        addToConfig(null, props);
    }

    public void addToActiveConfig(EditableProperties props) {
        addToConfig(getActive(), props);
    }

    //----------------------------------------------------------

    public void eraseConfig(String config) {
        assert !configNameWrong(config);
        assert config != null; // erasing default config not allowed
        RUN_CONFIGS.remove(config);
        if(ERASED_CONFIGS == null) {
            ERASED_CONFIGS = new HashSet<String>();
        }
        ERASED_CONFIGS.add(config);
    }

    //==========================================================

    /**
     * Returns true if property name is defined in configuration config, false otherwise
     * @param config
     * @param name
     * @return 
     */
    public boolean isPropertySet(String config, @NonNull String prop) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap != null) {
            return configMap.containsKey(prop);
        }
        return false;
    }

    public boolean isDefaultPropertySet(@NonNull String prop) {
        return isPropertySet(null, prop);
    }

    public boolean isActivePropertySet(@NonNull String prop) {
        return isPropertySet(getActive(), prop);
    }

    /**
     * Returns true if bounded properties exist for prop and at least
     * one of them is set. This is to be used in updateProperty() to
     * indicate that an empty property needs to be stored to editable properties
     * 
     * @param config
     * @param prop
     * @return 
     */
    private boolean isBoundedToNonemptyProperty(String config, String prop) {
        assert !configNameWrong(config);
        for(String name : groups.getBoundedProperties(prop)) {
            if(isPropertySet(config, name)) {
                return true;
            }
        }
        return false;
    }

    //----------------------------------------------------------

    /**
     * Returns property value from configuration config if defined, null otherwise
     * @param config
     * @param name
     * @return 
     */
    public String getProperty(String config, @NonNull String prop) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap != null) {
            return configMap.get(prop);
        }
        return null;
    }

    public String getDefaultProperty(@NonNull String prop) {
        return getProperty(null, prop);
    }

    public String getActiveProperty(@NonNull String prop) {
        return getProperty(getActive(), prop);
    }

    /**
     * Returns property value from configuration config (if exists), or
     * value from default config (if exists) otherwise
     * 
     * @param config
     * @param name
     * @return 
     */
    public String getPropertyTransparent(String config, @NonNull String prop) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        String value = null;
        if(configMap != null) {
            value = configMap.get(prop);
            if(value == null && config != null) {
                return getDefaultProperty(prop);
            }
        }
        return value;
    }

    public String getActivePropertyTransparent(@NonNull String prop) {
        return getPropertyTransparent(getActive(), prop);
    }

    //----------------------------------------------------------

    public void setProperty(String config, @NonNull String prop, String value) {
        setPropertyImpl(config, prop, value);
        solidifyBoundedGroups(config, prop);
        if(config == null) {
            for(String c: getConfigNames()) {
                if(c != null && JFXProjectProperties.isEqual(getProperty(c, prop), value) && !STATIC_PROPERTIES.contains(prop) && isBoundedPropertiesEraseable(c, prop)) {
                    eraseProperty(c, prop);
                }
            }
        }
    }

    private void setPropertyImpl(String config, @NonNull String prop, String value) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfigNonNull(config);
        configMap.put(prop, value);            
    }

    public void setDefaultProperty(@NonNull String prop, String value) {
        setProperty(null, prop, value);
    }

    public void setActiveProperty(@NonNull String prop, String value) {
        setProperty(getActive(), prop, value);
    }

    public void setPropertyTransparent(String config, @NonNull String prop, String value) {
        assert !configNameWrong(config);
        if(config != null && JFXProjectProperties.isEqual(getDefaultProperty(prop), value) && (!STATIC_PROPERTIES.contains(prop) || !isPropertySet(config, prop)) && isBoundedPropertiesEraseable(config, prop)) {
            eraseProperty(config, prop);
        } else {
            setProperty(config, prop, value);
        }
    }

    public void setActivePropertyTransparent(@NonNull String prop, String value) {
        setPropertyTransparent(getActive(), prop, value);
    }

    //----------------------------------------------------------

    /**
     * In non-default configurations if prop is not set, then
     * this method sets it to a value taken from default config.
     * The result is transparent to getPropertyTransparent(), which
     * returns the same value before and after solidifyProperty() call.
     * 
     * @param config
     * @param prop
     * @return false if property had existed in config, true if it had been set by this method
     */
    public boolean solidifyProperty(String config, @NonNull String prop) {
        if(!isPropertySet(config, prop)) {
            if(config != null) {
                setPropertyImpl(config, prop, getDefaultProperty(prop));
            } else {
                setPropertyImpl(null, prop, ""); // NOI18N
            }
            return true;
        }
        return false;
    }

    /**
     * Solidifies all properties that are in any bounded group with the 
     * property prop
     * 
     * @param config
     * @param prop
     * @return false if nothing was solidified, true otherwise
     */
    private boolean solidifyBoundedGroups(String config, @NonNull String prop) {
        boolean solidified = false;
        for(String name : groups.getBoundedProperties(prop)) {
            solidified |= solidifyProperty(config, name);
        }
        return solidified;
    }

    //----------------------------------------------------------

    public void eraseProperty(String config, @NonNull String prop) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> configMap = getConfig(config);
        if(configMap != null) {
            configMap.remove(prop);
            for(String name : groups.getBoundedProperties(prop)) {
                configMap.remove(name);
            }
        }
    }

    public void eraseDefaultProperty(@NonNull String prop) {
        eraseProperty(null, prop);
    }

    public void eraseActiveProperty(@NonNull String prop) {
        eraseProperty(getActive(), prop);
    }

    /**
     * Returns true if property prop and all properties bounded to it
     * can be erased harmlessly, i.e., to ensure that getPropertyTransparent()
     * returns for each of them the same value before and after erasing
     * 
     * @param prop
     * @return 
     */
    private boolean isBoundedPropertiesEraseable(String config, String prop) {
        assert !configNameWrong(config);
        if(config == null) {
            return false;
        }
        boolean canErase = true;
        for(String name : groups.getBoundedProperties(prop)) {
            if((isPropertySet(config, name) && !JFXProjectProperties.isEqual(getDefaultProperty(name), getProperty(config, name))) || STATIC_PROPERTIES.contains(name)) {
                canErase = false;
                break;
            }
        }
        return canErase;
    }

    //==========================================================

    /**
     * Returns true if param named name is present in configuration
     * config in any form - with value or without value
     * @param config
     * @param name
     * @return 
     */
    public boolean hasParam(String config, @NonNull String name) {
        assert !configNameWrong(config);
        return getParam(config, name) != null;
    }

    public boolean hasDefaultParam(@NonNull String name) {
        return hasParam(null, name);
    }

    public boolean hasActiveParam(@NonNull String name) {
        return hasParam(getActive(), name);
    }

    public boolean hasParamTransparent(String config, @NonNull String name) {
        assert !configNameWrong(config);
        //return hasParam(config, name) || hasDefaultParam(name);
        return getParamTransparent(config, name) != null;
    }

    public boolean hasActiveParamTransparent(@NonNull String name) {
        return hasParamTransparent(getActive(), name);
    }

    //----------------------------------------------------------

    /**
     * Returns true if exactly the parameter with name name and value value
     * is present in configuration config
     * 
     * @param config
     * @param name
     * @param value
     * @return 
     */
    public boolean hasParam(String config, @NonNull String name, @NonNull String value) {
        assert !configNameWrong(config);
        String v = getParamValue(config, name);
        return JFXProjectProperties.isEqual(v, value);
    }

    public boolean hasDefaultParam(@NonNull String name, @NonNull String value) {
        return hasParam(null, name, value);
    }

    public boolean hasActiveParam(@NonNull String name, @NonNull String value) {
        return hasParam(getActive(), name, value);
    }

    public boolean hasParamTransparent(String config, @NonNull String name, @NonNull String value) {
        assert !configNameWrong(config);
        String v = getParamValueTransparent(config, name);
        return JFXProjectProperties.isEqual(v, value);
    }

    //----------------------------------------------------------

    public boolean hasParamValue(String config, @NonNull String name) {
        assert !configNameWrong(config);
        Map<String, String> param = getParam(config, name);
        if(param != null) {
            if(param.containsKey(APP_PARAM_SUFFIXES[1])) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDefaultParamValue(@NonNull String name) {
        return hasParamValue(null, name);
    }

    public boolean hasActiveParamValue(@NonNull String name) {
        return hasParamValue(getActive(), name);
    }

    public boolean hasParamValueTransparent(String config, @NonNull String name) {
        assert !configNameWrong(config);
        return (config != null && hasParamValue(config, name)) || hasDefaultParamValue(name);
    }

    public boolean hasActiveParamValueTransparent(@NonNull String name) {
        return hasParamValueTransparent(getActive(), name);
    }

    //----------------------------------------------------------

    /**
     * Returns param as map if exists in configuration config, null otherwise
     * 
     * @param config
     * @param name
     * @return 
     */
    public Map<String, String> getParam(String config, @NonNull String name) {
        assert !configNameWrong(config);
        return getParam(getParams(config), name);
    }

    public Map<String, String> getDefaultParam(@NonNull String name) {
        return getParam((String)null, name);
    }

    public Map<String, String> getActiveParam(@NonNull String name) {
        return getParam(getActive(), name);
    }

    public Map<String, String> getParamTransparent(String config, @NonNull String name) {
        assert !configNameWrong(config);
        Map<String, String> param = getParam(config, name);
        if(param == null) {
            param = getDefaultParam(name);
        }
        return param;
    }

    public Map<String, String> getActiveParamTransparent(@NonNull String name) {
        return getParamTransparent(getActive(), name);
    }

    //----------------------------------------------------------

    /**
     * Note that returned null is ambiguous - may mean that there
     * was no value defined or that it was defined and its value was null.
     * To check this ask has*ParamValue*()
     * 
     * @param config
     * @param name
     * @return 
     */
    public String getParamValue(String config, @NonNull String name) {
        assert !configNameWrong(config);
        Map<String,String/*|null*/> param = getParam(config, name);
        if(param != null) {
            return param.get(APP_PARAM_SUFFIXES[1]);
        }
        return null;
    }

    /**
     * Note that returned null is ambiguous - may mean that there
     * was no value defined or that it was defined and its value was null.
     * To check this ask has*ParamValue*()
     * 
     * @param name
     * @return 
     */
    public String getDefaultParamValue(@NonNull String name) {
        return getParamValue(null, name);
    }

    /**
     * Note that returned null is ambiguous - may mean that there
     * was no value defined or that it was defined and its value was null.
     * To check this ask has*ParamValue*()
     * 
     * @param name
     * @return 
     */
    public String getActiveParamValue(@NonNull String name) {
        return getParamValue(getActive(), name);
    }

    /**
     * Note that returned null is ambiguous - may mean that there
     * was no value defined or that it was defined and its value was null.
     * To check this ask has*ParamValue*()
     * 
     * @param config
     * @param name
     * @return 
     */
    public String getParamValueTransparent(String config, @NonNull String name) {
        assert !configNameWrong(config);
        Map<String, String> param = getParam(config, name);
        if(param != null) {
            return param.get(APP_PARAM_SUFFIXES[1]);
        }
        return getDefaultParamValue(name);
    }

    /**
     * Note that returned null is ambiguous - may mean that there
     * was no value defined or that it was defined and its value was null.
     * To check this ask has*ParamValue*()
     * 
     * @param name
     * @return 
     */
    public String getActiveParamValueTransparent(@NonNull String name) {
        return getParamValueTransparent(getActive(), name);
    }

    //----------------------------------------------------------

    private List<Map<String,String/*|null*/>> getParams(String config) {
        assert !configNameWrong(config);
        return APP_PARAMS.get(config);
    }

    private List<Map<String,String/*|null*/>> getDefaultParams() {
        return APP_PARAMS.get(null);
    }

    private List<Map<String,String/*|null*/>> getActiveParams() {
        return APP_PARAMS.get(getActive());
    }

    /**
    * Returns (copy of) list of default parameters if config==default or
    * union of default config and current config parameters otherwise
    * 
    * @param config current config
    * @return union of default and current parameters
    */
    private List<Map<String,String/*|null*/>> getParamsTransparent(String config) {
        assert !configNameWrong(config);
        List<Map<String,String/*|null*/>> union = JFXProjectUtils.copyList(getDefaultParams());
        if(config != null && getParams(config) != null) {
            for(Map<String,String> map : getParams(config)) {
                String name = map.get(APP_PARAM_SUFFIXES[0]);
                String value = map.get(APP_PARAM_SUFFIXES[1]);
                if(name != null && !name.isEmpty()) {
                    Map<String, String> old = getParam(union, name);
                    if(old != null) {
                        old.put(APP_PARAM_SUFFIXES[0], name);
                        old.put(APP_PARAM_SUFFIXES[1], value);
                    } else {
                        union.add(map);
                    }
                }
            }
        }
        return union;
    }   

    public List<Map<String,String/*|null*/>> getActiveParamsTransparent() {
        return getParamsTransparent(getActive());
    }

    //----------------------------------------------------------

    /**
    * Gathers all parameters applicable to config configuration to one String
    * 
    * @param commandLine if true, formats output as if to be passed on command line, otherwise prouces comma separated list
    * @return a String containing all parameters as if passed as command line parameters
    */
    public String getParamsTransparentAsString(String config, boolean commandLine) {
        assert !configNameWrong(config);
        return getParamsAsString(getParamsTransparent(config), commandLine);
    }

    public String getActiveParamsTransparentAsString(boolean commandLine) {
        return getParamsAsString(getActiveParamsTransparent(), commandLine);
    }

    public String getParamsAsString(String config, boolean commandLine) {
        return getParamsAsString(getParams(config), commandLine);
    }

    public String getActiveParamsAsString(boolean commandLine) {
        return getParamsAsString(getActiveParams(), commandLine);
    }

    public String getDefaultParamsAsString(boolean commandLine) {
        return getParamsAsString(getDefaultParams(), commandLine);
    }

    private String getParamsAsString(List<Map<String,String/*|null*/>> params, boolean commandLine)
    {
        StringBuilder sb = new StringBuilder();
        if(params != null) {
            int index = 0;
            for(Map<String,String> m : params) {
                String name = m.get(APP_PARAM_SUFFIXES[0]);
                String value = m.get(APP_PARAM_SUFFIXES[1]);
                if(name != null && name.length() > 0) {
                    if(sb.length() > 0) {
                        if(!commandLine) {
                            sb.append(","); // NOI18N
                        }
                        sb.append(" "); // NOI18N
                    }
                    if(value != null && value.length() > 0) {
                        if(commandLine) {
                            sb.append("--"); // NOI18N
                        }
                        sb.append(name);
                        sb.append("="); // NOI18N
                        sb.append(value);
                    } else {
                        sb.append(name);                        
                    }
                    index++;
                }
            }
        }
        return sb.toString();
    }

    //----------------------------------------------------------

    private Map<String, String> createParam(@NonNull String name) {
        Map<String, String> param = new TreeMap<String,String>(getComparator());
        param.put(APP_PARAM_SUFFIXES[0], name);
        return param;
    }

    private Map<String, String> createParam(@NonNull String name, String value) {
        Map<String, String> param = new TreeMap<String,String>(getComparator());
        param.put(APP_PARAM_SUFFIXES[0], name);
        param.put(APP_PARAM_SUFFIXES[1], value);
        return param;
    }

    //----------------------------------------------------------

    /**
     * Add (or replace if present) valueless param (i.e., argument)
     * to configuration config
     */
    public void addParam(String config, @NonNull String name) {
        assert !configNameWrong(config);
        List<Map<String,String/*|null*/>> params = getParams(config);
        if(params == null) {
            params = new ArrayList<Map<String,String/*|null*/>>();
            APP_PARAMS.put(config, params);
        } else {
            eraseParam(params, name);
        }
        params.add(createParam(name));
    }

    public void addDefaultParam(@NonNull String name) {
        addParam(null, name);
    }

    public void addActiveParam(@NonNull String name) {
        addParam(getActive(), name);
    }

    public void addParamTransparent(String config, @NonNull String name) {
        assert !configNameWrong(config);
        if(config == null) {
            addDefaultParam(name);
        } else {
            if(hasDefaultParam(name) && !hasDefaultParamValue(name)) {
                eraseParam(config, name);
            } else {
                addParam(config, name);
            }
        }
    }

    public void addActiveParamTransparent(@NonNull String name) {
        addParamTransparent(getActive(), name);
    }

    //----------------------------------------------------------

    /**
     * Add (or replace if present) named param (i.e., having a value)
     * to configuration config
     */
    public void addParam(String config, @NonNull String name, String value) {
        assert !configNameWrong(config);
        List<Map<String,String/*|null*/>> params = getParams(config);
        if(params == null) {
            params = new ArrayList<Map<String,String/*|null*/>>();
            APP_PARAMS.put(config, params);
        } else {
            eraseParam(params, name);
        }
        params.add(createParam(name, value));
    }

    public void addDefaultParam(@NonNull String name, String value) {
        addParam(null, name, value);
    }

    public void addActiveParam(@NonNull String name, String value) {
        addParam(getActive(), name, value);
    }

    public void addParamTransparent(String config, @NonNull String name, String value) {
        assert !configNameWrong(config);
        if(config == null) {
            addDefaultParam(name, value);
        } else {
            if(hasDefaultParam(name, value)) {
                eraseParam(config, name);
            } else {
                addParam(config, name, value);
            }
        }
    }

    public void addActiveParamTransparent(@NonNull String name, String value) {
        addParamTransparent(getActive(), name, value);
    }

    //----------------------------------------------------------

    /**
    * Updates parameters; if config==default, then simply updates default parameters,
    * otherwise updates parameters in current config so that only those different
    * from those in default config are stored.
    * 
    * @param config
    * @param params 
    */
    public void setParamsTransparent(String config, List<Map<String,String/*|null*/>>/*|null*/ params) {
        assert !configNameWrong(config);
        if(config == null) {
            APP_PARAMS.put(null, params);
        } else {
            List<Map<String,String/*|null*/>> reduct = new ArrayList<Map<String,String/*|null*/>>();
            List<Map<String,String/*|null*/>> def = JFXProjectUtils.copyList(getDefaultParams());
            if(params != null) {
                for(Map<String,String> map : params) {
                    String name = map.get(APP_PARAM_SUFFIXES[0]);
                    String value = map.get(APP_PARAM_SUFFIXES[1]);
                    Map<String, String> old = getDefaultParam(name);
                    if(old != null) {
                        String oldValue = old.get(APP_PARAM_SUFFIXES[1]);
                        if( !JFXProjectProperties.isEqual(value, oldValue) ) {
                            reduct.add(JFXProjectUtils.copyMap(old));
                        }
                        def.remove(old);
                    } else {
                        reduct.add(JFXProjectUtils.copyMap(map));
                    }
                }
                for(Map<String,String> map : def) {
                    map.put(APP_PARAM_SUFFIXES[1], ""); // NOI18N
                    reduct.add(JFXProjectUtils.copyMap(map));
                }
            }
            APP_PARAMS.put(config, reduct);
        }
    }

    public void setActiveParamsTransparent(List<Map<String,String/*|null*/>>/*|null*/ params) {
        setParamsTransparent(getActive(), params);
    }

    //----------------------------------------------------------

    public void eraseParam(String config, @NonNull String name) {
        assert !configNameWrong(config);
        eraseParam(getParams(config), name);
    }

    public void eraseDefaultParam(@NonNull String name) {
        eraseParam((String)null, name);
    }

    public void eraseActiveParam(@NonNull String name) {
        eraseParam(getActive(), name);
    }

    public void eraseParams(String config) {
        assert !configNameWrong(config);
        APP_PARAMS.remove(config);
    }

    public void eraseDefaultParams() {
        eraseParams(null);
    }

    public void eraseActiveParams() {
        eraseParams(getActive());
    }

    //==========================================================

    /**
    * If paramName exists in params, returns the map representing it
    * Returns null if param does not exist.
    * 
    * @param params list of application parameters (each stored in a map in keys 'name' and 'value'
    * @param paramName parameter to be searched for
    * @return parameter if found, null otherwise
    */
    private Map<String, String> getParam(List<Map<String, String>> params, String paramName) {
        if(params != null) {
            for(Map<String, String> map : params) {
                String name = map.get(APP_PARAM_SUFFIXES[0]);
                if(name != null && name.equals(paramName)) {
                    return map;
                }
            }
        }
        return null;
    }

    private void eraseParam(List<Map<String, String>> params, String paramName) {
        if(params != null) {
            Map<String, String> toErase = null;
            for(Map<String, String> map : params) {
                String name = map.get(APP_PARAM_SUFFIXES[0]);
                if(name != null && name.equals(paramName)) {
                    toErase = map;
                    break;
                }
            }
            if(toErase != null) {
                params.remove(toErase);
            }
        }
    }

    //==========================================================

    /**
     * Reads configuration properties from project properties files
     * (modified from "A mess." from J2SEProjectProperties)"
     */
    public void read() {
    //Map<String/*|null*/,Map<String,String>> readRunConfigs() {
        reset();
        // read project properties
        readDefaultConfig(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // overwrite by project private properties
        readDefaultConfig(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        // set properties that were not set but should have a value
        addDefaultsIfMissing();
        // add project properties read from config files
        readNonDefaultConfigs(PROJECT_CONFIGS_DIR, true);
        // add/overwrite project properties read from private config files
        readNonDefaultConfigs(PROJECT_PRIVATE_CONFIGS_DIR, false);
    }

    private void readDefaultConfig(String propsFile) {
        EditableProperties ep = null;
        try {
            ep = JFXProjectUtils.readFromFile(projectDir, propsFile);
        } catch (IOException ex) {
            // can be ignored
        }
        if(ep != null) {
            for (String prop : PROJECT_PROPERTIES) {
                String v = ep.getProperty(prop);
                if (v != null) {
                    setDefaultProperty(prop, v);
                }
            }
        }
        extractDefaultParams(ep);
    }

    private void addDefaultsIfMissing() {
        for(String prop : DEFAULT_IF_MISSING.keySet()) {
            if(!isDefaultPropertySet(prop)) {
                setDefaultProperty(prop, DEFAULT_IF_MISSING.get(prop));
            }
        }
    }

    private void readNonDefaultConfigs(String subDir, boolean createIfNotExists) {
        FileObject configsFO = projectDir.getFileObject(subDir); // NOI18N
        if (configsFO != null) {
            for (FileObject kid : configsFO.getChildren()) {
                if (!kid.hasExt(PROPERTIES_FILE_EXT)) { // NOI18N
                    continue;
                }
                Map<String,String> c = getConfig(kid.getName());
                if (c == null && !createIfNotExists) {
                    continue;
                }
                EditableProperties cep = null;
                try {
                    cep = JFXProjectUtils.readFromFile( kid );
                } catch (IOException ex) {
                    // can be ignored
                }
                addToConfig(kid.getName(), cep);
                extractParams(cep, kid.getName());
            }
        }
    }

    /**
    * Extract from editable properties all properties depicting application parameters
    * and store them as such in params. If such exist in params, then override their values.
    * 
    * @param ep editable properties to extract from
    * @param params application parameters to add to / update in
    */
    private void extractParams(@NonNull EditableProperties ep, String config) {
        if(ep != null) {
            for(String prop : ep.keySet()) {
                if(prop.startsWith(APP_PARAM_PREFIX) && prop.endsWith(APP_PARAM_SUFFIXES[0])) {
                    String name = ep.getProperty(prop);
                    if(name != null) {
                        String propV = prop.replace(APP_PARAM_SUFFIXES[0], APP_PARAM_SUFFIXES[1]);
                        String value = ep.getProperty(propV);
                        if(value != null) {
                            addParam(config, name, value);
                        } else {
                            addParam(config, name);
                        }
                    }
                }
            }
        }
    }

    private void extractDefaultParams(@NonNull EditableProperties ep) {
        extractParams(ep, null);
    }

    private void extractActiveParams(@NonNull EditableProperties ep) {
        extractParams(ep, getActive());
    }

    //----------------------------------------------------------

    public void readActive() {
        try {
            setActive(JFXProjectUtils.readFromFile(projectDir, CONFIG_PROPERTIES_FILE).getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG));
        } catch(IOException e) {
            LOG.log(Level.WARNING, "Failed to read active configuration from {0}.", CONFIG_PROPERTIES_FILE); // NOI18N
        }
    }

    public void storeActive() throws IOException {
        String configPath = CONFIG_PROPERTIES_FILE;
        if (active == null) {
            try {
                JFXProjectUtils.deleteFile(projectDir, configPath);
            } catch (IOException ex) {
            }
        } else {
            final EditableProperties configProps = JFXProjectUtils.readFromFile(projectDir, configPath);
            configProps.setProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG, active);
            JFXProjectUtils.saveToFile(projectDir, configPath, configProps);
        }
    }

    //----------------------------------------------------------

    /**
     * Stores/updates configuration properties and parameters to EditableProperties in case of default
     * config, or directly to project properties files in case of non-default configs.
     * (modified from "A royal mess." from J2SEProjectProperties)"
     */
    public void store(EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {

        for (String name : PROJECT_PROPERTIES) {
            String value = getDefaultProperty(name);
            updateProperty(name, value, projectProperties, privateProperties, isBoundedToNonemptyProperty(null, name));
        }
        List<String> paramNamesUsed = new ArrayList<String>();
        updateDefaultParamProperties(projectProperties, privateProperties, paramNamesUsed);
        storeDefaultParamsAsCommandLine(privateProperties);

        for (Map.Entry<String,Map<String,String>> entry : RUN_CONFIGS.entrySet()) {
            String config = entry.getKey();
            if (config == null) {
                continue;
            }
            String sharedPath = getSharedConfigFilePath(config);
            String privatePath = getPrivateConfigFilePath(config);
            Map<String,String> configProps = entry.getValue();
            if (configProps == null) {
                try {
                    JFXProjectUtils.deleteFile(projectDir, sharedPath);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Failed to delete file: {0}", sharedPath); // NOI18N
                }
                try {
                    JFXProjectUtils.deleteFile(projectDir, privatePath);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Failed to delete file: {0}", privatePath); // NOI18N
                }
                continue;
            }
            final EditableProperties sharedCfgProps = JFXProjectUtils.readFromFile(projectDir, sharedPath);
            final EditableProperties privateCfgProps = JFXProjectUtils.readFromFile(projectDir, privatePath);
            boolean privatePropsChanged = false;

            for (Map.Entry<String,String> prop : configProps.entrySet()) {
                String name = prop.getKey();
                String value = prop.getValue();
                String defaultValue = getDefaultProperty(name);
                boolean storeIfEmpty = (defaultValue != null && defaultValue.length() > 0) || isBoundedToNonemptyProperty(config, name);
                privatePropsChanged |= updateProperty(name, value, sharedCfgProps, privateCfgProps, storeIfEmpty);
            }

            cleanPropertiesIfEmpty(CLEAN_EMPTY_PROJECT_PROPERTIES.toArray(new String[0]), 
                    config, sharedCfgProps);
            privatePropsChanged |= cleanPropertiesIfEmpty(CLEAN_EMPTY_PRIVATE_PROPERTIES.toArray(new String[0]), 
                    config, privateCfgProps);
            privatePropsChanged |= updateParamProperties(config, sharedCfgProps, privateCfgProps, paramNamesUsed);  
            privatePropsChanged |= storeParamsAsCommandLine(config, privateCfgProps);

            JFXProjectUtils.saveToFile(projectDir, sharedPath, sharedCfgProps);    //Make sure the definition file is always created, even if it is empty.
            if (privatePropsChanged) {                              //Definition file is written, only when changed
                JFXProjectUtils.saveToFile(projectDir, privatePath, privateCfgProps);
            }
        }
        if(ERASED_CONFIGS != null) {
            for (String entry : ERASED_CONFIGS) {
                if(!RUN_CONFIGS.containsKey(entry)) {
                    // config has been erased, and has not been recreated
                    String sharedPath = getSharedConfigFilePath(entry);
                    String privatePath = getPrivateConfigFilePath(entry);
                    try {
                        JFXProjectUtils.deleteFile(projectDir, sharedPath);
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Failed to delete file: {0}", sharedPath); // NOI18N
                    }
                    try {
                        JFXProjectUtils.deleteFile(projectDir, privatePath);
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Failed to delete file: {0}", privatePath); // NOI18N
                    }
                }
            }
        }
    }

    //----------------------------------------------------------

    /**
    * Updates the value of existing property in editable properties if value differs.
    * If value is not set or is set empty, removes property from editable properties
    * unless storeEmpty==true, in which case the property is preserved and set to empty
    * in editable properties.
    * 
    * @param name property to be updated
    * @param value new property value
    * @param projectProperties project editable properties
    * @param privateProperties private project editable properties
    * @param storeEmpty true==keep empty properties in editable properties, false==remove empty properties
    * @return true if private properties have been edited
    */
    private boolean updateProperty(@NonNull String name, String value, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, boolean storeEmpty) {
        boolean changePrivate = PRIVATE_PROPERTIES.contains(name) || privateProperties.containsKey(name);
        EditableProperties ep = changePrivate ? privateProperties : projectProperties;
        if(changePrivate) {
            projectProperties.remove(name);
        }
        if (!Utilities.compareObjects(value, ep.getProperty(name))) {
            if (value != null && (value.length() > 0 || storeEmpty)) {
                ep.setProperty(name, value);
            } else {
                ep.remove(name);
            }
            return changePrivate;
        }
        return false;
    }

    /**
    * Updates the value of existing property in editable properties if value differs.
    * If value is not set or is set empty, removes property from editable properties.
    *
    * @param name property to be updated
    * @param value new property value
    * @param projectProperties project editable properties
    * @param privateProperties private project editable properties
    */
    private boolean updateProperty(@NonNull String name, String value, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties) {
        return updateProperty(name, value, projectProperties, privateProperties, false);
    }

    /**
     * If property not present in config configuration, remove it from editable properties.
     * This is to propagate property deletions in config to property files
     * @param name
     * @param config
     * @param ep
     * @return true if properties have been edited
     */
    private boolean cleanPropertyIfEmpty(@NonNull String name, String config, @NonNull EditableProperties ep) {
        if(!isPropertySet(config, name)) {
            ep.remove(name);
            return true;
        }
        return false;
    }

    private boolean cleanPropertiesIfEmpty(@NonNull String[] names, String config, @NonNull EditableProperties ep) {
        boolean updated = false;
        for(String name : names) {
            updated |= cleanPropertyIfEmpty(name, config, ep);
        }
        return updated;
    }

    //----------------------------------------------------------

    private boolean isParamNameProperty(@NonNull String prop) {
        return prop != null && prop.startsWith(APP_PARAM_PREFIX) && prop.endsWith(APP_PARAM_SUFFIXES[0]);
    }

    private boolean isParamValueProperty(@NonNull String prop) {
        return prop != null && prop.startsWith(APP_PARAM_PREFIX) && prop.endsWith(APP_PARAM_SUFFIXES[1]);
    }

    private String getParamValueProperty(String paramNameProperty) {
        if(paramNameProperty != null && isParamNameProperty(paramNameProperty)) {
            return paramNameProperty.replace(APP_PARAM_SUFFIXES[0], APP_PARAM_SUFFIXES[1]);
        }
        return null;
    }

    private String getParamNameProperty(int index) {
        return APP_PARAM_PREFIX + index + "." + APP_PARAM_SUFFIXES[0]; // NOI18N
    }

    private String getParamValueProperty(int index) {
        return APP_PARAM_PREFIX + index + "." + APP_PARAM_SUFFIXES[1]; // NOI18N
    }

    private boolean isFreeParamPropertyIndex(int index, @NonNull EditableProperties ep) {
        return !ep.containsKey(getParamNameProperty(index));
    }

    private int getFreeParamPropertyIndex(int start, @NonNull EditableProperties ep, @NonNull EditableProperties pep, List<String> paramNamesUsed) {
        int index = (start >= 0) ? start : 0;
        while(index >= 0) {
            if(isFreeParamPropertyIndex(index, ep) && isFreeParamPropertyIndex(index, pep) && (paramNamesUsed == null || !paramNamesUsed.contains(getParamNameProperty(index)))) {
                break;
            }
            index++;
        }
        return (index >= 0) ? index : 0;
    }

    /**
     * Adds/updates properties representing parameters in editable properties
     * 
     * @param config
     * @param projectProperties
     * @param privateProperties
     * @return 
     */
    private boolean updateParamProperties(String config, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, @NonNull List<String> paramNamesUsed) {
        assert !configNameWrong(config);
        boolean privateUpdated = false;
        List<Map<String, String>> reduce = JFXProjectUtils.copyList(getParams(config));
        // remove properties with indexes used before (to be replaced later by new unique property names)
        for(String prop : paramNamesUsed) {
            if(prop != null && prop.length() > 0) {
                projectProperties.remove(prop);
                projectProperties.remove(getParamValueProperty(prop));
                privateProperties.remove(prop);
                privateProperties.remove(getParamValueProperty(prop));
            }
        }
        // delete those private param properties not present in config and log usage of the remaining
        cleanParamPropertiesIfEmpty(config, privateProperties);
        for(String prop : privateProperties.keySet()) {
            if(isParamNameProperty(prop)) {
                paramNamesUsed.add(prop);
            }
        }
        // update private properties
        List<Map<String, String>> toEraseList = new LinkedList<Map<String, String>>();
        for(Map<String, String> map : reduce) {
            String name = map.get(APP_PARAM_SUFFIXES[0]);
            String value = map.get(APP_PARAM_SUFFIXES[1]);
            if(updateParamPropertyIfExists(name, value, privateProperties, true)) {
                toEraseList.add(map);
                privateUpdated = true;
            }
        }
        for(Map<String, String> toErase : toEraseList) {
            reduce.remove(toErase);
        }
        // delete those nonprivate param properties not present in reduce and log usage of the remaining
        cleanParamPropertiesNotListed(reduce, projectProperties);
        for(String prop : projectProperties.keySet()) {
            if(isParamNameProperty(prop)) {
                paramNamesUsed.add(prop);
            }
        }
        // now create new nonprivate param properties
        int index = 0;
        for(Map<String, String> map : reduce) {
            String name = map.get(APP_PARAM_SUFFIXES[0]);
            String value = map.get(APP_PARAM_SUFFIXES[1]);
            if(name != null && name.length() > 0 && !updateParamPropertyIfExists(name, value, projectProperties, false)) {
                index = getFreeParamPropertyIndex(index, projectProperties, privateProperties, paramNamesUsed);
                exportParamProperty(map, getParamNameProperty(index), getParamValueProperty(index), projectProperties);
                paramNamesUsed.add(getParamNameProperty(index));
            }
        }
        return privateUpdated;
    }

    private boolean updateDefaultParamProperties(@NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, List<String> paramNamesUsed) {
        return updateParamProperties(null, projectProperties, privateProperties, paramNamesUsed);
    }

    /**
    * Searches in properties for parameter named 'name'. If found, updates
    * both existing param properties (for 'name' and 'value') and returns
    * true, otherwise returns false.
    * 
    * @param name parameter name
    * @param value parameter value
    * @param properties editable properties in which to search for updateable properties
    * @param storeEmpty true==keep empty properties in editable properties, false==remove empty properties
    * @return true if updated existing property, false otherwise
    */
    private boolean updateParamPropertyIfExists(@NonNull String name, String value, EditableProperties ep, boolean storeEmpty) {
        if(name != null && !name.isEmpty()) {
            for(String prop : ep.keySet()) {
                if(isParamNameProperty(prop)) {
                    if(JFXProjectProperties.isEqual(name, ep.get(prop))) {
                        String propVal = getParamValueProperty(prop);
                        if (value != null && (value.length() > 0 || storeEmpty)) {
                            ep.setProperty(propVal, value);
                        } else {
                            ep.remove(propVal);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
    * Remove from ep all parameter related properties that represent
    * params not present in config
    * 
    * @param ep editable properties
    */
    private void cleanParamPropertiesIfEmpty(String config, EditableProperties ep) {
        assert !configNameWrong(config);
        List<String> toRemove = new LinkedList<String>();
        for(String prop : ep.keySet()) {
            if(isParamNameProperty(prop)) {
                String name = ep.get(prop);
                if(!hasParam(config, name)) {
                    toRemove.add(prop);
                }
            }
        }
        for(String prop : toRemove) {
            ep.remove(prop);
            ep.remove(getParamValueProperty(prop));
        }
    }

    /**
    * Remove from ep all parameter related properties that represent
    * params not present in props
    * 
    * @param ep editable properties
    */
    private void cleanParamPropertiesNotListed(List<Map<String, String>> props, EditableProperties ep) {
        List<String> toRemove = new LinkedList<String>();
        for(String name : ep.keySet()) {
            if(isParamNameProperty(name)) {
                boolean inProps = false;
                for(Map<String,String> map : props) {
                    String prop = map.get(APP_PARAM_SUFFIXES[0]);
                    if(JFXProjectProperties.isEqual(name, prop)) {
                        inProps = true;
                        break;
                    }
                }
                if(!inProps) {
                    toRemove.add(name);
                }
            }
        }
        for(String prop : toRemove) {
            ep.remove(prop);
            ep.remove(getParamValueProperty(prop));
        }
    }

    /**
    * Store one parameter to editable properties (effectively as two properties,
    * one for name, second for value), index is used to distinguish among
    * parameter-property instances
    * 
    * @param param parameter to be stored in editable properties
    * @param newPropName name of property to store parameter name
    * @param newPropValue name of property to store parameter value
    * @param ep editable properties to which param is to be stored
    */
    private void exportParamProperty(@NonNull Map<String, String> param, String newPropName, String newPropValue, @NonNull EditableProperties ep) {
        String name = param.get(APP_PARAM_SUFFIXES[0]);
        String value = param.get(APP_PARAM_SUFFIXES[1]);
        if(name != null) {
            ep.put(newPropName, name);
            if(value != null && value.length() > 0) {
                ep.put(newPropValue, value);
            }
        }
    }

    /**
    * Gathers application parameters to one property APPLICATION_ARGS
    * to be passed to run/debug target in build-impl.xml when Run as Standalone
    * 
    * @param config
    * @param ep editable properties to which to store the generated property
    * @return true if properties have been edited
    */
    private boolean storeParamsAsCommandLine(String config, EditableProperties projectProperties) {
        assert !configNameWrong(config);
        String params = getParamsTransparentAsString(config, true);
        if(config != null) {
            if(JFXProjectProperties.isEqual(params, getDefaultParamsAsString(true))) {
                params = null;
            }
        }
        if (!Utilities.compareObjects(params, projectProperties.getProperty(APPLICATION_ARGS))) {
            if (params != null && params.length() > 0) {
                projectProperties.setProperty(APPLICATION_ARGS, params);
                projectProperties.setComment(APPLICATION_ARGS, new String[]{"# " + NbBundle.getMessage(JFXProjectConfigurations.class, "COMMENT_app_args")}, false); // NOI18N
            } else {
                projectProperties.remove(APPLICATION_ARGS);
            }
            return true;
        }
        return false;
    }

    private boolean storeDefaultParamsAsCommandLine(EditableProperties projectProperties) {
        return storeParamsAsCommandLine(null, projectProperties);
    }

    //----------------------------------------------------------

    /**
     * For properties registered in bounded groups special
     * handling is to be followed. Either all bounded properties
     * must exist or none of bounded properties must exist
     * in project configuration. The motivation is to enable
     * treating all Preloader related properties is one pseudo-property
     */
    private class BoundedPropertyGroups {

        Map<String, Set<String>> groups = new HashMap<String, Set<String>>();

        public void defineGroup(String groupName, Collection<String> props) {
            Set<String> group = new HashSet<String>();
            group.addAll(props);
            groups.put(groupName, group);
        }

        public void clearGroup(String groupName) {
            groups.remove(groupName);
        }

        public void clearAllGroups() {
            groups.clear();
        }

        /**
         * Returns true if property prop is bound with any other properties
         * @return 
         */
        public boolean isBound(String prop) {
            for(Map.Entry<String, Set<String>> entry : groups.entrySet()) {
                Set<String> group = entry.getValue();
                if(group != null && group.contains(prop) && group.size() > 1) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns collection of all properties from any group of which
         * property prop is member. prop is not included in result.
         * @param prop
         * @return 
         */
        public Collection<String> getBoundedProperties(String prop) {
            Set<String> bounded = new HashSet<String>();
            for(Map.Entry<String, Set<String>> entry : groups.entrySet()) {
                Set<String> group = entry.getValue();
                if(group != null && group.contains(prop)) {
                    bounded.addAll(group);
                }
            }
            bounded.remove(prop);
            return bounded;
        }
    }
}
