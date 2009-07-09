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
package org.netbeans.modules.maven.junit;

import hidden.org.codehaus.plexus.util.StringUtils;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.junit.nodes.JUnitTestRunnerNodeFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class JUnitOutputListenerProvider implements OutputProcessor {
    private Project prj;
    private NbMavenProject mavenproject;
    TestSession session;
    private Pattern runningPattern;
    private Pattern outDirPattern2;
    private Pattern outDirPattern;
    String outputDir;
    String runningTestClass;
    private Pattern testNamePattern = Pattern.compile(".*\\((.*)\\).*<<< (?:FAILURE)?(?:ERROR)?!\\s*"); //NOI18N
    
    private Logger LOG = Logger.getLogger(JUnitOutputListenerProvider.class.getName());
    private RunConfig config;
    
    public JUnitOutputListenerProvider(Project project, RunConfig config) {
        prj = project;
        mavenproject = prj.getLookup().lookup(NbMavenProject.class);
        runningPattern = Pattern.compile("(?:\\[surefire\\] )?Running (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern = Pattern.compile("Surefire report directory\\: (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern2 = Pattern.compile("Setting reports dir\\: (.*)", Pattern.DOTALL); //NOI18N
        this.config = config;
    }


    public String[] getRegisteredOutputSequences() {
        return new String[] {
            "mojo-execute#surefire:test" //NOI18N
        };
    }

    public void processLine(String line, OutputVisitor visitor) {
        if (session == null) {
            return;
        }
        Matcher match = outDirPattern.matcher(line);
        if (match.matches()) {
            outputDir = match.group(1);
            return;
        }
        match = outDirPattern2.matcher(line);
        if (match.matches()) {
            outputDir = match.group(1);
            return;
        }
        
        match = runningPattern.matcher(line);
        if (match.matches()) {
            if (runningTestClass != null && outputDir != null) {
                FileObject report = getTestReportFileObject(outputDir, "TEST-" + runningTestClass + ".xml"); //NOI18N
                generateTest(report);
            }
            runningTestClass = match.group(1);
            return;
        }
    }

    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
        if (session == null) {
            TestSession.SessionType type = TestSession.SessionType.TEST;
            String action = config.getActionName();
            if (action != null) { //custom
                if (action.contains("debug")) { //NOI81N
                     type = TestSession.SessionType.DEBUG;
                }
            }
            final TestSession.SessionType fType = type;
            session = new TestSession(mavenproject.getMavenProject().getId(), prj, TestSession.SessionType.TEST,
                    new JUnitTestRunnerNodeFactory(session, prj));
            session.setRerunHandler(new RerunHandler() {
                public void rerun() {
                    RunUtils.executeMaven(config);
                }
                public boolean enabled() {
                    //TODO debug doesn't property update debug port in runconfig..
                    return fType.equals(TestSession.SessionType.TEST);
                }
                public void addChangeListener(ChangeListener listener) {
                }
                public void removeChangeListener(ChangeListener listener) {
                }
            });
            Manager.getInstance().testStarted(session);
        }
    }

    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        if (session == null) {
            return;
        }
        if (runningTestClass != null && outputDir != null) {
            FileObject report = getTestReportFileObject(outputDir, "TEST-" + runningTestClass + ".xml"); //NOI18N
            generateTest(report);
        }
        Manager.getInstance().sessionFinished(session);
        runningTestClass = null;
        outputDir = null;
    }

    private static Pattern COMPARISON_PATTERN = Pattern.compile(".*expected:<(.*)> but was:<(.*)>$"); //NOI18N

    static Trouble constructTrouble(String type, String message, String text, boolean error) {
        Trouble t = new Trouble(error);
        if (message != null) {
            Matcher match = COMPARISON_PATTERN.matcher(message);
            if (match.matches()) {
                t.setComparisonFailure(new Trouble.ComparisonFailure(match.group(1), match.group(2)));
            }
        }
        if (text != null) {
            String[] strs = StringUtils.split(text, "\n");
            List<String> lines = new ArrayList<String>();
            lines.add(message);
            lines.add(type);
            for (int i = 1; i < strs.length; i++) {
                lines.add(strs[i]);
            }
            t.setStackTrace(lines.toArray(new String[0]));
        }
        return t;
    }

    private FileObject getTestReportFileObject(String outputDirectory, String reportFileName) {
        assert outputDirectory != null;
        File file = FileUtil.normalizeFile(new File(outputDir));
        FileUtil.refreshFor(file);
        FileObject outDir = FileUtil.toFileObject(file);
        if (outDir != null) {
            FileObject report = outDir.getFileObject(reportFileName);
            return report;
        }
        return null;
    }



    public void sequenceFail(String sequenceId, OutputVisitor visitor) {
        sequenceEnd(sequenceId, visitor);
    }

    
    private void generateTest(FileObject report) {
        if (report == null) {
            return;
        }
        try {
            SAXBuilder builder = new SAXBuilder();
            InputStream stream = report.getInputStream();
            Document document = builder.build(stream);
            Element testSuite = document.getRootElement();
            assert "testsuite".equals(testSuite.getName()) : "Root name " + testSuite.getName(); //NOI18N
            TestSuite suite = new TestSuite(testSuite.getAttributeValue("name"));
            session.addSuite(suite);
            Manager.getInstance().displaySuiteRunning(session, suite.getName());
            
            @SuppressWarnings("unchecked")
            List<Element> testcases = testSuite.getChildren("testcase"); //NOI18N
            
            for (Element testcase : testcases) {
                String name = testcase.getAttributeValue("name");
                Testcase test = new Testcase(name, null, session);
                Element stdout = testcase.getChild("system-out"); //NOI18N
                if (stdout != null) {
                    logText(stdout.getText(), test, false);
                }
                Element failure = testcase.getChild("failure"); //NOI18N
                Status status = Status.PASSED;
                Trouble trouble = null;
                if (failure != null) {
                    status = Status.FAILED;
                    trouble = constructTrouble(failure.getAttributeValue("type"), failure.getAttributeValue("message"), failure.getText(), false);
                }
                Element error = testcase.getChild("error"); //NOI18N
                if (error != null) {
                    status = Status.ERROR;
                    trouble = constructTrouble(error.getAttributeValue("type"), error.getAttributeValue("message"), error.getText(), true);
                }
                test.setStatus(status);
                if (trouble != null) {
                    test.setTrouble(trouble);
                }
                String time = testcase.getAttributeValue("time");
                if (time != null) {
                    float fl = Float.parseFloat(time);
                    test.setTimeMillis((long)(fl * 1000));
                }
                String classname = testcase.getAttributeValue("classname");
                if (classname != null) {
                    test.setClassName(classname);
                    test.setLocation(test.getClassName().replace('.', '/') + ".java");
                }
                session.addTestCase(test);
            }
            String time = testSuite.getAttributeValue("time");
            float fl = Float.parseFloat(time);
            long timeinmilis = (long)(fl * 1000);
            Manager.getInstance().displayReport(session, session.getReport(timeinmilis));
        } catch (Exception exc) {
            ErrorManager.getDefault().notify(exc);
        }
    }

    private void logText(String text, Testcase test, boolean failure) {
        StringTokenizer tokens = new StringTokenizer(text, "\n"); //NOI18N
        List<String> lines = new ArrayList<String>();
        while (tokens.hasMoreTokens()) {
            lines.add(tokens.nextToken());
        }
        Manager.getInstance().displayOutput(session, text, failure);
        test.addOutputLines(lines);
    }

}
