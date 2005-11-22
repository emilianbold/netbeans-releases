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

import java.net.URL;
import java.net.URLClassLoader;
import junit.framework.*;
import org.openide.ErrorManager;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.ref.*;
import java.util.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.AbstractLookup.Pair;

/**
 *
 * @author Jaroslav Tulach
 */
public class MIMESupport69049Test extends TestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupport69049Test$Lkp");
    }
    
    
    public MIMESupport69049Test (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }

    public void testProblemWithRecursionInIssue69049() throws Exception {
        Lkp lkp = (Lkp)Lookup.getDefault();
        
        class Pair extends AbstractLookup.Pair {
            public MIMEResolver[] all;
            
            
            protected boolean instanceOf(Class c) {
                return c.isAssignableFrom(getType());
            }

            protected boolean creatorOf(Object obj) {
                return false;
            }

            public Object getInstance() {
                assertNull("Not queried yet", all);
                all = MIMESupport.getResolvers();
                assertNotNull("Computed", all);
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
        }
        
        lkp.turn(Lkp.c1);
        Pair p = new Pair();
        lkp.ic.addPair(p);
        for (int i = 0; i < 30; i++) {
            lkp.ic.add(new Integer(i));
        }
        
        MIMEResolver[] all = MIMESupport.getResolvers();
        assertEquals("There is one", 1, all.length);
        assertEquals("There is C1", Lkp.c1, all[0]);
        
        assertNotNull("Been in the query", p.all);
        assertEquals("In query we cannot do better than nothing", 0, p.all.length);
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
    
    
    private static class ErrMgr extends ErrorManager {
        public static boolean switchDone;
        
        public Throwable attachAnnotations (Throwable t, ErrorManager.Annotation[] arr) {
            return null;
        }

        public ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }

        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return null;
        }

        public void notify (int severity, Throwable t) {
        }

        public void log (int severity, String s) {
            if (s.startsWith ("Resolvers computed")) {
                switchDone = true;
                Lkp lkp = (Lkp)org.openide.util.Lookup.getDefault ();
                lkp.turn (Lkp.c2);
            }
        }

        public ErrorManager getInstance (String name) {
            return this;
        }
        
    }
    
}
