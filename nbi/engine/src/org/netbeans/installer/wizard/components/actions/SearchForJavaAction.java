/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.dependencies.InstallAfter;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.JavaUtils.JavaInfo;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
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
    // Static
    public static String getLabel(File javaHome) {
        JavaInfo javaInfo = JavaUtils.getInfo(javaHome);
        
        return getLabel(javaHome, javaInfo);
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static String getLabel(File javaHome, JavaInfo javaInfo) {
        if (javaInfo.isNonFinal()) {
            return StringUtils.format(JAVA_ENTRY_LABEL_NON_FINAL,
                    javaHome,
                    javaInfo.getVersion().toJdkStyle(),
                    javaInfo.getVendor());
        } else {
            return StringUtils.format(JAVA_ENTRY_LABEL,
                    javaHome,
                    javaInfo.getVersion().toJdkStyle(),
                    javaInfo.getVendor());
        }
    }
    
    private static String getLabel(File javaHome, Version version, String vendor) {
        return StringUtils.format(JAVA_ENTRY_LABEL,
                javaHome,
                version.toJdkStyle(),
                vendor);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public static List<File> javaLocations = new LinkedList<File>();
    public static List<String> javaLabels = new LinkedList<String>();
    
    public static File lastSelectedJava = null;
    
    public SearchForJavaAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
    }
    
    public void execute() {
        final Progress progress = new Progress();
        final List<File> locations = new LinkedList<File>();
        
        getWizardUi().setProgress(progress);
        
        progress.setTitle(SEARCH_INSTALLED_JAVAS);
        progress.setDetail(StringUtils.EMPTY_STRING);
        progress.setPercentage(Progress.START);
        
        SystemUtils.sleep(200);
        
        progress.setDetail(PREPARE_JAVA_LIST);
        if (SystemUtils.isWindows()) {
            fetchLocationsFromWindowsRegistry(locations);
        }
        fetchLocationsFromEnvironment(locations);
        fetchLocationsFromFilesystem(locations);
        fetchLocationsFromRegistry(locations);
        
        for (int i = 0; i < locations.size(); i++) {
            final File javaHome = locations.get(i).getAbsoluteFile();
            
            progress.setDetail(StringUtils.format(CHECKING, javaHome));
            
            if (isCanceled()) return; // check for cancel status
            
            LogManager.logIndent("investigating java home candidate: " + javaHome);
            
            // check whether it is a java installation - the result will be null if
            // it is not
            final JavaInfo javaInfo = JavaUtils.getInfo(javaHome);
            
            if (javaInfo != null) {
                LogManager.logUnindent(
                        "... parsed java: " + javaInfo.getVersion() + " " + // NOI18N
                        "by " + javaInfo.getVendor() + "; " + // NOI18N
                        "final=" + !javaInfo.isNonFinal()); // NOI18N
                        
                // filter out "private" jres
                if (javaHome.getName().equals("jre") &&
                        JavaUtils.isJdk(javaHome.getParentFile())) {
                    continue;
                }
                
                // add the location to the list if it's not already there
                if (!javaLocations.contains(javaHome)) {
                    javaLocations.add(javaHome);
                    javaLabels.add(getLabel(javaHome));
                }
            } else {
                LogManager.unindent();
            }
            
            progress.setPercentage(Progress.COMPLETE * i / locations.size());
            SystemUtils.sleep(50);
        }
        
        // finally we should scan the registry for jdks planned for installation, if
        // the current product is scheduled to be installed after 'jdk', i.e. has
        // an install-after dependency on 'jdk' uid
        final Product product = (Product) getWizard().
                getContext().
                get(Product.class);
        for (Dependency dependency: product.getDependencies(
                InstallAfter.class)) {
            if (dependency.getUid().equals(JDK_PRODUCT_UID)) {
                for (Product jdk: Registry.getInstance().getProducts(JDK_PRODUCT_UID)) {
                    if (jdk.getStatus() == Status.TO_BE_INSTALLED) {
                        javaLocations.add(jdk.getInstallationLocation());
                        javaLabels.add(getLabel(
                                jdk.getInstallationLocation(),
                                jdk.getVersion(),
                                SUN_MICROSYSTEMS_VENDOR));
                    }
                }
                
                break;
            }
        }
        
        // sort the found java installations:
        //   1) by version descending
        //   2) by path acending
        //   3) by vendor descending (so Sun comes first, hehe)
        for (int i = 0; i < javaLocations.size(); i++) {
            for (int j = javaLocations.size() - 1; j > i ; j--) {
                File file1 = javaLocations.get(j);
                File file2 = javaLocations.get(j - 1);
                
                String label1 = javaLabels.get(j);
                String label2 = javaLabels.get(j - 1);
                
                JavaInfo info1 = JavaUtils.getInfo(javaLocations.get(j));
                JavaInfo info2 = JavaUtils.getInfo(javaLocations.get(j - 1));
                
                if (info1.getVersion().equals(info2.getVersion())) {
                    if (file1.getPath().compareTo(file2.getPath()) == 0) {
                        if (info1.getVendor().compareTo(info2.getVendor()) == 0) {
                            continue;
                        } else if (info1.getVendor().compareTo(info2.getVendor()) < 0) {
                            javaLocations.set(j, file2);
                            javaLocations.set(j - 1, file1);
                            
                            javaLabels.set(j, label2);
                            javaLabels.set(j - 1, label1);
                        }
                    } else if (file1.getPath().length() < file2.getPath().length()) {
                        javaLocations.set(j, file2);
                        javaLocations.set(j - 1, file1);
                        
                        javaLabels.set(j, label2);
                        javaLabels.set(j - 1, label1);
                    }
                } else if (info1.getVersion().newerThan(info2.getVersion())) {
                    javaLocations.set(j, file2);
                    javaLocations.set(j - 1, file1);
                    
                    javaLabels.set(j, label2);
                    javaLabels.set(j - 1, label1);
                }
            }
        }
        
        //if (javaLocations.size() > 0) {
        //    lastSelectedJava = javaLocations.get(0);
        //}
        
        progress.setDetail("");
        progress.setPercentage(Progress.COMPLETE);
        
        SystemUtils.sleep(200);
    }
    
    @Override
    public boolean canExecuteForward() {
        return javaLocations.size() == 0;
    }
    
    private void fetchLocationsFromFilesystem(final List<File> locations) {
        final List<String> candidateLocations = new ArrayList<String>();
        
        for(String location : JAVA_FILESYSTEM_LOCATIONS_COMMON) {
            candidateLocations.add(location);
        }
        
        if (SystemUtils.isWindows()) {
            candidateLocations.addAll(
                    Arrays.asList(JAVA_FILESYSTEM_LOCATIONS_WINDOWS));
        } else if (SystemUtils.isMacOS()) {
            candidateLocations.addAll(
                    Arrays.asList(JAVA_FILESYSTEM_LOCATIONS_MACOSX));
        } else {
            candidateLocations.addAll(
                    Arrays.asList(JAVA_FILESYSTEM_LOCATIONS_UNIX));
        }
        
        for (String location: candidateLocations) {
            final File parent = SystemUtils.resolvePath(location);
            
            if (parent.exists() && parent.isDirectory()) {
                locations.add(parent);
                final File[] children = parent.listFiles(new FileFilter() {
                    public boolean accept(final File pathname) {
                        return pathname.isDirectory();
                    }
                });
                    
                if (children != null) {
                    for (File child: children) {
                        locations.add(child);
                    }
                } else {
                    LogManager.log(ErrorLevel.DEBUG,
                            "Can`t get children of existing directory : " + parent.getPath());
                }
            }
        }
    }
    
    private void fetchLocationsFromEnvironment(final List<File> locations) {
        LogManager.logIndent("checking for possible java locations in environment");
        
        for (String name: JAVA_ENVIRONMENT_VARIABLES) {
            final String value = System.getenv(name);
            
            if (value != null) {
                LogManager.log("found: " + name + " = " + value); // NOI18N
                
                final File file = new File(value).getAbsoluteFile();
                if (!locations.contains(file)) {
                    locations.add(file);
                }
            }
        }
        
        LogManager.logUnindent("... finished");
    }
    
    private void fetchLocationsFromWindowsRegistry(final List<File> locations) {
        LogManager.logIndent("checking for possible java locations in environment");
        
        final WindowsNativeUtils nativeUtils =
                ((WindowsNativeUtils) SystemUtils.getNativeUtils());
        final WindowsRegistry registry =
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
                                JavaUtils.JAVAHOME_VALUE)) {
                            continue;
                        }
                        
                        final String javaHome = registry.getStringValue(
                                section,
                                path + WindowsRegistry.SEPARATOR + key,
                                JavaUtils.JAVAHOME_VALUE,
                                false);
                        
                        LogManager.log("found: " + (section == HKLM ?
                            "HKEY_LOCAL_MACHINE" : "HKEY_CURRENT_USER") + // NOI18N
                                "\\" + path + "\\" + key + "\\" + // NOI18N
                                JavaUtils.JAVAHOME_VALUE + " = " + javaHome); // NOI18N
                        
                        
                        // add java home to the list if it's not there already
                        final File file = new File(javaHome);
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
    
    private void fetchLocationsFromRegistry(final List<File> locations) {
        for (Product jdk: Registry.getInstance().getProducts(JDK_PRODUCT_UID)) {
            if (jdk.getStatus() == Status.INSTALLED) {
                if (!locations.contains(jdk.getInstallationLocation())) {
                    locations.add(jdk.getInstallationLocation());
                }
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.description"); // NOI18N
    public static final String PREPARE_JAVA_LIST =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.prepare.list"); //NOI18N
    public static final String CHECKING =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.checking");//NOI18N
    public static final String SEARCH_INSTALLED_JAVAS =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.search.java");//NOI18N
    public static final String JAVA_ENTRY_LABEL =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.entry.label");//NOI18N
    public static final String JAVA_ENTRY_LABEL_NON_FINAL =
            ResourceUtils.getString(SearchForJavaAction.class,
            "SFJA.entry.label.non.final");//NOI18N
    
    private static final String SUN_MICROSYSTEMS_VENDOR =
            "Sun Microsystems Inc." ; //NOI18N
    
    private static final String JDK_PRODUCT_UID = "jdk"; //NOI18N
    
    public static final String [] JAVA_WINDOWS_REGISTRY_ENTRIES = new String [] {
        "SOFTWARE\\JavaSoft\\Java Development Kit", // NOI18N
        "SOFTWARE\\JRockit\\Java Development Kit", // NOI18N
        "SOFTWARE\\IBM\\Java Development Kit", // NOI18N
        "SOFTWARE\\IBM\\Java2 Development Kit", // NOI18N
        
        "SOFTWARE\\JavaSoft\\Java Runtime Environment", // NOI18N
        "SOFTWARE\\JRockit\\Java Runtime Environment", // NOI18N
        "SOFTWARE\\IBM\\Java Runtime Environment", // NOI18N
        "SOFTWARE\\IBM\\Java2 Runtime Environment" // NOI18N
    };
    
    public static final String[] JAVA_ENVIRONMENT_VARIABLES = new String[] {
        "JAVA_HOME", // NOI18N
        "JAVAHOME", // NOI18N
        "JAVA_PATH", // NOI18N
        "JDK_HOME", // NOI18N
        "JDKHOME", // NOI18N
        "ANT_JAVA", // NOI18N
        "JAVA", // NOI18N
        "JDK" // NOI18N
    };
    
    public static final String[] JAVA_FILESYSTEM_LOCATIONS_COMMON = new String[] {
        "$S{java.home}", // NOI18N
        "$S{java.home}/.." // NOI18N        
    };
    
    public static final String[] JAVA_FILESYSTEM_LOCATIONS_UNIX = new String[] {
        "$N{home}", // NOI18N
        "$N{home}/java", // NOI18N
        "$N{home}/jdk", // NOI18N
        "$N{home}/Java", // NOI18N
        
        "$N{install}", // NOI18N
        "$N{install}/Java", // NOI18N
        
        "/usr", // NOI18N
        "/usr/jdk", // NOI18N
        "/usr/jdk/instances", // NOI18N
        "/usr/java", // NOI18N
        "/usr/java/jdk", // NOI18N
        "/usr/j2se", // NOI18N
        "/usr/j2sdk", // NOI18N  
        
        
        "/usr/local", // NOI18N
        "/usr/local/jdk", // NOI18N
        "/usr/local/jdk/instances", // NOI18N
        "/usr/local/java", // NOI18N
        "/usr/local/j2se", // NOI18N
        "/usr/local/j2sdk", // NOI18N  
        
        "/export", // NOI18N
        "/export/jdk", // NOI18N
        "/export/jdk/instances", // NOI18N
        "/export/java", // NOI18N
        "/export/j2se",  // NOI18N
        "/export/j2sdk", // NOI18N
        
        "/opt", // NOI18N
        "/opt/jdk", // NOI18N
        "/opt/jdk/instances", // NOI18N
        "/opt/java", // NOI18N
        "/opt/j2se",  // NOI18N
        "/opt/j2sdk", // NOI18N
        
        "/usr/lib/jvm", // NOI18N        
    };
    
    public static final String[] JAVA_FILESYSTEM_LOCATIONS_MACOSX = new String[] {
        "/Library/Java", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.5", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.6", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.7", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.7.0" // NOI18N
    };
    
    public static final String[] JAVA_FILESYSTEM_LOCATIONS_WINDOWS = new String[] {
        "$N{install}", // NOI18N
        "$N{install}/Java" // NOI18N
    };
}
