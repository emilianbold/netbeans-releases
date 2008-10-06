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

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.run.LoggerTrampoline;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.output.NotifyFinishOutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
public class JUnitOutputListenerProvider implements NotifyFinishOutputProcessor {
    private Project prj;
    private NbMavenProject mavenproject;
    private AntSession session;
    private AntLogger unitLogger;
    private Pattern runningPattern;
    private Pattern outDirPattern2;
    private Pattern outDirPattern;
    String outputDir;
    String runningTestClass;
    private Pattern testNamePattern = Pattern.compile(".*\\((.*)\\).*<<< (?:FAILURE)?(?:ERROR)?!\\s*"); //NOI18N
    
    private Logger LOG = Logger.getLogger(JUnitOutputListenerProvider.class.getName());
    
    public JUnitOutputListenerProvider(Project project) {
        prj = project;
        mavenproject = prj.getLookup().lookup(NbMavenProject.class);
        runningPattern = Pattern.compile("(?:\\[surefire\\] )?Running (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern = Pattern.compile("Surefire report directory\\: (.*)", Pattern.DOTALL); //NOI18N
        outDirPattern2 = Pattern.compile("Setting reports dir\\: (.*)", Pattern.DOTALL); //NOI18N
        
    }

    public String[] getRegisteredOutputSequences() {
        return new String[] {
            "mojo-execute#surefire:test" //NOI18N
        };
    }

    public void processLine(String line, OutputVisitor visitor) {
        if (unitLogger == null) {
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
                generateAntLogSequence(report);
            }
            runningTestClass = match.group(1);
            return;
        }
    }

    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
        if (session == null && unitLogger == null) {
            session = LoggerTrampoline.ANT_SESSION_CREATOR.makeAntSession(new FakeAntSession());
            Collection<? extends AntLogger> all = Lookup.getDefault().lookupAll(AntLogger.class);
            for (AntLogger logger : all) {
                if (logger.getClass().getName().equals("org.netbeans.modules.junit.output.JUnitAntLogger")) { //NOI18N
                    unitLogger = logger;
                    break;
                }
            }
        }
        FakeAntEvent evnt = new FakeAntEvent(session, prj);
        AntEvent event = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(evnt);
        //TODO debugging seems to be handled differently "java" + runners as classname.
        evnt.setTaskName("junit"); //NOI18N
        //now setup children taskStructures to get the count of tests to be executed.
        FakeAntEvent.FakeTaskStructure tsImpl = new FakeAntEvent.FakeTaskStructure("XXX");
        TaskStructure str = LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(tsImpl);
        tsImpl.setChildren(createTaskStructure());
        evnt.setTaskStructure(str);
        unitLogger.taskStarted(event);
    }

    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        if (unitLogger == null) {
            return;
        }
        if (runningTestClass != null && outputDir != null) {
            FileObject report = getTestReportFileObject(outputDir, "TEST-" + runningTestClass + ".xml"); //NOI18N
            generateAntLogSequence(report);
        }
        runningTestClass = null;
        outputDir = null;
        
        FakeAntEvent evnt = new FakeAntEvent(session, prj);
        AntEvent event = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(evnt);
        evnt.setTaskName("junit"); //NOI18N
        unitLogger.taskFinished(event);
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

    
    private void generateAntLogSequence(FileObject report) {
        if (report == null) {
            return;
        }
        try {
            SAXBuilder builder = new SAXBuilder();
            InputStream stream = report.getInputStream();
            Document document = builder.build(stream);
            Element testSuite = document.getRootElement();
            assert "testsuite".equals(testSuite.getName()) : "Root name " + testSuite.getName(); //NOI18N
            
            String executing ="Executing '/home/mkleint/javatools/jdk1.5.0_09/jre/bin/java' with arguments:\n" +  //NOI18N
"'-classpath'\n" +  //NOI18N
"'" + mavenproject.getMavenProject().getBuild().getTestOutputDirectory() + "'\n" + //NOI18N
"'org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner'\n" +  //NOI18N
"'" + testSuite.getAttributeValue("name") + "'\n" +                      //NOI18N
//'filtertrace=true'
//'haltOnError=false'
//'haltOnFailure=false'
//'showoutput=true'
//'outputtoformatters=true'
//'logtestlistenerevents=true'
"'formatter=org.apache.tools.ant.taskdefs.optional.junit.BriefJUnitResultFormatter'\n" +  //NOI18N
"'formatter=org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter," + FileUtil.toFile(report).getAbsolutePath() + "'\n\n" +  //NOI18N
//'crashfile=/home/mkleint/NetBeansProjects/MarsRoverViewer/junitvmwatcher415696509.properties'
//'propsfile=/home/mkleint/NetBeansProjects/MarsRoverViewer/junit1756164759.properties'

"The ' characters around the executable and arguments are\n" +  //NOI18N
"not part of the command."; //NOI18N
            logText(executing, AntEvent.LOG_VERBOSE);
            
            logText("Testsuite: " + testSuite.getAttributeValue("name"), AntEvent.LOG_INFO); //NOI18N
            
            @SuppressWarnings("unchecked")
            List<Element> testcases = testSuite.getChildren("testcase"); //NOI18N
            
            logText("junit.framework.TestListener: tests to run: " + testcases.size(), AntEvent.LOG_VERBOSE); //NOI18N
            String stdoutAll = "";
            for (Element testcase : testcases) {
                String name = testcase.getAttributeValue("name"); //NOI18N
                logText("junit.framework.TestListener: startTest(" + name + ")", AntEvent.LOG_VERBOSE); //NOI18N
                
                Element stdout = testcase.getChild("system-out"); //NOI18N
                if (stdout != null) {
                    logText(stdout.getText(), AntEvent.LOG_INFO);
                    stdoutAll = stdoutAll + "\n" + stdout.getText(); //NOI18N
                }
                Element failure = testcase.getChild("failure"); //NOI18N
                if (failure != null) {
                    logText("junit.framework.TestListener: addFailure(" + name + ", " + failure.getAttributeValue("message") + ")", AntEvent.LOG_VERBOSE); //NOI18N
                }
                Element error = testcase.getChild("error"); //NOI18N
                if (error != null) {
                    logText("junit.framework.TestListener: addError(" + name + ", " + error.getAttributeValue("message") + ")", AntEvent.LOG_VERBOSE); //NOI18N
                }
                logText("junit.framework.TestListener: endTest(" + name + ")", AntEvent.LOG_VERBOSE);
            }
            logText("Tests run: " + testSuite.getAttributeValue("tests") + //NOI18N
                    ", Failures: " + testSuite.getAttributeValue("failures") +  //NOI18N
                    ", Errors: " + testSuite.getAttributeValue("errors") +  //NOI18N
                    ", Time elapsed: " + testSuite.getAttributeValue("time") + " sec", //NOI18N
                    AntEvent.LOG_INFO);
            logText("", AntEvent.LOG_INFO);
            logText("------------- Standard Output ---------------", AntEvent.LOG_INFO); //NOI18N
            logText(stdoutAll, AntEvent.LOG_INFO);
            logText("------------- ---------------- ---------------", AntEvent.LOG_INFO); //NOI18N
            for (Element testcase : testcases) {
                String name = testcase.getAttributeValue("name"); //NOI18N
                Element failure = testcase.getChild("failure"); //NOI18N
                if (failure != null) {
                    String failureText = failure.getAttributeValue("message") + "\n" + failure.getText() + "\n"; //NOI18N
                    logText("Testcase: " + name + //NOI18N
                            "(" + testSuite.getAttributeValue("name") +  //NOI18N
                            "):\tFAILED", AntEvent.LOG_INFO); //NOI18N
                    logText(failureText, AntEvent.LOG_INFO);
                    logText("", AntEvent.LOG_INFO);
                    logText("", AntEvent.LOG_INFO);
                }
                Element error = testcase.getChild("error"); //NOI18N
                if (error != null) {
                    String errorText = error.getAttributeValue("message") + "\n" + error.getText() + "\n"; //NOI18N
                    logText("Testcase: " + name + //NOI18N
                            "(" + testSuite.getAttributeValue("name") +  //NOI18N
                            "):\tCaused an ERROR", AntEvent.LOG_INFO); //NOI18N
                    logText(errorText, AntEvent.LOG_INFO);
                    logText("", AntEvent.LOG_INFO); //NOI18N
                    logText("", AntEvent.LOG_INFO); //NOI18N
                }
                
            }
            
            
        } catch (Exception exc) {
            ErrorManager.getDefault().notify(exc);
        }
    }

    
    private TaskStructure[] createTaskStructure() {
        TaskStructure[] struct = new TaskStructure[0];
//        for (int i = 0; i < 1; i++) {
//            FakeAntEvent.FakeTaskStructure tsImpl = new FakeAntEvent.FakeTaskStructure("test");
//            struct[i] = LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(tsImpl);
//        }
        return struct;
    }

    private void logText(String failureText, int level) {
        if (failureText.length() == 0) {
            FakeAntEvent evnt = new FakeAntEvent(session, prj);
            AntEvent event = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(evnt);
            evnt.setTaskName("junit"); //NOI18N
            evnt.setLogLevel(level);
            evnt.setMessage("");
            unitLogger.messageLogged(event);
            return;
        }
        StringTokenizer tokens = new StringTokenizer(failureText, "\n"); //NOI18N
        while (tokens.hasMoreTokens()) {
            FakeAntEvent evnt = new FakeAntEvent(session, prj);
            AntEvent event = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(evnt);
            evnt.setTaskName("junit"); //NOI18N
            evnt.setLogLevel(level);
            evnt.setMessage(tokens.nextToken());
            unitLogger.messageLogged(event);
        }
    }

    public void buildFinished() {
        if (unitLogger == null) {
            return;
        }
        LOG.fine("build finished!!!!"); //NOI18N
        FakeAntEvent evnt = new FakeAntEvent(session, prj);
        AntEvent event = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(evnt);
        evnt.setTaskName("junit"); //NOI18N
        unitLogger.buildFinished(event);
        unitLogger = null;
        session = null;
    }
}
