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
package org.openide.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/** Useful utility and methods to work with exceptions.
 * Allows to annotate exceptions with messages, extract such messages
 * and provides a common utility method to report an exception.
 *
 * @since 7.2
 */
public final class Exceptions extends Object {
    private Exceptions() {
    }

    /** Attaches additional message to given exception. This message will
     * be visible when one does <code>e.printStackTrace()</code>.
     *
     * @param e exception to annotate
     * @param msg the message to add to the exception
     * @return the exception <code>e</code>
     */
    public static <E extends Throwable> E attachMessage(E e, String msg) {
        AnnException ae = AnnException.findOrCreate(e, true);
        LogRecord rec = new LogRecord(Level.ALL, msg);
        ae.addRecord(rec);
        return e;
    }

    /** Attaches additional localized message to given exception. This message 
     * can be extracted later by using {@link Exceptions.findLocalizedMessage}.
     *
     * @param e exception to annotate
     * @param msg the localized message to add to the exception
     * @return the exception <code>e</code>
     */
    public static <E extends Throwable> E attachLocalizedMessage(E e, final String localizedMessage) {
        AnnException ae = AnnException.findOrCreate(e, true);
        LogRecord rec = new LogRecord(Level.ALL, "msg"); // NOI18N
        ResourceBundle rb = new ResourceBundle() {
            public Object handleGetObject(String key) {
                if ("msg".equals(key)) { // NOI18N
                    return localizedMessage;
                } else {
                    return null;
                }
            }

            public Enumeration<String> getKeys() {
                return Enumerations.singleton("msg"); // NOI18N
            }
        };
        rec.setResourceBundle(rb);
        ae.addRecord(rec);
        return e;
    }

    /** Extracts previously attached localized message for a given throwable.
     * Complements the {@link Exceptions.attachLocalizedMessage} method.
     *
     * @param t the exception to search for a message in
     * @return localized message attached to provided exception or <code>null</code>
     *   if no such message has been attached
     */
    public static String findLocalizedMessage(Throwable t) {
        while (t != null) {
            if (t instanceof Callable) {
                Object res = null;
                try {
                    res = ((Callable) t).call();
                } catch (Exception ex) {
                    Logger.global.log(Level.WARNING, null, t);
                }
                if (res instanceof LogRecord[]) {
                    for (LogRecord r : (LogRecord[])res) {
                        ResourceBundle b = r.getResourceBundle();
                        if (b != null) {
                            String msg = b.getString(r.getMessage());
                            return msg;
                        }
                    }
                }
            }
            t = t.getCause();
        }
        return null;
    }
    
    /** Notifies an exception with a severe level. Such exception is going
     * to be printed to log file and possibly also notified to alarm the
     * user somethow.
     *
     * @param t the exception to notify
     */
    public static void printStackTrace(Throwable t) {
        Logger.global.log(OwnLevel.UNKNOWN, null, t);
    }

    /** An exception that has a log record associated with itself, so
     * the NbErrorManager can extract info about the annotation.
     */
    private static final class AnnException extends Exception implements Callable<LogRecord[]> {
        private List<LogRecord> records;

        public String getMessage() {
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (LogRecord r : records) {
                if (r.getMessage() != null) {
                    sb.append(sep);
                    sb.append(r.getMessage());
                    sep = "\n";
                }
            }
            return sb.toString();
        }

        static AnnException findOrCreate(Throwable t, boolean create) {
            if (t instanceof AnnException) {
                return (AnnException)t;
            }
            if (t.getCause() == null) {
                if (create) {
                    try {
                        t.initCause(new AnnException());
                    } catch (IllegalStateException x) {
                        Logger.getLogger(Exceptions.class.getName()).log(Level.WARNING, "getCause was null yet initCause failed for " + t, x);
                        return new AnnException();
                    }
                }
                return (AnnException)t.getCause();
            }
            return findOrCreate(t.getCause(), create);
        }

        private AnnException() {
        }

        public synchronized void addRecord(LogRecord rec) {
            if (records == null) {
                records = new ArrayList<LogRecord>();
            }
            records.add(rec);
        }

        public LogRecord[] call() {
            List<LogRecord> r = records;
            LogRecord[] empty = new LogRecord[0];
            return r == null ? empty : r.toArray(empty);
        }

        public void printStackTrace(PrintStream s) {
            super.printStackTrace(s);
            logRecords(s);
        }

        public void printStackTrace(PrintWriter s) {
            super.printStackTrace(s);
            logRecords(s);
        }

        public void printStackTrace() {
            printStackTrace(System.err);
        }

        private void logRecords(Appendable a) {
            List<LogRecord> r = records;
            if (r == null) {
                return;
            }
            try {

                for (LogRecord log : r) {
                    if (log.getMessage() != null) {
                        a.append(log.getMessage()).append("\n");;
                    }
                    if (log.getThrown() != null) {
                        StringWriter w = new StringWriter();
                        log.getThrown().printStackTrace(new PrintWriter(w));
                        a.append(w.toString()).append("\n");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    } // end AnnException
    private static final class OwnLevel extends Level {
        public static final Level UNKNOWN = new OwnLevel("SEVERE", Level.SEVERE.intValue() + 1); // NOI18N

        private OwnLevel(String s, int i) {
            super(s, i);
        }
    } // end of OwnLevel
}
