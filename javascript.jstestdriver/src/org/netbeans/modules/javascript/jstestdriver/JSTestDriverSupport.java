/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.jstestdriver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.libs.jstestdriver.api.JsTestDriver;
import org.netbeans.libs.jstestdriver.api.ServerListener;
import org.netbeans.libs.jstestdriver.api.TestListener;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowserPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

@Messages("JsTestDriverResolver=js-test-driver Conf Files")
@MIMEResolver.Registration(
    displayName="#JsTestDriverResolver",
    position=117, // has to be before languages.apachecon resolver
    resource="resources/mime-resolver.xml"
)
public class JSTestDriverSupport {

    private static JSTestDriverSupport def;
    private static final Logger LOGGER = Logger.getLogger(JSTestDriverSupport.class.getName());
    private RequestProcessor RP = new RequestProcessor("js-test-driver server", 5);
    private AbstractLookup projectContext;
    private InstanceContent lookupContent;
    
    public static synchronized JSTestDriverSupport getDefault() {
        if (def == null) {
            def = new JSTestDriverSupport();
        }
        return def;
    }

    private JsTestDriver testDriver;
    private boolean starting = false;
    
    private JSTestDriverSupport() {
        lookupContent = new InstanceContent();
        projectContext = new AbstractLookup(lookupContent);
    }

    public JsTestDriver getJsTestDriver() {
        if (testDriver == null) {
            if (!isConfiguredProperly()) {
                if (!configure()) {
                    return null;
                }
            }
            String jsTestDriverJar = JSTestDriverCustomizerPanel.getJSTestDriverJar();
            File f = new File(jsTestDriverJar);
            try {
                testDriver = new JsTestDriver(f);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.INFO, "cannot access js-test-driver wrapper", ex);
                return null;
            }
        }
        return testDriver;
    }
    
    
    public String getUserDescription() {
        if (wasStartedExternally()) {
            return "Server was started externally on port "+JSTestDriverCustomizerPanel.getPort()+". IDE cannot manage this instance.";
        } else if (isRunning()) {
            return "Running on port "+JSTestDriverCustomizerPanel.getPort();
        } else {
            return "Not running";
        }
    }

    public boolean isRunning() {
        JsTestDriver td = testDriver;
        return (td != null && td.isRunning());
    }

    public boolean wasStartedExternally() {
        JsTestDriver td = testDriver;
        return (td != null && td.wasStartedExternally());
    }

    public boolean isStarting() {
        return starting;
    }

    public void stop() {
        assert isRunning();
        JsTestDriver td = testDriver;
        if (td != null && td.isRunning()) {
            td.stopServer();
            // reset server configuration:
            testDriver = null;
        }
        TestDriverServiceNode.getInstance().refresh();
    }

    public void start(final ServerListener l) {
        assert !isRunning();
        JsTestDriver td = testDriver;
        assert td == null;
        td = getJsTestDriver();
        if (!isConfiguredProperly()) {
            return;
        }
        if (td == null) {
            DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                    "js-test-driver server could not be started. See IDE log for more details."));
            return;
        }
        final JsTestDriver td2 = td;
        starting = true;
        TestDriverServiceNode.getInstance().refresh();
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    td2.startServer(JSTestDriverCustomizerPanel.getPort(), 
                            JSTestDriverCustomizerPanel.isStricModel(),
                            new ServerListener() {

                        @Override
                        public void serverStarted() {
                            RP.post(new Runnable() {
                                @Override
                                public void run() {
                                    captureBrowsers();
                                    if (l != null) {
                                        l.serverStarted();
                                    }
                                    starting = false;
                                    TestDriverServiceNode.getInstance().refresh();
                                }
                            });
                        }

                    });
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "cannot start server", t);
                }
            }
        });
    }
    
    public boolean configure() {
        final boolean[] res = new boolean[1];
        Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean b = JSTestDriverCustomizerPanel.showCustomizer();
                res[0] = b;
            }
        };
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                // it should be safe to call invokeAndWait here
                // as this action will be either called from AWT thread or from
                // RequestProcessor as a result of "test project" or "start server" in
                // case when configuration is missing the first time:
                SwingUtilities.invokeAndWait(r);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return res[0];
    }

    public boolean isConfiguredProperly() {
        return JSTestDriverCustomizerPanel.isConfiguredProperly();
    }

    public void runAllTests(Project project, int port, boolean strictMode, File baseFolder, File configFile, 
            String testsToRun) {
        if (!isRunning()) {
            final Semaphore s = new Semaphore(0);
            start(new ServerListener() {
                @Override
                public void serverStarted() {
                    try {
                        // give browsers some time to start and connect to server
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    s.release();
                }
            });
            try {
                s.acquire();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (!isRunning()) {
                return;
            }
        }
        updateJsDebuggerProjectContext(project);
        TestListener listener = new Listener(project);
        JsTestDriver td = testDriver;
        td.runTests(port, strictMode, baseFolder, configFile, testsToRun, listener);
    }
    
    private void updateJsDebuggerProjectContext(Project p) {
        // update lookup used by JS debugger with the right project 
        lookupContent.set(Collections.singletonList(p), null);
    }

    private void captureBrowsers() {
        for (JSTestDriverCustomizerPanel.WebBrowserDesc bd : JSTestDriverCustomizerPanel.getBrowsers()) {
            String s = JSTestDriverCustomizerPanel.getServerURL()+"/capture";
            if (bd.nbIntegration) {
                // '/timeout/-1/' - will prevent js-test-driver from timeouting the test
                //   when test execution takes too much time, for example when test is being debugged
                s += "/timeout/-1/";
            }
            if (JSTestDriverCustomizerPanel.isStricModel()) {
                s += "?strict";
            }
            try {
                URL u = new URL(s);
                WebBrowserPane pane = bd.browser.createNewBrowserPane(true, false);
                pane.disablePageInspector();
                // the problem here is following: js-test-driver is a global server
                // which does not have any project specific context. But in order to
                // debug JavaScript the JS debugger needs a project context in order
                // to correctly resolve breakpoints etc. So when server is started
                // there will not be any project context; only when a test is 
                // executed from a project the project context will be set for JS debugger by
                // updating the lookup; JS debugger listens on lookup changes
                pane.setProjectContext(projectContext);
                pane.showURL(u);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private static class Listener implements TestListener {

        private TestSession testSession;
        private Manager manager;
        private Report report;

        public Listener(Project project) {
            manager = Manager.getInstance();
            testSession = new TestSession(ProjectUtils.getInformation(project).getDisplayName() + " Testing", project, TestSession.SessionType.TEST);
            manager.testStarted(testSession);
        }
        
        @Override
        public void onTestComplete(TestResult testResult) {
            TestSuite currentSuite = testSession.getCurrentSuite();
            if (currentSuite == null || !currentSuite.getName().equals(testResult.getBrowserInfo().getDisplayName())) {
                if (report != null) {
                    manager.displayReport(testSession, report, true);
                }
                TestSuite ts = new TestSuite(testResult.getBrowserInfo().getDisplayName());
                testSession.addSuite(ts);
                report = testSession.getReport(0);
                manager.displaySuiteRunning(testSession, ts.getName());
            }
            Testcase testCase = new Testcase(testResult.getTestCaseName(), null, testSession);
            testCase.setStatus(convert(testResult.getResult()));
            testCase.setTimeMillis(testResult.getDuration());
            if (testResult.getResult() == TestResult.Result.failed || testResult.getResult() == TestResult.Result.error) {
                Trouble t = new Trouble(true);
                if (testResult.getStack().length() > 0) {
                    t.setStackTrace(testResult.getStack().split("\\u000d"));
                    testCase.addOutputLines(Arrays.asList(testResult.getStack().split("\\u000d")));
                    //manager.displayOutput(testSession, testResult.getStack(), true);
                }
                if (testResult.getMessage().length() > 0) {
                    //manager.displayOutput(testSession, testResult.getMessage(), true);
                }
                if (testResult.getLog().length() > 0) {
                    //manager.displayOutput(testSession, testResult.getLog(), true);
                }
                testCase.setTrouble(t);
            }
            testSession.addTestCase(testCase);
            report.update(testSession.getReport(0));
            manager.displayReport(testSession, report, false);
        }

        private Status convert(TestResult.Result res) {
            switch (res) {
                case passed:
                    return Status.PASSED;
                case failed:
                    return Status.FAILED;
                case error:
                    return Status.ERROR;
                case started:
                    return Status.PENDING;
                default:
                    throw new AssertionError(res.name());
            }
        }
        
        @Override
        public void onTestingFinished() {
            manager.sessionFinished(testSession);
            if (report == null) {
                // no tests were run; generate empty report:
                testSession.addSuite(TestSuite.ANONYMOUS_TEST_SUITE);
                report = testSession.getReport(0);
            }
            manager.displayReport(testSession, report, true);
        }
        
    }

}
