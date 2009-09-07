/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.performance.scanning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;

/**
 * 
 * @author Pavel Flaska
 */
public class ScanProjectPerfTest extends NbTestCase {

    private final List<PerformanceData> data;
    
    public ScanProjectPerfTest(String name) {
        super(name);
        data = new ArrayList<PerformanceData>();
    }
    
    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws IOException, InterruptedException {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
    }

    @Override
    protected int timeOut() {
        return 15*60000; // 15min
    }

    public void testScanJEdit() throws IOException, ExecutionException, InterruptedException {
        scanProject("http://spbweb.russia.sun.com/~ok153203/jEdit41.zip", 
                    "jEdit41.zip",
                    "jEdit41");
    }

    public void testScanNigelsFreeForm() throws IOException, ExecutionException, InterruptedException {
        scanProject("http://beetle.czech.sun.com/~pf124312/nigel.zip",
                    "Clean.zip",
                    "clean/projects/ST");
    }

    public void testPhpProject() throws IOException, ExecutionException, InterruptedException {
        scanProject("http://wiki.netbeans.org/attach/FitnessViaSamples/mediawiki-1.14.0-nbproject.zip",
                    "mediawiki.zip",
                    "mediawiki-1.14.0"
                );
    }
    
    public void testLimeWire() throws IOException, ExecutionException, InterruptedException {
        scanProject("http://spbweb.russia.sun.com/~ok153203/lime6.zip",
                    "lime6.zip",
                    "lime6");
    }

    public void testOpenJdk7() throws IOException, ExecutionException, InterruptedException {
        scanProject("http://beetle.czech.sun.com/~pf124312/openjdk-7-ea-src-b63-02_jul_2009.zip",
                    "openjdk.zip",
                    "openjdk/jdk/make/netbeans/world");
    }

    private void scanProject(
            final String networkFileLoc,
            final String compressedProject,
            final String project)
            throws IOException, ExecutionException, InterruptedException
    {
        String zipPath = System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + compressedProject;
        File f = new File(zipPath);
        if (!f.exists()) {
             zipPath = Utilities.projectOpen(networkFileLoc, compressedProject);
        }
        File zipFile = FileUtil.normalizeFile(new File(zipPath));
        Utilities.unzip(zipFile, getWorkDirPath());
        
        FileObject projectDir = Utilities.openProject(project, getWorkDir());
        final String projectName = projectDir.getName();
        
        Logger repositoryUpdater = Logger.getLogger(RepositoryUpdater.class.getName());
        repositoryUpdater.setLevel(Level.INFO);
        ScanningHandler handler = new ScanningHandler(projectName);
        repositoryUpdater.addHandler(handler);
        JavaSource src = JavaSource.create(ClasspathInfo.create(projectDir));

        src.runWhenScanFinished(new Task<CompilationController>() {
            
            @Override()
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
            }
        }, false).get();
        
        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
        projectDir = Utilities.openProject(project, getWorkDir());

        handler.setType(ScanType.UP_TO_DATE);
        src = JavaSource.create(ClasspathInfo.create(projectDir));

        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override()
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
            }
        }, false).get();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        for (PerformanceData rec : getPerformanceData()) {
            Utilities.processUnitTestsResults(ScanProjectPerfTest.class.getCanonicalName(), rec);
        }
        data.clear();
    }

    public static Test suite() throws InterruptedException {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().
                addTest(ScanProjectPerfTest.class).
                clusters(".*").gui(false).
                enableModules("php.*", ".*"));
    }

    public PerformanceData[] getPerformanceData() {
        return data.toArray(new PerformanceData[0]);
    }

    @SuppressWarnings("serial")
    private static enum ScanType {
        INITIAL(" initial "),
        UP_TO_DATE(" up-to-date ");

        private final String name;

        private ScanType(String name) {
            this.name = name;
        }
        
        private String getName() {
            return name;
        }
    }
    
    private class ScanningHandler extends Handler {

        private final String projectName;
        private ScanType type;

        public ScanningHandler(String projectName) {
            this.projectName = projectName;
            this.type = ScanType.INITIAL;
        }

        public void setType(ScanType type) {
            this.type = type;
        }
        
        @Override
        public void publish(LogRecord record) {
            String message = record.getMessage();
            if (message != null && message.startsWith("Complete indexing")) {
                if (message.contains("source roots")) {
                    PerformanceData res = new PerformanceData();
                    StringTokenizer tokenizer = new StringTokenizer(message, " ");
                    int count = tokenizer.countTokens();
                    res.name = projectName + type.getName() + "source scan";
                    
                    String token = "0";
                    for (int i = 0; i < count; i++) {
                        String next = tokenizer.nextToken();
                        if (next.startsWith("ms")) {
                            break;
                        }
                        token = next;
                    }
                    res.value = Long.parseLong(token);
                    res.unit = "ms";
                    res.runOrder = 0;
                    data.add(res);
                } else if (message.contains("binary roots")) {
                    PerformanceData res = new PerformanceData();
                    StringTokenizer tokenizer = new StringTokenizer(message, " ");
                    int count = tokenizer.countTokens();
                    res.name = projectName + type.getName() + "binary scan";
                    for (int i = 0; i < count-2; i++) {
                        tokenizer.nextToken();
                    }
                    String token = tokenizer.nextToken();
                    res.value = Long.parseLong(token);
                    res.unit = "ms";
                    res.runOrder = 0;
                    data.add(res);
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
