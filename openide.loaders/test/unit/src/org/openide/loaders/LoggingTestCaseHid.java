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

package org.openide.loaders;
import java.beans.PropertyChangeEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
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
class LoggingTestCaseHid extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.loaders.LoggingTestCaseHid$Lkp");
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
        }
    }
    
    /** Allows subclasses to register content for the lookup. Can be used in 
     * setUp and test methods, after that the content is cleared.
     */
    protected final void registerIntoLookup(Object instance) {
        Lookup l = Lookup.getDefault();
        assertEquals("We can run only with our Lookup", Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        lkp.ic.add(instance);
    }
    
    /** Registers hints for controlling thread switching in multithreaded
     * applications.
    
     */
    protected final void registerSwitches(String order) {
        LinkedList switches = new LinkedList();
        
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
            
            Switch s = new Switch(thrName, msgText);
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
                for(;;) {
                    synchronized (switches) {
                        if (switches.isEmpty()) {
                            return;
                        }


                        Switch w = (Switch)switches.getFirst();

                        int m = w.matches(s);
                        if (log) {
                            messages.append("t: " + Thread.currentThread().getName() + " log: " + s + " result: " + m + "\n");
                        }
                        boolean expectingMsg = false;
                        switch (m) {
                            case 1:  // same thread but wrong message => go on
                                return;
                            case 2:  // the correct message found
                                switches.removeFirst();
                                if (switches.isEmpty()) {
                                    // end of sample
                                    return;
                                }
                                w = (Switch)switches.getFirst();
                                if (w.matches("") > 0) {
                                    // next message is also from this thread, go on
                                    return;
                                }
                                // fall thru
                                expectingMsg = true;
                            case 0:
                                // some other thread is supposed to run
                                Thread t = (Thread)threads.get(w.name);
                                if (t != null) {
                                    t.interrupt();
                                }
                                threads.put(Thread.currentThread().getName(), Thread.currentThread());

                                java.util.Iterator it = switches.iterator();
                                while (it.hasNext()) {
                                    Switch check = (Switch)it.next();
                                    if (check.msg.equals(s)) {
                                        expectingMsg = true;
                                    }
                                }
                                if (!expectingMsg) {
                                    return;
                                }

                                try {
                                    if (log) {
                                        messages.append("t: " + Thread.currentThread().getName() + " log: " + s + " waiting\n");
                                    }
                                    switches.wait(300);
                                    if (log) {
                                        messages.append("t: " + Thread.currentThread().getName() + " log: " + s + " timeout\n");
                                    }
                                } catch (InterruptedException ex) {
                                    // ok, we love to be interrupted => go on
                                    if (log) {
                                        messages.append("t: " + Thread.currentThread().getName() + " log: " + s + " interrupted\n");
                                    }
                                }

                                break;
                            default:
                                assert false;
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
        private String msg;
        private String name;
        
        public Switch(String n, String m) {
            this.name = n;
            this.msg = m;
        }
        
        /** @return 0 if nothing matches 
                    1 if the thread name is good
                    2 if the thread name and message is the same
         */
        public int matches(String logMsg) {
            String thr = Thread.currentThread().getName();
            if (name.equals(thr)) {
                return msg.equals(logMsg) ? 2 : 1;
            }
            return 0;
        }
        
        public String toString() {
            return "Switch[" + name + "]: " + msg;
        }
    }
}
