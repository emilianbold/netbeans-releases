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
package org.netbeans.performance.j2se.refactoring;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.ExecutionException;
import javax.lang.model.element.Element;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;

import java.io.*;
import javax.lang.model.element.PackageElement;
import junit.framework.Assert;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.ui.WhereUsedQueryUI;
import static org.netbeans.performance.j2se.Utilities.*;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test find usages functionality. Measure the the usages time.
 * 
 * @author Pavel Flaska
 */
public class FindUsagesPerfTest extends NbTestCase implements NbPerformanceTest {

    private static final MyHandler handler;
    private static final List<PerformanceData> data;
    private ClassPath boot;
    private ClassPath source;
    private ClassPath compile;
    private FileObject projectDir;


    static {
        FindUsagesPerfTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", TestLkp.class.getName());
        Assert.assertEquals(TestLkp.class, Lookup.getDefault().getClass());
        handler = new MyHandler();
        handler.setLevel(Level.FINE);
        data = new ArrayList<PerformanceData>();
    }

    public FindUsagesPerfTest(String name) {
        super(name);
    }

    public void testFindUsage()
            throws IOException, InterruptedException, ExecutionException {
        // logging is used to obtain data about consumed time
        Logger timer = Logger.getLogger("TIMER.RefactoringSession");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        timer = Logger.getLogger("TIMER.RefactoringPrepare");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        FileObject testFile = projectDir.getFileObject("/src/org/gjt/sp/jedit/jEdit.java");

        ClasspathInfo cpi = ClasspathInfo.create(boot, compile, source);

        final JavaSource src = JavaSource.create(cpi, testFile);

        // find usages of symbols collected below
        final List<TreePathHandle> handle = new ArrayList<TreePathHandle>();

        src.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                PackageElement pckg = controller.getElements().getPackageElement("org.gjt.sp.jedit");
                for (final Element element : pckg.getEnclosedElements()) {
                    handle.add(TreePathHandle.create(element, controller));
                }
            }
        }, false);

        // do find usages query
        for (final TreePathHandle element : handle) {

            src.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.RESOLVED);

                    final WhereUsedQueryUI ui = new WhereUsedQueryUI(element, controller);
                    ui.getPanel(null);
                    try {
                        ui.setParameters();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final AbstractRefactoring wuq = ui.getRefactoring();
                    RefactoringSession rs = RefactoringSession.create("Session");
                    wuq.prepare(rs);
                    rs.doRefactoring(false);
                    Collection<RefactoringElement> elems = rs.getRefactoringElements();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Symbol: '").append(element.resolveElement(controller).getSimpleName()).append("'");
                    sb.append('\n').append("Number of usages: ").append(elems.size()).append('\n');
                    try {
                        long prepare = handler.get("refactoring.prepare");
                        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
                        d.name = "refactoring.prepare";
                        d.value = prepare;
                        d.unit = "ms";
                        d.runOrder = 0;
                        sb.append("Prepare phase: ").append(prepare).append(" ms.\n");
                    } catch (Exception ex) {
                        sb.append("Cannot collect usages: ").append(ex.getCause());
                    }
                    getLog().append(sb);
                    System.err.println(sb);

                }
            }, false);
            System.gc(); System.gc();
        }
    }

    public PerformanceData[] getPerformanceData() {
        return data.toArray(new PerformanceData[0]);
    }

    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws IOException, InterruptedException {
        clearWorkDir();
        MockServices.setServices();

        File cache = new File(getWorkDir(), "cache");       //NOI18N
        cache.mkdirs();
        IndexUtil.setCacheFolder(cache);

        RepositoryUpdater.getDefault();

        String work = getWorkDirPath();
        String zipPath = work + "/../../../../../../../../../nbextra/qa/projectized/jEdit41.zip";
        File zipFile = FileUtil.normalizeFile(new File(zipPath));
        unzip(zipFile, work);
        projectDir = openProject("jEdit41", getWorkDir());
        File projectSourceRoot = new File(getWorkDirPath(), "jEdit41.src".replace('.', File.separatorChar));
        FileObject fo = FileUtil.toFileObject(projectSourceRoot);

        boot = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        source = createSourcePath(projectDir);
        compile = createEmptyPath();

        ClassLoader l = FindUsagesPerfTest.class.getClassLoader();
        TestLkp.setLookupsWrapper(
                Lookups.singleton(new ClassPathProvider() {

                    public ClassPath findClassPath(FileObject file, String type) {
                        if (ClassPath.BOOT.equals(type)) {
                            return boot;
                        }

                        if (ClassPath.SOURCE.equals(type)) {
                            return source;
                        }

                        if (ClassPath.COMPILE.equals(type)) {
                            return compile;
                        }
                        return null;
                    }
                }),
                Lookups.metaInfServices(l),
                Lookups.singleton(l));

        RepositoryUpdater.getDefault().scheduleCompilationAndWait(fo, fo).await();
    }

    /**
     * Clear work-dir
     */
    @Override
    protected void tearDown() throws IOException {
        clearWorkDir();
    }

    public static NbTestSuite suite() throws InterruptedException {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new FindUsagesPerfTest("testFindUsage"));
        return suite;
    }

    private static class MyHandler extends Handler {

        private Map<String, Long> map = new HashMap<String, Long>();

        @Override
        public void publish(LogRecord record) {
            Long data;
            if (record == null) {
                return;
            }
            for (Object o : record.getParameters()) {
                if (o instanceof Long) {
                    data = (Long) o;
                    map.put(record.getMessage(), data);
                }
            }
        }

        public Long get(String key) {
            return map.get(key);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
