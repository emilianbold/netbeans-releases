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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        // see issue 83144
        if (Utilities.isWindows()) {
            LOGGER.log(Level.FINE, "Operating system: Windows");
            String programFilesPath = System.getProperty("Env-ProgramFiles"); // NOI18N
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
