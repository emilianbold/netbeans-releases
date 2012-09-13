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
package org.netbeans.modules.maven.junit;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.RerunType;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.modules.maven.junit.nodes.JUnitTestRunnerNodeFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class JUnitOutputListenerProvider implements OutputProcessor {
    TestSession session;
    private Pattern runningPattern;
    private Pattern outDirPattern2;
    private Pattern outDirPattern;
    private File outputDir;
    String runningTestClass;
    private final Set<String> usedNames;
    
    private static final Logger LOG = Logger.getLogger(JUnitOutputListenerProvider.class.getName());
    private RunConfig config;
    
    public JUnitOutputListenerProvider(RunConfig config) {
        runningPattern = Pattern.compile("(?:\\[surefire\\] )?Running (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern = Pattern.compile("Surefire report directory\\: (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern2 = Pattern.compile("Setting reports dir\\: (.*)", Pattern.DOTALL); //NOI18N
        this.config = config;
        usedNames = new HashSet<String>();
    }


    public @Override String[] getRegisteredOutputSequences() {
        return new String[] {
            "mojo-execute#surefire:test" //NOI18N
        };
    }

    public @Override void processLine(String line, OutputVisitor visitor) {
        Matcher match = outDirPattern.matcher(line);
        if (match.matches()) {
            outputDir = new File(match.group(1));
            if (session == null) {
                createSession(outputDir);
            }
            return;
        }
        match = outDirPattern2.matcher(line);
        if (match.matches()) {
            outputDir = new File(match.group(1));
            if (session == null) {
                createSession(outputDir);
            }
            return;
        }
        
        if (session == null) {
            return;
        }
        match = runningPattern.matcher(line);
        if (match.matches()) {
            if (runningTestClass != null && outputDir != null) {
                generateTest();
            }
            runningTestClass = match.group(1);
        }
    }

    public @Override void sequenceStart(String sequenceId, OutputVisitor visitor) {
        session = null;
    }

    //#179703 allow multiple sessions per project, in case there are multiple executions of surefire plugin.
    private String createSessionName(String projectId) {
        String name = projectId;
        int index = 2;
        while (usedNames.contains(name)) {
            name = projectId + "_" + index;
            index = index + 1;
        }
        usedNames.add(name);
        return name;
    } 
    
    private void createSession(File file) {
        if (session == null) {
            File fil = FileUtil.normalizeFile(file);
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                Project prj = FileOwnerQuery.getOwner(fo);
                if (prj != null) {
                    NbMavenProject mvnprj = prj.getLookup().lookup(NbMavenProject.class);
                    if (mvnprj != null) {
                        TestSession.SessionType type = TestSession.SessionType.TEST;
                        String action = config.getActionName();
                        if (action != null) { //custom
                            if (action.contains("debug")) { //NOI81N
                                type = TestSession.SessionType.DEBUG;
                            }
                        }
                        final TestSession.SessionType fType = type;
                        session = new TestSession(createSessionName(mvnprj.getMavenProject().getId()), prj, TestSession.SessionType.TEST,
                                new JUnitTestRunnerNodeFactory(session, prj));
                        session.setRerunHandler(new RerunHandler() {
                            public @Override
                            void rerun() {
                                RunUtils.executeMaven(config);
                            }

                            public @Override
                            void rerun(Set<Testcase> tests) {
                                RunConfig brc = RunUtils.cloneRunConfig(config);
                                StringBuilder tst = new StringBuilder();
                                Map<String, Collection<String>> methods = new HashMap<String, Collection<String>>();
                                for (Testcase tc : tests) {
                                    //TODO just when is the classname null??
                                    if (tc.getClassName() != null) {
                                        Collection<String> lst = methods.get(tc.getClassName());
                                        if (lst == null) {
                                            lst = new ArrayList<String>();
                                            methods.put(tc.getClassName(), lst);
                                        }
                                        lst.add(tc.getName());
                                    }
                                }
                                for (Map.Entry<String, Collection<String>> ent : methods.entrySet()) {
                                        tst.append(",");
                                        tst.append(ent.getKey());

                                        //#name only in surefire > 2.7.2 and junit > 4.0 or testng
                                        // bug works with the setting also for junit 3.x
                                        tst.append("#");
                                        boolean first = true;
                                        for (String meth : ent.getValue()) {
                                            if (!first) {
                                                tst.append("+");
                                            }
                                            first = false;
                                            tst.append(meth);
                                        }
                                }
                                if (tst.length() > 0) {
                                    brc.setProperty("test", tst.substring(1));
                                }
                                RunUtils.executeMaven(brc);
                            }

                            public @Override
                            boolean enabled(RerunType type) {
                                //TODO debug doesn't property update debug port in runconfig..
                                if (fType.equals(TestSession.SessionType.TEST)) {
                                    if (RerunType.ALL.equals(type)) {
                                        return true;
                                    }
                                    if (RerunType.CUSTOM.equals(type)) {
                                        if (usingTestNG(config.getMavenProject())) { //#214334 test for testng has to come first, as itself depends on junit
                                            return usingSurefire28(config.getMavenProject());
                                        } 
                                        else 
                                        if (usingJUnit4(config.getMavenProject())) { //#214334
                                            return usingSurefire2121(config.getMavenProject());
                                        } 
                                    }
                                }
                                return false;
                            }

                            public @Override
                            void addChangeListener(ChangeListener listener) {
                            }

                            public @Override
                            void removeChangeListener(ChangeListener listener) {
                            }
                        });
                        Manager.getInstance().testStarted(session);

                    }
                }
            }
        }
    }
    
    private boolean usingSurefire2121(MavenProject prj) {
        String v = PluginPropertyUtils.getPluginVersion(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.12.1")) >= 0;
    }
    
    private boolean usingSurefire28(MavenProject prj) {
        String v = PluginPropertyUtils.getPluginVersion(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.8")) >= 0;
    } 
    
     private boolean usingTestNG(MavenProject prj) {
        for (Artifact a : prj.getArtifacts()) {
            if ("org.testng".equals(a.getGroupId()) && "testng".equals(a.getArtifactId())) {
                return true;
            }
        }
        return false;
    }   

    private boolean usingJUnit4(MavenProject prj) { // SUREFIRE-724
        for (Artifact a : prj.getArtifacts()) {
            if ("junit".equals(a.getGroupId()) && ("junit".equals(a.getArtifactId()) || "junit-dep".equals(a.getArtifactId()))) { //junit-dep  see #214238
                String version = a.getVersion();
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("4.8")) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }    

    public @Override void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        if (session == null) {
            return;
        }
        if (runningTestClass != null && outputDir != null) {
            generateTest();
        }
        Manager.getInstance().sessionFinished(session);
        runningTestClass = null;
        outputDir = null;
        session = null;
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

    public @Override void sequenceFail(String sequenceId, OutputVisitor visitor) {
        sequenceEnd(sequenceId, visitor);
    }

    
    private void generateTest() {
        String reportNameSuffix = PluginPropertyUtils.getPluginProperty(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE, "reportNameSuffix", null);
        String suffix = reportNameSuffix;
        if (suffix == null) {
            suffix = "";
        } else {
            //#204480
            suffix = "-" + suffix;
        }
        File report = new File(outputDir, "TEST-" + runningTestClass + suffix + ".xml");
        if (!report.isFile()) {
            return;
        }
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(report);
            Element testSuite = document.getRootElement();
            assert "testsuite".equals(testSuite.getName()) : "Root name " + testSuite.getName(); //NOI18N
            TestSuite suite = new TestSuite(testSuite.getAttributeValue("name"));
            session.addSuite(suite);
            Manager.getInstance().displaySuiteRunning(session, suite.getName());
            
            @SuppressWarnings("unchecked")
            List<Element> testcases = testSuite.getChildren("testcase"); //NOI18N
            String nameSuffix = reportNameSuffix != null ? "(" + reportNameSuffix + ")" : "";
            for (Element testcase : testcases) {
                //#204480
                String name = testcase.getAttributeValue("name");
                if (name.endsWith(nameSuffix)) {
                    name = name.substring(0, name.length() - nameSuffix.length());
                }
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
                    float fl = NumberFormat.getNumberInstance().parse(time).floatValue();
                    test.setTimeMillis((long)(fl * 1000));
                }
                String classname = testcase.getAttributeValue("classname");
                if (classname != null) {
                    //#204480
                    if (classname.endsWith(nameSuffix)) {
                        classname = classname.substring(0, classname.length() - nameSuffix.length());
                    }
                    test.setClassName(classname);
                    test.setLocation(test.getClassName().replace('.', '/') + ".java");
                }
                session.addTestCase(test);
            }
            String time = testSuite.getAttributeValue("time");
            float fl = NumberFormat.getNumberInstance().parse(time).floatValue();
            long timeinmilis = (long)(fl * 1000);
            Manager.getInstance().displayReport(session, session.getReport(timeinmilis));
            File output = new File(outputDir, runningTestClass + suffix + "-output.txt");
            if (output.isFile()) {
                Manager.getInstance().displayOutput(session, FileUtils.fileRead(output), false);
            }
        } catch (JDOMException x) {
            LOG.log(Level.INFO, "parsing " + report, x);
        } catch (Exception x) {
            LOG.log(Level.WARNING, "parsing " + report, x);
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
