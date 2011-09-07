/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javafx2.platform.registration;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Registers Java FX SDK and Java FX Runtime instances by creating instance file
 * in cluster config directory. Designed to be called from installer.
 * <p>
 * Sample command line<br>
 *  java -cp .\platform\core\core.jar:.\platform\lib\boot.jar:.\platform\lib\org-openide-modules.jar:
 *       .\platform\core\org-openide-filesystems.jar:.\platform\lib\org-openide-util.jar:
 *       .\platform\lib\org-openide-util-lookup.jar:.\javafx\modules\org-netbeans-modules-javafx2-platform.jar
 *             org.netbeans.modules.javafx2.platform.registration.AutomaticRegistration
 *                .\ide C:\Program Files\Oracle\JavaFX 2.0 SDK C:\Program Files\Oracle\JavaFX Runtime 2.0
 *
 * @author Anton Chechel
 * @see #main(args)
 */
public class AutomaticRegistration {

    public static final String JAVAFX_SDK_AUTOREGISTERED_INSTANCE = "javafx_sdk_autoregistered_instance"; // NOI18N

    static final String CONFIG = "JavaFX/Instances"; // NOI18N
    static final String SDK_ATTR = "javafxSDK"; // NOI18N
    static final String RUNTIME_ATTR = "javafxRuntime"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N

    /**
     * Performs registration.
     *
     * Exit codes:<p>
     * <ul>
     *   <li> 2: could not find/create config/JavaFX/Instances folder
     *   <li> 3: could not write registration FileObject
     * </ul>
     * @param args command line arguments - cluster path and GlassFish home expected
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Parameters: <ide clusterDir> <Java FX SDK path> <Java FX Runtime path>"); // NOI18N
            System.exit(-1);
        }

        System.out.println(Arrays.toString(args));
        int status = autoregisterFXSDKInstance(args[0], args[1], args[2]);
        System.exit(status);
    }

    private static int autoregisterFXSDKInstance(String clusterDirValue, String sdkPath, String rtPath) throws IOException {
        // tell the infrastructure that the userdir is cluster dir
        System.setProperty("netbeans.user", clusterDirValue); // NOI18N

        FileObject sdkInstanceDir = FileUtil.getConfigFile(CONFIG);

        if (sdkInstanceDir == null) {
            sdkInstanceDir = FileUtil.createFolder(FileUtil.getConfigRoot(), CONFIG);
            if (sdkInstanceDir == null) {
                LOGGER.log(Level.INFO, "Cannot register the default Java FX SDK. The config/" + CONFIG + " folder cannot be created."); // NOI18N
                return 2;
            }
        }

        // make sure the Java FX SDK is not registered yet
        for (FileObject fo : sdkInstanceDir.getChildren()) {
            if (fo.getAttribute(SDK_ATTR).equals(sdkPath)) {
                // the Java FX SDK is already registered, do nothing
                return 0;
            }
        }

        boolean ok = registerFXSDKInstanceFO(sdkInstanceDir, sdkPath, rtPath);
        if (ok) {
            return 0;
        } else {
            return 3;
        }
    }

    private static boolean registerFXSDKInstanceFO(FileObject sdkInstanceDir, String sdkPath, String rtPath) {
        String name = FileUtil.findFreeFileName(sdkInstanceDir, JAVAFX_SDK_AUTOREGISTERED_INSTANCE, null);
        FileObject instanceFO;
        try {
            instanceFO = sdkInstanceDir.createData(name);
            instanceFO.setAttribute(SDK_ATTR, sdkPath);
            instanceFO.setAttribute(RUNTIME_ATTR, rtPath);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Cannot register the default Java FX SDK."); // NOI18N
            LOGGER.log(Level.INFO, null, e);
        }
        return false;
    }

}
