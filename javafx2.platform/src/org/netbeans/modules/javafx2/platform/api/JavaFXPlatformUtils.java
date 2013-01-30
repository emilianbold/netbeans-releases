/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.platform.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.javafx2.platform.PlatformPropertiesHandler;
import org.netbeans.modules.javafx2.platform.Utils;
import org.netbeans.modules.javafx2.platform.registration.PlatformAutoInstaller;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Parameters;

/**
 * API Utility class for JavaFX platform.
 *
 * @author Anton Chechel
 * @author Petr Somol
 */
public final class JavaFXPlatformUtils {

    /**
     * Key for ant platform name
     * @see {@link J2SEPlatformImpl}
     */
    public static final String PLATFORM_ANT_NAME = "platform.ant.name"; // NOI18N
    
    /**
     * Property name for JavaFX Runtime
     */
    public static final String PROPERTY_JAVAFX_RUNTIME = "javafx.runtime"; // NOI18N

    /**
     * Property name for JavaFX SDK
     */
    public static final String PROPERTY_JAVAFX_SDK = "javafx.sdk"; // NOI18N

    /**
     * Property name for JavaFX support
     */
    public static final String PROPERTY_JAVA_FX = "javafx"; // NOI18N

    // TODO any Mac OS predefined locations?
    public static final String[] KNOWN_JFX_LOCATIONS = new String[]{
        (System.getenv("ProgramFiles") != null ? // NOI18N
            System.getenv("ProgramFiles") : "C:\\Program Files") + "\\Oracle", // NOI18N
        (System.getenv("ProgramFiles(x86)") != null ? // NOI18N
            System.getenv("ProgramFiles(x86)") : "C:\\Program Files (x86)") + "\\Oracle", // NOI18N
        File.separatorChar + "Library" + File.separatorChar + "Java" + File.separatorChar + "JavaVirtualMachines" // NOI18N
    };
    
    /**
     * On Mac JDK subdirs are located deeper by MAC_JDK_SUBDIR relative to JDK root
     */
    private static final String MAC_JDK_SUBDIR = File.separatorChar + "Contents" + File.separatorChar + "Home"; // NOI18N

    /**
     * Default on-line location of FX2 JavaDoc
     */
    private static final String JAVADOC_ONLINE_URL = "http://docs.oracle.com/javafx/2/api/"; // NOI18N
    
    private JavaFXPlatformUtils() {
    }

    /**
     * Determines whether given Java Platform supports JavaFX
     * 
     * @param IDE java platform instance
     * @return is JavaFX supported
     */
    public static boolean isJavaFXEnabled(@NullAllowed final JavaPlatform platform) {
        if (platform == null) {
            return false;
        }
        EditableProperties properties = PlatformPropertiesHandler.getGlobalProperties();
        String sdkPath = properties.get(Utils.getSDKPropertyKey(platform));
        String runtimePath = properties.get(Utils.getRuntimePropertyKey(platform));
        return sdkPath != null && runtimePath != null;
    }

    /**
     * Returns path to JavaFX Runtime installation
     * 
     * @param IDE java platform name
     * @return JavaFX Runtime location, or null if not recognized
     */
    @CheckForNull
    public static String getJavaFXRuntimePath(@NonNull String platformName) {
        return PlatformPropertiesHandler.getGlobalProperties().get(Utils.getRuntimePropertyKey(platformName));
    }

    /**
     * Returns a reference to JavaFX Runtime Folder
     * @param platformName the name of the platform for which the reference should be created
     * @return the reference to JavaFX Runtime Folder for given platform
     * @since 1.5
     */
    @NonNull
    public static String getJavaFXRuntimePathReference(@NonNull String platformName) {
        Parameters.notNull("platformName", platformName);   //NOI18N
        return String.format("${platforms.%s.javafx.runtime.home}", platformName);  //NOI18N
    }

    /**
     * Returns path to JavaFX SDK installation
     * 
     * @param IDE java platform name
     * @return JavaFX SDK location, or null if not recognized
     */
    @CheckForNull
    public static String getJavaFXSDKPath(@NonNull String platformName) {
        return PlatformPropertiesHandler.getGlobalProperties().get(Utils.getSDKPropertyKey(platformName));
    }
    
    /**
     * Returns a reference to JavaFX SDK Folder
     * @param platformName the name of the platform for which the reference should be created
     * @return the reference to JavaFX SDK Folder for given platform
     * @since 1.5
     */
    @NonNull
    public static String getJavaFXSDKPathReference(@NonNull String platformName) {
        Parameters.notNull("platformName", platformName);   //NOI18N
        return String.format("${platforms.%s.javafx.sdk.home}", platformName);  //NOI18N
    }
    /**
     * Constructs classpath for JavaFX project
     * xxx: Is this really an "API"?
     * xxx:Is hard coding of jars really what you want?
     * @return classpath entries
     */
    @NonNull
    public static String[] getJavaFXClassPath() {
        return new String[] {
                    "${" + PROPERTY_JAVAFX_RUNTIME + "}/lib/jfxrt.jar:", // NOI18N
                    "${" + PROPERTY_JAVAFX_RUNTIME + "}/lib/deploy.jar:", // NOI18N
                    "${" + PROPERTY_JAVAFX_RUNTIME + "}/lib/javaws.jar:", // NOI18N
                    "${" + PROPERTY_JAVAFX_RUNTIME + "}/lib/plugin.jar" // NOI18N
        };
    }
    
    /**
     * Determines whether any JavaFX enabled platform exist
     * 
     * @return is there any JavaFX platform
     */
    public static boolean isThereAnyJavaFXPlatform() {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (JavaPlatform javaPlatform : platforms) {
            if (JavaFXPlatformUtils.isJavaFXEnabled(javaPlatform)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Creates new default JavaFX platform
     * 
     * @return instance of created JavaFX Platform, or null if creation was
     * not successful: such platform already exists or IO exception has occurred
     * @throws IOException if the platform was invalid or its definition could not be stored
     * @throws IllegalArgumentException if a default JavaFX Platform already exists
     */
    @CheckForNull
    public static JavaPlatform createDefaultJavaFXPlatform() throws IOException, IllegalArgumentException {
        String sdkPath = null;
        String runtimePath = null;
        String javadocPath = null;
        String srcPath = null;

        // try to create using registry entries (defined in NB-JDK co-bundle)
        JavaPlatform registered = PlatformAutoInstaller.createRegisteredJavaFXPlatform();
        if(registered != null) {
            return registered;
        }        
        Set<String> locations = getLocations(JavaPlatformManager.getDefault().getDefaultPlatform());
        for (String path : locations.toArray(new String[0])) {
            if (sdkPath == null) {
                sdkPath = predictSDKLocation(path);
            }
            if (runtimePath == null) {
                runtimePath = predictRuntimeLocation(path);
            }
            // SDK and RT location is enought for JFX platform definition
            if (sdkPath != null && runtimePath != null) {
                if (javadocPath == null) {
                    javadocPath = predictJavadocLocation(sdkPath);
                }
                if (srcPath == null) {
                    srcPath = predictSourcesLocation(sdkPath);
                }
                break;
            }
        }
        if (sdkPath != null && runtimePath != null) {
            return Utils.createJavaFXPlatform(Utils.DEFAULT_FX_PLATFORM_NAME, sdkPath, runtimePath, javadocPath, srcPath);
        }
        return null;
    }

    /**
     * Returns a set of locations where FX can potentially be found
     * 
     * @param platform JavaPlatform in relation to which FX location predictions are made
     * @return set of locations
     */
    @CheckForNull
    public static Set<String> getLocations(JavaPlatform platform) {
        Set<String> locations = new LinkedHashSet<String>();
        Collection<FileObject> roots = platform.getInstallFolders();
        for(FileObject root : roots) {
            assert root != null && root.isFolder();
            locations.add(root.getPath());
        }        
        Set<String> parentLocations = new LinkedHashSet<String>();
        for(FileObject root : roots) {
            FileObject parent = root.getParent();
            if(parent != null) {
                parentLocations.add(parent.getPath());
                // on Mac compensate for the two additional subdirs (cf. MAC_JDK_SUBDIR)
                parent = parent.getParent();
                if(parent != null) {
                    parent = parent.getParent();
                    if(parent != null) {
                        parentLocations.add(parent.getPath());
                    }
                }
            }
        }
        SpecificationVersion ver = platform.getSpecification().getVersion();
        if(ver.equals(new SpecificationVersion("1.6"))) {
            locations.addAll(Arrays.asList(KNOWN_JFX_LOCATIONS));
            locations.addAll(parentLocations);
        } else {
            locations.addAll(parentLocations);
            locations.addAll(Arrays.asList(KNOWN_JFX_LOCATIONS));
        }
        return locations;
    }
    
    /**
     * Tries to predict JavaFX SDK location for given path
     * Can return null.
     * 
     * @param folder where to look up
     * @return JavaFX SDK location absolute path, or null if not predicted
     */
    @CheckForNull
    public static String predictSDKLocation(@NonNull String path) {
        File location = new File(path);
        if (location.exists()) {
            List<File> locations = new ArrayList<File>();
            locations.add(location); // check root location
            File[] children = location.listFiles();
            if (children != null) {
                locations.addAll(Arrays.asList(children));
            }
            for (File file : locations) {
                if(file.isDirectory()) {
                    if(isSdkPathCorrect(file)) {
                        return file.getAbsolutePath();
                    }
                    File macSubDir = new File(file.getAbsolutePath() + MAC_JDK_SUBDIR); // NOI18N
                    if (isSdkPathCorrect(macSubDir)) {
                        return macSubDir.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Tries to predict JavaFX Runtime location for given path
     * Can return null.
     * 
     * @param folder where to look up
     * @return JavaFX Runtime location absolute path, or null if not predicted
     */
    @CheckForNull
    public static String predictRuntimeLocation(@NonNull String path) {
        return predictRuntimeLocation(path, true);
    }

    /**
     * Tries to predict JavaFX Runtime location for given path
     * Can return null.
     * 
     * @param folder where to look up
     * @return JavaFX Runtime location absolute path, or null if not predicted
     */
    @CheckForNull
    public static String predictRuntimeLocation(@NonNull String path, boolean allowIncomplete) {
        File location = new File(path);
        if (location.exists()) {
            List<File> locations = new ArrayList<File>();
            locations.add(location); // check root location
            if(path.endsWith("SDK")) { //NOI18N
                locations.add(new File(path.replace("SDK", "Runtime"))); // NOI18N
            }
            File[] children = location.listFiles();
            if (children == null) {
                return null;
            }
            for(File child : Arrays.asList(children)) {
                locations.add(child);
                File[] f = child.listFiles(); // jre subdir
                if (f != null) {
                    locations.addAll(Arrays.asList(f));
                }
                File macSubDir = new File(child.getAbsolutePath() + MAC_JDK_SUBDIR); // NOI18N
                if(macSubDir.exists()) {
                    locations.add(macSubDir);
                    f = macSubDir.listFiles();
                    if (f != null) {
                        locations.addAll(Arrays.asList(f));
                    }
                }
            }
            for (File file : locations) {
                if(file.isDirectory() && isRuntimePathCorrectAndComplete(file)) {
                    return file.getAbsolutePath();
                }
            }
            if(allowIncomplete) {
                for (File file : locations) {
                    if(file.isDirectory() && isRuntimePathCorrect(file)) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Tries to predict JavaFX SDK Javadoc location for given path
     * If local JavaDoc is not found, a fallback online URL is returned.
     * 
     * @param folder where to look up
     * @return JavaFX SDK Javadoc location absolute path, or fallback online URL if not predicted
     */
    @CheckForNull
    public static String predictJavadocLocation(@NonNull String path) {
        File location = new File(path);
        if (location.exists()) {
            List<File> locations = new ArrayList<File>();
            locations.add(location); // check root location
            File[] children = location.listFiles();
            if (children == null) {
                return null;
            }
            locations.addAll(Arrays.asList(children));
            for (File file : locations) {
                if(file.isDirectory()) {
                    File docs = new File(file.getAbsolutePath() + File.separatorChar + "docs" + File.separatorChar + "api"); // NOI18N
                    if (docs.exists()) {
                        return docs.getAbsolutePath();
                    }
                    docs = new File(file.getAbsolutePath() + MAC_JDK_SUBDIR + File.separatorChar + "docs" + File.separatorChar + "api"); // NOI18N
                    if (docs.exists()) {
                        return docs.getAbsolutePath();
                    }
                }
            }
        }
        return JAVADOC_ONLINE_URL;
    }

    /**
     * Tries to predict JavaFX SDK Sources location for given path
     * Can return null.
     * 
     * @param folder where to look up
     * @return JavaFX SDK Sources location absolute path, or null if not predicted
     */
    // TODO when sources will be availabe
    @CheckForNull
    public static String predictSourcesLocation(@NonNull String path) {
        return null;
    }

    /**
     * Determines whether JavaFX SDK and JavaFX Runtime locations are valid
     * 
     * @param JavaFX SDK path
     * @param JavaFX Runtime path
     * @return are locations correct
     */
    public static boolean areJFXLocationsCorrect(@NonNull String sdkPath, @NonNull String runtimePath) {
        return isSdkPathCorrect(sdkPath) && isRuntimePathCorrect(runtimePath);
    }
    
    /**
     * Determines whether JavaFX SDK location is valid
     * 
     * @param JavaFX SDK path
     * @return true if location is correct
     */
    public static boolean isSdkPathCorrect(@NonNull String sdkPath) {
        if (sdkPath.isEmpty()) {
            return false;
        }
        File file = new File(sdkPath);
        if (!file.exists()) {
            return false;
        }
        return isSdkPathCorrect(file);
    }
    
    /**
     * Determines whether JavaFX SDK location is valid
     * 
     * @param JavaFX SDK path
     * @return true if location is correct
     */
    public static boolean isSdkPathCorrect(@NonNull File file) {
        File toolsJar = new File(file.getAbsolutePath() + File.separatorChar + "lib" + File.separatorChar + "ant-javafx.jar"); // NOI18N
        if(!toolsJar.exists()) {
            toolsJar = new File(file.getAbsolutePath() + File.separatorChar + "tools" + File.separatorChar + "ant-javafx.jar"); // NOI18N
        }
        return toolsJar.exists();
    }

    /**
     * Determines whether JavaFX RT location is valid
     * 
     * @param JavaFX RT path
     * @return true if location is correct
     */
    public static boolean isRuntimePathCorrect(@NonNull String runtimePath) {
        if (runtimePath.isEmpty()) {
            return false;
        }
        File file = new File(runtimePath);
        if (!file.exists()) {
            return false;
        }
        return isRuntimePathCorrect(file);
    }

    /**
     * Determines whether JavaFX RT location is valid and containing all required artifacts
     * 
     * @param JavaFX RT path
     * @return true if location is correct and containing all required artifacts
     */
    public static boolean isRuntimePathCorrectAndComplete(@NonNull String runtimePath) {
        if (runtimePath.isEmpty()) {
            return false;
        }
        File file = new File(runtimePath);
        if (!file.exists()) {
            return false;
        }
        return isRuntimePathCorrectAndComplete(file);
    }

    /**
     * Determines whether JavaFX RT location is valid
     * 
     * @param JavaFX RT path
     * @return true if location is correct
     */
    public static boolean isRuntimePathCorrect(@NonNull File file) {
        File rtJar = new File(file.getAbsolutePath() + File.separatorChar + "lib" + File.separatorChar + "jfxrt.jar"); // NOI18N
        return rtJar.exists();
    }

    /**
     * Determines whether JavaFX RT location is valid and contains all required artifacts
     * 
     * @param JavaFX RT path
     * @return true if location is correct and containing all required artifacts
     */
    public static boolean isRuntimePathCorrectAndComplete(@NonNull File file) {
        File rtJar1 = new File(file.getAbsolutePath() + File.separatorChar + "lib" + File.separatorChar + "jfxrt.jar"); // NOI18N
        File rtJar2 = new File(file.getAbsolutePath() + File.separatorChar + "lib" + File.separatorChar + "deploy.jar"); // NOI18N
        File rtJar3 = new File(file.getAbsolutePath() + File.separatorChar + "lib" + File.separatorChar + "javaws.jar"); // NOI18N
        File rtJar4 = new File(file.getAbsolutePath() + File.separatorChar + "lib" + File.separatorChar + "plugin.jar"); // NOI18N
        return rtJar1.exists() && rtJar2.exists() && rtJar3.exists() && rtJar4.exists();
    }

}
