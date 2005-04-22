/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide;

import junit.framework.*;
import org.netbeans.junit.*;

/** Test for general ErrorManager functionality.
 *
 * @author Jaroslav Tulach
 */
public class ErrorManagerTest extends NbTestCase {
    
    public ErrorManagerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ErrorManagerTest.class);
        
        return suite;
    }
    
    protected void setUp () {
        System.setProperty("org.openide.util.Lookup", "org.openide.ErrorManagerTest$Lkp");
        assertNotNull ("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault ().lookup (ErrManager.class));
    }
    
    /** Test of getDefault method, of class org.openide.ErrorManager. */
    public void testGetDefault() {
        assertNotNull("There has to be a manager", ErrorManager.getDefault ());
    }
    
    /** Test of notify method, of class org.openide.ErrorManager. */
    public void testNotify() {
        Throwable t = new Throwable ();
        ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, t);
        ErrManager.get ().assertNotify (ErrManager.INFORMATIONAL, t);
        t = new Throwable ();
        ErrorManager.getDefault ().notify (t);
        ErrManager.get ().assertNotify (ErrManager.UNKNOWN, t);
    }
    
    /** Test of log method, of class org.openide.ErrorManager. */
    public void testLog() {
        ErrorManager.getDefault ().log (ErrorManager.INFORMATIONAL, "A text");
        ErrManager.get ().assertLog (ErrorManager.INFORMATIONAL, "A text");
        ErrorManager.getDefault ().log ("Another text");
        ErrManager.get ().assertLog (ErrorManager.INFORMATIONAL, "Another text");
    }
    
    /** Test of isLoggable method, of class org.openide.ErrorManager. */
    public void testIsLoggable() {
        ErrorManager.getDefault ().isLoggable(ErrorManager.INFORMATIONAL);
    }
    
    /** Test of annotate method, of class org.openide.ErrorManager. */
    public void testReturnValues () {
        Throwable t = new Throwable ();
        Throwable value = ErrorManager.getDefault ().annotate(t, ErrorManager.INFORMATIONAL, null, null, null, null);
        assertEquals ("Annotate must return the same exception", t, value);
        
        value = ErrorManager.getDefault ().copyAnnotation (t, new Throwable ());
        assertEquals ("copyAnnotation must return the same exception", t, value);
        
        value = ErrorManager.getDefault ().attachAnnotations(t, new ErrorManager.Annotation[0]);
        assertEquals ("attachAnnotations must return the same exception", t, value);
        
    }
    
    public void testGetInstanceIsInfluencedByChangesOfErrorManagersWeDelegateTo () {
        Lkp.turn (false);
        try {
            ErrorManager man = ErrorManager.getDefault ().getInstance ("hi");
            man.log ("Anything");
            ErrManager.get ().assertLog (-1, null); // no logging because we are disabled
            Lkp.turn (true);
            man.log ("Something");
            ErrManager.assertLog (ErrorManager.INFORMATIONAL, "Something");
        } finally {
            Lkp.turn (true);
        }
    }
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private ErrManager err = new ErrManager ();
        private org.openide.util.lookup.InstanceContent ic;
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (err);
            this.ic = ic;
        }
        
        public static void turn (boolean on) {
            Lkp lkp = (Lkp)org.openide.util.Lookup.getDefault ();
            if (on) {
                lkp.ic.add (lkp.err);
            } else {
                lkp.ic.remove (lkp.err);
            }
        }
    }
    
    //
    // Manager to delegate to
    //
    public static final class ErrManager extends org.openide.ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        
        private String prefix;
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public static ErrManager get () {
            return (ErrManager)org.openide.util.Lookup.getDefault ().lookup (ErrManager.class);
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, java.util.Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, org.openide.ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public org.openide.ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public org.openide.ErrorManager getInstance (String name) {
            if (
                name.startsWith ("org.netbeans.core.AutomountSupport") ||
                name.startsWith ("org.openide.loaders.FolderList") ||
                name.startsWith ("org.openide.loaders.FolderInstance")
            ) {
                return new ErrManager ('[' + name + ']');
            } else {
                // either new non-logging or myself if I am non-logging
                return new ErrManager ();
            }
        }
        
        public void log (int severity, String s) {
            lastSeverity = severity;
            lastText = s;
        }
        
        public void notify (int severity, Throwable t) {
            lastThrowable = t;
            lastSeverity = severity;
        }
        private static int lastSeverity;
        private static Throwable lastThrowable;
        private static String lastText;

        public static void assertNotify (int sev, Throwable t) {
            assertEquals ("Severity is same", sev, lastSeverity);
            assertSame ("Throwable is the same", t, lastThrowable);
            lastThrowable = null;
            lastSeverity = -1;
        }
        
        public static void assertLog (int sev, String t) {
            assertEquals ("Severity is same", sev, lastSeverity);
            assertEquals ("Text is the same", t, lastText);
            lastText = null;
            lastSeverity = -1;
        }
        
    } 
    
}
