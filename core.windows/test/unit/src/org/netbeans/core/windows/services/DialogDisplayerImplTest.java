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


package org.netbeans.core.windows.services;

import java.awt.*;
import junit.framework.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jaroslav Tulach
 */
public class DialogDisplayerImplTest extends TestCase {
    private DialogDisplayer dd;
    private final Object RESULT = "DialogDisplayerImplTestResult";
    
    public DialogDisplayerImplTest (java.lang.String testName) {
        super (testName);
    }
    
    public static Test suite () {
        TestSuite suite = new TestSuite (DialogDisplayerImplTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
        dd = new DialogDisplayerImpl (RESULT);
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    public void testUnitTestByDefaultReturnsRESULT () throws Exception {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("AnyQuestion?");
        Object r = dd.notify (nd);
        assertEquals (RESULT, r);
    }

    public void testWorksFromAWTImmediatelly () throws Exception {
        class FromAWT implements Runnable {
            public void run () {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("HowAreYou?");
                Object r  = dd.notify (nd);
                assertEquals ("Returns ok", RESULT, r);
            }
        }
        
        javax.swing.SwingUtilities.invokeAndWait (new FromAWT ());
    }
    
    public void testDeadlock41544IfItIsNotPossibleToAccessAWTReturnAfterTimeout () throws Exception {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("HowAreYou?");
        
        class BlockAWT implements Runnable {
            public volatile int state;
            public synchronized void run () {
                state = 1;
                try {
                    notify ();
                    long t = System.currentTimeMillis ();
                    wait (15000);
                    if (System.currentTimeMillis () - t > 13000) {
                        // this is wrong
                        state = 3;
                        // wait for the dialog to finish
                        notify ();
                        return ;
                    }
                } catch (Exception ex) {
                }
                state = 2;
                notify ();
            }
        }
        
        BlockAWT b = new BlockAWT ();
        synchronized (b) {
            javax.swing.SwingUtilities.invokeLater (b);
            b.wait ();
            assertEquals ("In state one", 1, b.state);
        }
        
        Object res = dd.notify (nd);
        
        if (b.state == 3) {
            fail ("This means that the AWT blocked timeouted - e.g. no time out implemented in the dd.notify at all");
        }

        assertEquals ("Returns as closed, if cannot access AWT", nd.CLOSED_OPTION, res);
        
        synchronized (b) {
            b.notify ();
            b.wait ();
            assertEquals ("Exited correctly", 2, b.state);
        }
    }
    
}
