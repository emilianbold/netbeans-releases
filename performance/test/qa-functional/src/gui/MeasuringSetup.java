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

package gui;

import org.netbeans.junit.*;
import junit.framework.*;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author  Radim Kubacki
 */
public class MeasuringSetup extends NbTestCase {
    
    public MeasuringSetup (java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite("UI Responsiveness Setup suite");
        
        suite.addTest(new gui.setup.IDESetupTest("testCloseMemoryToolbar"));
        suite.addTest(new gui.setup.IDESetupTest("testCloseWelcome"));
        suite.addTest(new gui.setup.IDESetupTest("testOpenFoldersProject"));
        suite.addTest(new gui.setup.IDESetupTest("testOpenDataProject"));
        suite.addTest(new gui.setup.IDESetupTest("testOpenWebProject"));

        return suite;
    }
    
}
