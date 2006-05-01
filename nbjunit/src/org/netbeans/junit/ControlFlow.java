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

package org.netbeans.junit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestResult;

/** Basic skeleton for logging test case.
 *
 * @author  Jaroslav Tulach
 */
final class ControlFlow extends Object {
    /** Registers hints for controlling thread switching in multithreaded
     * applications.
     * @param url the url to read the file from
     * @exception IOException thrown when there is problem reading the url
     */
    static void registerSwitches(Logger listenTo, Logger reportTo, URL url, int timeout) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        for (;;) {
            int ch = is.read ();
            if (ch == -1) break;
            os.write (ch);
        }
        os.close();
        is.close();
        
        registerSwitches(listenTo, reportTo, new String(os.toByteArray(), "utf-8"), timeout);
    }
    
    /** Registers hints for controlling thread switching in multithreaded
     * applications.
    
     */
    static void registerSwitches(Logger listenTo, Logger reportTo, String order, int timeout) {
        LinkedList switches = new LinkedList();
        
        HashMap exprs = new HashMap();
        
        int pos = 0;
        for(;;) {
            int thr = order.indexOf("THREAD:", pos);
            if (thr == -1) {
                break;
            }
            int msg = order.indexOf("MSG:", thr);
            if (msg == -1) {
                Assert.fail("After THREAD: there must be MSG: " + order.substring(thr));
            }
            int end = order.indexOf("THREAD:", msg);
            if (end == -1) {
                end = order.length();
            }
            
            String thrName = order.substring(pos + 7, msg).trim();
            String msgText = order.substring(msg + 4, end).trim();
            
            Pattern p = (Pattern)exprs.get(msgText);
            if (p == null) {
                p = Pattern.compile(msgText);
                exprs.put(msgText, p);
            }
            
            Switch s = new Switch(thrName, p);
            switches.add(s);
            
            pos = end;
        }

        ErrManager h = new ErrManager(switches, listenTo, reportTo, timeout);
        listenTo.addHandler(h);
    }

    //
    // Logging support
    //
    private static final class ErrManager extends Handler {
        private LinkedList switches;
        private int timeout;
        /** maps names of threads to their instances*/
        private java.util.Map threads = new java.util.HashMap();

        /** the logger to send internal messages to, if any */
        private Logger msg;


        /** assigned to */
        private Logger assigned;

        public ErrManager (LinkedList switches, Logger assigned, Logger msg, int t) {
            this.switches = switches;
            this.msg = msg;
            this.timeout = t;
            this.assigned = assigned;
            setLevel(Level.FINEST);
        }
        
        public void publish (LogRecord record) {
            if (switches == null) {
                assigned.removeHandler(this);
                return;
            }


            String s = record.getMessage();
            if (s != null && record.getParameters() != null) {
                s = MessageFormat.format(s, record.getParameters());
            }

            boolean log = msg != null;
            boolean expectingMsg = false;
            for(;;) {
                synchronized (switches) {
                    if (switches.isEmpty()) {
                        return;
                    }


                    Switch w = (Switch)switches.getFirst();
                    String threadName = Thread.currentThread().getName();
                    boolean foundMatch = false;

                    if (w.matchesThread()) {
                        if (!w.matchesMessage(s)) {
                            // same thread but wrong message => go on
                            return;
                        }
                        // the correct message from the right thread found
                        switches.removeFirst();
                        if (switches.isEmpty()) {
                            // end of sample, make all run
                            switches.notifyAll();
                            return;
                        }
                        w = (Switch)switches.getFirst();
                        if (w.matchesThread()) {
                            // next message is also from this thread, go on
                            return;
                        }
                        expectingMsg = true;
                        foundMatch = true;
                    } else {
                        // compute whether we shall wait or not
                        java.util.Iterator it = switches.iterator();
                        while (it.hasNext()) {
                            Switch check = (Switch)it.next();
                            if (check.matchesMessage(s)) {
                                expectingMsg = true;
                                break;
                            }
                        }                            
                    }

                    // make it other thread run
                    Thread t = (Thread)threads.get(w.name);
                    if (t != null) {
                        if (log) {
                            loginternal("t: " + threadName + " interrupts: " + t.getName());
                        }
                        t.interrupt();
                    }
                    threads.put(threadName, Thread.currentThread());

//                        
//                        if (log) {
//                            loginternal("t: " + Thread.currentThread().getName() + " log: " + s + " result: " + m + " for: " + w + "\n");
//                        }
                    if (!expectingMsg) {
                        return;
                    }

                    // clear any interrupt that happend before
                    Thread.interrupted();
                    try {
                        if (log) {
                            loginternal("t: " + threadName + " log: " + s + " waiting");
                        }
                        switches.wait(timeout);
                        if (log) {
                            loginternal("t: " + threadName + " log: " + s + " timeout");
                        }
                        return;
                    } catch (InterruptedException ex) {
                        // ok, we love to be interrupted => go on
                        if (log) {
                            loginternal("t: " + threadName + " log: " + s + " interrupted");
                        }
                        if (foundMatch) {
                            return;
                        }
                    }
                }
            }
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }

        private void loginternal(String string) {
            msg.info(string);
        }
        
    } // end of ErrManager
    
    private static final class Switch {
        private Pattern msg;
        private String name;
        
        public Switch(String n, Pattern m) {
            this.name = n;
            this.msg = m;
        }
        
        /** @return true if the thread name of the caller matches this switch
         */
        public boolean matchesThread() {
            String thr = Thread.currentThread().getName();
            return name.equals(thr);
        }
        
        /** @return true if the message matches the one provided by this switch
         */
        public boolean matchesMessage(String logMsg) {
            return msg.matcher(logMsg).matches();
        }
        
        public String toString() {
            return "Switch[" + name + "]: " + msg;
        }
    }
}
