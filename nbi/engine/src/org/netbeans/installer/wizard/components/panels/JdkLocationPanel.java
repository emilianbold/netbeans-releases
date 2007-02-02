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
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.wizard.components.actions.SearchForJavaAction;

/**
 *
 * @author Kirill Sorokin
 */
public class JdkLocationPanel extends ApplicationLocationPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String JDK_LOCATION_PROPERTY = "jdk.location";
    
    public static final String MINIMUM_JDK_VERSION_PROPERTY = "minimum.jdk.version";
    public static final String MAXIMUM_JDK_VERSION_PROPERTY = "maximum.jdk.version";
    
    public static final String DEFAULT_LOCATION_LABEL_TEXT = ResourceUtils.getString(JdkLocationPanel.class, "JLP.location.label.text");
    public static final String DEFAULT_LOCATION_BUTTON_TEXT = ResourceUtils.getString(JdkLocationPanel.class, "JLP.location.button.text");
    public static final String DEFAULT_LIST_LABEL_TEXT = ResourceUtils.getString(JdkLocationPanel.class, "JLP.list.label.text");
    
    public static final String ERROR_NULL_PROPERTY = "error.null";
    public static final String ERROR_NOT_VALID_PATH_PROPERTY = "error.not.valid.path";
    public static final String ERROR_NOT_JAVAHOME_PROPERTY = "error.not.javahome";
    public static final String ERROR_NOT_JDK_PROPERTY = "error.not.jdk";
    public static final String ERROR_WRONG_VERSION_OLDER_PROPERTY = "error.wrong.version.older";
    public static final String ERROR_WRONG_VERSION_NEWER_PROPERTY = "error.wrong.version.newer";
    public static final String ERROR_UNKNOWN_PROPERTY = "error.unknown";
    
    public static final String DEFAULT_ERROR_NULL = ResourceUtils.getString(JdkLocationPanel.class, "JLP.error.null");
    public static final String DEFAULT_ERROR_NOT_VALID_PATH = ResourceUtils.getString(JdkLocationPanel.class, "JLP.error.not.valid.path");
    public static final String DEFAULT_ERROR_NOT_JAVAHOME = ResourceUtils.getString(JdkLocationPanel.class, "JLP.error.not.javahome");
    public static final String DEFAULT_ERROR_NOT_JDK = ResourceUtils.getString(JdkLocationPanel.class, "JLP.error.not.jdk");
    public static final String DEFAULT_ERROR_WRONG_VERSION_OLDER = ResourceUtils.getString(JdkLocationPanel.class, "JLP.error.wrong.version.older");
    public static final String DEFAULT_ERROR_WRONG_VERSION_NEWER = ResourceUtils.getString(JdkLocationPanel.class, "JLP.error.wrong.version.newer");
    public static final String DEFAULT_ERROR_UNKNOWN = ResourceUtils.getString(JdkLocationPanel.class, "JLP.error.unknown");
    
    public static final String DEFAULT_MINIMUM_JDK_VERSION = ResourceUtils.getString(JdkLocationPanel.class, "JLP.minimum.jdk.version");;
    public static final String DEFAULT_MAXIMUM_JDK_VERSION = ResourceUtils.getString(JdkLocationPanel.class, "JLP.maximum.jdk.version");;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Version minimumVersion;
    private Version maximumVersion;
    
    private List<File>   jdkLocations;
    private List<String> jdkLabels;
    
    public JdkLocationPanel() {
        setProperty(MINIMUM_JDK_VERSION_PROPERTY, DEFAULT_MINIMUM_JDK_VERSION);
        setProperty(MAXIMUM_JDK_VERSION_PROPERTY, DEFAULT_MAXIMUM_JDK_VERSION);
        
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
        minimumVersion = Version.getVersion(getProperty(MINIMUM_JDK_VERSION_PROPERTY));
        maximumVersion = Version.getVersion(getProperty(MAXIMUM_JDK_VERSION_PROPERTY));
        
        jdkLocations = new LinkedList<File>();
        jdkLabels    = new LinkedList<String>();
        
        for (int i = 0; i < SearchForJavaAction.javaLocations.size(); i++) {
            File    location = SearchForJavaAction.javaLocations.get(i);
            String  label    = SearchForJavaAction.javaLabels.get(i);
            Version version  = null;
            
            // initialize the version; if the location exists, it must be an
            // already installed jdk and we should fetch the version in a
            // "traditional" way; otherwise the jdk is only planned for
            // installation and we should try to get its version from the
            // registry
            if (location.exists()) {
                version = JavaUtils.getVersion(location);
            } else {
                for (Product jdk: Registry.getInstance().getProducts("jdk")) {
                    if ((jdk.getStatus() == Status.TO_BE_INSTALLED) &&
                            jdk.getInstallationLocation().equals(location)) {
                        version = jdk.getVersion();
                    }
                }
            }
            
            // if we could not fetch the version, we should skip this jdk
            // installation
            if (version == null) {
                continue;
            }
            
            // if the location exists and is a jdk installation (or if the
            // location does not exist - in this case we're positive that it
            // WILL be a jdk) and if version satisfies the requirements - add
            // the location to the list
            if ((!location.exists() || JavaUtils.isJdk(location)) &&
                    !version.olderThan(minimumVersion) &&
                    !version.newerThan(maximumVersion)) {
                jdkLocations.add(location);
                jdkLabels.add(label);
            }
        }
    }
    
    public List<File> getLocations() {
        return jdkLocations;
    }
    
    public List<String> getLabels() {
        return jdkLabels;
    }
    
    public File getSelectedLocation() {
        final String path =
                getWizard().getProperty(JDK_LOCATION_PROPERTY);
        
        // the first obvious choice is the jdk that has already been selected for
        // this product; if it has not yet been set - check whether we can reuse the
        // location which was selected on another jdk location panel; if it does
        // not fit the requirements fallback to the first item in the list or
        // an empty path
        if (path != null) {
            return new File(path);
        } else {
            if ((SearchForJavaAction.lastSelectedJava != null) &&
                    jdkLocations.contains(SearchForJavaAction.lastSelectedJava)) {
                return SearchForJavaAction.lastSelectedJava;
            } else {
                if (jdkLocations.size() != 0) {
                    return jdkLocations.get(0);
                } else {
                    return new File("");
                }
            }
        }
    }
    
    public String validateLocation(String path) {
        final File file = new File(path);
        
        if (path.equals("")) {
            return StringUtils.format(getProperty(ERROR_NULL_PROPERTY));
        }
        
        if (!SystemUtils.isPathValid(path)) {
            return StringUtils.format(getProperty(ERROR_NOT_VALID_PATH_PROPERTY), path);
        }
        
        Version version = null;
        if (file.exists()) {
            if (!JavaUtils.isJavaHome(file)) {
                return StringUtils.format(getProperty(ERROR_NOT_JAVAHOME_PROPERTY), path);
            }
            
            if (!JavaUtils.isJdk(file)) {
                return StringUtils.format(getProperty(ERROR_NOT_JDK_PROPERTY), path);
            }
            
            version = JavaUtils.getVersion(file);
        } else {
            for (Product jdk: Registry.getInstance().getProducts("jdk")) {
                if ((jdk.getStatus() == Status.TO_BE_INSTALLED) &&
                        jdk.getInstallationLocation().equals(file)) {
                    version = jdk.getVersion();
                }
            }
        }
        
        if (version != null) {
            if (version.olderThan(minimumVersion)) {
                return StringUtils.format(getProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY), path, version, minimumVersion);
            }
            
            if (version.newerThan(maximumVersion)) {
                return StringUtils.format(getProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY), path, version, maximumVersion);
            }
        } else {
            return StringUtils.format(getProperty(ERROR_UNKNOWN_PROPERTY), path);
        }
        
        return null;
    }
    
    public void setLocation(File location) {
        SearchForJavaAction.lastSelectedJava = location;
        
        if (!SearchForJavaAction.javaLocations.contains(location)) {
            SearchForJavaAction.javaLocations.add(location);
            SearchForJavaAction.javaLabels.add(
                    SearchForJavaAction.getLabel(location));
        }
        
        getWizard().setProperty(JDK_LOCATION_PROPERTY, location.getAbsolutePath());
    }
}
