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
package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JDKUtils;
import org.netbeans.installer.utils.applications.JDKUtils.JavaInfo;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKLM;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKCU;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Kirill Sorokin
 */
public class SearchForJavaAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.description"); // NOI18N
    
    public static final String [] JAVA_WINDOWS_REGISTRY_ENTRIES = new String [] {
        "SOFTWARE\\JavaSoft\\Java Development Kit",                         // NOI18N
        "SOFTWARE\\JRockit\\Java Development Kit",                          // NOI18N
        "SOFTWARE\\IBM\\Java Development Kit",                              // NOI18N
        "SOFTWARE\\IBM\\Java2 Development Kit",                             // NOI18N
        
        "SOFTWARE\\JavaSoft\\Java Runtime Environment",                     // NOI18N
        "SOFTWARE\\JRockit\\Java Runtime Environment",                      // NOI18N
        "SOFTWARE\\IBM\\Java Runtime Environment",                          // NOI18N
        "SOFTWARE\\IBM\\Java2 Runtime Environment"                          // NOI18N
    };
    
    public static final String[] JAVA_ENVIRONMENT_VARIABLES = new String[] {
        "JAVA_HOME",                                                        // NOI18N
        "JAVAHOME",                                                         // NOI18N
        "JAVA_PATH",                                                        // NOI18N
        "JDK_HOME",                                                         // NOI18N
        "JDKHOME",                                                          // NOI18N
        "ANT_JAVA",                                                         // NOI18N
        "JAVA",                                                             // NOI18N
        "JDK"                                                               // NOI18N
    };
    
    public static final String[] JAVA_FILESYSTEM_LOCATIONS = new String[] {
        "$S{java.home}", // NOI18N
        "$S{java.home}/..", // NOI18N
        
        "$N{install}", // NOI18N
        "$N{install}/Java", // NOI18N
        
        "$N{home}", // NOI18N
        "$N{home}/Java", // NOI18N
        
        "/usr", // NOI18N
        "/usr/jdk", // NOI18N
        "/usr/jdk/instances", // NOI18N
        "/usr/java", // NOI18N
        
        "/usr/local", // NOI18N
        "/usr/local/jdk", // NOI18N
        "/usr/local/jdk/instances", // NOI18N
        "/usr/local/java", // NOI18N
        
        "/export", // NOI18N
        "/export/jdk", // NOI18N
        "/export/jdk/instances", // NOI18N
        "/export/java", // NOI18N
        
        "/opt", // NOI18N
        "/opt/jdk", // NOI18N
        "/opt/jdk/instances", // NOI18N
        "/opt/java", // NOI18N
        
        "/Library/Java", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.5", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.6", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.7", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.7.0" // NOI18N
    };
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public static List<File>   javaLocations = new LinkedList<File>();
    public static List<String> javaLabels    = new LinkedList<String>();
    
    public SearchForJavaAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
    }
    
    public void execute() {
        final Progress progress = new Progress();
        
        getWizardUi().setProgress(progress);
        
        progress.setTitle("Searching for installed JDKs...");
        progress.setDetail("");
        progress.setPercentage(Progress.START);
        
        SystemUtils.sleep(200);
        
        progress.setDetail("Preparing locations list");
        
        List<File> locations = new LinkedList<File>();
        
        if (SystemUtils.isWindows()) {
            fetchLocationsFromWindowsRegistry(locations);
        }
        
        fetchLocationsFromEnvironment(locations);
        
        fetchLocationsFromFilesystem(locations);
        
        for (int i = 0; i < locations.size(); i++) {
            final File javaHome = locations.get(i).getAbsoluteFile();
            
            progress.setDetail("Checking " + javaHome);
            
            if (canceled) return; // check for cancel status
            
            // check whether it is a java installation - the result will be null if
            // it is not
            final JavaInfo javaInfo = JDKUtils.getInfo(javaHome);
            
            // filter out "private" jres
            if (javaHome.getName().equals("jre") &&
                    JDKUtils.isJdk(javaHome.getParentFile())) {
                continue;
            }
            
            // add the location to the list if it's not already there
            if ((javaInfo != null) && !javaLocations.contains(javaHome)) {
                javaLocations.add(javaHome);
                javaLabels.add(
                        "" +
                        javaHome +
                        " (v. " +
                        javaInfo.getVersion().toJdkStyle() +
                        " by " +
                        javaInfo.getVendor() +
                        ")");
            }
            
            progress.setPercentage(Progress.COMPLETE * i / locations.size());
            SystemUtils.sleep(50);
        }
        
        progress.setDetail("");
        progress.setPercentage(Progress.COMPLETE);
        
        SystemUtils.sleep(200);
    }
    
    public boolean canExecuteForward() {
        return javaLocations.size() == 0;
    }
    
    private void fetchLocationsFromFilesystem(List<File> locations) {
        for (String location: JAVA_FILESYSTEM_LOCATIONS) {
            File parent = SystemUtils.parsePath(location);
            
            if (parent.exists() && parent.isDirectory()) {
                locations.add(parent);
                
                for (File child: parent.listFiles()) {
                    if (child.isDirectory()) {
                        locations.add(child);
                    }
                }
            }
        }
    }
    
    private void fetchLocationsFromEnvironment(List<File> locations) {
        LogManager.logIndent("checking for possible java locations in environment");
        
        for (String name: JAVA_ENVIRONMENT_VARIABLES) {
            String value = System.getenv(name);
            
            if (value != null) {
                LogManager.log("found: " + name + " = " + value); // NOI18N
                
                File file = new File(value).getAbsoluteFile();
                if (!locations.contains(file)) {
                    locations.add(file);
                }
            }
        }
        
        LogManager.logUnindent("... finished");
    }
    
    private void fetchLocationsFromWindowsRegistry(List<File> locations) {
        LogManager.logIndent("checking for possible java locations in environment");
        
        WindowsNativeUtils nativeUtils =
                ((WindowsNativeUtils) SystemUtils.getNativeUtils());
        WindowsRegistry registry =
                nativeUtils.getWindowsRegistry();
        
        try {
            for (int section : new int[]{HKLM, HKCU}) {
                for (String path: JAVA_WINDOWS_REGISTRY_ENTRIES) {
                    // check whether current path exists in this section
                    if (!registry.keyExists(section, path)) {
                        continue;
                    }
                    
                    // get the names of all installed jdks
                    String[] keys = registry.getSubKeyNames(section, path);
                    
                    // iterate over the list of jdks, checking their versions
                    // and taking actions appropriate to the current search
                    // mode
                    for (int i = 0; i < keys.length; i++) {
                        // get the name of the current examined jdk
                        String key = keys[i];
                        
                        // get the java home of the current jdk, if it exists
                        if (!registry.valueExists(
                                section,
                                path + WindowsRegistry.SEPARATOR + key,
                                JDKUtils.JAVAHOME_VALUE)) {
                            continue;
                        }
                        
                        String javaHome = registry.getStringValue(
                                section,
                                path + WindowsRegistry.SEPARATOR + key,
                                JDKUtils.JAVAHOME_VALUE,
                                false);
                        
                        LogManager.log("found: " + (section == HKLM ?
                            "HKEY_LOCAL_MACHINE" : "HKEY_CURRENT_USER") + // NOI18N
                            "\\" + path + "\\" + key + "\\" + // NOI18N
                            JDKUtils.JAVAHOME_VALUE + " = " + javaHome); // NOI18N
                        
                        
                        // add java home to the list if it's not there already
                        File file = new File(javaHome);
                        if (file.exists() &&
                                file.isDirectory() &&
                                !locations.contains(file)) {
                            locations.add(file);
                        }
                    }
                }
            }
        } catch (NativeException e) {
            ErrorManager.notify(ErrorLevel.DEBUG, "Failed to search in the windows registry", e);
        }
        
        LogManager.logUnindent("... finished");
    }
}
