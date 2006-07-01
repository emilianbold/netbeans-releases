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
package org.openide;


import javax.swing.*;
import org.netbeans.junit.*;

/** Testing issue 56878.
 * @author  Jiri Rechtacek
 *
 */
public class NotifyDescriptorTest extends NbTestCase {


    public NotifyDescriptorTest (String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run (new NbTestSuite (NotifyDescriptorTest.class));
        System.exit (0);
    }

    protected final void setUp () {
    }

    public void testDefaultValue () {
        JButton defaultButton = new JButton ("Default");
        JButton customButton = new JButton ("Custom action");
        JButton [] options = new JButton [] {defaultButton, customButton};
        DialogDescriptor dd = new DialogDescriptor ("Test", "Test dialog", false, options, defaultButton, NotifyDescriptor.PLAIN_MESSAGE, null, null);
        assertEquals ("Test descriptor has defaultButton as defaultValue", defaultButton, dd.getValue ());
        dd.setClosingOptions (null);
        
        DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
        customButton.doClick ();
        
        assertEquals ("Test dialog closed by CustomButton", customButton, dd.getValue ());
        assertEquals ("Test dialog has the same default value as before", defaultButton, dd.getDefaultValue ());
    }
}
