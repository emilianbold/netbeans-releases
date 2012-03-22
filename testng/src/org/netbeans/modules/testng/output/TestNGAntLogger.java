/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.testng.output;

import org.netbeans.modules.testng.output.AntSessionInfo;
import java.io.File;
import java.util.*;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Ant logger interested in task &quot;junit&quot;,
 * dispatching events to instances of the {@link TestNGOutputReader} class.
 * There is one <code>TestNGOutputReader</code> instance created per each
 * Ant session.
 *
 * @see  TestNGOutputReader
 * @see  Report
 * @author  Marian Petras
 * @author  Lukas Jungmann
 */
@ServiceProvider(service = AntLogger.class)
public final class TestNGAntLogger extends AntLogger {

    /** levels of interest for logging (info, warning, error, ...) */
    private static final int[] LEVELS_OF_INTEREST = {
        AntEvent.LOG_INFO,
        AntEvent.LOG_WARN, //test failures
        AntEvent.LOG_VERBOSE, //our test listener
        AntEvent.LOG_ERR
    };
    public static final String TASK_JAVA = "java";                      //NOI18N
    public static final String TASK_TESTNG = "testng";                    //NOI18N
    private static final String[] INTERESTING_TASKS = {TASK_JAVA, TASK_TESTNG};
    private static final String ANT_TEST_RUNNER_CLASS_NAME =
            "org.testng.TestNG";//NOI18N

    /**
     * Default constructor for lookup
     */
    public TestNGAntLogger() {
    }

    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }

    @Override
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }

    @Override
    public String[] interestedInTasks(AntSession session) {
        return INTERESTING_TASKS;
    }

    /**
     * Detects type of the Ant task currently running.
     *
     * @param  event  event produced by the currently running Ant session
     * @return  {@code TaskType.TEST_TASK} if the task is a JUnit test task,
     *          {@code TaskType.DEBUGGING_TEST_TASK} if the task is a JUnit
     *             test task running in debugging mode,
     *          {@code TaskType.OTHER_TASK} if the task is not a JUnit test
     *             task;
     *          or {@code null} if no Ant task is currently running
     */
    private static SessionType detectSessionType(AntEvent event) {
        final String taskName = event.getTaskName();

        if (taskName == null) {
            return null;
        }

        if (taskName.equals(TASK_TESTNG)) {
            return SessionType.TEST;
        }

        if (taskName.equals(TASK_JAVA)) {
            TaskStructure taskStructure = event.getTaskStructure();

            String className = taskStructure.getAttribute("classname"); //NOI18N
            if (className == null) {
                return null;
            }

            className = event.evaluate(className);
            if (className.equals("org.testng.TestNG")) { //NOI18N
                TaskStructure[] nestedElems = taskStructure.getChildren();
                for (TaskStructure ts : nestedElems) {
                    if (ts.getName().equals("jvmarg")) {                //NOI18N
                        String a;
                        if ((a = ts.getAttribute("value")) != null) {   //NOI18N
                            if (event.evaluate(a).equals("-Xdebug")) {  //NOI18N
                                return SessionType.DEBUG;
                            }
                        } else if ((a = ts.getAttribute("line")) != null) {//NOI18N
                            for (String part : parseCmdLine(event.evaluate(a))) {
                                if (part.equals("-Xdebug")) {           //NOI18N
                                    return SessionType.DEBUG;
                                }
                            }
                        }
                    }
                }
                return SessionType.TEST;
            }
            return null;
        }
        assert false : "Unhandled task name";                           //NOI18N
        return null;
    }

    /**
     * Parses the given command-line string into individual arguments.
     * @param  cmdLine  command-line to be parsed
     * @return  list of invidividual parts of the given command-line,
     *          or an empty list if the command-line was empty
     */
    private static List<String> parseCmdLine(String cmdLine) {
        cmdLine = cmdLine.trim();

        /* maybe the command-line is empty: */
        if (cmdLine.length() == 0) {
            return Collections.<String>emptyList();
        }

        final char[] chars = cmdLine.toCharArray();

        /* maybe the command-line contains just one part: */
        boolean simple = true;
        for (char c : chars) {
            if ((c == ' ') || (c == '"') || (c == '\'')) {
                simple = false;
                break;
            }
        }
        if (simple) {
            return Collections.<String>singletonList(cmdLine);
        }

        /* OK, so it is not trivial: */
        List<String> result = new ArrayList<String>(4);
        StringBuilder buf = new StringBuilder(20);
        final int stateBeforeWord = 0;
        final int stateAfterWord = 1;
        final int stateInSingleQuote = 2;
        final int stateInDoubleQuote = 3;
        int state = stateBeforeWord;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (state) {
                case stateBeforeWord:
                    if (c == '"') {
                        state = stateInDoubleQuote;
                    } else if (c == '\'') {
                        state = stateInSingleQuote;
                    } else if (c == ' ') {
                        //do nothing - remain in state "before word"
                    } else {
                        buf.append(c);
                        state = stateAfterWord;
                    }
                    break;
                case stateInDoubleQuote:
                    if (c == '"') {
                        state = stateAfterWord;
                    } else {
                        buf.append(c);
                    }
                    break;
                case stateInSingleQuote:
                    if (c == '\'') {
                        state = stateAfterWord;
                    } else {
                        buf.append(c);
                    }
                    break;
                case stateAfterWord:
                    if (c == '"') {
                        state = stateInDoubleQuote;
                    } else if (c == '\'') {
                        state = stateInSingleQuote;
                    } else if (c == ' ') {
                        result.add(buf.toString());
                        buf = new StringBuilder(20);
                        state = stateBeforeWord;
                    }
                    break;
                default:
                    assert false;
            }
        }
        assert state != stateBeforeWord;        //thanks to cmdLine.trim()
        result.add(buf.toString());

        return result;
    }

    /**
     * Tells whether the given task type is a test task type or not.
     *
     * @param  taskType  taskType to be checked; may be {@code null}
     * @return  {@code true} if the given task type marks a test task;
     *          {@code false} otherwise
     */
    private static boolean isTestSessionType(SessionType sessionType) {
        return sessionType != null;
    }

    @Override
    public boolean interestedInScript(File script, AntSession session) {
        return true;
    }

    @Override
    public int[] interestedInLogLevels(AntSession session) {
        return LEVELS_OF_INTEREST;
    }

    /**
     */
    @Override
    public void messageLogged(final AntEvent event) {
        if (isTestTaskRunning(event)) {
            if (event.getLogLevel() != AntEvent.LOG_VERBOSE) {
                getOutputReader(event).messageLogged(event);
            } else {
                /* verbose messages are logged no matter which task produced them */
                getOutputReader(event).verboseMessageLogged(event);
            }
        }
    }

    /**
     */
    private boolean isTestTaskRunning(AntEvent event) {
        return isTestSessionType(getSessionInfo(event.getSession()).getCurrentSessionType());
    }

    /**
     */
    @Override
    public void taskStarted(final AntEvent event) {
        SessionType sessionType = detectSessionType(event);
        if (isTestSessionType(sessionType)) {
            AntSessionInfo sessionInfo = getSessionInfo(event.getSession());
            assert !isTestSessionType(sessionInfo.getCurrentSessionType());
            sessionInfo.setTimeOfTestTaskStart(System.currentTimeMillis());
            sessionInfo.setCurrentSessionType(sessionType);
            if (sessionInfo.getSessionType() == null) {
                sessionInfo.setSessionType(sessionType);
            }
            String suiteName = null;
            String logLevel = null;
            TaskStructure struct = event.getTaskStructure();
            if (TASK_TESTNG.equals(struct.getName())) {
                suiteName = struct.getAttribute("suitename");
                logLevel = struct.getAttribute("verbose");
                if (logLevel == null) {
                    logLevel = struct.getAttribute("log");
                }
            } else if (TASK_JAVA.equals(struct.getName())) {
                TaskStructure[] nestedElems = struct.getChildren();
                for (TaskStructure ts : nestedElems) {
                    if (ts.getName().equals("arg")) {                //NOI18N
                        String a = ts.getAttribute("line");
                        if (a != null) {
                            String[] args = event.evaluate(a).split(" ");
                            int size = args.length;
                            for (int i = 0; i < size; i++) {
                                String curr = args[i];
                                if ("-suitename".equals(curr)) {
                                    suiteName = i + 1 < size ? args[i + 1] : null;
                                    i++;
                                } else if ("-log".equals(curr) || "-verbose".equals(curr)) {
                                    logLevel = i + 1 < size ? args[i + 1] : null;
                                    i++;
                                }
                            }
                        }
                    }
                }

            } else {
                assert false : "Unexpeted task " + struct.getName();
            }
            /*
             * Count the test classes in the try-catch block so that
             * 'testTaskStarted(...)' is called even if counting fails (throws
             * an exception):
             */
            //would have to parse all incoming xmls, take includes/excludes
            //into accout, dependencies between tests, groups etc
//            int testClassCount;
//            try {
//                testClassCount = TestCounter.getTestClassCount(event);
//            } catch (Exception ex) {
//                testClassCount = 0;
//                Logger.getLogger(TestNGAntLogger.class.getName()).log(Level.SEVERE, null, ex);
//            }

            if (suiteName != null) {
                sessionInfo.setSessionName(event.evaluate(suiteName));
            }
            boolean offline = false;
            if (logLevel != null) {
                int lvl;
                try {
                    lvl = Integer.valueOf(event.evaluate(logLevel));
                } catch (NumberFormatException nfe) {
                    lvl = -1;
                }
                //logging is explicitly turned off by the user, so show only final
                //results computed off-line from testng-results.xml file
                offline = lvl == 0;
            }
            getOutputReader(event).testTaskStarted(offline, event);
//            getOutputReader(event).testTaskStarted(testClassCount, hasXmlOutput, event);
        }
    }

    /**
     */
    @Override
    public void taskFinished(final AntEvent event) {
        AntSessionInfo sessionInfo = getSessionInfo(event.getSession());
        if (isTestSessionType(sessionInfo.getCurrentSessionType())) {
            getOutputReader(event).testTaskFinished();
            sessionInfo.setCurrentSessionType(null);
        }
    }

    /**
     */
    @Override
    public void buildFinished(final AntEvent event) {
        AntSession session = event.getSession();
        AntSessionInfo sessionInfo = getSessionInfo(session);

        if (isTestSessionType(sessionInfo.getSessionType())) {
            getOutputReader(event).buildFinished(event);
        }

        session.putCustomData(this, null);          //forget AntSessionInfo
    }

    /**
     * Retrieve existing or creates a new reader for the given session.
     *
     * @param  session  session to return a reader for
     * @return  output reader for the session
     */
    private TestNGOutputReader getOutputReader(final AntEvent event) {
        assert isTestSessionType(getSessionInfo(event.getSession()).getSessionType());

        final AntSession session = event.getSession();
        final AntSessionInfo sessionInfo = getSessionInfo(session);
        TestNGOutputReader outputReader = sessionInfo.outputReader;
        if (outputReader == null) {
            String projectDir = null;
            Project project = null;
            try {
                projectDir = event.getProperty("work.dir"); //NOI18N
            } catch (Exception e) {
            }// Maven throws exception for this property
            try {
                if (projectDir == null) {
                    projectDir = event.getProperty("basedir"); // NOI18N
                }
                if ((projectDir != null) && (projectDir.length() != 0)) {
                    File f = FileUtil.normalizeFile(new File(projectDir));
                    project = FileOwnerQuery.getOwner(FileUtil.toFileObject(f)); //NOI18N
                }
            } catch (Exception e) {
            }
            Properties props = new Properties();
            String[] propsOfInterest = {"javac.includes", "classname", "methodname", "work.dir", "classpath", "platform.java"};//NOI18N
            for(String prop:propsOfInterest) {
                String val = event.getProperty(prop);
                if (val!=null) {
                    props.setProperty(prop, val);
                }
            }
            outputReader = new TestNGOutputReader(
                    session,
                    sessionInfo,
                    project,
                    props);
            sessionInfo.outputReader = outputReader;
        }
        return outputReader;
    }

    /**
     */
    private AntSessionInfo getSessionInfo(final AntSession session) {
        Object o = session.getCustomData(this);
        assert (o == null) || (o instanceof AntSessionInfo);

        AntSessionInfo sessionInfo;
        if (o != null) {
            sessionInfo = (AntSessionInfo) o;
        } else {
            sessionInfo = new AntSessionInfo();
            session.putCustomData(this, sessionInfo);
        }
        return sessionInfo;
    }
}
