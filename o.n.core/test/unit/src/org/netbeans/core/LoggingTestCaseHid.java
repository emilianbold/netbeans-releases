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

package org.netbeans.core;
import java.beans.PropertyChangeEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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


/** Basic skeleton for logging test case in core.
 *
 * @author  Jaroslav Tulach
 */
public class LoggingTestCaseHid extends NbTestCase {
    static {
        org.netbeans.core.startup.MainLookup.register(new ErrManager());
    }
    
    private HashSet unregister = new HashSet();
    
    protected LoggingTestCaseHid (String name) {
        super (name);
    }
    
    public void run(TestResult result) {
        Lookup l = Lookup.getDefault();
        
        assertEquals("We can run only with our Lookup", org.netbeans.core.startup.MainLookup.class, l.getClass());
        
        super.run(result);
        
        for (Iterator it = unregister.iterator(); it.hasNext();) {
            Object elem = it.next();

            org.netbeans.core.startup.MainLookup.unregister(elem);
        }
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
            ErrManager.messages.append("\nGot exception: " + ex.getMessage());
            Thread.sleep(1000);
            AssertionFailedError ne = new AssertionFailedError (ex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (ex.getStackTrace ());
            throw ne;
        } catch (IOException iex) {//#66208
            ErrManager.messages.append("\nGot exception: " + iex.getMessage());
            Thread.sleep(1000);
            IOException ne = new IOException (iex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (iex.getStackTrace ());
            throw ne;	    
    	}
    }
    
    /** Allows subclasses to register content for the lookup. Can be used in 
     * setUp and test methods, after that the content is cleared.
     */
    protected final void registerIntoLookup(Object instance) {
        unregister.add(instance);
        org.netbeans.core.startup.MainLookup.register(instance);
    }
    
    //
    // Logging support
    //
    public static final class ErrManager extends ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        static java.io.PrintStream log = System.err;
        
        private String prefix;

        private static LinkedList switches;
        /** maps names of threads to their instances*/
        private static java.util.Map threads = new java.util.HashMap();
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public ErrorManager getInstance (String name) {
            if (
                true
//                name.startsWith ("org.openide.loaders.FolderList")
//              || name.startsWith ("org.openide.loaders.FolderInstance")
            ) {
                return new ErrManager ('[' + name + "] ");
            } else {
                // either new non-logging or myself if I am non-logging
                return new ErrManager ();
            }
        }
        
        public void log (int severity, String s) {
            if (prefix != null) {
                StringBuffer oneMsg = new StringBuffer();
                oneMsg.append(prefix);
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
            }
            
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
                                // end of sample
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
                            switches.wait(300);
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
        
        public void notify (int severity, Throwable t) {
            log (severity, t.getMessage ());
        }
        
        public boolean isNotifiable (int severity) {
            return prefix != null;
        }
        
        public boolean isLoggable (int severity) {
            return prefix != null;
        }

        private static void clear(String n, PrintStream printStream) {
            ErrManager.log = printStream;
            ErrManager.messages.setLength(0);
            ErrManager.messages.append ("Starting test ");
            ErrManager.messages.append (n);
            ErrManager.messages.append ('\n');
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
