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

import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.DialogDescriptor;

import org.netbeans.junit.NbTestCase;

/** Tests issue 56534.
 *
 * @author Jiri Rechtacek
 */
public class NbPresenterTest extends NbTestCase {
    
    public NbPresenterTest (String testName) {
        super (testName);
    }
    
    public static Test suite () {
        TestSuite suite = new TestSuite (NbPresenterTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
        super.setUp ();
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    public void testDialogsOptionsOnDefaultSystem () {
        System.setProperty ("xtest.looks_as_mac", "false");
        doTestDialogsOptions ();
    }
    
    public void testDialogsOptionsOnMac () {
        System.setProperty ("xtest.looks_as_mac", "true");
        doTestDialogsOptions ();
    }
    
    private void doTestDialogsOptions () {
        boolean modal = false;
        //boolean modal = true;
        JButton erase = new JButton ("Erase all my data");
        JButton rescue = new JButton ("Rescue");
        JButton cancel = new JButton ("Cancel");
        JButton [] options = new JButton [] {erase, rescue, cancel};
        DialogDescriptor dd = new DialogDescriptor (new JLabel ("Something interesting"), "My dialog", modal,
                // options
                options,
                erase,
                // align
                DialogDescriptor.RIGHT_ALIGN,
                null, null);
                
        dd.setClosingOptions (new Object[0]);
                
        NbPresenter presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);
        
        erase.doClick ();
        assertEquals ("Erase was invoked.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        erase.doClick ();
        assertEquals ("Erase was invoked again on same dialog.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();

        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);

        erase.doClick ();
        assertEquals ("Erase was invoked of reused dialog.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        erase.doClick ();
        assertEquals ("Erase was invoked again on reused dialog.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();

        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);
        
        rescue.doClick ();
        assertEquals ("Rescue was invoked of reused dialog.", rescue.getText (), ((JButton)dd.getValue ()).getText ());
        rescue.doClick ();
        assertEquals ("Rescue was invoked again on reused dialog.", rescue.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();
    }
    
}
