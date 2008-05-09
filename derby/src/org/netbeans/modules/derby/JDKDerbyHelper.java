/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.derby;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Utilities;

/**
 *
 * @author Andrei Badea
 */
public class JDKDerbyHelper {

    private static final  Logger LOGGER = Logger.getLogger(JDKDerbyHelper.class.getName());

    private final JavaPlatform platform;

    public static JDKDerbyHelper forDefaultPlatform() {
        return new JDKDerbyHelper(JavaPlatformManager.getDefault().getDefaultPlatform());
    }

    private JDKDerbyHelper(JavaPlatform javaPlatform) {
        this.platform = javaPlatform;
    }

    public boolean canBundleDerby() {
        Specification specification = platform.getSpecification();
        if (specification == null) {
            return false;
        }
        SpecificationVersion version = specification.getVersion();
        if (version == null) {
            return false;
        }
        if (version.compareTo(new SpecificationVersion("1.6")) < 0) { // NOI18N
            return false;
        }
        return true;
    }

    public String findDerbyLocation() {
        if (!canBundleDerby()) {
            return null;
        }
        // see issue 83144
        if (Utilities.isWindows()) {
            LOGGER.log(Level.FINE, "Operating system: Windows");
            String programFilesPath = System.getenv("ProgramFiles"); // NOI18N
            LOGGER.log(Level.FINE, "Program Files path: {0}", programFilesPath);
            if (programFilesPath != null) {
                File derbyDirFile = new File(programFilesPath, "Sun/JavaDB"); // NOI18N
                String result = testDerbyInstallLocation(derbyDirFile);
                if (result != null) {
                    return result;
                }
            }
        }
        if (Utilities.isUnix()) {
            LOGGER.log(Level.FINE, "Operating system: Unix");
            String result = testDerbyInstallLocation(new File("/opt/SUNWjavadb")); // NOI18N
            if (result != null) {
                return result;
            }
            result = testDerbyInstallLocation(new File("/opt/sun/javadb")); // NOI18N
            if (result != null) {
                return result;
            }
            result = testDerbyInstallLocation(new File("/usr/share/javadb")); // NOI18N
            if (result != null) {
                return result;
            }
        }
        for (Object dir : platform.getInstallFolders()) {
            FileObject derbyDir = ((FileObject)dir).getFileObject("db"); // NOI18N
            if (derbyDir != null) {
                String result = testDerbyInstallLocation(FileUtil.toFile(derbyDir));
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static String testDerbyInstallLocation(File directory) {
        LOGGER.log(Level.FINE, "Testing directory: {0}", directory);
        if (Util.isDerbyInstallLocation(directory)) {
            return directory.getAbsolutePath();
        }
        return null;
    }
}
