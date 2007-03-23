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
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * @author Andrei Badea
 */
public class Installer extends ModuleInstall {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new RegisterJDKDerby());
    }

    private static final class RegisterJDKDerby implements Runnable {

        private final JDKDerbyHelper helper = JDKDerbyHelper.forDefaultPlatform();

        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                RequestProcessor.getDefault().post(this);
                return;
            }

            if (!helper.canBundleDerby()) {
                LOGGER.fine("Default platform cannot bundle Derby"); // NOI18N
                return;
            }

            ProgressHandle handle = ProgressHandleFactory.createSystemHandle(NbBundle.getMessage(Installer.class, "MSG_RegisterJavaDB"));
            handle.start();
            try {
                if (registerJDKDerby()) {
                    registerSampleDatabase();
                }
            } finally {
                handle.finish();
            }
        }

        private boolean registerJDKDerby() {
            if (DerbyOptions.getDefault().getLocation().length() > 0) {
                return false;
            }
            String derbyLocation = helper.findDerbyLocation();
            if (derbyLocation != null) {
                LOGGER.log(Level.FINE, "Registering JDK Derby at {0}", derbyLocation); // NOI18N
                return DerbyOptions.getDefault().trySetLocation(derbyLocation);
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
