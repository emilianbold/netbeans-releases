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

package org.openide.util;

import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Vector;
import junit.framework.*;
import org.netbeans.junit.*;

public class WeakListenersSpeedTest extends NbTestCase
implements java.lang.reflect.InvocationHandler {
    private static java.util.HashMap times = new java.util.HashMap ();
    private long time;
    
    private static final int COUNT = 100000;
    
    public WeakListenersSpeedTest (java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(WeakListenersSpeedTest.class));
    }
    
    protected void setUp () throws Exception {
        for (int i = 0; i < 10; i++) {
            try {
                super.runTest ();
            } catch (Throwable t) {
            }
        }
        
        time = System.currentTimeMillis ();
    }
    
    protected void tearDown () throws Exception {
        long now = System.currentTimeMillis ();
        times.put (getName (), new Long (now - time));
        
        assertNumbersAreSane ();
    }
    
    public void testThisIsTheBasicBenchmark () throws Exception {
        // RequestProcessor is a class with string argument that does
        // nearly nothing in its constructor, so it should be good reference
        // point
        Constructor c = org.openide.util.RequestProcessor.class.getConstructor (new Class[] { String.class });
        String  orig = "Ahoj";
        for (int i = 0; i < COUNT; i++) {
            // this is slow
            //java.lang.reflect.Proxy.newProxyInstance (getClass().getClassLoader(), new Class[] { Runnable.class }, this);
            c.newInstance (new Object[] { orig });
        }
    }

    
    public void testCreateListeners () {
        java.beans.PropertyChangeListener l = new java.beans.PropertyChangeListener () {
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
            }
        };
        
        for (int i = 0; i < COUNT; i++) {
            org.openide.util.WeakListeners.create (java.beans.PropertyChangeListener.class, l, this);
        }
    }
    public void testMoreTypesVariousListeners () {
        class X implements java.beans.PropertyChangeListener, java.beans.VetoableChangeListener {
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
            }
            public void vetoableChange (java.beans.PropertyChangeEvent ev) {
            }
        };
        
        X x = new X ();
        for (int i = 0; i < COUNT / 2; i++) {
            org.openide.util.WeakListeners.create (java.beans.PropertyChangeListener.class, x, this);
            org.openide.util.WeakListeners.create (java.beans.VetoableChangeListener.class, x, this);
        }
    }
    
    
    /** Compares that the numbers are in sane bounds */
    private void assertNumbersAreSane () {
        StringBuffer error = new StringBuffer ();
        {
            java.util.Iterator it = times.entrySet ().iterator ();
            while (it.hasNext ()) {
                java.util.Map.Entry en = (java.util.Map.Entry)it.next ();
                error.append ("Test "); error.append (en.getKey ());
                error.append (" took "); error.append (en.getValue ());
                error.append (" ms\n");
            }
        }
        
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        
        {
            java.util.Iterator it = times.values ().iterator ();
            while (it.hasNext ()) {
                Long l = (Long)it.next ();
                if (l.longValue () > max) max = l.longValue ();
                if (l.longValue () < min) min = l.longValue ();
            }
        }
        
        if (min * 5 < max) {
            fail ("Too big differences when various number of shadows is used:\n" + error.toString ());
        }
        
        System.err.println(error.toString ());
    }
    
    public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
        throw new Throwable ();
    }
    
}
