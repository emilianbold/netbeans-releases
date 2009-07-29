/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.CLIHandler;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.startup.layers.SessionManager;
import org.openide.LifecycleManager;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rudimentary manager useful for non-GUI platform applications.
 * Superseded by NbTopManager.NbLifecycleManager.
 * @see #158525
 */
@ServiceProvider(service=LifecycleManager.class)
public class ModuleLifecycleManager extends LifecycleManager {

    public void saveAll() {
        // XXX #77210 would make it possible for some objects to be saved here
    }

    private final AtomicBoolean exiting = new AtomicBoolean(false);
    public void exit() {
        if (exiting.getAndSet(true)) {
            return;
        }
        // Simplified version of NbTopManager.doExit.
        if (Main.getModuleSystem().shutDown(new Runnable() {
            public void run() {
                try {
                    CLIHandler.stopServer();
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    Exceptions.printStackTrace(t);
                }
            }
        })) {
            try {
                SessionManager.getDefault().close();
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
            if (System.getProperty("netbeans.close.no.exit") == null) {
                TopSecurityManager.exit(0);
            }
        }
    }

    public @Override void markForRestart() throws UnsupportedOperationException {
        try {
            Class.forName("javax.jnlp.BasicService"); // NOI18N
            throw new UnsupportedOperationException("cannot restart in JNLP mode"); // NOI18N
        } catch (ClassNotFoundException x) {
            // OK, running in normal mode
        }
        String userdir = System.getProperty("netbeans.user"); // NOI18N
        if (userdir == null) {
            throw new UnsupportedOperationException("no userdir"); // NOI18N
        }
        File restartFile = new File(userdir, "var/restart"); // NOI18N
        if (!restartFile.exists()) {
            try {
                restartFile.createNewFile();
            } catch (IOException x) {
                throw new UnsupportedOperationException(x);
            }
        }
    }

}
