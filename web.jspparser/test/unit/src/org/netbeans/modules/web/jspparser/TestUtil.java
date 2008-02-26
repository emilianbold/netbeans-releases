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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.project.JavaAntLogger;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author  pj97932, Tomas Mysik
 */
final class TestUtil {

    private TestUtil() {
    }

    static void setup(NbTestCase test) throws Exception {

        test.clearWorkDir();

        File javaCluster = new File(JavaAntLogger.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParentFile().getParentFile();
        File enterCluster = new File(WebModule.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParentFile().getParentFile();
        System.setProperty("netbeans.dirs", javaCluster.getPath() + File.pathSeparator + enterCluster.getPath());

        Logger.getLogger("org.netbeans.core.startup.ModuleList").setLevel(Level.OFF);

        Logger.getLogger("org.netbeans.modules.web.jspparser_ext").setLevel(Level.FINE);

        // module system
        Lookup.getDefault().lookup(ModuleInfo.class);

        // unzip test project
        TestUtil.getProject(test, "project3");
    }

    static FileObject getFileInWorkDir(String path, NbTestCase test) throws Exception {
        File f = test.getDataDir();
        FileObject workDirFO = FileUtil.toFileObject(f);
        return FileUtil.createData(workDirFO, path);
    }

    static WebModule getWebModule(FileObject fo) {
        WebModule wm =  WebModule.getWebModule(fo);
        if (wm == null) {
            return null;
        }
        FileObject wmRoot = wm.getDocumentBase();
        if (fo == wmRoot || FileUtil.isParentOf(wmRoot, fo)) {
            return WebModule.getWebModule(fo);
        }
        return null;
    }

    static Project getProject(NbTestCase test, String projectFolderName) throws Exception {
        File f = getProjectAsFile(test, projectFolderName);
        FileObject projectPath = FileUtil.toFileObject(f);
        Project project = ProjectManager.getDefault().findProject(projectPath);
        NbTestCase.assertNotNull("Project should exist", project);
        return project;
    }

    static FileObject getProjectFile(NbTestCase test, String projectFolderName, String filePath) throws Exception {
        Project project = getProject(test, projectFolderName);
        FileObject fo = project.getProjectDirectory().getFileObject(filePath);
        NbTestCase.assertNotNull("Project file should exist: " + filePath, fo);

        return fo;
    }

    static WebModule createWebModule(FileObject documentRoot) {
        WebModuleImplementation webModuleImpl = new WebModuleImpl(documentRoot);
        return WebModuleFactory.createWebModule(webModuleImpl);
    }

    private static File getProjectAsFile(NbTestCase test, String projectFolderName) throws Exception {
        File f = new File(test.getDataDir(), projectFolderName);
        if (!f.exists()) {
            // maybe it's zipped
            File archive = new File(test.getDataDir(), projectFolderName + ".zip");
            unZip(archive, test.getDataDir());
        }
        NbTestCase.assertTrue("project directory has to exists: " + f, f.exists());
        return f;
    }

    private static void unZip(File archive, File destination) throws Exception {
        if (!archive.exists()) {
            throw new FileNotFoundException(archive + " does not exist.");
        }
        ZipFile zipFile = new ZipFile(archive);
        Enumeration<? extends ZipEntry> all = zipFile.entries();
        while (all.hasMoreElements()) {
            extractFile(zipFile, all.nextElement(), destination);
        }
    }

    private static void extractFile(ZipFile zipFile, ZipEntry e, File destination) throws IOException {
        String zipName = e.getName();
        if (zipName.startsWith("/")) {
            zipName = zipName.substring(1);
        }
        if (zipName.endsWith("/")) {
            return;
        }
        int ix = zipName.lastIndexOf('/');
        if (ix > 0) {
            String dirName = zipName.substring(0, ix);
            File d = new File(destination, dirName);
            if (!(d.exists() && d.isDirectory())) {
                if (!d.mkdirs()) {
                    NbTestCase.fail("Warning: unable to mkdir " + dirName);
                }
            }
        }
        FileOutputStream os = new FileOutputStream(destination.getAbsolutePath() + "/" + zipName);
        InputStream is = zipFile.getInputStream(e);
        int n = 0;
        byte[] buff = new byte[8192];
        while ((n = is.read(buff)) > 0) {
            os.write(buff, 0, n);
        }
        is.close();
        os.close();
    }
}
