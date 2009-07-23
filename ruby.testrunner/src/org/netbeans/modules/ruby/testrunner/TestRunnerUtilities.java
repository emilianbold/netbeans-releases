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

package org.netbeans.modules.ruby.testrunner;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.rake.RakeTask;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;

/**
 * Utility methods for <code>TestRunner</code> implementations.
 *
 * @author Erno Mononen
 */
public final class TestRunnerUtilities {

    /**
     * Class path for tests under JRuby. If not specified, {@link SharedRubyProjectProperties#JAVAC_CLASSPATH} is
     * used.
     */
    private static final String TEST_JAVAC_CLASSPATH = "test.javac.classpath"; // NOI18N
    /**
     * JVM args for tests under JRuby. If not specified, {@link SharedRubyProjectProperties#JVM_ARGS} is
     * used.
     */
    private static final String TEST_JVM_ARGS = "test.jvm.args"; // NOI18N

    private static final List<String> NB_RUNNER_FILES = Arrays.asList(TestUnitRunner.MEDIATOR_SCRIPT_NAME,
            TestUnitRunner.TEST_RUNNER_SCRIPT_NAME, TestUnitRunner.SUITE_RUNNER_SCRIPT_NAME,
            RspecRunner.RSPEC_MEDIATOR_SCRIPT, AutotestRunner.AUTOTEST_LOADER);

    private TestRunnerUtilities() {
    }

    /**
     * Checks whether the given task should be run using the UI test runner.
     * 
     * @param project
     * @param property
     * @param task
     * @param taskEvaluator
     * @return true if the given task should be run using the UI test runner;
     * false otherwise.
     */
    static boolean useTestRunner(Project project, String property, RakeTask task, DefaultTaskEvaluator taskEvaluator) {
        PropertyEvaluator evaluator = project.getLookup().lookup(PropertyEvaluator.class);
        if (evaluator == null || evaluator.getProperty(property) == null) {
            return taskEvaluator.isDefault(task);
        }
        String definedTasks = evaluator.getProperty(property);
        if ("".equals(definedTasks.trim())) {
            return false;
        }
        for (String each : definedTasks.split(",")) { //NOI18N
            if (task.getTask().equals(each.trim())) {
                return true;
            }
        }
        return false;
    }
    

    /**
     * Adds the standard project properties (e.g. encoding, class path etc.) to the
     * given descriptor.
     * 
     * @param descriptor
     * @param project
     */
    static void addProperties(RubyExecutionDescriptor descriptor, Project project) {
        PropertyEvaluator evaluator = project.getLookup().lookup(PropertyEvaluator.class);
        if (evaluator == null) {
            return;
        }
        descriptor.addInitialArgs(evaluator.getProperty(SharedRubyProjectProperties.RUBY_OPTIONS));
        descriptor.setEncoding(evaluator.getProperty(SharedRubyProjectProperties.SOURCE_ENCODING));

        String classPath = evaluator.getProperty(TEST_JAVAC_CLASSPATH);
        if (classPath == null) {
            classPath = evaluator.getProperty(SharedRubyProjectProperties.JAVAC_CLASSPATH);
        }
        descriptor.classPath(classPath);

        String jvmArgs = evaluator.getProperty(TEST_JVM_ARGS);
        if (jvmArgs == null) {
            jvmArgs = evaluator.getProperty(SharedRubyProjectProperties.JVM_ARGS);
        }
        descriptor.jvmArguments(jvmArgs);
    }

    /**
     * @return true if the given line should be filtered out from the stack trace
     * printed to the output window.
     */
    public static boolean filterOutFromStacktrace(String line) {
        for (String runnerFile : NB_RUNNER_FILES) {
            if (line.contains(runnerFile)) {
                return true;
            }
        }
        return false;
    }

    interface DefaultTaskEvaluator {
        
        boolean isDefault(RakeTask task);
    }
    
}
