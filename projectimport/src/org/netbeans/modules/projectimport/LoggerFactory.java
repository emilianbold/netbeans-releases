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

package org.netbeans.modules.projectimport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logger factory.
 *
 * @author mkrauskopf
 */
public class LoggerFactory {
    
    private static LoggerFactory factory = new LoggerFactory();
    
    // used level
    // TODO: mkrauskopf: this have to be customized (e.g. with a system property)
    private static Level LEVEL = Level.INFO;
    
    // TOTO: mkrauskopf - enhance this
    private static final int MAX_LEVEL_LENGTH = Level.WARNING.getName().length();
    
    /** Returns factory instance. */
    public static LoggerFactory getDefault() {
        return factory;
    }
    
    static {
        try {
            LEVEL = Level.parse(
                    System.getProperty("projectimport.logging.level")); // NOI18N
        } catch (IllegalArgumentException iae) {
            // use default level - INFO
        }
    }
    
    /**
     * Creates logger for the given class.
     */
    public Logger createLogger(Class clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(LEVEL);
        ch.setFormatter(new BasicFormatter());
        logger.addHandler(ch);
        logger.setUseParentHandlers(false);
        logger.setLevel(LEVEL);
        return logger;
    }
    
    
    private static class BasicFormatter extends Formatter {
        private Date dat = new Date();
        private final static String format = "yyyyMMdd_HHmmss z"; // NOI18N
        private DateFormat formatter = new SimpleDateFormat(format);
        
        public String format(LogRecord record) {
            StringBuffer sb = new StringBuffer();
            String name = record.getLevel().getName();
            // indent on the base of severity to have nice and readable output
            for (int i = name.length(); i < MAX_LEVEL_LENGTH; i++) {
                sb.append(" "); // NOI18N
            }
            // append severity
            sb.append("[" + name + "]: "); // NOI18N
            // append date and time (minimize memory allocations here)
            dat.setTime(record.getMillis());
            sb.append(formatter.format(dat));
            sb.append(" - "); // NOI18N
            // append class and method names
            if (record.getSourceClassName() != null) {
                sb.append(record.getSourceClassName());
            } else {
                sb.append(record.getLoggerName());
            }
            if (record.getSourceMethodName() != null) {
                sb.append("."); // NOI18N
                sb.append(record.getSourceMethodName());
                sb.append("()"); // NOI18N
            }
            // append stacktrace if there is any
            sb.append(": "); // NOI18N
            sb.append(record.getMessage());
            if (record.getThrown() != null) {
                try {
                    sb.append("\n  "); // NOI18N
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                }
            } else {
                sb.append("\n"); // NOI18N
            }
            return sb.toString();
        }
    }
    
}
