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
/*
 * SecurityAddNamePanelTest.java
 * JUnit based test
 *
 * Created on March 18, 2004, 4:58 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.InputDialog;
import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class SecurityAddNamePanelTest extends TestCase {

    public void testCreate() {
        SecurityAddNamePanel testPanel;
		
		// !PW null parameter should really be a valid model allowing for more
		// extensive testing.
		testPanel = new SecurityAddNamePanel(null, "EditPrincipalName");
		testPanel = new SecurityAddNamePanel(null, "NewPrincipalName");
		testPanel = new SecurityAddNamePanel(null, "EditGroupName");
		testPanel = new SecurityAddNamePanel(null, "NewGroupName");
    }

    public SecurityAddNamePanelTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SecurityAddNamePanelTest.class);
        return suite;
    }
}
