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
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.panels;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JDKUtils;
import org.netbeans.installer.utils.applications.Version;
import org.netbeans.installer.wizard.components.actions.SearchForJdkAction;

/**
 *
 * @author ks152834
 */
public class JdkLocationPanel extends ApplicationLocationPanel {
    private Version minimumVersion;
    private Version maximumVersion;
    
    public JdkLocationPanel() {
        setProperty(MINIMUM_JDK_VERSION_PROPERTY, DEFAULT_MINIMUM_JDK_VERSION);
        setProperty(MAXIMUM_JDK_VERSION_PROPERTY, DEFAULT_MAXIMUM_JDK_VERSION);
        
        setProperty(MESSAGE_TEXT_PROPERTY, DEFAULT_MESSAGE_TEXT);
        setProperty(MESSAGE_TEXT_NOTHING_FOUND_PROPERTY, DEFAULT_MESSAGE_TEXT_NOTHING_FOUND);
        setProperty(MESSAGE_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_CONTENT_TYPE);
        setProperty(LOCATION_LABEL_TEXT_PROPERTY, DEFAULT_LOCATION_LABEL_TEXT);
        setProperty(LOCATION_BUTTON_TEXT_PROPERTY, DEFAULT_LOCATION_BUTTON_TEXT);
        setProperty(LIST_LABEL_TEXT_PROPERTY, DEFAULT_LIST_LABEL_TEXT);
        
        setProperty(ERROR_NULL_PROPERTY, DEFAULT_ERROR_NULL);
        setProperty(ERROR_NOT_VALID_PATH_PROPERTY, DEFAULT_ERROR_NOT_VALID_PATH);
        setProperty(ERROR_NOT_JAVAHOME_PROPERTY, DEFAULT_ERROR_NOT_JAVAHOME);
        setProperty(ERROR_NOT_JDK_PROPERTY, DEFAULT_ERROR_NOT_JDK);
        setProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY, DEFAULT_ERROR_WRONG_VERSION_OLDER);
        setProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY, DEFAULT_ERROR_WRONG_VERSION_NEWER);
        setProperty(ERROR_UNKNOWN_PROPERTY, DEFAULT_ERROR_UNKNOWN);
    }
    
    public void initialize() {
        minimumVersion = new Version(systemUtils.parseString(getProperty(MINIMUM_JDK_VERSION_PROPERTY), getClassLoader()));
        maximumVersion = new Version(systemUtils.parseString(getProperty(MAXIMUM_JDK_VERSION_PROPERTY), getClassLoader()));
        
        super.initialize();
    }
    
    private Map<File, Version> filterLocations() {
        Map<File, Version> filteredLocations = new HashMap<File, Version>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<File, Version> foundLocations = (Map<File, Version>) System.getProperties().get(SearchForJdkAction.JDKS_LIST_PROPERTY);
            
            if (foundLocations != null) {
                for (File file: foundLocations.keySet()) {
                    Version version = foundLocations.get(file);
                    if (version.newerOrEquals(minimumVersion) && version.olderOrEquals(maximumVersion)) {
                        filteredLocations.put(file, version);
                    }
                }
            }
        } catch (ClassCastException e) {
            ErrorManager.getInstance().notify(ErrorLevel.ERROR, e);
        }
        
        return filteredLocations;
    }
    
    public Map<String, File> getLocations() {
        Map<String, File> locations = new HashMap<String, File>();
        
        try {
            Map<File, Version> filteredLocations = filterLocations();
            
            for (File file: filteredLocations.keySet()) {
                locations.put(file.getAbsolutePath() + " (v. " + filteredLocations.get(file) + ")", file);
            }
        } catch (ClassCastException e) {
            ErrorManager.getInstance().notify(ErrorLevel.ERROR, e);
        }
        
        return locations;
    }
    
    public String validateLocation(String path) {
        File file = new File(path);
        
        if (path.equals("")) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NULL_PROPERTY), getClassLoader()));
        }
        
        if (!systemUtils.isPathValid(path)) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NOT_VALID_PATH_PROPERTY), getClassLoader()), path);
        }
        
        if (!jdkUtils.isJavaHome(file)) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NOT_JAVAHOME_PROPERTY), getClassLoader()), path);
        }
        
        if (!jdkUtils.isJDK(file)) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_NOT_JDK_PROPERTY), getClassLoader()), path);
        }
        
        Version version = JDKUtils.getInstance().getVersion(file);
        
        if (version == null) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_UNKNOWN_PROPERTY), getClassLoader()), path);
        }
        
        if (version.olderThan(minimumVersion)) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY), getClassLoader()), path, version, minimumVersion);
        }
        
        if (version.newerThan(maximumVersion)) {
            return stringUtils.formatMessage(systemUtils.parseString(getProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY), getClassLoader()), path, version, maximumVersion);
        }
        
        return null;
    }
    
    public void setLocation(File location) {
        getWizard().getProductComponent().setProperty(JDK_LOCATION_PROPERTY, location.getAbsolutePath());
    }
    
    public File getSelectedLocation() {
        String path = getWizard().getProductComponent().getProperty(JDK_LOCATION_PROPERTY);
        
        if (path == null) {
            File selectedLocation = null;
            Map<File, Version> locations = filterLocations();
            
            for (File file: locations.keySet()) {
                if ((selectedLocation == null) || locations.get(file).newerThan(locations.get(selectedLocation))) {
                    selectedLocation = file;
                }
            }
            
            return selectedLocation;
        } else {
            return new File(path);
        }
    }
    
    private static JDKUtils      jdkUtils      = JDKUtils.getInstance();
    private static ResourceUtils resourceUtils = ResourceUtils.getInstance();
    private static StringUtils   stringUtils   = StringUtils.getInstance();
    private static SystemUtils   systemUtils   = SystemUtils.getInstance();
    
    public static final String JDK_LOCATION_PROPERTY = "jdk.location";
    
    public static final String MINIMUM_JDK_VERSION_PROPERTY = "minimum.jdk.version";
    public static final String MAXIMUM_JDK_VERSION_PROPERTY = "maximum.jdk.version";
    
    public static final String DEFAULT_MESSAGE_TEXT = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.message.text");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.message.content.type");
    public static final String DEFAULT_LOCATION_LABEL_TEXT = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.location.label.text");
    public static final String DEFAULT_LOCATION_BUTTON_TEXT = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.location.button.text");
    public static final String DEFAULT_LIST_LABEL_TEXT = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.list.label.text");
    
    public static final String ERROR_NULL_PROPERTY = "error.null";
    public static final String ERROR_NOT_VALID_PATH_PROPERTY = "error.not.valid.path";
    public static final String ERROR_NOT_JAVAHOME_PROPERTY = "error.not.javahome";
    public static final String ERROR_NOT_JDK_PROPERTY = "error.not.jdk";
    public static final String ERROR_WRONG_VERSION_OLDER_PROPERTY = "error.wrong.version.older";
    public static final String ERROR_WRONG_VERSION_NEWER_PROPERTY = "error.wrong.version.newer";
    public static final String ERROR_UNKNOWN_PROPERTY = "error.unknown";
    
    public static final String DEFAULT_ERROR_NULL = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.null");
    public static final String DEFAULT_ERROR_NOT_VALID_PATH = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.not.valid.path");
    public static final String DEFAULT_ERROR_NOT_JAVAHOME = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.not.javahome");
    public static final String DEFAULT_ERROR_NOT_JDK = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.not.jdk");
    public static final String DEFAULT_ERROR_WRONG_VERSION_OLDER = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.wrong.version.older");
    public static final String DEFAULT_ERROR_WRONG_VERSION_NEWER = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.wrong.version.newer");
    public static final String DEFAULT_ERROR_UNKNOWN = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.unknown");
    
    public static final String DEFAULT_MINIMUM_JDK_VERSION = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.minimum.jdk.version");;
    public static final String DEFAULT_MAXIMUM_JDK_VERSION = resourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.maximum.jdk.version");;
}
