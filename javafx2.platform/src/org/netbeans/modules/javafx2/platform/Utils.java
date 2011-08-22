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
package org.netbeans.modules.javafx2.platform;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;

/**
 * Utility class for platform properties manipulation
 * 
 * @author Anton Chechel
 */
public final class Utils {
    private static final String PLATFORM_PREFIX = "platforms"; // NOI18N
    private static final String JAVAFX_SDK_PREFIX = "javafx.sdk.home"; // NOI18N
    private static final String JAVAFX_RUNTIME_PREFIX = "javafx.runtime.home"; // NOI18N
    private static final String JAVAFX_SOURCES_PREFIX = "javafx.src"; // NOI18N
    private static final String JAVAFX_JAVADOC_PREFIX = "javafx.javadoc"; // NOI18N

    private Utils() {
    }
    
    /**
     * Returns key for <b>JavaFX SDK location</b> IDE global property value for given java platform
     * 
     * @param IDE java platform instance
     * @return key for JavaFX SDK location
     */
    public static String getSDKPropertyKey(JavaPlatform platform) {
        String platformName = platform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
        return PLATFORM_PREFIX + '.' + platformName + '.' + JAVAFX_SDK_PREFIX; // NOI18N
    }
    
    /**
     * Returns key for <b>JavaFX Runtime location</b> IDE global property value for given java platform
     * 
     * @param IDE java platform instance
     * @return key for JavaFX Runtime location
     */
    public static String getRuntimePropertyKey(JavaPlatform platform) {
        String platformName = platform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
        return PLATFORM_PREFIX + '.' + platformName + '.' + JAVAFX_RUNTIME_PREFIX; // NOI18N
    }

    /**
     * Returns key for <b>JavaFX Runtime location</b> IDE global property value for given java platform
     * 
     * @param IDE java platform name
     * @return key for JavaFX Runtime location
     */
    public static String getRuntimePropertyKey(String platformName) {
        return PLATFORM_PREFIX + '.' + platformName + '.' + JAVAFX_RUNTIME_PREFIX; // NOI18N
    }

    /**
     * Returns key for <b>JavaFX Javadoc location</b> IDE global property value for given java platform
     * 
     * @param IDE java platform instance
     * @return key for JavaFX Javadoc location
     */
    public static String getJavadocPropertyKey(JavaPlatform platform) {
        String platformName = platform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
        return PLATFORM_PREFIX + '.' + platformName + '.' + JAVAFX_JAVADOC_PREFIX; // NOI18N
    }

    /**
     * Returns key for <b>JavaFX Sources location</b> IDE global property value for given java platform
     * 
     * @param IDE java platform instance
     * @return key for JavaFX Sources location
     */
    public static String getSourcesPropertyKey(JavaPlatform platform) {
        String platformName = platform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
        return PLATFORM_PREFIX + '.' + platformName + '.' + JAVAFX_SOURCES_PREFIX; // NOI18N
    }
}
