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

package org.netbeans.modules.openide.explorer;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.util.Exceptions;

/** Don't use this class outside fo core, copy its impl, if you really think
 * you need it. Prefered way is to use DialogDisplayer.notifyLater(...);
 *
 * @author Jaroslav Tulach
 */
public final class UIException {
    
    /** Creates a new instance of UIException */
    private UIException() {
    }
    
    public static void annotateUser(
        Throwable t,
        String msg,
        String locMsg,
        Throwable stackTrace,
        Date date
    ) {
        AnnException ex = AnnException.findOrCreate(t, true);
        LogRecord rec = new LogRecord(OwnLevel.USER, msg);
        if (stackTrace != null) {
            rec.setThrown(stackTrace);
        }
        ex.addRecord(rec);
        
        if (locMsg != null) {
            Exceptions.attachLocalizedMessage(t, locMsg);
        }
    }
    private static final class OwnLevel extends Level {
        public static final Level USER = new OwnLevel("USER", 1973); // NOI18N

        private OwnLevel(String s, int i) {
            super(s, i);
        }
    } // end of UserLevel
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
                    t.initCause(new AnnException());
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
                        a.append(log.getMessage()).append('\n');
                    }
                    if (log.getThrown() != null) {
                        StringWriter w = new StringWriter();
                        log.getThrown().printStackTrace(new PrintWriter(w));
                        a.append(w.toString()).append('\n');
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    } // end AnnException
}

