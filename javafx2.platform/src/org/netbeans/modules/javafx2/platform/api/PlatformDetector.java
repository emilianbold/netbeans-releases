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
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.javafx2.platform.Utils;

/**
 *
 * @author Anton Chechel
 */
public class PlatformDetector {

    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N
    private static final String[] KNOWN_LOCATIONS = new String[]{
        "C:\\Program Files\\Oracle",        // NOI18N
        "C:\\Program Files (x86)\\Oracle",  // NOI18N
        "<mac path>"                        // NOI18N
    };
    private static volatile PlatformDetector instance;

    public static synchronized PlatformDetector getInstance() {
        if (instance == null) {
            instance = new PlatformDetector();
        }
        return instance;
    }

    private PlatformDetector() {
    }

    public JavaPlatform detectJavaFXPlatform() {
        // TODO detect javadoc & src
        String sdkPath = null;
        String runtimePath = null;
        String javadocPath = null;
        String srcPath = null;

        for (String path : KNOWN_LOCATIONS) {
            if (sdkPath == null) {
                sdkPath = predictSDKLocation(path);
            }
            if (runtimePath == null) {
                runtimePath = predictRuntimeLocation(path);
            }
            if (sdkPath != null && runtimePath != null) {
                break;
            }
        }

        if (sdkPath != null && runtimePath != null) {
            return Utils.createJavaFXPlatform(Utils.DEFAULT_FX_PLATFORM_NAME, sdkPath, runtimePath, null, null);
        }

        return null;
    }

    private String predictSDKLocation(String path) {
        File location = new File(path);
        if (location.exists()) {
            File[] children = location.listFiles();
            for (File child : children) {
                if (child.getName().equalsIgnoreCase("SDK")) { // NOI18N
                    File toolsJar = new File(child.getAbsolutePath() + File.separatorChar + "tools" + File.separatorChar + "ant-javafx.jar"); // NOI18N
                    if (toolsJar.exists()) {
                        return child.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    private String predictRuntimeLocation(String path) {
        File location = new File(path);
        if (location.exists()) {
            File[] children = location.listFiles();
            for (File child : children) {
                if (child.getName().equalsIgnoreCase("Runtime")) { // NOI18N
                    File rtJar = new File(child.getAbsolutePath() + File.separatorChar + "lib" + File.separatorChar + "jfxrt.jar"); // NOI18N
                    if (rtJar.exists()) {
                        return child.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }
}
