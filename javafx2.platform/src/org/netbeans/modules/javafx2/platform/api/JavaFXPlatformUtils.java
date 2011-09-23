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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.javafx2.platform.PlatformPropertiesHandler;
import org.netbeans.modules.javafx2.platform.Utils;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 * API Utility class for JavaFX platform.
 *
 * @author Anton Chechel
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
        "C:\\Program Files\\Oracle",        // NOI18N
        "C:\\Program Files (x86)\\Oracle"   // NOI18N
    };

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
     * Constructs classpath for JavaFX project
     * 
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
     * Determines whether JavaFX SDK and JavaFX Runtime locations are correct
     * 
     * @param JavaFX SDK path
     * @param JavaFX Runtime path
     * @return are locations correct
     */
    public static boolean areJFXLocationsCorrect(@NonNull String sdkPath, @NonNull String runtimePath) {
        return isSdkPathCorrect(sdkPath) && isRuntimePathCorrect(runtimePath);
    }
    
    /**
     * Creates new default JavaFX platform
     * 
     * @return instance of created JavaFX Platform, or null if creation was
     * not successful: such platform already exists or IO exception has occurred
     */
    @CheckForNull
    public static JavaPlatform createDefaultJavaFXPlatform() {
        String sdkPath = null;
        String runtimePath = null;
        String javadocPath = null;
        String srcPath = null;

        for (String path : KNOWN_JFX_LOCATIONS) {
            if (sdkPath == null) {
                sdkPath = predictSDKLocation(path);
            }
            if (runtimePath == null) {
                runtimePath = predictRuntimeLocation(path);
            }
            if (javadocPath == null) {
                javadocPath = predictJavadocLocation(path);
            }
            if (srcPath == null) {
                srcPath = predictSourcesLocation(path);
            }

            // SDK and RT location is enought for JFX platform definition
            if (sdkPath != null && runtimePath != null) {
                break;
            }
        }

        if (sdkPath != null && runtimePath != null) {
            return Utils.createJavaFXPlatform(Utils.DEFAULT_FX_PLATFORM_NAME, sdkPath, runtimePath, javadocPath, srcPath);
        }

        return null;
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
            File[] children = location.listFiles();
            for (File child : children) {
                File toolsJar = new File(child.getAbsolutePath() + File.separatorChar + "tools" + File.separatorChar + "ant-javafx.jar"); // NOI18N
                if (toolsJar.exists()) {
                    return child.getAbsolutePath();
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
        File location = new File(path);
        if (location.exists()) {
            List<File> files = new ArrayList<File>();
            files.add(location); // check root location
            File[] children = location.listFiles();
            for (File child : children) {
                files.add(child); // check in neighbour folders: Win installation
                files.addAll(Arrays.asList(child.listFiles()));
            }
            for (File file : files) {
                File rtJar = new File(file.getAbsolutePath() + File.separatorChar + "lib" + File.separatorChar + "jfxrt.jar"); // NOI18N
                if (rtJar.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }

    /**
     * Tries to predict JavaFX SDK Javadoc location for given path
     * Can return null.
     * 
     * @param folder where to look up
     * @return JavaFX SDK Javadoc location absolute path, or null if not predicted
     */
    @CheckForNull
    public static String predictJavadocLocation(@NonNull String path) {
        File location = new File(path);
        if (location.exists()) {
            File[] children = location.listFiles();
            for (File child : children) {
                File docs = new File(child.getAbsolutePath() + File.separatorChar + "docs"); // NOI18N
                if (docs.exists()) {
                    return docs.getAbsolutePath();
                }
            }
        }
        return null;
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

    private static boolean isSdkPathCorrect(@NonNull String sdkPath) {
        if (sdkPath.isEmpty()) {
            return false;
        }
        File file = new File(sdkPath);
        if (!file.exists()) {
            return false;
        }
        File toolsJar = new File(file.getAbsolutePath() + File.separatorChar + "tools" + File.separatorChar + "ant-javafx.jar"); // NOI18N
        return toolsJar.exists();
    }
    
    private static boolean isRuntimePathCorrect(@NonNull String runtimePath) {
        if (runtimePath.isEmpty()) {
            return false;
        }
        File file = new File(runtimePath);
        if (!file.exists()) {
            return false;
        }
        File rtJar = new File(file.getAbsolutePath() + File.separatorChar + "lib" + File.separatorChar + "jfxrt.jar"); // NOI18N
        return rtJar.exists();
    }

}
