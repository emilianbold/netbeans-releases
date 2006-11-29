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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * @author Andrei Badea
 */
public class Installer extends ModuleInstall {

    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new RegisterJDKDerby());
    }

    private static final class RegisterJDKDerby implements Runnable {

        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                RequestProcessor.getDefault().post(this);
                return;
            }

            JavaPlatform javaPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
            SpecificationVersion version = javaPlatform.getSpecification().getVersion();
            if (new SpecificationVersion("1.6").compareTo(version) > 0) { // NOI18N
                return;
            }

            ProgressHandle handle = ProgressHandleFactory.createSystemHandle(NbBundle.getMessage(Installer.class, "MSG_RegisterJavaDB"));
            handle.start();
            try {
                if (registerJDKDerby(javaPlatform)) {
                    registerSampleDatabase();
                }
            } finally {
                handle.finish();
            }
        }

        private boolean registerJDKDerby(JavaPlatform javaPlatform) {
            if (DerbyOptions.getDefault().getLocation().length() > 0) {
                return false;
            }
            for (Object dir : javaPlatform.getInstallFolders()) {
                FileObject derbyDir = ((FileObject)dir).getFileObject("db"); // NOI18N
                if (derbyDir != null) {
                    DerbyOptions.getDefault().setLocation(FileUtil.toFile(derbyDir).getAbsolutePath());
                    return true;
                }
            }
            return false;
        }

        private void registerSampleDatabase() {
            try {
                DerbyDatabases.createSampleDatabase();
            } catch (DatabaseException e) {
                Logger.getLogger(Installer.class.getName()).log(Level.WARNING, null, e);
            } catch (IOException e) {
                Logger.getLogger(Installer.class.getName()).log(Level.WARNING, null, e);
            }
        }
    }
}
