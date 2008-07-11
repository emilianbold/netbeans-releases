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

package org.netbeans.modules.ruby.rubyproject.rake;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner.TestType;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Hooks into the rake runner for invoking the UI test runner when 
 * running rake tasks for testing, such as spec, test, test:units and 
 * test:functionals.
 *
 * @author Erno Mononen
 */
class TestTaskRunner {
    private static final String SPEC = "spec"; //NOI18N
    
    private final Project project;
    private final boolean debug;
    private TestRunner rspecRunner;
    private TestRunner testUnitRunner;
    private String task;
    /**
     * Maps test:* task names to folder names, i.e. to folders
     * that contain the tests that should be run by the task.
     */
    private final Map<String, String> taskToFolder = initTaskToFolder();

    TestTaskRunner(Project project, boolean debug) {
        assert project != null;
        this.project = project;
        this.debug = debug;
        this.rspecRunner = getTestRunner(TestType.RSPEC);
        this.testUnitRunner = getTestRunner(TestType.TEST_UNIT);
    }
    
    private static Map<String, String> initTaskToFolder() {
        Map<String, String> result = new HashMap<String, String>(3);
        result.put("test", "test/");//NOI18N
        result.put("test:units", "test/unit");//NOI18N
        result.put("test:functionals", "test/functional");//NOI18N
        result.put("test:integration", "test/integration");//NOI18N
        return result;
    }

    private boolean isSupportedTestTask(String task) {
        return taskToFolder.containsKey(task);
    }
    
    /**
     * Filters the list of rake tasks so that test tasks are removed (they will be 
     * run by the UI test runner instead. Also adds the db:test:prepare task
     * to the returned list if needed (it needs to run by the regular rake 
     * runner before running test:* tasks with the UI test runner).
     * 
     * @param tasks
     * @return
     */
    List<RakeTask> filter(final List<RakeTask> tasks) {
        // XXX does not properly handle cases when there are both spec and test tasks
        // in the passed task list -- that should not happen though

        List<RakeTask> result = new ArrayList<RakeTask>(tasks);
        for (Iterator<RakeTask> it = result.iterator(); it.hasNext();) {
            if (SPEC.equals(it.next().getTask()) && rspecRunner != null) {
                it.remove();
                task = SPEC;
                return result;
            }
        }

        int testTaskIndex = -1;
        for (int i = 0; i < tasks.size(); i++) {
            String taskName = tasks.get(i).getTask();
            if (isSupportedTestTask(taskName) && testUnitRunner != null) {
                testTaskIndex = i;
                task = taskName;
                break;
            }
        }
        
        if (testTaskIndex != -1) {
            RakeTask dbTestPrepare = RakeSupport.getRakeTask(project, "db:test:prepare"); //NOI18N
            if (dbTestPrepare != null) {
                result.set(testTaskIndex, dbTestPrepare);
            } else {
                result.remove(testTaskIndex);
            }
        }
        
        return result;
    }
    
    boolean needsPostRun() {
        return task != null;
    }
    
    void postRun() {
        if (task == null) {
            return;
        }
        
        if (SPEC.equals(task)) {
            rspecRunner.runAllTests(project, debug);
            return;
        }
        
        FileObject testFolder = project.getProjectDirectory().getFileObject(taskToFolder.get(task));
        testUnitRunner.runTest(testFolder, debug);
    }
    
    
    private static TestRunner getTestRunner(TestRunner.TestType testType) {
        Collection<? extends TestRunner> testRunners = Lookup.getDefault().lookupAll(TestRunner.class);
        for (TestRunner each : testRunners) {
            if (each.supports(testType)) {
                return each;
            }
        }
        return null;
    }

}
