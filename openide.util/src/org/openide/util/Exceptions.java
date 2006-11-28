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
package org.openide.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
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
     * can be extracted later by using {@link #findLocalizedMessage}.
     *
     * @param e exception to annotate
     * @param localizedMessage the localized message to add to the exception
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
     * Complements {@link #attachLocalizedMessage}.
     *
     * @param t the exception to search for a message in
     * @return localized message attached to provided exception or <code>null</code>
     *   if no such message has been attached
     */
    public static String findLocalizedMessage(Throwable t) {
        while (t != null) {
            String msg;
            AnnException extra = AnnException.extras.get(t);
            if (extra != null) {
                msg = extractLocalizedMessage(extra);
            } else {
                msg = extractLocalizedMessage(t);
            }
            
            if (msg != null) {
                return msg;
            }
            
            t = t.getCause();
        }
        return null;
    }

    private static String extractLocalizedMessage(final Throwable t) {
        String msg = null;
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
                        msg = b.getString(r.getMessage());
                        break;
                    }
                }
            }
        }
        return msg;
    }
    
    /** Notifies an exception with a severe level. Such exception is going
     * to be printed to log file and possibly also notified to alarm the
     * user somehow.
     *
     * @param t the exception to notify
     */
    public static void printStackTrace(Throwable t) {
        AnnException extra = AnnException.extras.get(t);
        if (extra != null) {
            assert t == extra.getCause();
            t = extra;
        }
        Logger.global.log(OwnLevel.UNKNOWN, null, t);
    }

    /** An exception that has a log record associated with itself, so
     * the NbErrorManager can extract info about the annotation.
     */
    private static final class AnnException extends Exception implements Callable<LogRecord[]> {
        private List<LogRecord> records;
        
        private AnnException() {
            super();
        }
        
        private AnnException(String msg) {
            super(msg);
        }

        @Override
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
        
        
        /** additional mapping from throwables that refuse initCause call */
        private static Map<Throwable, AnnException> extras = new WeakHashMap<Throwable, AnnException>();

        static AnnException findOrCreate(Throwable t, boolean create) {
            if (t instanceof AnnException) {
                return (AnnException)t;
            }
            if (t.getCause() == null) {
                if (create) {
                    try {
                        t.initCause(new AnnException());
                    } catch (IllegalStateException x) {
                        AnnException ann = extras.get(t);
                        if (ann == null) {
                            ann = new AnnException(t.getMessage());
                            ann.initCause(t);
                            Logger.getLogger(Exceptions.class.getName()).log(Level.FINE, "getCause was null yet initCause failed for " + t, x);
                            extras.put(t, ann);
                        }
                        return ann;
                    }
                }
                return (AnnException)t.getCause();
            }
            return findOrCreate(t.getCause(), create);
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

        @Override
        public void printStackTrace(PrintStream s) {
            super.printStackTrace(s);
            logRecords(s);
        }

        @Override
        public void printStackTrace(PrintWriter s) {
            super.printStackTrace(s);
            logRecords(s);
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }

        @Override
        public String toString() {
            return getMessage();
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
