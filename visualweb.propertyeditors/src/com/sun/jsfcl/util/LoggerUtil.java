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
package com.sun.jsfcl.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LoggerUtil {

    protected static HashMap loggerUtils;
    
    protected Logger logger;
    
    static {
        loggerUtils = new HashMap();
        InputStream in = LoggerUtil.class.getResourceAsStream("/local-logging.properties"); // NOI18N
        if (in == null) {
            in = LoggerUtil.class.getResourceAsStream("/logging.properties"); // NOI18N
        }
        if (in != null) {
/*
 * Commented out until I can figure out how to get the configuration I want without slamming everyone else
            try {
                LogManager.getLogManager().readConfiguration(in);
            } catch (IOException e) {
            } finally {
                try {in.close();} catch (IOException e) {};
                in= null;
            }
*/
        }

    }
    
    public static LoggerUtil getLogger(String loggerName) {
        LoggerUtil loggerUtil = (LoggerUtil) loggerUtils.get(loggerName);
        if (loggerUtil == null) {
            loggerUtil = new LoggerUtil(Logger.getLogger(loggerName));
            loggerUtils.put(loggerName, loggerUtil);
        }
        return loggerUtil;
    }
    
    protected LoggerUtil(Logger logger) {
        
        this.logger = logger;
    }
    
    public boolean config(String message) {
        
        log(Level.CONFIG, message, null);
        return true;
    }
    
    public boolean config(String message, Throwable throwable) {
        
        log(Level.CONFIG, message, throwable);
        return true;
    }
    
    protected String[] inferCaller() {
        String lookFor = "com.sun.jsfcl.util.LoggerUtil";
        // Get the stack trace.
        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        // First, search back to a method in the Logger class.
        int ix = 0;
        while (ix < stack.length) {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if (cname.equals(lookFor)) {
                break;
            }
            ix++;
        }
        // Now search for the first frame before the "Logger" class.
        while (ix < stack.length) {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if (!cname.equals(lookFor)) {
                // We've found the relevant frame.
                return new String[] {cname, frame.getMethodName()};
            }
            ix++;
        }
        // We haven't found a suitable frame, so just punt.  This is
        // OK as we are only commited to making a "best effort" here.
        return new String[2];
    }

    public boolean info(String message) {
        
        log(Level.INFO, message, null);
        return true;
    }

    public boolean info(String message, Throwable throwable) {
        
        log(Level.INFO, message, throwable);
        return true;
    }

    public boolean log(Level level, String message, Throwable throwable) {
        if (!logger.isLoggable(level)) {
            return true;
        }
        String[] inferedCaller = inferCaller();
        logger.logp(level, inferedCaller[0], inferedCaller[1], message, throwable);
        return true;
    }
    
    public boolean severe(String message) {
        
        log(Level.SEVERE, message, null);
        return true;
    }

    public boolean severe(String message, Throwable throwable) {
        
        log(Level.SEVERE, message, throwable);
        return true;
    }

    public boolean warning(String message) {
        
        log(Level.WARNING, message, null);
        return true;
    }

    public boolean warning(String message, Throwable throwable) {
        
        log(Level.WARNING, message, throwable);
        return true;
    }

}
