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
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.xml.crypto.dsig.keyinfo.PGPData;
import junit.framework.Test;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 * Test that all java-source instances are disposed at the end.
 *
 * @author Pavel Flaska
 */
public class MoveClassPerfTest extends RefPerfTestCase {

    public MoveClassPerfTest(String name) {
        super(name);
    }

    public void testRenamePackage()
            throws IOException, InterruptedException, ExecutionException {
        // logging is used to obtain data about consumed time
        Logger timer = Logger.getLogger("TIMER.RefactoringSession");
        timer.setLevel(Level.FINE);
        timer.addHandler(getHandler());

        timer = Logger.getLogger("TIMER.RefactoringPrepare");
        timer.setLevel(Level.FINE);
        timer.addHandler(getHandler());

        Log.enableInstances(Logger.getLogger("TIMER"), "JavacParser", Level.FINEST);
        final FileObject testFile = getProjectDir().getFileObject("/src/org/gjt/sp/jedit/ActionSet.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        System.err.println(src.getClasspathInfo());
        final MoveRefactoring[] moveRef = new MoveRefactoring[1];
        src.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("org.gjt.sp.jedit.ActionSet");
                moveRef[0] = new MoveRefactoring(Lookups.singleton(testFile));
                ClasspathInfo cpi = RetoucheUtils.getClasspathInfoFor(TreePathHandle.create(klass, controller));
                moveRef[0].getContext().add(cpi);
            }
        }, false);
        RefactoringSession rs = RefactoringSession.create("Session");

        File f = FileUtil.toFile(getProjectDir().getFileObject("/src/org/gjt/sp"));
        moveRef[0].setTarget(Lookups.singleton(f.toURI().toURL()));
        moveRef[0].prepare(rs);
        rs.doRefactoring(true);
        Collection<RefactoringElement> elems = rs.getRefactoringElements();
        StringBuilder sb = new StringBuilder();
                sb.append("Symbol: '").append("java.lang.Runnable").append("'");
        sb.append('\n').append("Number of usages: ").append(elems.size()).append('\n');
        try {
            long prepare = getHandler().get("refactoring.prepare");
            NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
            d.name = "refactoring.prepare"+" (" + "java.lang.Runnable" + ", usages:" + elems.size() + ")";
            d.value = prepare;
            d.unit = "ms";
            d.runOrder = 0;
            sb.append("Prepare phase: ").append(prepare).append(" ms.\n");
            Utilities.processUnitTestsResults(FindUsagesPerfTest.class.getCanonicalName(), d);
            System.err.println("Time: " + prepare);
        } catch (Exception ex) {
            sb.append("Cannot collect usages: ").append(ex.getCause());
        }
        getLog().append(sb);
        System.err.println(sb);

        src = null;
        moveRef[0] = null;
        System.gc(); System.gc();
        
        Log.assertInstances("Some instances of parser were not GCed");
    }

    public static Test suite() throws InterruptedException {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().addTest(MoveClassPerfTest.class, "testRenamePackage").gui(true));
    }
}