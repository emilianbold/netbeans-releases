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

package gui;

import gui.setup.IDESetupTest;

import org.netbeans.junit.NbTestSuite;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author  rkubacki@netbeans.org, mmirilovic@netbeans.org
 */
public class MeasuringSetup extends NbTestSuite {
    
    public MeasuringSetup (java.lang.String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("UI Responsiveness Setup suite");
        
        suite.addTest(new IDESetupTest("testCloseMemoryToolbar"));
        
        // replaced by close all documents suite.addTest(new gui.setup.IDESetupTest("testCloseWelcome"));
        // replaced by close all documents suite.addTest(new gui.setup.IDESetupTest("testCloseBluePrints"));
        suite.addTest(new IDESetupTest("testCloseAllDocuments"));
        
        suite.addTest(new IDESetupTest("testOpenFoldersProject"));
        suite.addTest(new IDESetupTest("testOpenDataProject"));
        suite.addTest(new IDESetupTest("testOpenWebProject"));

        return suite;
    }
    
}
