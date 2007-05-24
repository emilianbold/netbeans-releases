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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui;

import gui.setup.UMLSetupTest;

import org.netbeans.junit.NbTestSuite;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author mmirilovic@netbeans.org
 */
public class UMLMeasuringSetup extends NbTestSuite {

    public UMLMeasuringSetup (java.lang.String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("UI Responsiveness Setup suite for UML Pack");

        suite.addTest(new UMLSetupTest("closeMemoryToolbar"));
        suite.addTest(new UMLSetupTest("closeWelcome"));
        
        suite.addTest(new UMLSetupTest("openJeditModelProject"));
        
        suite.addTest(new UMLSetupTest("closeAllDocuments"));
        
        return suite;
    }
    
}
