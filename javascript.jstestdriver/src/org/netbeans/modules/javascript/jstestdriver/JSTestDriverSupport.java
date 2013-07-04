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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
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
import org.netbeans.modules.web.browser.api.WebBrowserFeatures;
import org.netbeans.modules.web.browser.api.WebBrowserPane;
import org.netbeans.modules.web.common.api.RemoteFileCache;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

@Messages("JsTestDriverResolver=js-test-driver Conf Files")
@MIMEResolver.Registration(
    displayName="#JsTestDriverResolver",
    position=117, // has to be before languages.apachecon resolver
    resource="resources/mime-resolver.xml"
)
public class JSTestDriverSupport {

    private static JSTestDriverSupport def;
    private static final Logger LOGGER = Logger.getLogger(JSTestDriverSupport.class.getName());
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.jstestdriver"); // NOI18N
    private RequestProcessor RP = new RequestProcessor("js-test-driver server", 5); //NOI18N
    private AbstractLookup projectContext;
    private InstanceContent lookupContent;
    private List<WebBrowserPane> integratedBrowserPanes;
    
    public static synchronized JSTestDriverSupport getDefault() {
        if (def == null) {
            def = new JSTestDriverSupport();
        }
        return def;
    }

    private JsTestDriver testDriver;
    private volatile boolean starting = false;
    
    private JSTestDriverSupport() {
        lookupContent = new InstanceContent();
        projectContext = new AbstractLookup(lookupContent);
    }

    private synchronized JsTestDriver getJsTestDriver() {
        if (testDriver == null) {
            if (!isConfiguredProperly()) {
                return null;
            }
            String jsTestDriverJar = JSTestDriverCustomizerPanel.getJSTestDriverJar();
            File f = new File(jsTestDriverJar);
            try {
                testDriver = new JsTestDriver(f);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.INFO, "cannot access js-test-driver wrapper", ex); //NOI18N
                return null;
            }
        }
        return testDriver;
    }
    
    
    public String getUserDescription() {
        if (JSTestDriverCustomizerPanel.getPort() == -1) {
            return NbBundle.getMessage(JSTestDriverSupport.class, "SERVER_EXTERNAL", JSTestDriverCustomizerPanel.getServerURL());
        } else if (wasStartedExternally()) {
            return NbBundle.getMessage(JSTestDriverSupport.class, "SERVER_EXTERNAL2", JSTestDriverCustomizerPanel.getPort());
        } else if (isRunning()) {
            return NbBundle.getMessage(JSTestDriverSupport.class, "SERVER_RUNNING", JSTestDriverCustomizerPanel.getServerURL());
        } else {
            return NbBundle.getMessage(JSTestDriverSupport.class, "SERVER_NOT_RUNNING");
        }
    }

    boolean isRunning() {
        return testDriver != null && testDriver.isRunning();
    }

    void forgetCurrentServer() {
        testDriver = null;
    }

    public boolean wasStartedExternally() {
        return testDriver != null && testDriver.wasStartedExternally();
    }

    public boolean isStarting() {
        return starting;
    }

    public void stop() {
        assert isRunning();
        assert testDriver != null;
        testDriver.stopServer();
        TestDriverServiceNode.getInstance().refresh();
        if (integratedBrowserPanes != null) {
            for (WebBrowserPane wbp : integratedBrowserPanes) {
                wbp.close(true);
            }
        }
    }

    public void start(final ServerListener l) {
        assert !isRunning();
        JsTestDriver td = getJsTestDriver();
        if (td == null) {
            if (configure()) {
                td = getJsTestDriver();
            }
        }
        if (td == null) {
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
                                    logUsage(JSTestDriverSupport.class, "USG_JSTESTDRIVER_STARTED", null); // NOI18N
                                }
                            });
                        }

                    });
                    if (td2.wasStartedExternally()) {
                        starting = false;
                        TestDriverServiceNode.getInstance().refresh();
                        if (l != null) {
                            l.serverStarted();
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "cannot start server", t); //NOI18N
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
                TestDriverServiceNode.getInstance().refresh();
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

    public void runAllTests(Project project, File baseFolder, File configFile, 
            String testsToRun) {
        JsTestDriver td = getJsTestDriver();
        if (td == null) {
            if (configure()) {
                td = getJsTestDriver();
            }
        }
        if (td == null) {
            return;
        }
        String serverURL = JSTestDriverCustomizerPanel.getServerURL();
        int port = JSTestDriverCustomizerPanel.getPort();
        boolean strictMode = JSTestDriverCustomizerPanel.isStricModel();
        if (!isRunning() && port != -1) {
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
        LineConvertor convertor = new LineConvertorImpl(project);
        td.runTests(serverURL, strictMode, baseFolder, configFile, testsToRun, listener, convertor);
    }
    
    private void updateJsDebuggerProjectContext(Project p) {
        // update lookup used by JS debugger with the right project 
        lookupContent.set(Collections.singletonList(p), null);
    }

    private void captureBrowsers() {
        integratedBrowserPanes = new ArrayList<WebBrowserPane>();        
        for (WebBrowser bd : JSTestDriverCustomizerPanel.getBrowsers()) {
            String s = JSTestDriverCustomizerPanel.getServerURL();
            // #230400 - use IP address instead of localhost so that mobile browsers testing works:
            if (bd.getBrowserFamily().isMobile() && s.startsWith("http://localhost:")) {
                s = s.replaceAll("localhost", WebUtils.getLocalhostInetAddress().getHostAddress());
            }
            s = s+"/capture"; //NOI18N
            if (bd.hasNetBeansIntegration()) {
                // '/timeout/-1/' - will prevent js-test-driver from timeouting the test
                //   when test execution takes too much time, for example when test is being debugged
                s += "/timeout/-1/"; //NOI18N
            }
            if (JSTestDriverCustomizerPanel.isStricModel()) {
                s += "?strict"; //NOI18N
            }
            try {
                URL u = new URL(s);
                WebBrowserFeatures features = new WebBrowserFeatures(true, true, false, false, true, false);
                WebBrowserPane pane = bd.createNewBrowserPane(features, true);
                // the problem here is following: js-test-driver is a global server
                // which does not have any project specific context. But in order to
                // debug JavaScript the JS debugger needs a project context in order
                // to correctly resolve breakpoints etc. So when server is started
                // there will not be any project context; only when a test is 
                // executed from a project the project context will be set for JS debugger by
                // updating the lookup; JS debugger listens on lookup changes
                pane.setProjectContext(projectContext);
                pane.showURL(u);
                if (bd.hasNetBeansIntegration()) {
                    integratedBrowserPanes.add(pane);
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private static class LineConvertorImpl implements LineConvertor {

        private Project p;

        public LineConvertorImpl(Project p) {
            this.p = p;
        }

        // XXX: exact this algorithm is also in 
        // web.javascript.debugger/src/org/netbeans/modules/web/javascript/debugger/console/BrowserConsoleLogger.java
        // keep them in sync
        @Override
        public List<ConvertedLine> convert(String line) {
            // pattern is "at ...... (file:line:column)"
            // file can be also http:// url
            if (!line.endsWith(")")) { //NOI18N
                return convertLineURL(line);
            }
            int start = line.lastIndexOf('(');
            if (start == -1) {
                return null;
            }
            int lineNumberEnd = line.lastIndexOf(':');
            if (lineNumberEnd == -1) {
                return null;
            }
            int fileEnd = line.lastIndexOf(':', lineNumberEnd-1);
            if (fileEnd == -1) {
                return null;
            }
            if (start >= fileEnd) {
                return null;
            }
            int lineNumber = -1;
            int columnNumber = -1;
            try {
                lineNumber = Integer.parseInt(line.substring(fileEnd+1, lineNumberEnd));
                columnNumber = Integer.parseInt(line.substring(lineNumberEnd+1, line.length()-1));
            } catch (NumberFormatException e) {
                //ignore
            }
            if (columnNumber != -1 && lineNumber == -1) {
                // perhaps stack trace had only line number:
                lineNumber = columnNumber;
            }
            if (lineNumber == -1) {
                return null;
            }
            String file = line.substring(start+1, fileEnd);
            if (file.length() == 0) {
                return null;
            }
            FileObject fo = p.getProjectDirectory().getFileObject(file);
            if (fo == null) {
                return null;
            }
            List<ConvertedLine> res = new ArrayList<ConvertedLine>();
            //res.add(ConvertedLine.forText(line.substring(0, start), null));
            ListenerImpl l = new ListenerImpl(fo, lineNumber, columnNumber);
            res.add(ConvertedLine.forText(/*line.substring(start, line.length()-1)*/line, l.isValidHyperlink() ? l : null));
            //res.add(ConvertedLine.forText(line.substring(line.length()-1, line.length()), null));
            return res;
        }
        
        private List<ConvertedLine> convertLineURL(String line) {
            int u1 = line.indexOf("http://");   // NOI18N
            if (u1 < 0) {
                u1 = line.indexOf("https://");  // NOI18N
            }
            if (u1 < 0) {
                return null;
            }
            int ue = line.indexOf(' ', u1);
            if (ue < 0) {
                ue = line.length();
            }
            int col2 = line.lastIndexOf(':', ue);
            if (col2 < 0) {
                return null;
            }
            int col1 = line.lastIndexOf(':', col2 - 1);
            if (col1 < 0) {
                return null;
            }
            int lineNumber = -1;
            int columnNumber = -1;
            try {
                lineNumber = Integer.parseInt(line.substring(col1+1, col2));
                columnNumber = Integer.parseInt(line.substring(col2+1, ue));
            } catch (NumberFormatException e) {
                //ignore
            }
            if (columnNumber != -1 && lineNumber == -1) {
                // perhaps stack trace had only line number:
                lineNumber = columnNumber;
            }
            if (lineNumber == -1) {
                return null;
            }
            String file = line.substring(u1, col1);
            if (file.length() == 0) {
                return null;
            }
            
            FileObject fo = null;
            try {
                URL url = URI.create(file).toURL();
                fo = ServerURLMapping.fromServer(p, url);
                if (fo == null) {
                    fo = RemoteFileCache.getRemoteFile(url);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (fo == null) {
                return null;
            }
            List<ConvertedLine> res = new ArrayList<ConvertedLine>();
            //res.add(ConvertedLine.forText(line.substring(0, start), null));
            ListenerImpl l = new ListenerImpl(fo, lineNumber, columnNumber);
            res.add(ConvertedLine.forText(/*line.substring(start, line.length()-1)*/line, l.isValidHyperlink() ? l : null));
            //res.add(ConvertedLine.forText(line.substring(line.length()-1, line.length()), null));
            return res;
        }
    
    }
    
    private static class ListenerImpl implements OutputListener {

        private FileObject fo;
        private int line;
        private int column;

        public ListenerImpl(FileObject fo, int line, int column) {
            this.fo = fo;
            this.line = line;
            this.column = column;
        }
        
        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            Line l = getLine();
            if (l != null) {
                l.show(Line.ShowOpenType.OPEN, 
                    Line.ShowVisibilityType.FOCUS, column != -1 ? column -1 : -1);
            }
        }
        
        private Line getLine() {
            LineCookie result = null;
            try {
                DataObject dataObject = DataObject.find(fo);
                if (dataObject != null) {
                    result = dataObject.getCookie(LineCookie.class);
                }
            } catch (DataObjectNotFoundException e) {
                e.printStackTrace();
            }
            if (result != null) {
                return result.getLineSet().getCurrent(line-1);
            }
            return null;
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
        
        public boolean isValidHyperlink() {
            return getLine() != null;
        }
    
    }
    
    private static class Listener implements TestListener {

        private TestSession testSession;
        private Manager manager;
        private Report report;

        public Listener(Project project) {
            manager = Manager.getInstance();
            testSession = new TestSession(
                    NbBundle.getMessage(JSTestDriverSupport.class, "TESTING", ProjectUtils.getInformation(project).getDisplayName()),
                    project, TestSession.SessionType.TEST);
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
            Testcase testCase = new Testcase(testResult.getTestCaseName()+"."+testResult.getTestName(), null, testSession); //NOI18N
            testCase.setStatus(convert(testResult.getResult()));
            testCase.setTimeMillis(testResult.getDuration());
            if (testResult.getResult() == TestResult.Result.failed || testResult.getResult() == TestResult.Result.error) {
                Trouble t = new Trouble(true);
                if (testResult.getStack().length() > 0) {
                    t.setStackTrace(trimArray(testResult.getStack().split("\\u000d"))); //NOI18N
                    testCase.addOutputLines(Arrays.asList(testResult.getStack().split("\\u000d"))); //NOI18N
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

        private String[] trimArray(String[] split) {
            if (split == null) {
                return null;
            }
            List<String> r = new ArrayList<String>();
            for (int i = 0; i < split.length; i++) {
                String s = split[i].trim();
                if (s.length() > 0) {
                    r.add(s);
                }
            }
            return r.toArray(new String[r.size()]);
        }
        
    }

    public static void logUsage(Class srcClass, String message, Object[] params) {
        Parameters.notNull("message", message); // NOI18N

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }

}
