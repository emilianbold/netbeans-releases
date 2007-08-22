/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.modules.junit.output.antutils.AntProject;
import org.netbeans.modules.junit.output.antutils.TestCounter;
import org.openide.ErrorManager;

/**
 * Ant logger interested in task &quot;junit&quot;,
 * dispatching events to instances of the {@link JUnitOutputReader} class.
 * There is one <code>JUnitOutputReader</code> instance created per each
 * Ant session.
 *
 * @see  JUnitOutputReader
 * @see  Report
 * @author  Marian Petras
 */
public final class JUnitAntLogger extends AntLogger {
    
    /** levels of interest for logging (info, warning, error, ...) */
    private static final int[] LEVELS_OF_INTEREST = {
        AntEvent.LOG_INFO,
        AntEvent.LOG_WARN,     //test failures
        AntEvent.LOG_VERBOSE
    };
    
    public static final String TASK_JAVA = "java";                      //NOI18N
    public static final String TASK_JUNIT = "junit";                    //NOI18N
    private static final String[] INTERESTING_TASKS = {TASK_JAVA, TASK_JUNIT};
    private static final String ANT_TEST_RUNNER_CLASS_NAME =
            "org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner";//NOI18N
    private static final String XML_FORMATTER_CLASS_NAME =
            "org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter";//NOI18N
    
    /** default constructor for lookup */
    public JUnitAntLogger() { }
    
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
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
    private static TaskType detectTaskType(AntEvent event) {
        final String taskName = event.getTaskName();
        
        if (taskName == null) {
            return null;
        }
        
        if (taskName.equals(TASK_JUNIT)) {
            return TaskType.TEST_TASK;
        }
        
        if (taskName.equals(TASK_JAVA)) {
            TaskStructure taskStructure = event.getTaskStructure();

            String className = taskStructure.getAttribute("classname"); //NOI18N
            if (className == null) {
                return TaskType.OTHER_TASK;
            }
            
            className = event.evaluate(className);
            if (className.equals("junit.textui.TestRunner")             //NOI18N
                    || className.startsWith("org.junit.runner.")        //NOI18N
                    || className.equals(
    "org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner")) {  //NOI18N
                TaskStructure[] nestedElems = taskStructure.getChildren();
                for (TaskStructure ts : nestedElems) {
                    if (ts.getName().equals("jvmarg")) {                //NOI18N
                        String value = ts.getAttribute("value");        //NOI18N
                        if ((value != null)
                                && event.evaluate(value)
                                   .equals("-Xdebug")) {                //NOI18N
                            return TaskType.DEBUGGING_TEST_TASK;
                        }
                    }
                }
                return TaskType.TEST_TASK;
            }
            
            return TaskType.OTHER_TASK;
        }
        
        assert false : "Unhandled task name";                           //NOI18N
        return TaskType.OTHER_TASK;
    }
    
    /**
     * Tells whether the given task type is a test task type or not.
     *
     * @param  taskType  taskType to be checked; may be {@code null}
     * @return  {@code true} if the given task type marks a test task;
     *          {@code false} otherwise
     */
    private static boolean isTestTaskType(TaskType taskType) {
        return (taskType != null) && (taskType != TaskType.OTHER_TASK);
    }
    
    public boolean interestedInScript(File script, AntSession session) {
        return true;
    }
    
    public int[] interestedInLogLevels(AntSession session) {
        return LEVELS_OF_INTEREST;
    }
    
    /**
     */
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
        return isTestTaskType(
                getSessionInfo(event.getSession()).currentTaskType);
    }
    
    /**
     */
    public void taskStarted(final AntEvent event) {
        TaskType taskType = detectTaskType(event);
        if (isTestTaskType(taskType)) {
            AntSessionInfo sessionInfo = getSessionInfo(event.getSession());
            assert !isTestTaskType(sessionInfo.currentTaskType);
            sessionInfo.timeOfTestTaskStart = System.currentTimeMillis();
            sessionInfo.currentTaskType = taskType;
            if (sessionInfo.sessionType == null) {
                sessionInfo.sessionType = taskType;
            }
            
            /*
             * Count the test classes in the try-catch block so that
             * 'testTaskStarted(...)' is called even if counting fails
             * (throws an exception):
             */
            int testClassCount;
            try {
                testClassCount = TestCounter.getTestClassCount(event);
            } catch (Exception ex) {
                testClassCount = 0;
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
            
            final boolean hasXmlOutput = hasXmlOutput(event);
            getOutputReader(event).testTaskStarted(testClassCount, hasXmlOutput);
        }
    }
    
    /**
     */
    public void taskFinished(final AntEvent event) {
        AntSessionInfo sessionInfo = getSessionInfo(event.getSession());
        if (isTestTaskType(sessionInfo.currentTaskType)) {
            getOutputReader(event).testTaskFinished();
            sessionInfo.currentTaskType = null;
        }
        
    }
    
    /**
     */
    public void buildFinished(final AntEvent event) {
        AntSession session = event.getSession();
        AntSessionInfo sessionInfo = getSessionInfo(session);

        if (isTestTaskType(sessionInfo.sessionType)) {
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
    private JUnitOutputReader getOutputReader(final AntEvent event) {
        assert isTestTaskType(getSessionInfo(event.getSession()).sessionType);
        
        final AntSession session = event.getSession();
        final AntSessionInfo sessionInfo = getSessionInfo(session);
        JUnitOutputReader outputReader = sessionInfo.outputReader;
        if (outputReader == null) {
            outputReader = new JUnitOutputReader(
                                        session,
                                        sessionInfo.sessionType,
                                        sessionInfo.getTimeOfTestTaskStart());
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
    
    /**
     * Finds whether the test report will be generated in XML format.
     */
    private static boolean hasXmlOutput(AntEvent event) {
        final String taskName = event.getTaskName();
        if (taskName.equals(TASK_JUNIT)) {
            return hasXmlOutputJunit(event);
        } else if (taskName.equals(TASK_JAVA)) {
            return hasXmlOutputJava(event);
        } else {
            assert false;
            return false;
        }
    }
    
    /**
     * Finds whether the test report will be generated in XML format.
     */
    private static boolean hasXmlOutputJunit(AntEvent event) {
        TaskStructure taskStruct = event.getTaskStructure();
        for (TaskStructure child : taskStruct.getChildren()) {
            String childName = child.getName();
            if (childName.equals("formatter")) {                        //NOI18N
                String type = child.getAttribute("type");               //NOI18N
                String usefile = child.getAttribute("usefile");         //NOI18N
                if ((type != null) && type.equals("xml")                //NOI18N
                       && (usefile != null) && !AntProject.toBoolean(usefile)) {
                    String ifPropName = child.getAttribute("if");       //NOI18N
                    String unlessPropName =child.getAttribute("unless");//NOI18N

                    if ((ifPropName == null
                                || event.getProperty(ifPropName) != null)
                        && (unlessPropName == null
                                || event.getProperty(unlessPropName) == null)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Finds whether the test report will be generated in XML format.
     */
    private static boolean hasXmlOutputJava(AntEvent event) {
        TaskStructure taskStruct = event.getTaskStructure();
        
        String classname = taskStruct.getAttribute("classname");        //NOI18N
        if ((classname == null) ||
                !event.evaluate(classname).equals(ANT_TEST_RUNNER_CLASS_NAME)) {
            return false;
        }
        
        for (TaskStructure child : taskStruct.getChildren()) {
            String childName = child.getName();
            if (childName.equals("arg")) {                              //NOI18N
                String argValue = child.getAttribute("value");          //NOI18N
                if (argValue == null) {
                    continue;
                }
                argValue = event.evaluate(argValue);
                if (argValue.startsWith("formatter=")) {                //NOI18N
                    int clsNameStartIndex = "formatter=".length();      //NOI18N
                    if ((argValue.indexOf(',', clsNameStartIndex) == -1)
                            && argValue.substring(clsNameStartIndex)
                               .equals(XML_FORMATTER_CLASS_NAME)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
}
