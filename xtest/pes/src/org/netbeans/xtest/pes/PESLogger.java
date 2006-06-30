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

/*
 * PESLog.java
 *
 * Created on June 12, 2002, 6:02 PM
 */

package org.netbeans.xtest.pes;


import java.util.logging.*;
import java.io.*;
import java.util.Date;

/**
 *
 * @author  mb115822
 */
public class PESLogger {


    private static String PES_LOGGER_NAME = "PESLogger";

    public static Logger logger = Logger.getLogger(PES_LOGGER_NAME);
    static {
        PESLogger.setLoggerLevel("ALL");
    }

    public static boolean setConsoleLoggingLevel(String level) {
        return setConsoleLoggingLevel(Level.parse(level));
    }
    
    public static void setLoggerLevel(String level) {
        if (logger != null) {
            logger.setLevel(Level.parse(level));
        }
    }
    
    
    // set console logging level 
    public static boolean setConsoleLoggingLevel(Level level) {
        boolean result = false;
        
        if (logger != null) {            
            Logger myLogger = logger;                        
            while (myLogger != null) {
                Handler[] consoleHandlers = getConsoleHandlers(myLogger);
                for (int i=0; i <  consoleHandlers.length; i++) {
                    consoleHandlers[i].setLevel(level);
                    result = true;
                }
                myLogger = myLogger.getParent();                
            }
        }
        return result;
    }
    
    
    
    public static boolean addEmailLogger(String level, PESMailer mailer) {
        return addEmailLogger(Level.parse(level), mailer);
    }    
    
    // this needs to be implemented
    public static boolean removeEmailLogger() {
        throw new RuntimeException("removeEmailLogger not implemented ");
    }
    
    // add Email logger
    public static boolean addEmailLogger(Level level, PESMailer mailer) {
        if (level == null) {
            throw new IllegalArgumentException("Level cannot be null");
        }
        if (mailer == null) {
            throw new IllegalArgumentException("PESMailer cannot be null");            
        }
        // do the stuff
        if (logger != null) {
            MailHandler mailHandler = new PESLogger.MailHandler(level, mailer);
            PESLogger.logger.addHandler(mailHandler);
            return true;
        } 
        return false;
    }
    
    // return all console handlers associated with this logger
    private static Handler[] getConsoleHandlers(Logger logger) {
       Handler[] handlers = logger.getHandlers();
       int consoleHandlersCount = 0;
       for ( int i=0; i< handlers.length; i++) {       
            if (handlers[i] instanceof ConsoleHandler) {
                consoleHandlersCount++;
            } else {
                handlers[i] = null;
            }
        }
       // create new array
       Handler[] consoleHandlers = new Handler[consoleHandlersCount];
       for (int i=0, j=0; i < handlers.length; i++) {
           if (handlers[i] != null) {
               consoleHandlers[j] = handlers[i];
               j++;
           }
       }
       return consoleHandlers;
    }
    
    
    /** this class has only static methods **/
    private PESLogger() {
    }
    
        // custom handler, which is able to send email messages when warrnings/severe logs appears
    private static class MailHandler extends Handler {
        /**
         * Set up the connection between the stream
         * handler and the stream window: log data
         * written to the handler goes to the window
         */
        
        private PESMailer mailer;
        
        public MailHandler(PESMailer mailer) {
            this(PES_LOGGER_NAME, mailer);
        }

        public MailHandler( Level level, PESMailer mailer) {
            this(PES_LOGGER_NAME, level, mailer);
        }
        
        public MailHandler( String loggerName, PESMailer mailer) {
            this(loggerName,Level.WARNING, mailer);
        }
        
        
        /**
         * Set up the connection between the stream
         * handler and the stream window: log data
         * written to the handler goes to the window
         */
        public MailHandler( String loggerName, Level level, PESMailer mailer) {
            this.mailer = mailer;
            logger = Logger.getLogger( loggerName );
            // Get the output stream that feeds the window
            // and install it in the Stream handler                                    
            setLevel( level );           
        }
        
        
        /**
         * Log a LogRecord. We send email after every log
         * because we want to see log messages as soon as
         * they arrive
         */
        public void publish( LogRecord lr ) {
            // Check any filter, and possibly other criteria,
            // before publishing
            if (!isLoggable( lr ))
                return;
            // send the email
            if (mailer != null) {
                PESMailer.Message mailMessage = new PESMailer.Message();
                String levelName = lr.getLevel().getName();
                String time = (new Date(lr.getMillis())).toString();
                mailMessage.setSubject("PES notification");
                StringBuffer message = new StringBuffer();
                message.append("PES logged '");
                message.append(levelName);
                message.append("' message at ");
                message.append(time);
                message.append("\n\nSource: ");
                message.append(lr.getSourceClassName());
                message.append('.');
                message.append(lr.getSourceMethodName());
                message.append("()\n\nMessage: ");
                message.append(lr.getMessage());
                
                Throwable t = lr.getThrown();
                if (t != null) {
                    message.append("\n\nThrown exception:\n\n");
                    message.append("Message: "+t.getMessage()+"\n\n");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintWriter writer = new PrintWriter(baos);
                    t.printStackTrace(writer);
                    writer.close();
                    try {
                    baos.close();
                    } catch (IOException ioe) {
                        // strange this should not happen
                        System.err.println("PESLogger caught IOException when getting throwable's stacktrace"+ioe);
                    }                    
                    message.append("Stacktrace: \n");
                    message.append(baos.toString());                    
                }
                 
                
                mailMessage.setMessage(message.toString());
                try {
                    mailer.send(mailMessage);
                } catch (IOException ioe) {
                    // mail was not sent 
                    System.err.println("PESLogger is not able to send email with subject "+mailMessage.getSubject()+" to "+mailMessage.getToAddress());
                }
            }            
        }
        /**
         * De-install this Handler from its Logger
         */
        private void removeHandler() {
            logger.removeHandler( this );
        }
        
        /** Close the <tt>Handler</tt> and free all associated resources.
         * <p>
         * The close method will perform a <tt>flush</tt> and then close the
         * <tt>Handler</tt>.   After close has been called this <tt>Handler</tt>
         * should no longer be used.  Method calls may either be silently
         * ignored or may throw runtime exceptions.
         *
         * @exception  SecurityException  if a security manager exists and if
         *             the caller does not have <tt>LoggingPermission("control")</tt>.
         */
        public void close() throws SecurityException {
        }
        
        /** Flush any buffered output.
         */
        public void flush() {
        }
        
    }

}
