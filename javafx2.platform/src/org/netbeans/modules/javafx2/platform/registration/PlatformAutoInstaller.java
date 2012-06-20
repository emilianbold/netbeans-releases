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
package org.netbeans.modules.javafx2.platform.registration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.javafx2.platform.Utils;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Checks registry for entries describing the location of FX SDK and RT.
 * If found, these entries are uset to create the default FX platform.
 * 
 * @author Anton Chechel
 * @author Petr Somol
 */
public class PlatformAutoInstaller implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N

    @Override
    public void run() {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (JavaPlatform javaPlatform : platforms) {
            if (JavaFXPlatformUtils.isJavaFXEnabled(javaPlatform)) {
                return;
            }
        }
        createRegisteredJavaFXPlatform();
    }
    
    @CheckForNull
    public static JavaPlatform createRegisteredJavaFXPlatform() {
        FileObject installedSDK = findInstalledSDK();
        if (installedSDK == null) {
            LOGGER.log(Level.INFO, "Can't find auto registered JavaFX SDK instance"); // NOI18N
            return null;
        }        
        return createJavaFXPlatform(installedSDK);
    }

    private static FileObject findInstalledSDK() {
        FileObject installedSDK = null;
        FileObject dir = getRepositoryDir(AutomaticRegistration.CONFIG, false);
        if (dir != null) {
            FileObject[] instanceFOs = dir.getChildren();
            if (instanceFOs != null && instanceFOs.length > 0) {
                for (int i = 0; i < instanceFOs.length; i++) {
                    if (AutomaticRegistration.JAVAFX_SDK_AUTOREGISTERED_INSTANCE.equals(instanceFOs[i].getName())) {
                        installedSDK = instanceFOs[i];
                        continue;
                    }
                }
            }
        }
        return installedSDK;
    }

    private static FileObject getRepositoryDir(String path, boolean create) {
        FileObject dir = FileUtil.getConfigFile(path);
        if (dir == null && create) {
            try {
                dir = FileUtil.createFolder(FileUtil.getConfigRoot(), path);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return dir;
    }

    @CheckForNull
    private static JavaPlatform createJavaFXPlatform(FileObject installedSDK) {
        Parameters.notNull("installedSDK", installedSDK); // NOI18N

        // Read installedSKD properties
        String sdkPath = getStringAttribute(installedSDK, AutomaticRegistration.SDK_ATTR);
        String runtimePath = getStringAttribute(installedSDK, AutomaticRegistration.RUNTIME_ATTR);
        if (sdkPath == null || runtimePath == null) {
            LOGGER.log(Level.FINE, "Can't read attributes from auto registered JavaFX SDK instance: {0}", installedSDK.getPath()); // NOI18N
            return null;
        }

        if (sdkPath == null || runtimePath == null) {
            LOGGER.log(Level.FINE, "Can't read attributes from auto registered JavaFX SDK instance: {0}", installedSDK.getPath()); // NOI18N
            return null;
        }
        
        if (!JavaFXPlatformUtils.areJFXLocationsCorrect(sdkPath, runtimePath)) {
            LOGGER.log(Level.FINE, "JavaFX SDK and/or JavaFX Runtime locations are not correct"); // NOI18N
            return null;
        }

        // Create java platform instance and register JavaFX platform extension
        JavaPlatform platform = null;
        try {
            platform = Utils.createJavaFXPlatform(
                    Utils.DEFAULT_FX_PLATFORM_NAME, 
                    sdkPath, 
                    runtimePath, 
                    JavaFXPlatformUtils.predictJavadocLocation(sdkPath), 
                    JavaFXPlatformUtils.predictSourcesLocation(sdkPath)
                    );
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Can't create Java Platform instance: {0}", ex); // NOI18N
        }
        if (platform != null) {
            LOGGER.log(Level.INFO, "JavaFX Platform instance has been successfully registered: {0}", platform); // NOI18N
        }
        return platform;
    }

    private static String getStringAttribute(FileObject fo, String attrName) {
        return getStringAttribute(fo, attrName, null);
    }

    private static String getStringAttribute(FileObject fo, String attrName, String defValue) {
        String result = defValue;
        Object attr = fo.getAttribute(attrName);
        if (attr instanceof String) {
            result = (String) attr;
        }
        return result;
    }

}
