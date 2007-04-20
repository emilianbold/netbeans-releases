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
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Andrei Badea
 */
public class JDKDerbyHelper {

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
        for (Object dir : platform.getInstallFolders()) {
            FileObject derbyDir = ((FileObject)dir).getFileObject("db"); // NOI18N
            if (derbyDir != null) {
                File derbyDirFile = FileUtil.toFile(derbyDir);
                if (Util.isDerbyInstallLocation(derbyDirFile)) {
                    return derbyDirFile.getAbsolutePath();
                }
            }
        }
        return null;
    }
}
