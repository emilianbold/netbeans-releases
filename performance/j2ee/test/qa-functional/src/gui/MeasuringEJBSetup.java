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

package gui;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author  Radim Kubacki
 */
public class MeasuringEJBSetup extends NbTestCase {
    
    public MeasuringEJBSetup (java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite("UI Responsiveness Setup suite");
        suite.addTest(new gui.setup.EJBSetupTest("testCloseMemoryToolbar"));
        suite.addTest(new gui.setup.EJBSetupTest("closeAllDocuments"));
        suite.addTest(new gui.setup.EJBSetupTest("closeNavigator"));
        //suite.addTest(new gui.setup.EJBSetupTest("testAddAppServer"));
        suite.addTest(new gui.setup.EJBSetupTest("testOpenEJBProject"));
        return suite;
    }
    
}
