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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner;
import org.netbeans.modules.ruby.testrunner.ui.AutotestHandlerFactory;
import org.netbeans.modules.ruby.testrunner.ui.Manager;
import org.netbeans.modules.ruby.testrunner.ui.TestRecognizer;
import org.netbeans.modules.ruby.testrunner.ui.TestSession;
import org.netbeans.modules.ruby.testrunner.ui.TestSession.SessionType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * Handles running the autotest command, hooks its execution with the
 * UI test runner.
 *
 * @author Erno Mononen
 */
public class AutotestRunner implements TestRunner {

    private static final Logger LOGGER = Logger.getLogger(AutotestRunner.class.getName());
    
    private static final TestRunner INSTANCE = new AutotestRunner();

    private static final String NB_RSPEC_MEDIATOR = "NB_RSPEC_MEDIATOR"; //NOI18N
    private static final String RSPEC_AUTOTEST_LOADER = "nb_autotest_loader.rb"; //NOI18N

    
    public AutotestRunner() {
    }

    public TestRunner getInstance() {
        return INSTANCE;
    }

    public boolean supports(TestType type) {
        return TestType.AUTOTEST == type;
    }

    public void runTest(FileObject testFile, boolean debug) {
        throw new UnsupportedOperationException("Not supported."); //NOI18N
    }

    public void runSingleTest(FileObject testFile, String testMethod, boolean debug) {
        throw new UnsupportedOperationException("Not supported."); //NOI18N
    }

    public void runAllTests(Project project, boolean debug) {

        RubyPlatform platform = RubyPlatform.platformFor(project);
        if (!platform.hasValidAutoTest(true)) {
            return;
        }

        String displayName = NbBundle.getMessage(AutotestRunner.class, "AutoTest", ProjectUtils.getInformation(project).getDisplayName());
        FileLocator locator = project.getLookup().lookup(FileLocator.class);

        RubyExecutionDescriptor desc = new RubyExecutionDescriptor(platform,
                displayName,
                FileUtil.toFile(project.getProjectDirectory()),
                platform.getAutoTest());

        desc.initialArgs("-r \"" + getLoaderScript().getAbsolutePath() + "\""); //NOI18N
        Map<String, String> env = new HashMap<String, String>(2);
        AutotestRunner.addRspecMediatorOptionsToEnv(env);
        TestUnitRunner.addTestUnitRunnerToEnv(env);
        desc.addAdditionalEnv(env);
        desc.debug(debug);
        desc.allowInput();
        desc.fileLocator(locator);
        desc.addStandardRecognizers();
        desc.setReadMaxWaitTime(TestUnitRunner.DEFAULT_WAIT_TIME);

        TestSession session = new TestSession(displayName,
                project,
                debug ? SessionType.DEBUG : SessionType.TEST);
        TestRecognizer recognizer = new TestRecognizer(Manager.getInstance(),
                AutotestHandlerFactory.getHandlers(),
                session,
                true);
        TestExecutionManager.getInstance().start(desc, recognizer);
    }
    
    private static void addRspecMediatorOptionsToEnv(Map<String, String> env) {
        // referenced from nb_autotest_loader.rb
        String options = "--require '"
                + RspecRunner.getMediatorScript().getAbsolutePath()
                + "' --runner NbRspecMediator";//NOI18N
        
        env.put(NB_RSPEC_MEDIATOR, options);
    }

    private static File getLoaderScript() {
        File mediatorScript = InstalledFileLocator.getDefault().locate(
                RSPEC_AUTOTEST_LOADER, "org.netbeans.modules.ruby.testrunner", false);  // NOI18N

        if (mediatorScript == null) {
            throw new IllegalStateException("Could not locate " + RSPEC_AUTOTEST_LOADER); // NOI18N

        }
        return mediatorScript;
    }

}
