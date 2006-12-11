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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.*;
import java.lang.ref.*;
import java.util.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class IconManagerGetLoaderTest extends TestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.util.IconManagerGetLoaderTest$Lkp");


        Logger l = Logger.getLogger("");
        Handler[] arr = l.getHandlers();
        for (int i = 0; i < arr.length; i++) {
            l.removeHandler(arr[i]);
        }
        l.addHandler(new ErrMgr());
        l.setLevel(Level.ALL);
    }
    
    
    public IconManagerGetLoaderTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }

    public static Test suite () {
        TestSuite suite = new TestSuite(IconManagerGetLoaderTest.class);
        return suite;
    }
    
    
    public void testWrongImplOfGetLoaderIssue62194() throws Exception {
        ClassLoader l = IconManager.getLoader ();
        assertTrue("Error manager race condition activated", ErrMgr.switchDone);
        assertEquals("c1 the original one", Lkp.c1, l);
        
        ClassLoader n = IconManager.getLoader ();
        assertEquals("c2 the new one", Lkp.c2, n);
    }
    
    
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private org.openide.util.lookup.InstanceContent ic;
        static ClassLoader c1 = new URLClassLoader(new URL[0]);
        static ClassLoader c2 = new URLClassLoader(new URL[0]);
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            this.ic = ic;
            
            turn(c1);
        }
        
        public void turn (ClassLoader c) {
            ArrayList l = new ArrayList();
            l.add(c);
            ic.set (l, null);
        }
    }
    
    
    private static class ErrMgr extends Handler {
        public static boolean switchDone;
        
        public void log (String s) {
            if (s == null) return;

            if (s.startsWith ("Loader computed")) {
                switchDone = true;
                Lkp lkp = (Lkp)org.openide.util.Lookup.getDefault ();
                lkp.turn (Lkp.c2);
            }
        }

        public void publish(LogRecord record) {
            log(record.getMessage());
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }
        
    }
    
}
