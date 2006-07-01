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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.netbeans.junit.*;
import org.openide.ErrorManager;

/** Verify that things delegation of ErrorManager to logging and back does not cause
 * stack overflows.
 *
 * @author Jaroslav Tulach
 */
public class NbErrorManagerCyclicDepTest extends NbTestCase {

    
    public NbErrorManagerCyclicDepTest(java.lang.String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Logger l = new LoggerAdapter("double");
        LogManager.getLogManager().addLogger(l);
    }

    public void testSendLogMsg() {
        ErrorManager e = ErrorManager.getDefault().getInstance("double");
        e.log(ErrorManager.WARNING, "Ahoj");
    }

    public void testSendNotify() {
        ErrorManager e = ErrorManager.getDefault().getInstance("double");
        Logger.global.log(Level.WARNING, null, new Exception("Ahoj"));
    }

    /** based on
     * https://thinnbeditor.dev.java.net/source/browse/thinnbeditor/thinnbeditor/src/net/java/dev/thinnbeditor/logging/LoggerAdapter.java?rev=1.1&view=auto&content-type=text/vnd.viewcvs-markup
     */
    private static final class LoggerAdapter extends Logger {
        private static final Map levelMap = new HashMap();
        private static final Map errorManagerMap = new TreeMap();
        private static final Map exceptionLevelMap = new HashMap();
        
        static {
            levelMap.put(Level.SEVERE, new Integer(ErrorManager.ERROR));
            levelMap.put(Level.WARNING, new Integer(ErrorManager.WARNING));
            levelMap.put(Level.INFO, new Integer(ErrorManager.INFORMATIONAL));
            levelMap.put(Level.CONFIG, new Integer(ErrorManager.INFORMATIONAL));
            levelMap.put(Level.FINE, new Integer(3));
            levelMap.put(Level.FINER, new Integer(2));
            levelMap.put(Level.FINEST, new Integer(1));
            
            for (Iterator i = levelMap.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry entry = (Map.Entry)i.next();
                errorManagerMap.put(entry.getValue(), entry.getKey());
            }
            
            errorManagerMap.put(new Integer(ErrorManager.INFORMATIONAL), Level.CONFIG);
            
            exceptionLevelMap.put(Level.SEVERE, new Integer(ErrorManager.USER));
            exceptionLevelMap.put(Level.WARNING, new Integer(ErrorManager.USER));
            exceptionLevelMap.put(Level.INFO, new Integer(ErrorManager.INFORMATIONAL));
            exceptionLevelMap.put(Level.CONFIG, new Integer(ErrorManager.INFORMATIONAL));
            exceptionLevelMap.put(Level.FINE, new Integer(3));
            exceptionLevelMap.put(Level.FINER, new Integer(2));
            exceptionLevelMap.put(Level.FINEST, new Integer(1));
        }
        
        private ErrorManager errorManager;
        private final Formatter formatter = new SimpleFormatter();
        
        public LoggerAdapter(String name) {
            super(name, null);
        }

        private void init() {
            if (errorManager != null) {
                return;
            }
            
            errorManager = ErrorManager.getDefault().getInstance(getName());
            
            for (Iterator i = errorManagerMap.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry entry = (Map.Entry)i.next();
                
                int level = ((Integer)entry.getKey()).intValue();
                
                if (errorManager.isLoggable(level)) {
                    setLevel((Level)entry.getValue());
                    break;
                }
            }
        }
        
        public void log(LogRecord record) {
            init();

            Integer l = (Integer)levelMap.get(record.getLevel());
            if (l == null) {
                l = Integer.valueOf(1);
            }
            errorManager.log(l.intValue(), formatter.format(record));
            
            if (record.getThrown() != null) {
                Integer x = (Integer)exceptionLevelMap.get(record.getLevel());
                if (x == null) {
                    x = new Integer(1);
                }
                errorManager.notify(x.intValue(), record.getThrown());
            }
        }
    }

}
