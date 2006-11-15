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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 *
 * @author Kirill Sorokin
 */
public class LogManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LOG_FILE_PROPERTY       = "nbi.utils.log.file";
    public static final String LOG_LEVEL_PROPERTY      = "nbi.utils.log.level";
    public static final String LOG_TO_CONSOLE_PROPERTY = "nbi.utils.log.to.console";
    
    public static final String INDENT = "    ";
    
    public static final String  DEFAULT_LOG_FILE       = System.getProperty("user.home") + File.separator + ".nbi" + File.separator + "log" + File.separator + DateUtils.getTimestamp() + ".log";
    public static final int     DEFAULT_LOG_LEVEL      = ErrorLevel.DEBUG;
    public static final boolean DEFAULT_LOG_TO_CONSOLE = true;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static File    logFile;
    private static int     logLevel;
    private static boolean logToConsole;
    private static Writer  logWriter;
    
    private static boolean loggingAvailable;
    
    private static int     indent;
    
    private static boolean initialized;
    
    public static synchronized void indent() {
        indent++;
    }
    
    public static synchronized void unindent() {
        indent--;
    }
    
    public static synchronized void log(int level, String message) {
        if (!initialized) {
            initialize();
        }
        
        if (level <= logLevel) {
            BufferedReader reader = new BufferedReader(new StringReader(message));
            
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String string = "[" + DateUtils.getFormattedTimestamp() + "]: " + StringUtils.pad(INDENT, indent) + line + SystemUtils.getLineSeparator();
                    
                    if (loggingAvailable) {
                        logWriter.write(string);
                        logWriter.flush();
                    }
                    
                    if (logToConsole) {
                        System.out.print(string);
                    }
                }
            } catch (IOException e) {
                loggingAvailable = false;
                ErrorManager.notify(ErrorLevel.WARNING, "Error writing to the log file. Logging disabled.");
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
        StackTraceElement traceElement = new Exception().getStackTrace()[1];
        
        log(ErrorLevel.DEBUG, "entering -- " + 
                (traceElement.isNativeMethod() ? "[native] " : "") + 
                traceElement.getClassName() + "." + 
                traceElement.getMethodName() + "():" + 
                traceElement.getLineNumber());
        log(ErrorLevel.MESSAGE, message);
        indent();
    }
    
    public static synchronized void logExit(String message) {
        StackTraceElement traceElement = new Exception().getStackTrace()[1];
        
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
        log(message);
        unindent();
    }
    
    public static File getLogFile() {
        return logFile;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static synchronized void initialize() {
        // check for custom log file
        if (System.getProperty(LOG_FILE_PROPERTY) != null) {
            logFile = new File(System.getProperty(LOG_FILE_PROPERTY));
        } else {
            logFile = new File(DEFAULT_LOG_FILE);
        }
        
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
            logWriter = new OutputStreamWriter(new FileOutputStream(logFile));
            loggingAvailable = true;
        } catch (IOException e) {
            e.printStackTrace();
            loggingAvailable = false;
        }
        
        // set the initial indent
        indent = 0;
        
        // set the initialization marker
        initialized = true;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private LogManager() {
        // does nothing
    }
}