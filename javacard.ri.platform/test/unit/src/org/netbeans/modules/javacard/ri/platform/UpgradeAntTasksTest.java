/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.ri.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javacard.ri.platform.installer.RIPlatformFactory;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author tim
 */
public class UpgradeAntTasksTest extends NbTestCase {
    EditableProperties pprops;
    RIPlatformFactory factory;
    File dir;
    public UpgradeAntTasksTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(IFL.class);
        pprops = new EditableProperties(true);
        pprops.load(UpgradeAntTasksTest.class.getResourceAsStream("UpgradeAntTasks.properties"));
        clearWorkDir();
        dir = getWorkDir();
        File libDir = new File(dir, "lib");
        libDir.mkdir();
        File fakeOrigTasks = new File (libDir, "nbtasks.jar");
        fakeOrigTasks.createNewFile();
        System.setProperty ("netbeans.user", dir.getAbsolutePath());
        ProgressHandle handle = ProgressHandleFactory.createHandle(getClass().getName());
        factory = new RIPlatformFactory(pprops, null, FileUtil.toFileObject(dir), handle, "Stuff");
    }

    public void testPlatformIsUpgraded() throws Exception {
        FileObject fo = factory.createPlatform();
        assertTrue (factory.antTasksUpdated);
        EditableProperties p = new EditableProperties(true);
        InputStream in = fo.getInputStream();
        try {
            p.load(in);
        } finally {
            in.close();
        }
        assertEquals (Boolean.TRUE.toString(), p.getProperty(JavacardPlatformKeyNames.PLATFORM_302_ANT_TASKS_UPDATED));
        boolean found = false;
        for (String k : JavacardPlatformKeyNames.getPathPropertyNames(p)) {
            String s = p.getProperty(k);
            if (s != null) {
                found |= s.contains(fakeAntTasksJar.getAbsolutePath());
            }
        }
        assertTrue (found);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clearWorkDir();
        if (instance != null) {
            synchronized (instance) {
                if (fakeAntTasksJar != null && fakeAntTasksJar.exists()) { //temp files only really deleted on windows
                    fakeAntTasksJar.delete();
                }
            }
        }
        fakeAntTasksJar = null;
        instance = null;
    }

    private static File fakeAntTasksJar;
    static IFL instance;

    public static class IFL extends InstalledFileLocator {

        private synchronized File getFile() throws IOException {
            if (fakeAntTasksJar == null) {
                fakeAntTasksJar = File.createTempFile("anttasks", ".jar");
                System.err.println("FAKE ANT TASKS REQUESTED " + fakeAntTasksJar.getAbsolutePath());
            }
            return fakeAntTasksJar;
        }

        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (RIPlatformFactory.ANT_TASKS_302_JAR_PATH.equals(relativePath)) {
                try {
                    return getFile();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return null;
        }
    }
}
