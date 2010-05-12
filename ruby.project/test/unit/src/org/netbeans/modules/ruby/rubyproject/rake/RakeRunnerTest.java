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

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.RubyProjectTestBase;

public class RakeRunnerTest extends RubyProjectTestBase {

    public RakeRunnerTest(String testName) {
        super(testName);
    }

    public void testDoStandardConfiguration() throws Exception {
        RubyProject project = createTestProject();

        RakeRunner runner = new RakeRunner(project);
        runner.setPWD(getWorkDir());
        runner.showWarnings(true);
        runner.run("clean");
    }

    public void testBuildExecutionDescriptorsNoParams() throws Exception {
        RubyProject project = createTestProject();
        RakeRunner rakeRunner = new RakeRunner(project);
        RakeTask task1 = new RakeTask("atask", "A Task", "This is a task");
        RakeTask task2 = new RakeTask("anothertask", "Another Task", "This is another task");
        List<RubyExecutionDescriptor> executionDescriptors =
                rakeRunner.getDescriptors(Arrays.asList(task1, task2));

        RubyExecutionDescriptor task1Descriptor = executionDescriptors.get(0);
        assertEquals(0, task1Descriptor.getInitialArgs().length);
        assertEquals(1, task1Descriptor.getAdditionalArgs().length);
        assertEquals("atask", task1Descriptor.getAdditionalArgs()[0]);

        RubyExecutionDescriptor task2Descriptor = executionDescriptors.get(1);
        assertEquals(0, task2Descriptor.getInitialArgs().length);
        assertEquals(1, task2Descriptor.getAdditionalArgs().length);
        assertEquals("anothertask", task2Descriptor.getAdditionalArgs()[0]);

    }

    public void testBuildExecutionDescriptorsWithParams() throws Exception {
        RubyProject project = createTestProject();
        RakeRunner rakeRunner = new RakeRunner(project);
        RakeTask task1 = new RakeTask("atask", "A Task", "This is a task");
        task1.addTaskParameters("ATASK_PARAM=param");
        RakeTask task2 = new RakeTask("anothertask", "Another Task", "This is another task");
        task2.addTaskParameters("PARAM1=1", "PARAM2=2");
        List<RubyExecutionDescriptor> executionDescriptors =
                rakeRunner.getDescriptors(Arrays.asList(task1, task2));

        RubyExecutionDescriptor task1Descriptor = executionDescriptors.get(0);
        assertEquals(0, task1Descriptor.getInitialArgs().length);
        assertEquals(2, task1Descriptor.getAdditionalArgs().length);
        assertEquals("atask", task1Descriptor.getAdditionalArgs()[0]);
        assertEquals("ATASK_PARAM=param", task1Descriptor.getAdditionalArgs()[1]);

        RubyExecutionDescriptor task2Descriptor = executionDescriptors.get(1);
        assertEquals(0, task2Descriptor.getInitialArgs().length);
        assertEquals(3, task2Descriptor.getAdditionalArgs().length);
        assertEquals("anothertask", task2Descriptor.getAdditionalArgs()[0]);
        assertEquals("PARAM1=1", task2Descriptor.getAdditionalArgs()[1]);
        assertEquals("PARAM2=2", task2Descriptor.getAdditionalArgs()[2]);

    }

    public void testBuildExecutionDescriptorsWithRakeRunnerParams() throws Exception {
        RubyProject project = createTestProject();
        RakeRunner rakeRunner = new RakeRunner(project);
        rakeRunner.setParameters("RAKE_RUNNER_PARAM=mama");

        RakeTask noTaskParamsTask = new RakeTask("atask", "A Task", "This is a task");
        RakeTask taskWithTaskParams = new RakeTask("anothertask", "Another Task", "This is another task");
        taskWithTaskParams.addTaskParameters("TASK_PARAM1=1", "TASK_PARAM2=2");
        List<RubyExecutionDescriptor> executionDescriptors =
                rakeRunner.getDescriptors(Arrays.asList(noTaskParamsTask, taskWithTaskParams));

        RubyExecutionDescriptor task1Descriptor = executionDescriptors.get(0);
        assertEquals(0, task1Descriptor.getInitialArgs().length);
        assertEquals(2, task1Descriptor.getAdditionalArgs().length);
        assertEquals("atask", task1Descriptor.getAdditionalArgs()[0]);
        assertEquals("RAKE_RUNNER_PARAM=mama", task1Descriptor.getAdditionalArgs()[1]);

        RubyExecutionDescriptor task2Descriptor = executionDescriptors.get(1);
        assertEquals(0, task2Descriptor.getInitialArgs().length);
        assertEquals(4, task2Descriptor.getAdditionalArgs().length);
        assertEquals("anothertask", task2Descriptor.getAdditionalArgs()[0]);
        assertEquals("RAKE_RUNNER_PARAM=mama", task1Descriptor.getAdditionalArgs()[1]);
        assertEquals("TASK_PARAM1=1", task2Descriptor.getAdditionalArgs()[2]);
        assertEquals("TASK_PARAM2=2", task2Descriptor.getAdditionalArgs()[3]);

    }

    public void testBuildExecutionDescriptorsWithInitialParams() throws Exception {
        RubyProject project = createTestProject();
        RakeRunner rakeRunner = new RakeRunner(project);
        rakeRunner.setParameters("RAKE_RUNNER_PARAM=mama");

        RakeTask task = new RakeTask("atask", "A Task", "This is a task");
        task.addRakeParameters("-r/path/to/nowhere.rb");
        task.addTaskParameters("PARAM=value");
        List<RubyExecutionDescriptor> executionDescriptors =
                rakeRunner.getDescriptors(Arrays.asList(task));

        RubyExecutionDescriptor taskDescriptor = executionDescriptors.get(0);
        assertEquals(1, taskDescriptor.getInitialArgs().length);
        assertEquals("-r/path/to/nowhere.rb", taskDescriptor.getInitialArgs()[0]);
        assertEquals(3, taskDescriptor.getAdditionalArgs().length);
        assertEquals("atask", taskDescriptor.getAdditionalArgs()[0]);
        assertEquals("RAKE_RUNNER_PARAM=mama", taskDescriptor.getAdditionalArgs()[1]);
        assertEquals("PARAM=value", taskDescriptor.getAdditionalArgs()[2]);

        RakeRunner runner = new RakeRunner(project);
        runner.setPWD(getWorkDir());
        runner.showWarnings(true);
        runner.run("clean");
    }

}
