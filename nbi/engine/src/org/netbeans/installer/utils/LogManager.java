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
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 *
 * @author Kirill Sorokin
 */
public final class LogManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LOG_LEVEL_PROPERTY      = "nbi.utils.log.level";
    public static final String LOG_TO_CONSOLE_PROPERTY = "nbi.utils.log.to.console";
    
    public static final String INDENT = "    ";
    
    public static final int     DEFAULT_LOG_LEVEL      = ErrorLevel.DEBUG;
    public static final boolean DEFAULT_LOG_TO_CONSOLE = true;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static File         logFile      = null;
    private static PrintWriter  logWriter    = null;
    private static int          logLevel     = DEFAULT_LOG_LEVEL;
    private static boolean      logToConsole = DEFAULT_LOG_TO_CONSOLE;
    
    private static int          indent       = 0;
    
    private static boolean      started      = false;
    
    private static List<String> logCache     = new LinkedList<String>();
    
    public static synchronized void start() {
        // check for custom log level
        if (System.getProperty(LOG_LEVEL_PROPERTY) != null) {
            try {
                logLevel = Integer.parseInt(System.getProperty(LOG_LEVEL_PROPERTY));
            } catch (NumberFormatException e) {
                logLevel = DEFAULT_LOG_LEVEL;
            }
        } else {
            logLevel = DEFAULT_LOG_LEVEL;
        }
        
        // check whether we should log to console as well
        if (System.getProperty(LOG_TO_CONSOLE_PROPERTY) != null) {
            logToConsole = new Boolean(System.getProperty(LOG_TO_CONSOLE_PROPERTY));
        } else {
            logToConsole = DEFAULT_LOG_TO_CONSOLE;
        }
        
        // init the log file and streams
        try {
            logFile.getParentFile().mkdirs();
            logFile.createNewFile();
            logWriter = new PrintWriter(new FileWriter(logFile));
            
            // here is a small assumption that there will be no calls to log*(*)
            // during the cache dumping. Otherwise we'll get a concurrent
            // modification exception
            for (String string: logCache) {
                write(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logWriter = null;
        }
        
        started = true;
    }
    
    public static synchronized void indent() {
        indent++;
    }
    
    public static synchronized void unindent() {
        indent--;
    }
    
    public static synchronized void log(int level, String message) {
        if (level <= logLevel) {
            BufferedReader reader = new BufferedReader(new StringReader(message));
            
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String string =
                            "[" + DateUtils.getFormattedTimestamp() + "]: " +
                            StringUtils.pad(INDENT, indent) + line;
                    
                    if (started) {
                        write(string);
                    } else {
                        logCache.add(string);
                    }
                }
            } catch (IOException e) {
                logWriter = null;
                ErrorManager.notifyWarning("Error writing to the log file. Logging disabled.");
            }
        }
    }
    
    public static synchronized void log(int level, Throwable exception) {
        log(level, StringUtils.asString(exception));
    }
    
    public static synchronized void log(int level, Object object) {
        log(level, object.toString());
    }
    
    public static synchronized void log(String message) {
        log(ErrorLevel.MESSAGE, message);
    }
    
    public static synchronized void log(Throwable exception) {
        log(ErrorLevel.MESSAGE, exception);
    }
    
    public static synchronized void log(Object object) {
        log(ErrorLevel.MESSAGE, object);
    }
    
    public static synchronized void log(String message, Throwable exception) {
        log(message);
        log(exception);
    }
    
    public static synchronized void logEntry(String message) {
        StackTraceElement traceElement = Thread.currentThread().getStackTrace()[2];
        
        log(ErrorLevel.DEBUG, "entering -- " +
                (traceElement.isNativeMethod() ? "[native] " : "") +
                traceElement.getClassName() + "." +
                traceElement.getMethodName() + "():" +
                traceElement.getLineNumber());
        log(ErrorLevel.MESSAGE, message);
        indent();
    }
    
    public static synchronized void logExit(String message) {
        StackTraceElement traceElement = Thread.currentThread().getStackTrace()[2];
        
        unindent();
        log(message);
        log(ErrorLevel.DEBUG, "exiting -- " +
                (traceElement.isNativeMethod() ? "[native] " : "") +
                traceElement.getClassName() + "." +
                traceElement.getMethodName() + "():" +
                traceElement.getLineNumber());
    }
    
    public static synchronized void logIndent(String message) {
        log(message);
        indent();
    }
    
    public static synchronized void logUnindent(String message) {
        unindent();
        log(message);
    }
    
    public static File getLogFile() {
        return logFile;
    }
    
    public static void setLogFile(final File logFile) {
        LogManager.logFile = logFile;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static void write(String string) throws IOException {
        if (logWriter != null) {
            logWriter.println(string);
            logWriter.flush();
        }
        
        if (logToConsole) {
            System.out.println(string);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private LogManager() {
        // does nothing
    }
}