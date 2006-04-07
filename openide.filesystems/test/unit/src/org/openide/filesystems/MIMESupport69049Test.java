/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.*;
import java.util.*;
import java.util.logging.Level;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class MIMESupport69049Test extends TestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupport69049Test$Lkp");
        Logger.getLogger("").addHandler(new ErrMgr());
    }
    
    
    public MIMESupport69049Test (String testName) {
        super (testName);
    }

    protected Level logLevel() {
        return Level.FINE;
    }

    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }

    public void testProblemWithRecursionInIssue69049() throws Throwable {
        Lkp lkp = (Lkp)Lookup.getDefault();
        
        class Pair extends AbstractLookup.Pair implements Runnable {
            public MIMEResolver[] all;
            public MIMEResolver[] all2;
            public Throwable ex;
            
            
            protected boolean instanceOf(Class c) {
                return c.isAssignableFrom(getType());
            }

            protected boolean creatorOf(Object obj) {
                return false;
            }

            public Object getInstance() {
                if (all == null) {
                    all = MIMESupport.getResolvers();
                    assertNotNull("Computed", all);
                } else {
                    all2 = MIMESupport.getResolvers();
                    assertNotNull("Computed", all2);
                }
                return null;
            }

            public Class getType() {
                return MIMEResolver.class;
            }

            public String getId() {
                return getType().getName();
            }

            public String getDisplayName() {
                return getId();
            }
            
            public void run() {
                try {
                    all = MIMESupport.getResolvers();
                } catch (Throwable e) {
                    ex.printStackTrace();
                    ex = e;
                }
            }
            
            public void assertResults() throws Throwable {
                if (ex != null) {
                    throw ex;
                }

                assertNotNull("result was computed", all);
                assertEquals("There is one", 1, all.length);
                assertEquals("There is C1", Lkp.c1, all[0]);
            }
        }
        
        lkp.turn(Lkp.c1);
        Pair p = new Pair();
        lkp.ic.addPair(p);

        Pair run1 = new Pair();
        Pair run2 = new Pair();

        RequestProcessor.Task t1 = new RequestProcessor("t1").post(run1);
        RequestProcessor.Task t2 = new RequestProcessor("t2").post(run2, 20, Thread.NORM_PRIORITY);
        
        t1.waitFinished();
        t2.waitFinished();
        
        assertTrue("t1 done", t1.isFinished());
        assertTrue("t2 done", t2.isFinished());
        
        run1.assertResults();
        run2.assertResults();

        assertNotNull("Been in the query", p.all);
        assertEquals("In query we cannot do better than nothing", 0, p.all.length);
        assertNotNull("Been in the query twice", p.all2);
        /* second query does not need to know anything, it can either see
         * result of the previous one or not
        assertEquals("Second query knows the result", 1, p.all2.length);
        assertEquals("and the result is right", Lkp.c1, p.all2[0]);
         */
    }
    
    
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private ErrMgr err = new ErrMgr();
        private org.openide.util.lookup.InstanceContent ic;
        static MIMEResolver c1 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            
            public String toString() {
                return "C1";
            }
        };
        static MIMEResolver c2 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            public String toString() {
                return "C2";
            }
        };
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            this.ic = ic;
            
            turn(c1);
        }
        
        public void turn (MIMEResolver c) {
            ArrayList l = new ArrayList();
            l.add(err);
            l.add(c);
            ic.set (l, null);
        }
    }
    
    
    private static class ErrMgr extends Handler {
        private boolean block = true;
        
        public synchronized void publish(LogRecord r) {
            String s = r.getMessage();
            if (s.startsWith ("Computing resolvers")) {
                notifyAll();
                if (block) {
                    try {
                        wait(200);
                    } catch (InterruptedException ex) {
                        fail("Wrong exception");
                    }
                }
            }
            
            if (s.startsWith("Resolvers computed")) {
                block = false;
                notifyAll();
            }
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }

    }
    
}
