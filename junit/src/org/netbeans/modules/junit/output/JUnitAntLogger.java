/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;

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
    
    /** */
    private static final String[] JUNIT_STRARR = {"junit"};             //NOI18N
    /** */
    private static final String[] JAVA_STRARR = {"java"};               //NOI18N
    
    /** default constructor for lookup */
    public JUnitAntLogger() { }
    
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    public String[] interestedInTasks(AntSession session) {
        final int sessionType = getSessionType(session);
        return (sessionType == AntSessionInfo.SESSION_TYPE_TEST)
               ? JUNIT_STRARR  //{"junit"}
               : (sessionType == AntSessionInfo.SESSION_TYPE_DEBUG_TEST)
                 ? JAVA_STRARR //{"java"}
                 : AntLogger.NO_TASKS;
    }
    
    /**
     * Detects type of the given Ant session.
     * Recognized types are:
     * <ul>
     *     <li>test session</li>
     *     <li>test debugging session</li>
     * </ul>
     * Session types are recognized by the sessions' originating targets.
     *
     * @param  session  session whose type is to be recognized
     * @return  <code>SESSION_TYPE_UNKNOWN</code> if the session type
     *                                                            is unknown,
     *          <code>SESSION_TYPE_OTHER</code> if the session is a test
     *                                                            session,<br />
     *          <code>SESSION_TYPE_DEBUG_TEST</code> if the session is a test
     *                                                  debugging session,<br />
     *          <code>SESSION_TYPE_OTHER</code> otherwise
     * @see  AntSession#getOriginatingTargets()
     */
    private static int detectSessionType(final AntSession session) {
        final String[] originatingTargets = session.getOriginatingTargets();
        if (originatingTargets.length == 0) {
            return AntSessionInfo.SESSION_TYPE_UNKNOWN;
        }
        if (originatingTargets.length == 1) {
            final String origTarget = originatingTargets[0];
            if (origTarget.startsWith("test")                           //NOI18N
                && ((origTarget.length() == 4)
                    || !Character.isLetter(origTarget.charAt(4)))
                || origTarget.equals("run-tests")) {                    //NOI18N
                /*
                 * Target names:
                 *    "test", "test-single"  (J2SE projects, NB module projects)
                 *    "run-tests"            (many freeform projects)
                 */
                return AntSessionInfo.SESSION_TYPE_TEST;
            } else if (origTarget.startsWith("debug-test")              //NOI18N
                       && ((origTarget.length() == 10)
                           || !Character.isLetter(origTarget.charAt(10)))) {
                /*
                 * Target names:
                 *    "debug-test"           (J2SE projects)
                 *    "debug-test-single-nb" (NB module projects)
                 */
                return AntSessionInfo.SESSION_TYPE_DEBUG_TEST;
            }
        }
        return AntSessionInfo.SESSION_TYPE_OTHER;
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
        if (event.getLogLevel() != AntEvent.LOG_VERBOSE) {
            getOutputReader(event.getSession()).messageLogged(event);
        } else {
            /* verbose messages are logged no matter which task produced them */
            verboseMessageLogged(event);
        }
    }
    
    /**
     */
    private void verboseMessageLogged(final AntEvent event) {
        final String currTask = event.getTaskName();
        if (currTask == null) {
            return;
        }

        final AntSession session = event.getSession();
        final String[] myTasks = interestedInTasks(session);
        for (int i = 0; i < myTasks.length; i++) {
            if (currTask.equals(myTasks[i])) {
                getOutputReader(session).verboseMessageLogged(event);
                break;
            }
        }
    }
    
    /**
     */
    public void taskStarted(final AntEvent event) {
        getOutputReader(event.getSession()).testTaskStarted();
    }
    
    /**
     */
    public void buildFinished(final AntEvent event) {
        final AntSession session = event.getSession();
        final AntSessionInfo sessionInfo = getSessionInfo(session);
        final int sessionType = sessionInfo.sessionType;

        if ((sessionType != AntSessionInfo.SESSION_TYPE_UNKNOWN)
                && (sessionType != AntSessionInfo.SESSION_TYPE_OTHER)) {
            getOutputReader(event.getSession()).buildFinished(event);
        }
        
        session.putCustomData(this, null);          //forget AntSessionInfo
    }
    
    /**
     */
    private int getSessionType(final AntSession session) {
        final AntSessionInfo sessionInfo = getSessionInfo(session);
        assert sessionInfo != null;
        
        int sessionType = sessionInfo.sessionType;
        if (sessionType == AntSessionInfo.SESSION_TYPE_UNKNOWN) {
            sessionType = detectSessionType(session);
            if (sessionType != AntSessionInfo.SESSION_TYPE_UNKNOWN) {
                sessionInfo.sessionType = sessionType;
                if (sessionType != AntSessionInfo.SESSION_TYPE_OTHER) {
                    getOutputReader(session).testTargetStarted();
                }
            }
        }
        return sessionType;
    }
    
    /**
     * Retrieve existing or creates a new reader for the given session.
     *
     * @param  session  session to return a reader for
     * @return  output reader for the session
     */
    private JUnitOutputReader getOutputReader(final AntSession session) {
        final AntSessionInfo sessionInfo = getSessionInfo(session);
        JUnitOutputReader outputReader = sessionInfo.outputReader;
        if (outputReader == null) {
            outputReader = new JUnitOutputReader(
                                        session,
                                        getSessionType(session),
                                        sessionInfo.getTimeOfSessionStart());
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
