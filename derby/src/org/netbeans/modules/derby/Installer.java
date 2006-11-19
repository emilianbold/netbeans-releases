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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.derby;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbCollections;
import org.openide.windows.WindowManager;

/**
 * @author Andrei Badea
 */
public class Installer extends ModuleInstall {

    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                registerJDKDerby();
            }
        });
    }

    private void registerJDKDerby() {
        if (DerbyOptions.getDefault().getLocation().length() > 0) {
            return;
        }
        JavaPlatform javaPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        SpecificationVersion version = javaPlatform.getSpecification().getVersion();
        if (new SpecificationVersion("1.6").compareTo(version) > 0) { // NOI18N
            return;
        }
        for (Object dir : javaPlatform.getInstallFolders()) {
            FileObject derbyDir = ((FileObject)dir).getFileObject("db"); // NOI18N
            if (derbyDir != null) {
                DerbyOptions.getDefault().setLocation(FileUtil.toFile(derbyDir).getAbsolutePath());
                break;
            }
        }
    }
}
