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

    private JavaFXPlatformUtils() {
    }

    /**
     * Determines whether given Java Platform supports JavaFX
     * 
     * @param IDE java platform instance
     * @return is JavaFX supported
     */
    public static boolean isJavaFXEnabled(final JavaPlatform platform) {
        EditableProperties properties = PlatformPropertiesHandler.getGlobalProperties();
        String sdkPath = properties.get(Utils.getSDKPropertyKey(platform));
        String runtimePath = properties.get(Utils.getRuntimePropertyKey(platform));
        return sdkPath != null && runtimePath != null;
    }

    /**
     * Returns path to JavaFX Runtime installation
     * 
     * @param IDE java platform name
     * @return JavaFX Runtime location
     */
    public static String getJavaFXRuntimePath(String platformName) {
        return PlatformPropertiesHandler.getGlobalProperties().get(Utils.getRuntimePropertyKey(platformName));
    }
    
    /**
     * Returns path to JavaFX SDK installation
     * 
     * @param IDE java platform name
     * @return JavaFX SDK location
     */
    public static String getJavaFXSDKPath(String platformName) {
        return PlatformPropertiesHandler.getGlobalProperties().get(Utils.getSDKPropertyKey(platformName));
    }
    
    /**
     * Constructs classpath for JavaFX project
     * 
     * @return classpath entries
     */
    public static String[] getJavaFXClassPath() {
        return new String[] {
                    "${" + PROPERTY_JAVAFX_RUNTIME + "}/jfxrt.jar:", // NOI18N
                    "${" + PROPERTY_JAVAFX_RUNTIME + "}/deploy.jar:", // NOI18N
                    "${" + PROPERTY_JAVAFX_RUNTIME + "}/javaws.jar:", // NOI18N
                    "${" + PROPERTY_JAVAFX_RUNTIME + "}/plugin.jar" // NOI18N
        };
    }
    
    /**
     * Tries to predict JavaFX Runtime location for JavaFX SDK installation
     * Can return null.
     * 
     * @param JavaFX SDK installation folder
     * @return JavaFX Runtime location absolute path or null
     */
    public static String guessRuntimePath(File sdkPath) {
        File parent = sdkPath.getParentFile();
        File[] brothers = parent.listFiles();
        for (File brother : brothers) {
            if (brother.getName().contains("Runtime") || brother.getName().contains("runtime")) { // NOI18N
                return brother.getAbsolutePath();
            }
        }
        return null;
    }
    
    /**
     * Tries to predict Javadoc location for JavaFX SDK installation
     * 
     * @param JavaFX SDK installation folder
     * @return Javadoc location absolute path
     */
    public static String guessJavadocPath(File sdkPath) {
        return sdkPath.getAbsolutePath() + "\\docs"; // NOI18N
    }
}
