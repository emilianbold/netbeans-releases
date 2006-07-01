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

package org.netbeans.core.windows.services;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import junit.framework.TestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Jiri Rechtacek
 */
public class DialogDisplayer50960Test extends TestCase {
    private boolean performed = false;

    public DialogDisplayer50960Test (String testName) {
        super (testName);
    }

    protected void setUp() throws Exception {
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
