/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
        AntEvent.LOG_INFO
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
                    || !Character.isLetter(origTarget.charAt(4)))) {
                /*
                 * Target names:
                 *    "test", "test-single"  (J2SE projects, NB module projects)
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
        final AntSession session = event.getSession();
        if (getOutputReader(session).messageLogged(event.getMessage())) {
            Manager.getInstance().reportStarted(session,
                                                getSessionType(session));
        }
    }
    
    /**
     */
    public void taskStarted(final AntEvent event) {
        final AntSession session = event.getSession();
        Manager.getInstance().taskStarted(session,
                                          getSessionType(session));
    }
    
    /**
     */
    public void buildFinished(final AntEvent event) {
        final AntSession session = event.getSession();
        final JUnitOutputReader reader = getOutputReader(session);
        
        reader.finishReport(event.getException());
        //PENDING: status - may be shown in the output window
        Manager.getInstance().sessionFinished(session, reader.getReport());
        
        session.putCustomData(this, null);
    }
    
    /**
     */
    private int getSessionType(AntSession session) {
        AntSessionInfo sessionInfo = getSessionInfo(session);
        return (sessionInfo != null) ? sessionInfo.sessionType
                                     : AntSessionInfo.SESSION_TYPE_UNKNOWN;
    }
    
    /**
     * Retrieve existing or creates a new reader for the given session.
     *
     * @param  session  session to return a reader for
     * @return  output reader for the session
     */
    private JUnitOutputReader getOutputReader(AntSession session) {
        return getSessionInfo(session).outputReader;
    }
    
    /**
     */
    private AntSessionInfo getSessionInfo(AntSession session) {
        Object o = session.getCustomData(this);
        assert (o == null) || (o instanceof AntSessionInfo);
        
        AntSessionInfo sessionInfo;
        if (o != null) {
            sessionInfo = (AntSessionInfo) o;
        } else {
            int sessionType = detectSessionType(session);
            if (sessionType == AntSessionInfo.SESSION_TYPE_UNKNOWN) {
                sessionInfo = null;
            } else {
                sessionInfo = new AntSessionInfo(new JUnitOutputReader(session),
                                                 sessionType);
                session.putCustomData(this, sessionInfo);
            }
        }
        return sessionInfo;
    }
    
}
