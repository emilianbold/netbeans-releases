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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach
 */
public class Issue169448DeadlockTest extends NbTestCase {

    public Issue169448DeadlockTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }

    public void testLoadingNbPlatformWhileElseoneHoldsProjectMutex() throws Exception {
        IFL.root = getWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        MockServices.setServices(IFL.class);
        class Block implements Runnable {
            public void run() {
                if (ProjectManager.mutex().isReadAccess()) {
                    synchronized (this) {
                        try {
                            notifyAll();
                            wait();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } else {
                    ProjectManager.mutex().readAccess(this);
                }
            }
        }
        Block b = new Block();
        RequestProcessor.Task t;
        synchronized (b) {
            t = RequestProcessor.getDefault().post(b);
            b.wait();
        }

        Class<?> c = Class.forName("org.netbeans.modules.apisupport.project.universe.NbPlatform");
        Object out = c.getDeclaredMethod("defaultPlatformLocation").invoke(null);
        assertNotNull("Loaded and created OK", out);

        synchronized (b) {
            b.notifyAll();
        }
        for (int i = 0; i < 5000; i++) {
            String p = PropertyUtils.getGlobalProperties().getProperty("nbplatform.default.harness.dir");
            if (p != null) {
                // OK
                return;
            }
            Thread.sleep(100);
        }
        fail("Global property not written!");
    }

    public static final class IFL extends InstalledFileLocator {
        static File root;

        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            File f = new File(root, relativePath.replace('/', File.separatorChar));
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException ex) {
                throw new AssertionFailedErrorException(ex);
            }
            return f;
        }

    }
}