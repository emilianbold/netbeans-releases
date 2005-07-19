/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;

/**
 * Basic support for storing general module's properties.
 *
 * @author Martin Krauskopf
 */
abstract class ModuleProperties {
    
    // Helpers for storing and retrieving real values currently stored on the disk
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    
    /** Represent main module's properties (nbproject/project.properties). */
    private EditableProperties projectProperties;
    
    /** Represent module's private properties (nbproject/private/private.properties). */
    private EditableProperties privateProperties;
    
    /** Creates a new instance of ModuleProperties */
    ModuleProperties(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.projectProperties = helper.getProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        this.privateProperties = helper.getProperties(
                AntProjectHelper.PRIVATE_PROPERTIES_PATH);
    }
    
    /**
     * Returns map of keys form main module properties and their default values.
     * Must be overriden by a subclass.
     */
    abstract Map/*<String, String>*/ getDefaultValues();
    
    AntProjectHelper getHelper() {
        return helper;
    }
    
    PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    EditableProperties getProjectProperties() {
        return projectProperties;
    }
    
    EditableProperties getPrivateProperties() {
        return privateProperties;
    }
    
    final String getProperty(String key) {
        String value = getProjectProperties().getProperty(key);
        return value != null ? value : (String) getDefaultValues().get(key);
    }
    
    final boolean getBooleanProperty(String key) {
        String bValue = getProperty(key);
        return bValue != null &&  (bValue.equalsIgnoreCase("true") || // NOI18N
                bValue.equalsIgnoreCase("yes")); // NOI18N
    }
    
    final String removeProperty(String key) {
        return (String) getProjectProperties().remove(key);
    }
    
    final String removePrivateProperty(String key) {
        return (String) getPrivateProperties().remove(key);
    }
    
    /**
     * The given property will be stored into the project's properties. If the
     * given value is equals to the default value it will be removed from the
     * properties.
     */
    final void setProperty(String key, String value) {
        String def = (String) getDefaultValues().get(key);
        if (def == null) {
            def = ""; // NOI18N
        }
        if (def.equals(value)) {
            getProjectProperties().remove(key);
        } else {
            getProjectProperties().setProperty(key, value);
        }
    }
    
    /**
     * The given property will be stored into the project's properties. If the
     * given value is equals to the default value it will be removed from the
     * properties.
     */
    final void setPrivateProperty(String key, String value) {
        String def = (String) getDefaultValues().get(key);
        if (def == null) {
            def = ""; // NOI18N
        }
        if (def.equals(value)) {
            getPrivateProperties().remove(key);
        } else {
            getPrivateProperties().setProperty(key, value);
        }
    }
    
    void setProperty(String key, String[] value) {
        getProjectProperties().setProperty(key, value);
    }
    
    final void setBooleanProperty(String key, boolean bProp) {
        setProperty(key, Boolean.toString(bProp));
    }
    
    String getProjectDisplayName() {
        return Util.getDisplayName(getHelper().getProjectDirectory());
    }
    
    final File getProjectDirectoryFile() {
        return FileUtil.toFile(getHelper().getProjectDirectory());
    }
    
    final String getProjectDirectory() {
        return getProjectDirectoryFile().getAbsolutePath();
    }
    
    /**
     * Stores cached properties. This is called when the user press <em>OK</em>
     * button in the properties customizer. If <em>Cancel</em> button is
     * pressed properties will not be saved. Be sure this method is called
     * whitin {@link ProjectManager#mutex}. However, you are well advised to
     * explicitly enclose a <em>complete</em> operation within write access to
     * prevent race conditions.
     */
    void storeProperties() throws IOException {
        // Store changes into in nbproject/project.properties
        getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,
                getProjectProperties());
        // Store changes into in nbproject/private/private.properties
        getHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH,
                getPrivateProperties());
    }
    
    /**
     * Helper method for storing platform into the appropriate place.
     */
    static void storePlatform(AntProjectHelper helper, NbPlatform platform) {
        // store platform properties
        EditableProperties props = helper.getProperties(
                NbModuleProjectGenerator.PLATFORM_PROPERTIES_PATH);
        props.put("nbplatform.active", platform.getID()); // NOI18N
        helper.putProperties(
                NbModuleProjectGenerator.PLATFORM_PROPERTIES_PATH, props);
    }
}
