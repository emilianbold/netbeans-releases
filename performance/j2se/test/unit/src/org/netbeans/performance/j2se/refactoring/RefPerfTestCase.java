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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.performance.j2se.refactoring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import static org.netbeans.performance.j2se.Utilities.*;
import org.netbeans.modules.performance.utilities.CommonUtilities;

/**
 * 
 * @author Jan Becicka
 * @author Pavel Flaska
 */
public class RefPerfTestCase extends NbTestCase implements NbPerformanceTest {

    private ClassPath boot;
    private ClassPath source;
    private ClassPath compile;
    
    private FileObject projectDir;

    final MyHandler handler;
    final List<PerformanceData> data;

    protected RefPerfTestCase(String name) {
        super(name);
        handler = new MyHandler();
        handler.setLevel(Level.FINE);
        data = new ArrayList<PerformanceData>();
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

        IndexingManager.getDefault();

        String work = getWorkDirPath();
        //String zipPath = work + "/../../../../../../../../../nbextra/qa/projectized/jEdit41.zip";
        String zipPath=CommonUtilities.jEditProjectOpen();
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

        IndexingManager.getDefault().refreshIndexAndWait(fo.getURL(), null);
    }

    /**
     * Clear work-dir
     */
    @Override
    protected void tearDown() throws IOException {
    }

    public FileObject getProjectDir() {
        return projectDir;
    }

    public ClassPath getBoot() {
        return boot;
    }

    public ClassPath getCompile() {
        return compile;
    }

    public ClassPath getSource() {
        return source;
    }

    public boolean perform(AbstractRefactoring absRefactoring, ParameterSetter parameterSetter) {
        Problem problem = absRefactoring.preCheck();
        boolean fatal = false;
        while (problem != null) {
            ref(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if (fatal) {
            return false;
        }
        parameterSetter.setParameters();
        problem = absRefactoring.fastCheckParameters();
        while (problem != null) {
            ref(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if (fatal) {
            return false;
        }
        problem = absRefactoring.checkParameters();
        while (problem != null) {
            ref(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if (fatal) {
            return false;
        }
        RefactoringSession rs = RefactoringSession.create("Session");
        try {
            absRefactoring.prepare(rs);
            Collection<RefactoringElement> elems = rs.getRefactoringElements();
//            for (RefactoringElement refactoringElement : elems) {
//                addRefactoringElement(refactoringElement);
//            }
            rs.doRefactoring(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return true;
    }
    
    public PerformanceData[] getPerformanceData() {
        return data.toArray(new PerformanceData[0]);
    }

}

