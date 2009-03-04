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
package org.netbeans.modules.refactoring.java.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import junit.framework.Test;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.ui.WhereUsedQueryUI;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.netbeans.modules.refactoring.java.test.Utilities.*;

/**
 * Test that all java-source instances are disposed at the end.
 *
 * @author Pavel Flaska
 */
public class FindSubclassesTest extends RefPerfTestCase {

    public FindSubclassesTest(String name) {
        super(name);
    }

    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws IOException, InterruptedException {
        clearWorkDir();
        String work = getWorkDirPath();
        System.setProperty("netbeans.user", work);
        projectDir = openProject("SimpleJ2SEApp", getDataDir());
        File projectSourceRoot = new File(getWorkDirPath(), "SimpleJ2SEApp.src".replace('.', File.separatorChar));
        FileObject fo = FileUtil.toFileObject(projectSourceRoot);

        boot = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        source = createSourcePath(projectDir);
        compile = createEmptyPath();
    }

    public void testFindSub()
            throws IOException, InterruptedException, ExecutionException {
        // logging is used to obtain data about consumed time
        Logger timer = Logger.getLogger("TIMER.RefactoringSession");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        timer = Logger.getLogger("TIMER.RefactoringPrepare");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        Log.enableInstances(Logger.getLogger("TIMER"), "JavacParser", Level.FINEST);

        ClasspathInfo cpi = ClasspathInfo.create(getBoot(), getCompile(), getSource());
        
        FileObject testFile = getProjectDir().getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.create(cpi, testFile);

        src.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                TypeMirror mirror = klass.getSuperclass();
                Element object = controller.getTypes().asElement(mirror);
                System.err.println(object);
                TreePathHandle element = TreePathHandle.create(object, controller);
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
                    d.name = "refactoring.prepare"+" ("+element.resolveElement(controller).getSimpleName()+", usages:"+elems.size()+")";
                    d.value = prepare;
                    d.unit = "ms";
                    d.runOrder = 0;
                    sb.append("Prepare phase: ").append(prepare).append(" ms.\n");
                    Utilities.processUnitTestsResults(FindUsagesPerfTest.class.getCanonicalName(), d);
                } catch (Exception ex) {
                    sb.append("Cannot collect usages: ").append(ex.getCause());
                }
                getLog().append(sb);
                System.err.println(sb);
            }
        }, false);

        src = null;
        Log.assertInstances("Some instances of parser were not GCed");
    }

    public static Test suite() throws InterruptedException {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().addTest(FindSubclassesTest.class, "testFindSub").gui(false));
    }
}