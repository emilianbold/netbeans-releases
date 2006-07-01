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

package org.openide.loaders;
import java.beans.PropertyChangeEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import junit.framework.AssertionFailedError;
import junit.framework.TestResult;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/** Basic skeleton for logging test case.
 *
 * @author  Jaroslav Tulach
 */
public abstract class LoggingTestCaseHid extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.loaders.LoggingTestCaseHid$Lkp");

        Logger l = Logger.getLogger("");
        Handler[] arr = l.getHandlers();
        for (int i = 0; i < arr.length; i++) {
            l.removeHandler(arr[i]);
        }
        l.addHandler(new ErrManager());
        l.setLevel(Level.ALL);
    }

    protected LoggingTestCaseHid (String name) {
        super (name);
    }
    
    public void run(TestResult result) {
        Lookup l = Lookup.getDefault();
        assertEquals("We can run only with our Lookup", Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        lkp.reset();
        
        super.run(result);
    }

    /** If execution fails we wrap the exception with 
     * new log message.
     */
    protected void runTest () throws Throwable {
        
        assertNotNull ("ErrManager has to be in lookup", Lookup.getDefault().lookup(ErrManager.class));
        
        ErrManager.clear(getName(), getLog());
        
        try {
            super.runTest ();
        } catch (AssertionFailedError ex) {
            AssertionFailedError ne = new AssertionFailedError (ex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (ex.getStackTrace ());
            throw ne;
        } catch (IOException iex) {//#66208
            IOException ne = new IOException (iex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (iex.getStackTrace ());
            throw ne;
        } catch (RuntimeException ex) {
            throw new InvocationTargetException(ex, ex.getMessage() + " Log:\n" + ErrManager.messages);
	} finally {
            // do not write to log files anymore
            ErrManager.clear(getName(), System.err);
        }
    }
    
    /** Allows subclasses to register content for the lookup. Can be used in 
     * setUp and test methods, after that the content is cleared.
     * @deprecated Please use {@link org.netbeans.junit.MockServices} instead.
     */
    protected final void registerIntoLookup(Object instance) {
        Lookup l = Lookup.getDefault();
        assertEquals("We can run only with our Lookup", Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        lkp.ic.add(instance);
    }
    
    /** Registers hints for controlling thread switching in multithreaded
     * applications.
     * @param url the url to read the file from
     * @exception IOException thrown when there is problem reading the url
     */
    protected final void registerSwitches(URL url, int timeout) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        for (;;) {
            int ch = is.read ();
            if (ch == -1) break;
            os.write (ch);
        }
        os.close();
        is.close();
        
        registerSwitches(new String(os.toByteArray(), "utf-8"), timeout);
    }
    
    /** Registers hints for controlling thread switching in multithreaded
     * applications.
    
     */
    protected final void registerSwitches(String order, int timeout) {
        ErrManager.timeout = timeout;
        
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
                fail("After THREAD: there must be MSG: " + order.substring(thr));
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
        
        ErrManager.switches = switches;
    }

    //
    // Our fake lookup
    //
    public static final class Lkp extends ProxyLookup {
        InstanceContent ic;
        
        public Lkp () {
            super(new Lookup[0]);
        }
    
        public void reset() {
            this.ic = new InstanceContent();
            AbstractLookup al = new AbstractLookup(ic);

            ic.add (new ErrManager ());
            
            setLookups(new Lookup[] { al, Lookups.metaInfServices(getClass().getClassLoader()) });

        }
    }
    //
    // Logging support
    //
    public static final class ErrManager extends Handler {
        public static final StringBuffer messages = new StringBuffer ();
        static java.io.PrintStream log = System.err;
        
        private static LinkedList switches;
        private static int timeout;
        /** maps names of threads to their instances*/
        private static java.util.Map threads = new java.util.HashMap();
        
        public ErrManager () {
        }
        
        public void publish (LogRecord record) {
            String s = record.getMessage();

            StringBuffer oneMsg = new StringBuffer();
            if (record.getLoggerName() != null) {
                oneMsg.append(record.getLoggerName());
            } else {
                oneMsg.append("[default] ");
            }
            oneMsg.append("THREAD:");
            oneMsg.append(Thread.currentThread().getName());
            oneMsg.append(" MSG:");
            oneMsg.append(s);


            messages.append(oneMsg.toString());
            messages.append ('\n');

            if (messages.length() > 40000) {
                messages.delete(0, 20000);
            }

            log.println(oneMsg.toString());
            
            if (switches != null) {
                boolean log = true;
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
                                messages.append("t: " + threadName + " interrupts: " + t.getName() + "\n");
                            }
                            t.interrupt();
                        }
                        threads.put(threadName, Thread.currentThread());
                        
//                        
//                        if (log) {
//                            messages.append("t: " + Thread.currentThread().getName() + " log: " + s + " result: " + m + " for: " + w + "\n");
//                        }
                        if (!expectingMsg) {
                            return;
                        }

                        // clear any interrupt that happend before
                        Thread.interrupted();
                        try {
                            if (log) {
                                messages.append("t: " + threadName + " log: " + s + " waiting\n");
                            }
                            switches.wait(timeout);
                            if (log) {
                                messages.append("t: " + threadName + " log: " + s + " timeout\n");
                            }
                            return;
                        } catch (InterruptedException ex) {
                            // ok, we love to be interrupted => go on
                            if (log) {
                                messages.append("t: " + threadName + " log: " + s + " interrupted\n");
                            }
                            if (foundMatch) {
                                return;
                            }
                        }
                    }
                }
            }
        }

        static void resetMessages() {
            messages.setLength(0);
        }
        
        private static void clear(String n, PrintStream printStream) {
            ErrManager.log = printStream;
            ErrManager.messages.setLength(0);
            ErrManager.messages.append ("Starting test ");
            ErrManager.messages.append (n);
            ErrManager.messages.append ('\n');
            threads.clear();
        }

        public void flush() {
        }

        public void close() throws SecurityException {
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
