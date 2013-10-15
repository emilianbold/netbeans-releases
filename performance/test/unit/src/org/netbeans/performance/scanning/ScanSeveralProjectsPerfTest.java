/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.performance.scanning;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import junit.framework.Test;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;

/**
 *
 * @author Pavel Flaska
 */
public class ScanSeveralProjectsPerfTest extends NbTestCase {

    private ScanningHandler handler;

    public ScanSeveralProjectsPerfTest(String name) {
        super(name);
    }

    /**
     * Set-up the services and project
     *
     * @throws java.io.IOException
     */
    @Override
    protected void setUp() throws IOException {
        System.out.println("###########  " + getName() + " ###########");
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
    }

    @Override
    protected int timeOut() {
        return 15 * 60000; // 15min
    }

    public void testScanProjects()
            throws IOException, ExecutionException, InterruptedException {
        String[][] files = {
            {"http://hg.netbeans.org/binaries/BBD005CDF8785223376257BD3E211C7C51A821E7-jEdit41.zip",
                "jEdit41.zip",
                "jEdit"
            },
            {"https://netbeans.org/projects/performance/downloads/download/Mediawiki-1_FitnessViaSamples.14.0-nbproject.zip",
                "Mediawiki-1_FitnessViaSamples.14.0-nbproject.zip",
                "mediawiki-1.14.0"
            },
            {"http://hg.netbeans.org/binaries/70CE8459CA39C3A49A2722C449117CE5DCFBA56A-tomcat6.zip",
                "tomcat6.zip",
                "tomcat6"
            },
            {"http://jupiter.cz.oracle.com/wiki/pub/NbQE/TestingProjects/BigWebProject.zip",
                "BigWebProject.zip",
                "FrankioskiProject"
            }
        };

        for (String[] row : files) {
            final String networkFileLoc = row[0];
            final String compressedProject = row[1];

            String zipPath = System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + compressedProject;
            File f = new File(zipPath);
            if (!f.exists()) {
                zipPath = Utilities.projectOpen(networkFileLoc, compressedProject);
            }
            File zipFile = FileUtil.normalizeFile(new File(zipPath));
            Utilities.unzip(zipFile, getWorkDirPath());
        }

        final Project[] projects = new Project[files.length];
        int i = 0;
        for (String[] row : files) {
            final String projectName = row[2];

            File projectsDir = FileUtil.normalizeFile(getWorkDir());
            System.out.println("projectsDir= " + projectsDir.toString());
            FileObject projectsDirFO = FileUtil.toFileObject(projectsDir);
            System.out.println("projectsDirFO= " + projectsDirFO.toString());
            FileObject projdir = projectsDirFO.getFileObject(projectName);
            System.out.println("projectName= " + projectName.toString());
            FileObject nbproject = projdir.getFileObject("nbproject");
            if (nbproject.getFileObject("private") != null) {
                for (FileObject ch : nbproject.getFileObject("private").getChildren()) {
                    ch.delete();
                }
            }
            Project p = ProjectManager.getDefault().findProject(projdir);
            if (p == null) {
                throw new IOException("Project is not found " + projectName);
            }
            projects[i++] = p;
        }
        Logger repositoryUpdater = Logger.getLogger(RepositoryUpdater.class.getName());
        repositoryUpdater.setLevel(Level.INFO);
        handler = new ScanningHandler("test projects");
        repositoryUpdater.addHandler(handler);

        Logger log = Logger.getLogger("org.openide.filesystems.MIMESupport");
        log.setLevel(Level.FINE);
        ReadingHandler readHandler = new ReadingHandler();
        log.addHandler(readHandler);
        // assertFalse("File read ", readHandler.wasRead());

        OpenProjects.getDefault().open(projects, false);

        JavaSource src = JavaSource.create(ClasspathInfo.create(getWorkDir()));

        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override()
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
            }
        }, false).get();

        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        for (PerformanceData rec : getPerformanceData()) {
            Utilities.processUnitTestsResults(ScanSeveralProjectsPerfTest.class.getCanonicalName(), rec);
        }
        handler.clear();
    }

    public static Test suite() throws InterruptedException {
        return NbModuleSuite.createConfiguration(ScanSeveralProjectsPerfTest.class).
                clusters(".*").enableModules(".*").suite();
    }

    public PerformanceData[] getPerformanceData() {
        List<PerformanceData> data = handler.getData();
        if (data != null) {
            return data.toArray(new PerformanceData[0]);
        } else {
            return null;
        }
    }

    private class ReadingHandler extends Handler {

        private boolean read = false;

        @Override
        public void publish(LogRecord record) {
            if ("MSG_CACHED_INPUT_STREAM".equals(record.getMessage())) {
                read = true;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public boolean wasRead() {
            return read;
        }
    }
}
