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
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JDKUtils;
import org.netbeans.installer.utils.helper.Version;
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
        minimumVersion = new Version(getProperty(MINIMUM_JDK_VERSION_PROPERTY));
        maximumVersion = new Version(getProperty(MAXIMUM_JDK_VERSION_PROPERTY));
        
        super.initialize();
    }
    
    private Map<File, Version> filterLocations() {
        Map<File, Version> filteredLocations = new HashMap<File, Version>();
        
        try {
            if (SearchForJdkAction.locatedJdks != null) {
                for (File file: SearchForJdkAction.locatedJdks.keySet()) {
                    Version version = SearchForJdkAction.locatedJdks.get(file);
                    if (version.newerOrEquals(minimumVersion) && version.olderOrEquals(maximumVersion)) {
                        filteredLocations.put(file, version);
                    }
                }
            }
        } catch (ClassCastException e) {
            ErrorManager.notify(ErrorLevel.ERROR, e);
        }
        
        return filteredLocations;
    }
    
    public Map<String, File> getLocations() {
        Map<String, File> locations = new HashMap<String, File>();
        
        try {
            Map<File, Version> filteredLocations = filterLocations();
            
            for (File file: filteredLocations.keySet()) {
                locations.put(file.getAbsolutePath() + " (v. " + filteredLocations.get(file).toJdkStyle() + ")", file);
            }
        } catch (ClassCastException e) {
            ErrorManager.notify(ErrorLevel.ERROR, e);
        }
        
        return locations;
    }
    
    public String validateLocation(String path) {
        File file = new File(path);
        
        if (path.equals("")) {
            return StringUtils.format(getProperty(ERROR_NULL_PROPERTY));
        }
        
        if (!SystemUtils.isPathValid(path)) {
            return StringUtils.format(getProperty(ERROR_NOT_VALID_PATH_PROPERTY), path);
        }
        
        if (!JDKUtils.isJavaHome(file)) {
            return StringUtils.format(getProperty(ERROR_NOT_JAVAHOME_PROPERTY), path);
        }
        
        if (!JDKUtils.isJdk(file)) {
            return StringUtils.format(getProperty(ERROR_NOT_JDK_PROPERTY), path);
        }
        
        Version version = JDKUtils.getVersion(file);
        
        if (version == null) {
            return StringUtils.format(getProperty(ERROR_UNKNOWN_PROPERTY), path);
        }
        
        if (version.olderThan(minimumVersion)) {
            return StringUtils.format(getProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY), path, version, minimumVersion);
        }
        
        if (version.newerThan(maximumVersion)) {
            return StringUtils.format(getProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY), path, version, maximumVersion);
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
    
    public static final String JDK_LOCATION_PROPERTY = "jdk.location";
    
    public static final String MINIMUM_JDK_VERSION_PROPERTY = "minimum.jdk.version";
    public static final String MAXIMUM_JDK_VERSION_PROPERTY = "maximum.jdk.version";
    
    public static final String DEFAULT_MESSAGE_TEXT = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.message.text");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.message.content.type");
    public static final String DEFAULT_LOCATION_LABEL_TEXT = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.location.label.text");
    public static final String DEFAULT_LOCATION_BUTTON_TEXT = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.location.button.text");
    public static final String DEFAULT_LIST_LABEL_TEXT = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.list.label.text");
    
    public static final String ERROR_NULL_PROPERTY = "error.null";
    public static final String ERROR_NOT_VALID_PATH_PROPERTY = "error.not.valid.path";
    public static final String ERROR_NOT_JAVAHOME_PROPERTY = "error.not.javahome";
    public static final String ERROR_NOT_JDK_PROPERTY = "error.not.jdk";
    public static final String ERROR_WRONG_VERSION_OLDER_PROPERTY = "error.wrong.version.older";
    public static final String ERROR_WRONG_VERSION_NEWER_PROPERTY = "error.wrong.version.newer";
    public static final String ERROR_UNKNOWN_PROPERTY = "error.unknown";
    
    public static final String DEFAULT_ERROR_NULL = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.null");
    public static final String DEFAULT_ERROR_NOT_VALID_PATH = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.not.valid.path");
    public static final String DEFAULT_ERROR_NOT_JAVAHOME = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.not.javahome");
    public static final String DEFAULT_ERROR_NOT_JDK = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.not.jdk");
    public static final String DEFAULT_ERROR_WRONG_VERSION_OLDER = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.wrong.version.older");
    public static final String DEFAULT_ERROR_WRONG_VERSION_NEWER = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.wrong.version.newer");
    public static final String DEFAULT_ERROR_UNKNOWN = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.error.unknown");
    
    public static final String DEFAULT_MINIMUM_JDK_VERSION = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.minimum.jdk.version");;
    public static final String DEFAULT_MAXIMUM_JDK_VERSION = ResourceUtils.getString(JdkLocationPanel.class, "JdkLocationPanel.default.maximum.jdk.version");;
}
