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


package org.netbeans.core.windows.services;

import java.awt.*;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class DialogDisplayerImplTest extends NbTestCase {
    private DialogDisplayer dd;
    private final Object RESULT = "DialogDisplayerImplTestResult";
    private JOptionPane pane;
    private JButton closeOwner;
    private DialogDescriptor childDD;
    private JButton openChild;
    private JButton closeChild;
    private Dialog child;
    
    public DialogDisplayerImplTest (java.lang.String testName) {
        super (testName);
    }
    
    public static Test suite () {
        TestSuite suite = new TestSuite (DialogDisplayerImplTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
        dd = new DialogDisplayerImpl (RESULT);
        closeOwner = new JButton ("Close this dialog");
        childDD = new DialogDescriptor ("Child", "Child", false, null);
        openChild = new JButton ("Open child");
        closeChild = new JButton ("Close child");
        pane = new JOptionPane ("", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {openChild, closeChild});
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    protected boolean runInEQ () {
        return false;
    }
    
    public void XXXtestUnitTestByDefaultReturnsRESULT () throws Exception {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("AnyQuestion?");
        Object r = dd.notify (nd);
        assertEquals (RESULT, r);
    }

    public void XXXtestWorksFromAWTImmediatelly () throws Exception {
        class FromAWT implements Runnable {
            public void run () {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ("HowAreYou?");
                Object r  = dd.notify (nd);
                assertEquals ("Returns ok", RESULT, r);
            }
        }
        
        javax.swing.SwingUtilities.invokeAndWait (new FromAWT ());
    }
    
    public void XXXtestDeadlock41544IfItIsNotPossibleToAccessAWTReturnAfterTimeout () throws Exception {
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
    
    public void testLeafDialog () throws Exception {
        boolean leaf = true;
        DialogDescriptor ownerDD = new DialogDescriptor (pane, "Owner", true, new Object[] {closeOwner}, null, 0, null, null, leaf);
        final Dialog owner = DialogDisplayer.getDefault ().createDialog (ownerDD);
        
        // make leaf visible
        postInAwtAndWaitOutsideAwt (new Runnable () {
            public void run () {
                owner.setVisible (true);
            }
        });
        while (!owner.isVisible ()) {}
        
        child = DialogDisplayer.getDefault ().createDialog (childDD);

        // make the child visible
        postInAwtAndWaitOutsideAwt (new Runnable () {
            public void run () {
                child.setVisible (true);
            }
        });
        while (!child.isVisible ()) {}
        
        assertFalse ("No dialog is owned by leaf dialog.", owner.equals (child.getOwner ()));
        assertEquals ("The leaf dialog has no child.", 0, owner.getOwnedWindows ().length);
        
        assertTrue ("Leaf is visible", owner.isVisible ());
        assertTrue ("Child is visible", child.isVisible ());
        
        // close the leaf window
        postInAwtAndWaitOutsideAwt (new Runnable () {
            public void run () {
                owner.setVisible (false);
            }
        });
        while (owner.isVisible ()) {}
        
        assertFalse ("Leaf is dead", owner.isVisible ());
        assertTrue ("Child is visible still", child.isVisible ());
        
        // close the child dialog
        postInAwtAndWaitOutsideAwt (new Runnable () {
            public void run () {
                child.setVisible (false);
            }
        });        
        while (child.isVisible ()) {}
        
        assertFalse ("Child is dead too", child.isVisible ());
    }
    
    public void testNonLeafDialog () throws Exception {
        boolean leaf = false;
        DialogDescriptor ownerDD = new DialogDescriptor (pane, "Owner", true, new Object[] {closeOwner}, null, 0, null, null, leaf);
        final Dialog owner = DialogDisplayer.getDefault ().createDialog (ownerDD);
        
        // make leaf visible
        postInAwtAndWaitOutsideAwt (new Runnable () {
            public void run () {
                owner.setVisible (true);
            }
        });
        while (!owner.isVisible ()) {}
        
        child = DialogDisplayer.getDefault ().createDialog (childDD);

        // make the child visible
        postInAwtAndWaitOutsideAwt (new Runnable () {
            public void run () {
                child.setVisible (true);
            }
        });
        while (!child.isVisible ()) {}
        
        assertTrue ("The child is owned by leaf dialog.", owner.equals (child.getOwner ()));
        assertEquals ("The leaf dialog has one child.", 1, owner.getOwnedWindows ().length);
        
        assertTrue ("Leaf is visible", owner.isVisible ());
        assertTrue ("Child is visible", child.isVisible ());
        
        // close the leaf window
        postInAwtAndWaitOutsideAwt (new Runnable () {
            public void run () {
                owner.setVisible (false);
            }
        });
        while (owner.isVisible ()) {}
        
        assertFalse ("Leaf is dead", owner.isVisible ());
        assertFalse ("Child is dead too", child.isVisible ());
    }
    
    private void postInAwtAndWaitOutsideAwt (final Runnable run) throws Exception {
        // pendig to better implementation
        SwingUtilities.invokeLater (run);
//        Thread.sleep (10);
        while (EventQueue.getCurrentEvent () != null) {
//            Thread.sleep (10);
        }
    }
    
}
