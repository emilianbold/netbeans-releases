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

package org.netbeans.modules.php.project.ui.actions.support;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.gsf.testrunner.api.RerunType;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpActionProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.testrunner.ControllableRerunHandler;
import org.netbeans.modules.php.project.ui.testrunner.UnitTestRunner;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 * Action implementation for TEST configuration.
 * It means running and debugging tests.
 * @author Tomas Mysik
 */
class ConfigActionTest extends ConfigAction {

    protected ConfigActionTest(PhpProject project) {
        super(project);
    }

    protected FileObject getTestDirectory(boolean showCustomizer) {
        return ProjectPropertiesSupport.getTestDirectory(project, showCustomizer);
    }

    @Override
    public boolean isProjectValid() {
        throw new IllegalStateException("Validation is not needed for tests");
    }

    @Override
    public boolean isFileValid() {
        throw new IllegalStateException("Validation is not needed for tests");
    }

    @Override
    public boolean isDebugProjectEnabled() {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public boolean isRunFileEnabled(Lookup context) {
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context);
        return file != null && FileUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(Lookup context) {
        if (XDebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(context);
    }

    @Override
    public void runProject() {
        // first, let user select test directory
        FileObject testDirectory = getTestDirectory(true);
        if (testDirectory == null) {
            return;
        }
        TestRunInfo testRunInfo = getTestRunInfo(null, false);
        assert testRunInfo != null;
        run(testRunInfo);
    }

    @Override
    public void debugProject() {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public void runFile(final Lookup context) {
        TestRunInfo testRunInfo = getTestRunInfo(context, false);
        if (testRunInfo == null) {
            // XXX
            return;
        }
        run(testRunInfo);
    }

    @Override
    public void debugFile(final Lookup context) {
        TestRunInfo testRunInfo = getTestRunInfo(context, false);
        if (testRunInfo == null) {
            // XXX
            return;
        }
        run(testRunInfo);
    }

    void run(final TestRunInfo testRunInfo) {
        new UnitTestRunner(project, testRunInfo, new RerunUnitTestHandler(testRunInfo))
                .run();
    }

    @CheckForNull
    private TestRunInfo getTestRunInfo(Lookup context, boolean debug) {
        if (context == null) {
            // run project
            FileObject testDirectory = getTestDirectory(true);
            if (testDirectory == null) {
                return null;
            }
            return getProjectTestRunInfo(testDirectory, debug);
        }
        return getFileTestRunInfo(context, debug);
    }

    @CheckForNull
    private TestRunInfo getProjectTestRunInfo(FileObject testDirectory, boolean debug) {
        if (debug) {
            return TestRunInfo.debug(testDirectory, testDirectory, null);
        }
        return TestRunInfo.test(testDirectory, testDirectory, null);
    }

    @CheckForNull
    private TestRunInfo getFileTestRunInfo(Lookup context, boolean debug) {
        assert context != null;

        FileObject fileObj = CommandUtils.fileForContextOrSelectedNodes(context);
        assert fileObj != null : "Fileobject not found for context: " + context;
        if (!fileObj.isValid()) {
            return null;
        }
        final FileObject workDir;
        final String name;
        if (fileObj.isFolder()) {
            // #195525 - run tests in folder
            workDir = fileObj;
            name = fileObj.getNameExt();
        } else {
            workDir = fileObj.getParent();
            name = fileObj.getName();
        }
        if (debug) {
            return TestRunInfo.debug(workDir, fileObj, name);
        }
        return TestRunInfo.test(workDir, fileObj, name);
    }

    //~ Inner classes

    private final class RerunUnitTestHandler implements ControllableRerunHandler {

        private final TestRunInfo info;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private volatile boolean enabled = false;


        public RerunUnitTestHandler(TestRunInfo info) {
            assert info != null;
            this.info = info;
        }

        @Override
        public void rerun() {
            info.setRerun(true);
            PhpActionProvider.submitTask(new Runnable() {
                @Override
                public void run() {
                    ConfigActionTest.this.run(info);
                }
            });
        }

        @Override
        public void rerun(Set<Testcase> tests) {
            info.setCustomTests(map(tests));
            rerun();
        }

        @Override
        public boolean enabled(RerunType type) {
            boolean supportedType = false;
            switch (type) {
                case ALL:
                case CUSTOM:
                    supportedType = true;
                    break;
                default:
                    assert false : "Unknown RerunType: " + type;
                    break;
            }
            return supportedType && enabled;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public void enable() {
            if (!enabled) {
                enabled = true;
                changeSupport.fireChange();
            }
        }

        @Override
        public void disable() {
            if (enabled) {
                enabled = false;
                changeSupport.fireChange();
            }
        }

        //~ Mappers

        private Collection<TestRunInfo.TestInfo> map(Set<Testcase> tests) {
            Set<TestRunInfo.TestInfo> testCases = new HashSet<TestRunInfo.TestInfo>();
            for (Testcase test : tests) {
                testCases.add(new TestRunInfo.TestInfo(test.getType(), test.getName(), test.getClassName(), test.getLocation()));
            }
            return testCases;
        }

    }

}
