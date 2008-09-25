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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jiri Prox
 */
public class MoveClassPerfTest extends RefactoringTestCase implements NbPerformanceTest {

    private final MyHandler handler;
    private List<PerformanceData> data = new ArrayList<PerformanceData>();

    public MoveClassPerfTest(String name) {
        super(name);
        handler = new MyHandler();
        handler.setLevel(Level.FINE);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MoveClassPerfTest("testMoveIt"));
        return suite;
    }
    
    public void testMoveIt() throws Exception {
        Logger timer = Logger.getLogger("TIMER.RefactoringSession");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        timer = Logger.getLogger("TIMER.RefactoringPrepare");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        FileObject test = getFileInProject("jEdit41", "/src/org/gjt/sp/jedit/jEdit.java");
        FileObject target = getFileInProject("jEdit41","src/org" );
        final URL targetURL = target.getURL();
        final MoveRefactoring moveRefactoring = new MoveRefactoring(Lookups.fixed(test));
        perform(moveRefactoring,new ParameterSetter() {
            public void setParameters() {                
                moveRefactoring.setTarget(Lookups.singleton(targetURL));
            }
        });
        long prepare = handler.get("refactoring.prepare");
        long doIt = handler.get("refactoringSession.doRefactoring");
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = "refactoring.prepare";
        d.value = prepare;
        d.unit = "ms";
        d.runOrder = 0;
        data.add(d);
        d.name = "refactoringSession.doRefactoring";
        d.value = doIt;
        d.unit = "ms";
        d.runOrder = 0;
        System.err.println("usages collection: " + prepare);
        System.err.println("do refactoring: " + doIt);
    }
    
    public PerformanceData[] getPerformanceData() {
        return data.toArray(new PerformanceData[0]);
    }

    private static class MyHandler extends Handler {

        private Map<String, Long> map = new HashMap<String, Long>();

        @Override
        public void publish(LogRecord record) {
            Long data;

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
