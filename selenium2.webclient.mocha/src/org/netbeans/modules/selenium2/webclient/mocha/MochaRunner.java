/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.mocha;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.selenium2.api.Utils;
import org.netbeans.modules.selenium2.webclient.api.RunInfo;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProviders;
import org.netbeans.modules.selenium2.webclient.api.Utilities;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaJSPreferences;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaSeleniumPreferences;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaPreferencesValidator;
import org.netbeans.modules.selenium2.webclient.mocha.run.MochaRerunHandler;
import org.netbeans.modules.selenium2.webclient.mocha.run.MochaTestRunner;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.common.api.ExternalExecutable;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Theofanis Oikonomou
 */
public class MochaRunner {
    private static final Logger LOG = Logger.getLogger(MochaRunner.class.getName());
    
    public static void runTests(FileObject[] activatedFOs, boolean isSelenium) {
        assert !EventQueue.isDispatchThread();
        final Project p = FileOwnerQuery.getOwner(activatedFOs[0]);
        if (p == null) {
            return;
        }
        
        File mochaNBReporter = InstalledFileLocator.getDefault().locate(
                    "mocha/netbeans-reporter.js", "org.netbeans.modules.selenium2.webclient.mocha", false); // NOI18N
        if(mochaNBReporter == null) {
            return;
        }
        
        String mochaInstallFolder = MochaSeleniumPreferences.getMochaDir(p);
        ValidationResult validationResult = new MochaPreferencesValidator()
                .validateMochaInstallFolder(mochaInstallFolder)
                .getResult();
        if(mochaInstallFolder == null || mochaInstallFolder.isEmpty() || !validationResult.isFaultless()) {
            Utilities.openCustomizer(p, SeleniumTestingProviders.CUSTOMIZER_SELENIUM_TESTING_IDENT);
            return;
        }
        
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(mochaInstallFolder + "/bin/mocha");
        if(!isSelenium && MochaJSPreferences.isAutoWatch(p)) {
            arguments.add("-w");
        }
        arguments.add("-t");
        arguments.add(isSelenium ? Integer.toString(MochaSeleniumPreferences.getTimeout(p)) : Integer.toString(MochaJSPreferences.getTimeout(p)));
        arguments.add("-R");
        arguments.add(mochaNBReporter.getPath());
        
        FileObject testsFolder = isSelenium ? Utilities.getTestsSeleniumFolder(p, true) : Utilities.getTestsFolder(p, true);
        if(testsFolder == null) {
            Utilities.openCustomizer(p, WebClientProjectConstants.CUSTOMIZER_SOURCES_IDENT);
            return;
        }

        boolean testProject = activatedFOs.length == 1 && activatedFOs[0].equals(p.getProjectDirectory());
        RunInfo.Builder builder = new RunInfo.Builder(activatedFOs).setTestingProject(testProject)
                .addEnvVar("MOCHA_DIR", mochaInstallFolder)
                .setRerunHandler(new MochaRerunHandler(p, activatedFOs, isSelenium))
                .setIsSelenium(isSelenium);
        if(activatedFOs.length == 1 && !activatedFOs[0].equals(p.getProjectDirectory())) {
            String testFile = FileUtil.getRelativePath(testsFolder, activatedFOs[0]);
            builder = builder.setTestFile(testFile);
        }
        
        final RunInfo runInfo = builder.build();
        
        if(testProject) {
            if(isSelenium) {
                String testFolder = FileUtil.getRelativePath(p.getProjectDirectory(), testsFolder);
                arguments.add(testFolder + "/**/*.js");
            }
        } else {
            ArrayList<String> files2test = new ArrayList<>();
            for(FileObject fo : activatedFOs) {
                String file2test = FileUtil.getRelativePath(p.getProjectDirectory(), fo);
                if(file2test != null) {
                    if(!files2test.contains(file2test)) {
                        files2test.add(file2test);
                    }
                }
            }
            for(String file2test : files2test) {
                arguments.add(file2test);
            }
        }

        String displayname = ProjectUtils.getInformation(runInfo.getProject()).getDisplayName() + (isSelenium ? " Selenium" : " Unit") + " Tests";
        final ExternalExecutable externalexecutable = new ExternalExecutable("/usr/local/bin/node")
                .workDir(FileUtil.toFile(p.getProjectDirectory()))
                .displayName(displayname)
                .additionalParameters(arguments)
                .environmentVariables(runInfo.getEnvVars());
        final MochaTestRunner testRunner = new MochaTestRunner(runInfo);

        ExecutionDescriptor.LineConvertorFactory outputLineConvertorFactory = new ExecutionDescriptor.LineConvertorFactory() {
            @Override
            public LineConvertor newLineConvertor() {
                return new OutputLineConvertor(testRunner, runInfo);
            }
        };

        final ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .frontWindow(true)
                .controllable(true)
                .showProgress(true)
                .showSuspended(true)
                .outLineBased(true)
                .errLineBased(true)
                .outConvertorFactory(outputLineConvertorFactory);

        Future<Integer> run = externalexecutable.run(descriptor);
    }
    
    private static class OutputLineConvertor implements LineConvertor {
        private final MochaTestRunner testRunner;
        private final RunInfo runInfo;

        public OutputLineConvertor(MochaTestRunner testRunner, RunInfo runInfo) {
            this.testRunner = testRunner;
            this.runInfo = runInfo;
        }

        @Override
        public List<ConvertedLine> convert(String line) {
            String output2display = testRunner.processLine(line);
            MochaTestRunner.CallStackCallback callStackCallback = new MochaTestRunner.CallStackCallback(runInfo.getProject());
            Pair<File, int[]> parsedLocation = callStackCallback.parseLocation(line, false);
            FileOutputListener fileOutputListener = parsedLocation == null ? null : new FileOutputListener(parsedLocation.first(), parsedLocation.second()[0], parsedLocation.second()[1]);
            return Collections.singletonList(ConvertedLine.forText(output2display, fileOutputListener));
        }
    }
    
    private static final class FileOutputListener implements OutputListener {

        final File file;
        final int line;
        final int column;

        public FileOutputListener(File file, int line, int column) {
            assert file != null;
            this.file = file;
            this.line = line;
            this.column =column;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
            // noop
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    Utils.openFile(file, line, column);
                }
            });
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
            // noop
        }
    }
    
}
