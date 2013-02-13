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
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Project configurations maintenance class
 * 
 * Getter/Setter naming conventions:
 * "Property" in method name -> method deals with single properties in configuration given by method parameter config
 * "Default" in method name -> method deals with properties in default configuration
 * "Active" in method name -> method deals with properties in currently chosen configuration
 * "Transparent" in method name -> method deals with property in configuration fiven by method parameter config if
 *     exists, or with property in default configuration otherwise. This is to provide simple access to
 *     union of default and non-default properties that are to be presented to users in non-default configurations
 *
 * @author Petr Somol
 */
public class JFXProjectConfigurations {
    
    private static final Logger LOG = Logger.getLogger(JFXProjectConfigurations.class.getName());

    public static final String APPLICATION_ARGS = ProjectProperties.APPLICATION_ARGS;
    public static final String DEFAULT_CONFIG_NAME = "default";
    
    public static final String APP_PARAM_PREFIX = "javafx.param."; // NOI18N
    public static final String APP_PARAM_SUFFIXES[] = new String[] { "name", "value" }; // NOI18N
    public static final String APP_PARAM_CONNECT_SIGN = "="; // NOI18N

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
    private MultiProperty appParams;
            
    private Set<String> ERASED_CONFIGS;
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
        appParams = new MultiProperty(APP_PARAM_PREFIX, APP_PARAM_SUFFIXES, APP_PARAM_CONNECT_SIGN);
    }

    private boolean configNameWrong(String config) {
        return config !=null && config.contains(DEFAULT_CONFIG_NAME); //NOI18N
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
    // public proxies to access application parameters. May not cover
    // the whole of Multiproperty; add missing if needed
    
    /**
     * Proxy
     * @param config
     * @param name
     * @return 
     */
    public boolean hasParam(String config, @NonNull String name) {
        return appParams.hasEntry(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @param value
     * @return 
     */
    public boolean hasDefaultParam(@NonNull String name, @NonNull String value) {
        return appParams.hasDefaultEntry(name, value);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasDefaultParam(@NonNull String name) {
        return appParams.hasDefaultEntry(name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasDefaultParamValue(@NonNull String name) {
        return appParams.hasDefaultEntryValue(name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasActiveParam(@NonNull String name) {
        return appParams.hasActiveEntry(name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public boolean hasActiveParamTransparent(@NonNull String name) {
        return appParams.hasActiveEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public Map<String, String> getActiveParamTransparent(@NonNull String name) {
        return appParams.getActiveEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param name
     * @return 
     */
    public String getActiveParamValue(@NonNull String name) {
        return appParams.getActiveEntryValue(name);
    }
    
    /**
     * Proxy
     * @param config
     * @param name
     * @param value 
     */
    public void addParam(String config, @NonNull String name, String value) {
        appParams.addEntry(config, name, value);
    }
    
    /**
     * Proxy
     * @param config
     * @param name 
     */
    public void addParam(String config, @NonNull String name) {
        appParams.addEntry(config, name);
    }
    
    /**
     * Proxy
     * @param name
     * @param value 
     */
    public void addDefaultParam(@NonNull String name, String value) {
        appParams.addDefaultEntry(name, value);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void addDefaultParam(@NonNull String name) {
        appParams.addDefaultEntry(name);
    }
    
    /**
     * Proxy
     * @param name
     * @param value 
     */
    public void addActiveParam(@NonNull String name, String value) {
        appParams.addActiveEntry(name, value);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void addActiveParam(@NonNull String name) {
        appParams.addActiveEntry(name);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void addActiveParamTransparent(@NonNull String name) {
        appParams.addActiveEntryTransparent(name);
    }
    
    /**
     * Proxy
     * @param config
     * @param name 
     */
    public void eraseParam(String config, @NonNull String name) {
        appParams.eraseEntry(config, name);
    }
    
    /**
     * Proxy
     * @param name 
     */
    public void eraseDefaultParam(@NonNull String name) {
        appParams.eraseDefaultEntry(name);
    }
    
    /**
     * Proxy
     * @param config 
     */
    public void eraseParams(String config) {
        appParams.eraseEntry(config);
    }
    
    /**
     * Proxy
     */
    public void eraseDefaultParams() {
        appParams.eraseDefaultEntries();
    }

    /**
     * Proxy
     * @return 
     */
    public List<Map<String,String/*|null*/>> getActiveParamsTransparent() {
        return appParams.getActiveEntriesTransparent();
    }

    /**
     * Proxy
     * @param params 
     */
    public void setActiveParamsTransparent(List<Map<String,String/*|null*/>>/*|null*/ params) {
        appParams.setActiveEntriesTransparent(params);
    }

    /**
     * Proxy
     * @param config
     * @param commandLine
     * @return 
     */
    public String getParamsTransparentAsString(String config, boolean commandLine) {
        return appParams.getEntriesTransparentAsString(config, commandLine);
    }
    
    /**
     * Proxy
     * @param commandLine
     * @return 
     */
    public String getActiveParamsAsString(boolean commandLine) {
        return appParams.getActiveEntriesAsString(commandLine);
    }
    
    /**
     * Proxy
     * @param commandLine
     * @return 
     */
    public String getActiveParamsTransparentAsString(boolean commandLine) {
        return appParams.getActiveEntriesTransparentAsString(commandLine);
    }

    /**
     * Proxy
     * @param commandLine
     * @return 
     */
    public String getDefaultParamsAsString(boolean commandLine) {
        return appParams.getDefaultEntriesAsString(commandLine);
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
        appParams.extractDefaultEntries(ep);
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
                appParams.extractEntries(cep, kid.getName());
            }
        }
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
    * Gathers application parameters to one property APPLICATION_ARGS
    * to be passed to run/debug target in build-impl.xml when Run as Standalone
    * 
    * @param config
    * @param ep editable properties to which to store the generated property
    * @return true if properties have been edited
    */
    private boolean storeParamsAsCommandLine(String config, EditableProperties projectProperties) {
        assert !configNameWrong(config);
        String params = appParams.getEntriesTransparentAsString(config, true);
        if(config != null) {
            if(JFXProjectProperties.isEqual(params, appParams.getDefaultEntriesAsString(true))) {
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
        appParams.updateDefaultEntryProperties(projectProperties, privateProperties, paramNamesUsed);
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
            privatePropsChanged |= appParams.updateEntryProperties(config, sharedCfgProps, privateCfgProps, paramNamesUsed);  
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

    //----------------------------------------------------------

    /**
     * Project properties maintenance class. Handles properties that may have multiple
     * instances, like FX Application parameters, or custom manifest entries
     */
    private class MultiProperty {
    
        private Map<String/*|null*/,List<Map<String,String/*|null*/>>/*|null*/> APP_MULTIPROPS;
        private String prefix;
        private String suffixes[];
        private String connectSign;

        public MultiProperty(@NonNull String prefix, @NonNull String suffixes[], @NonNull String connectSign) {
            assert suffixes.length == 2; // need "name" and "value"
            this.prefix = prefix;
            this.suffixes = suffixes;
            this.connectSign = connectSign;
            reset();
        }
        
        public void reset() {
            APP_MULTIPROPS = new TreeMap<String,List<Map<String,String>>>(getComparator());
        }

        //==========================================================

        /**
         * Returns true if entry named name is present in configuration
         * config in any form - with value or without value
         * @param config
         * @param name
         * @return 
         */
        public boolean hasEntry(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return getEntry(config, name) != null;
        }

        public boolean hasDefaultEntry(@NonNull String name) {
            return hasEntry(null, name);
        }

        public boolean hasActiveEntry(@NonNull String name) {
            return hasEntry(getActive(), name);
        }

        public boolean hasEntryTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return getEntryTransparent(config, name) != null;
        }

        public boolean hasActiveEntryTransparent(@NonNull String name) {
            return hasEntryTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Returns true if exactly the entry with name name and value value
         * is present in configuration config
         * 
         * @param config
         * @param name
         * @param value
         * @return 
         */
        public boolean hasEntry(String config, @NonNull String name, @NonNull String value) {
            assert !configNameWrong(config);
            String v = getEntryValue(config, name);
            return JFXProjectProperties.isEqual(v, value);
        }

        public boolean hasDefaultEntry(@NonNull String name, @NonNull String value) {
            return hasEntry(null, name, value);
        }

        public boolean hasActiveEntry(@NonNull String name, @NonNull String value) {
            return hasEntry(getActive(), name, value);
        }

        public boolean hasEntryTransparent(String config, @NonNull String name, @NonNull String value) {
            assert !configNameWrong(config);
            String v = getEntryValueTransparent(config, name);
            return JFXProjectProperties.isEqual(v, value);
        }

        //----------------------------------------------------------
        // note that these do not search for concrete value, they
        // search for entry named name and ask whether such
        // entry has any value
        
        public boolean hasEntryValue(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> param = getEntry(config, name);
            if(param != null) {
                if(param.containsKey(suffixes[1])) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasDefaultEntryValue(@NonNull String name) {
            return hasEntryValue(null, name);
        }

        public boolean hasActiveEntryValue(@NonNull String name) {
            return hasEntryValue(getActive(), name);
        }

        public boolean hasEntryValueTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return (config != null && hasEntryValue(config, name)) || hasDefaultEntryValue(name);
        }

        public boolean hasActiveEntryValueTransparent(@NonNull String name) {
            return hasEntryValueTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Returns entry as map if exists in configuration config, null otherwise
         * 
         * @param config
         * @param name
         * @return 
         */
        public Map<String, String> getEntry(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return getEntry(getEntries(config), name);
        }

        public Map<String, String> getDefaultEntry(@NonNull String name) {
            return getEntry((String)null, name);
        }

        public Map<String, String> getActiveEntry(@NonNull String name) {
            return getEntry(getActive(), name);
        }

        public Map<String, String> getEntryTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> param = getEntry(config, name);
            if(param == null) {
                param = getDefaultEntry(name);
            }
            return param;
        }

        public Map<String, String> getActiveEntryTransparent(@NonNull String name) {
            return getEntryTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param config
         * @param name
         * @return 
         */
        public String getEntryValue(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> param = getEntry(config, name);
            if(param != null) {
                return param.get(suffixes[1]);
            }
            return null;
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param name
         * @return 
         */
        public String getDefaultEntryValue(@NonNull String name) {
            return getEntryValue(null, name);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param name
         * @return 
         */
        public String getActiveEntryValue(@NonNull String name) {
            return getEntryValue(getActive(), name);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param config
         * @param name
         * @return 
         */
        public String getEntryValueTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> param = getEntry(config, name);
            if(param != null) {
                return param.get(suffixes[1]);
            }
            return getDefaultEntryValue(name);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*EntryValue*()
         * 
         * @param name
         * @return 
         */
        public String getActiveEntryValueTransparent(@NonNull String name) {
            return getEntryValueTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        private List<Map<String,String/*|null*/>> getEntries(String config) {
            assert !configNameWrong(config);
            return APP_MULTIPROPS.get(config);
        }

        private List<Map<String,String/*|null*/>> getDefaultEntries() {
            return APP_MULTIPROPS.get(null);
        }

        private List<Map<String,String/*|null*/>> getActiveEntries() {
            return APP_MULTIPROPS.get(getActive());
        }

        /**
        * Returns (copy of) list of default entries if config==default or
        * union of default config and current config entries otherwise
        * 
        * @param config current config
        * @return union of default and current entries
        */
        private List<Map<String,String/*|null*/>> getEntriesTransparent(String config) {
            assert !configNameWrong(config);
            List<Map<String,String/*|null*/>> union = JFXProjectUtils.copyList(getDefaultEntries());
            if(config != null && getEntries(config) != null) {
                for(Map<String,String> map : getEntries(config)) {
                    String name = map.get(suffixes[0]);
                    String value = map.get(suffixes[1]);
                    if(name != null && !name.isEmpty()) {
                        Map<String, String> old = getEntry(union, name);
                        if(old != null) {
                            old.put(suffixes[0], name);
                            old.put(suffixes[1], value);
                        } else {
                            union.add(map);
                        }
                    }
                }
            }
            return union;
        }   

        public List<Map<String,String/*|null*/>> getActiveEntriesTransparent() {
            return getEntriesTransparent(getActive());
        }

        //----------------------------------------------------------

        /**
        * Gathers all entries applicable to config configuration to one String
        * 
        * @param commandLine if true, formats output as if to be passed on command line, otherwise prouces comma separated list
        * @return a String containing all entries as if passed as command line parameters
        */
        public String getEntriesTransparentAsString(String config, boolean commandLine) {
            assert !configNameWrong(config);
            return getEntriesAsString(getEntriesTransparent(config), commandLine);
        }

        public String getActiveEntriesTransparentAsString(boolean commandLine) {
            return getEntriesAsString(getActiveEntriesTransparent(), commandLine);
        }

        public String getEntriesAsString(String config, boolean commandLine) {
            return getEntriesAsString(getEntries(config), commandLine);
        }

        public String getActiveEntriesAsString(boolean commandLine) {
            return getEntriesAsString(getActiveEntries(), commandLine);
        }

        public String getDefaultEntriesAsString(boolean commandLine) {
            return getEntriesAsString(getDefaultEntries(), commandLine);
        }

        private String getEntriesAsString(List<Map<String,String/*|null*/>> props, boolean commandLine)
        {
            StringBuilder sb = new StringBuilder();
            if(props != null) {
                int index = 0;
                for(Map<String,String> m : props) {
                    String name = m.get(suffixes[0]);
                    String value = m.get(suffixes[1]);
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
                            if(commandLine) {
                                sb.append("="); // NOI18N
                            } else {
                                sb.append(connectSign); // NOI18N
                            }
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

        private Map<String, String> createEntry(@NonNull String name) {
            Map<String, String> prop = new TreeMap<String,String>(getComparator());
            prop.put(suffixes[0], name);
            return prop;
        }

        private Map<String, String> createEntry(@NonNull String name, String value) {
            Map<String, String> prop = new TreeMap<String,String>(getComparator());
            prop.put(suffixes[0], name);
            prop.put(suffixes[1], value);
            return prop;
        }

        //----------------------------------------------------------

        /**
         * Add (or replace if present) valueless entry (e.g., run argument)
         * to configuration config
         */
        public void addEntry(String config, @NonNull String name) {
            assert !configNameWrong(config);
            List<Map<String,String/*|null*/>> props = getEntries(config);
            if(props == null) {
                props = new ArrayList<Map<String,String/*|null*/>>();
                APP_MULTIPROPS.put(config, props);
            } else {
                eraseEntry(props, name);
            }
            props.add(createEntry(name));
        }

        public void addDefaultEntry(@NonNull String name) {
            addEntry(null, name);
        }

        public void addActiveEntry(@NonNull String name) {
            addEntry(getActive(), name);
        }

        public void addEntryTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            if(config == null) {
                addDefaultEntry(name);
            } else {
                if(hasDefaultEntry(name) && !hasDefaultEntryValue(name)) {
                    eraseEntry(config, name);
                } else {
                    addEntry(config, name);
                }
            }
        }

        public void addActiveEntryTransparent(@NonNull String name) {
            addEntryTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Add (or replace if present) named entry (i.e., having a value)
         * to configuration config
         */
        public void addEntry(String config, @NonNull String name, String value) {
            assert !configNameWrong(config);
            List<Map<String,String/*|null*/>> props = getEntries(config);
            if(props == null) {
                props = new ArrayList<Map<String,String/*|null*/>>();
                APP_MULTIPROPS.put(config, props);
            } else {
                eraseEntry(props, name);
            }
            props.add(createEntry(name, value));
        }

        public void addDefaultEntry(@NonNull String name, String value) {
            addEntry(null, name, value);
        }

        public void addActiveEntry(@NonNull String name, String value) {
            addEntry(getActive(), name, value);
        }

        public void addEntryTransparent(String config, @NonNull String name, String value) {
            assert !configNameWrong(config);
            if(config == null) {
                addDefaultEntry(name, value);
            } else {
                if(hasDefaultEntry(name, value)) {
                    eraseEntry(config, name);
                } else {
                    addEntry(config, name, value);
                }
            }
        }

        public void addActiveEntryTransparent(@NonNull String name, String value) {
            addEntryTransparent(getActive(), name, value);
        }

        //----------------------------------------------------------

        /**
        * Updates entries; if config==default, then simply updates default entries,
        * otherwise updates entries in current config so that only those different
        * from those in default config are stored.
        * 
        * @param config
        * @param entries 
        */
        public void setEntriesTransparent(String config, List<Map<String,String/*|null*/>>/*|null*/ entries) {
            assert !configNameWrong(config);
            if(config == null) {
                APP_MULTIPROPS.put(null, entries);
            } else {
                List<Map<String,String/*|null*/>> reduct = new ArrayList<Map<String,String/*|null*/>>();
                List<Map<String,String/*|null*/>> def = JFXProjectUtils.copyList(getDefaultEntries());
                if(entries != null) {
                    for(Map<String,String> map : entries) {
                        String name = map.get(suffixes[0]);
                        String value = map.get(suffixes[1]);
                        Map<String, String> old = getDefaultEntry(name);
                        if(old != null) {
                            String oldValue = old.get(suffixes[1]);
                            if( !JFXProjectProperties.isEqual(value, oldValue) ) {
                                reduct.add(JFXProjectUtils.copyMap(old));
                            }
                            def.remove(old);
                        } else {
                            reduct.add(JFXProjectUtils.copyMap(map));
                        }
                    }
                    for(Map<String,String> map : def) {
                        map.put(suffixes[1], ""); // NOI18N
                        reduct.add(JFXProjectUtils.copyMap(map));
                    }
                }
                APP_MULTIPROPS.put(config, reduct);
            }
        }

        public void setActiveEntriesTransparent(List<Map<String,String/*|null*/>>/*|null*/ entries) {
            setEntriesTransparent(getActive(), entries);
        }

        //----------------------------------------------------------

        public void eraseEntry(String config, @NonNull String name) {
            assert !configNameWrong(config);
            eraseEntry(getEntries(config), name);
        }

        public void eraseDefaultEntry(@NonNull String name) {
            eraseEntry((String)null, name);
        }

        public void eraseActiveEntry(@NonNull String name) {
            eraseEntry(getActive(), name);
        }

        public void eraseEntry(String config) {
            assert !configNameWrong(config);
            APP_MULTIPROPS.remove(config);
        }

        public void eraseDefaultEntries() {
            eraseEntry(null);
        }

        public void eraseActiveEntries() {
            eraseEntry(getActive());
        }

        //==========================================================

        /**
        * If entryName exists in entries, returns the map representing it
        * Returns null if entry does not exist.
        * 
        * @param entries list of application entries (each stored in a map in keys 'name' and 'value'
        * @param entryName entry to be searched for
        * @return entry if found, null otherwise
        */
        private Map<String, String> getEntry(List<Map<String, String>> entries, String entryName) {
            if(entries != null) {
                for(Map<String, String> map : entries) {
                    String name = map.get(suffixes[0]);
                    if(name != null && name.equals(entryName)) {
                        return map;
                    }
                }
            }
            return null;
        }

        private void eraseEntry(List<Map<String, String>> entries, String entryName) {
            if(entries != null) {
                Map<String, String> toErase = null;
                for(Map<String, String> map : entries) {
                    String name = map.get(suffixes[0]);
                    if(name != null && name.equals(entryName)) {
                        toErase = map;
                        break;
                    }
                }
                if(toErase != null) {
                    entries.remove(toErase);
                }
            }
        }

        //----------------------------------------------------------

        private boolean isEntryNameProperty(@NonNull String prop) {
            return prop != null && prop.startsWith(prefix) && prop.endsWith(suffixes[0]);
        }

        private boolean isEntryValueProperty(@NonNull String prop) {
            return prop != null && prop.startsWith(prefix) && prop.endsWith(suffixes[1]);
        }

        private String getEntryValueProperty(String entryNameProperty) {
            if(entryNameProperty != null && isEntryNameProperty(entryNameProperty)) {
                return entryNameProperty.replace(suffixes[0], suffixes[1]);
            }
            return null;
        }

        private String getEntryNameProperty(int index) {
            return prefix + index + "." + suffixes[0]; // NOI18N
        }

        private String getEntryValueProperty(int index) {
            return prefix + index + "." + suffixes[1]; // NOI18N
        }

        private boolean isFreeEntryPropertyIndex(int index, @NonNull EditableProperties ep) {
            return !ep.containsKey(getEntryNameProperty(index));
        }

        private int getFreeEntryPropertyIndex(int start, @NonNull EditableProperties ep, @NonNull EditableProperties pep, List<String> propNamesUsed) {
            int index = (start >= 0) ? start : 0;
            while(index >= 0) {
                if(isFreeEntryPropertyIndex(index, ep) && isFreeEntryPropertyIndex(index, pep) && (propNamesUsed == null || !propNamesUsed.contains(getEntryNameProperty(index)))) {
                    break;
                }
                index++;
            }
            return (index >= 0) ? index : 0;
        }

        /**
         * Adds/updates properties representing entries in editable properties
         * 
         * @param config
         * @param projectProperties
         * @param privateProperties
         * @return 
         */
        private boolean updateEntryProperties(String config, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, @NonNull List<String> propNamesUsed) {
            assert !configNameWrong(config);
            boolean privateUpdated = false;
            List<Map<String, String>> reduce = JFXProjectUtils.copyList(getEntries(config));
            // remove properties with indexes used before (to be replaced later by new unique property names)
            for(String prop : propNamesUsed) {
                if(prop != null && prop.length() > 0) {
                    projectProperties.remove(prop);
                    projectProperties.remove(getEntryValueProperty(prop));
                    privateProperties.remove(prop);
                    privateProperties.remove(getEntryValueProperty(prop));
                }
            }
            // delete those private prop properties not present in config and log usage of the remaining
            cleanEntryPropertiesIfEmpty(config, privateProperties);
            for(String prop : privateProperties.keySet()) {
                if(isEntryNameProperty(prop)) {
                    propNamesUsed.add(prop);
                }
            }
            // update private properties
            List<Map<String, String>> toEraseList = new LinkedList<Map<String, String>>();
            for(Map<String, String> map : reduce) {
                String name = map.get(suffixes[0]);
                String value = map.get(suffixes[1]);
                if(updateEntryPropertyIfExists(name, value, privateProperties, true)) {
                    toEraseList.add(map);
                    privateUpdated = true;
                }
            }
            for(Map<String, String> toErase : toEraseList) {
                reduce.remove(toErase);
            }
            // delete those nonprivate prop properties not present in reduce and log usage of the remaining
            cleanEntryPropertiesNotListed(reduce, projectProperties);
            for(String prop : projectProperties.keySet()) {
                if(isEntryNameProperty(prop)) {
                    propNamesUsed.add(prop);
                }
            }
            // now create new nonprivate prop properties
            int index = 0;
            for(Map<String, String> map : reduce) {
                String name = map.get(suffixes[0]);
                String value = map.get(suffixes[1]);
                if(name != null && name.length() > 0 && !updateEntryPropertyIfExists(name, value, projectProperties, false)) {
                    index = getFreeEntryPropertyIndex(index, projectProperties, privateProperties, propNamesUsed);
                    exportEntryProperty(map, getEntryNameProperty(index), getEntryValueProperty(index), projectProperties);
                    propNamesUsed.add(getEntryNameProperty(index));
                }
            }
            return privateUpdated;
        }

        private boolean updateDefaultEntryProperties(@NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, List<String> propNamesUsed) {
            return updateEntryProperties(null, projectProperties, privateProperties, propNamesUsed);
        }

        /**
        * Searches in properties for entry named 'name'. If found, updates
        * both existing entry properties (for 'name' and 'value') and returns
        * true, otherwise returns false.
        * 
        * @param name entry name
        * @param value entry value
        * @param properties editable properties in which to search for updateable entries
        * @param storeEmpty true==keep empty properties in editable properties, false==remove empty properties
        * @return true if updated existing property, false otherwise
        */
        private boolean updateEntryPropertyIfExists(@NonNull String name, String value, EditableProperties ep, boolean storeEmpty) {
            if(name != null && !name.isEmpty()) {
                for(String prop : ep.keySet()) {
                    if(isEntryNameProperty(prop)) {
                        if(JFXProjectProperties.isEqual(name, ep.get(prop))) {
                            String propVal = getEntryValueProperty(prop);
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
        * Remove from ep all prop related properties that represent
        * entries not present in config
        * 
        * @param ep editable properties
        */
        private void cleanEntryPropertiesIfEmpty(String config, EditableProperties ep) {
            assert !configNameWrong(config);
            List<String> toRemove = new LinkedList<String>();
            for(String prop : ep.keySet()) {
                if(isEntryNameProperty(prop)) {
                    String name = ep.get(prop);
                    if(!hasEntry(config, name)) {
                        toRemove.add(prop);
                    }
                }
            }
            for(String prop : toRemove) {
                ep.remove(prop);
                ep.remove(getEntryValueProperty(prop));
            }
        }

        /**
        * Remove from ep all entry related properties that represent
        * entries not present in 'entries'
        * 
        * @param ep editable properties
        */
        private void cleanEntryPropertiesNotListed(List<Map<String, String>> entries, EditableProperties ep) {
            List<String> toRemove = new LinkedList<String>();
            for(String name : ep.keySet()) {
                if(isEntryNameProperty(name)) {
                    boolean inProps = false;
                    for(Map<String,String> map : entries) {
                        String prop = map.get(suffixes[0]);
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
                ep.remove(getEntryValueProperty(prop));
            }
        }

        /**
        * Store one entry to editable properties (effectively as two properties,
        * one for name, second for value), index is used to distinguish among
        * entry-property instances
        * 
        * @param entry property to be stored in editable properties
        * @param newPropName name of property to store entry name
        * @param newPropValue name of property to store entry value
        * @param ep editable properties to which prop is to be stored
        */
        private void exportEntryProperty(@NonNull Map<String, String> entry, String newPropName, String newPropValue, @NonNull EditableProperties ep) {
            String name = entry.get(suffixes[0]);
            String value = entry.get(suffixes[1]);
            if(name != null) {
                ep.put(newPropName, name);
                if(value != null && value.length() > 0) {
                    ep.put(newPropValue, value);
                }
            }
        }

        // -------------------------------------------------------------------
        
        /**
        * Extract from editable properties all properties depicting application entries
        * and store them as such in 'entries'. If such exist in 'entries', then override their values.
        * 
        * @param ep editable properties to extract from
        * @param props application entries to add to / update in
        */
        private void extractEntries(@NonNull EditableProperties ep, String config) {
            if(ep != null) {
                for(String prop : ep.keySet()) {
                    if(prop.startsWith(prefix) && prop.endsWith(suffixes[0])) {
                        String name = ep.getProperty(prop);
                        if(name != null) {
                            String propV = prop.replace(suffixes[0], suffixes[1]);
                            String value = ep.getProperty(propV);
                            if(value != null) {
                                addEntry(config, name, value);
                            } else {
                                addEntry(config, name);
                            }
                        }
                    }
                }
            }
        }

        private void extractDefaultEntries(@NonNull EditableProperties ep) {
            extractEntries(ep, null);
        }

        private void extractActiveEntries(@NonNull EditableProperties ep) {
            extractEntries(ep, getActive());
        }

    }
    
}
