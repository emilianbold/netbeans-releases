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
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import junit.framework.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Jiri Rechtacek
 */
public class DialogDisplayer50960Test extends TestCase {
    private boolean performed = false;
    
    public DialogDisplayer50960Test (java.lang.String testName) {
        super (testName);
    }
    
    public static Test suite () {
        TestSuite suite = new TestSuite (DialogDisplayer50960Test.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
        performed = false;
    }


    // test issue #50960: avoid redundant actionPerformed() from DialogDescriptors
    public void testRedundantActionPerformed () {
        JButton b1 = new JButton ("Do");
        JButton b2 = new JButton ("Don't");
        ActionListener listener = new ActionListener () {
            public void actionPerformed (ActionEvent event) {
                assertFalse ("actionPerformed() only once.", performed);
                performed = true;
            }
        };
        DialogDescriptor dd = new DialogDescriptor (
                            "...",
                            "My Dialog",
                            true,
                            new JButton[] {b1, b2},
                            b2,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            null
                        );
        dd.setButtonListener (listener);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd);
        b1.doClick ();
        assertTrue ("Button b1 invoked.", performed);
    }
    

}
