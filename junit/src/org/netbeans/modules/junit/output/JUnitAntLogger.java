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
    
    /** Ant task names we will pay attention to */
    private static final String[] TASKS_OF_INTEREST = {
        "junit"                                                         //NOI18N
    };
    
    /** default constructor for lookup */
    public JUnitAntLogger() { }
    
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    public String[] interestedInTasks(AntSession session) {
        return TASKS_OF_INTEREST;
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
        getOutputReader(event.getSession()).messageLogged(event.getMessage());
    }
    
    /**
     */
    public void taskStarted(final AntEvent event) {
        final AntSession session = event.getSession();
        getOutputReader(session).taskStarted(session);
    }
    
    /**
     */
    public void taskFinished(final AntEvent event) {
        getOutputReader(event.getSession()).taskFinished(event.getException());
    }
    
    /**
     * Retrieve existing or creates a new reader for the given session.
     *
     * @param  session  session to return a reader for
     * @return  output reader for the session
     */
    private JUnitOutputReader getOutputReader(AntSession session) {
        Object o = session.getCustomData(this);
        assert (o == null) || (o instanceof JUnitOutputReader);
        
        JUnitOutputReader outputReader;
        if (o == null) {
            outputReader = new JUnitOutputReader();
            session.putCustomData(this, outputReader);
        } else {
            outputReader = (JUnitOutputReader) o;
        }
        return outputReader;
    }
    
}
