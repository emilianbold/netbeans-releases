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
