/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.windows.services;

import java.awt.Dialog;
import java.awt.EventQueue;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

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
    
    public DialogDisplayerImplTest (String testName) {
        super (testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        dd = new DialogDisplayerImpl (RESULT);
        closeOwner = new JButton ("Close this dialog");
        childDD = new DialogDescriptor ("Child", "Child", false, null);
        openChild = new JButton ("Open child");
        closeChild = new JButton ("Close child");
        pane = new JOptionPane ("", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {openChild, closeChild});
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    @Override
    protected boolean runInEQ () {
        return false;
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
        
        SwingUtilities.invokeAndWait (new FromAWT ());
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
            SwingUtilities.invokeLater (b);
            b.wait ();
            assertEquals ("In state one", 1, b.state);
        }
        
        Object res = dd.notify (nd);
        
        if (b.state == 3) {
            fail ("This means that the AWT blocked timeouted - e.g. no time out implemented in the dd.notify at all");
        }

        assertEquals ("Returns as closed, if cannot access AWT", NotifyDescriptor.CLOSED_OPTION, res);
        
        synchronized (b) {
            b.notify ();
            b.wait ();
            assertEquals ("Exited correctly", 2, b.state);
        }
    }
    
    @RandomlyFails
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
    
    @RandomlyFails
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
    
    static void postInAwtAndWaitOutsideAwt (final Runnable run) throws Exception {
        // pendig to better implementation
        SwingUtilities.invokeLater (run);
//        Thread.sleep (10);
        while (EventQueue.getCurrentEvent () != null) {
//            Thread.sleep (10);
        }
    }
    
    private void waitAWT() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
    }
}
