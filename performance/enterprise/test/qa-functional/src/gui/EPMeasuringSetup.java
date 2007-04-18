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

import gui.setup.EnterpriseSetupTest;

import org.netbeans.junit.NbTestSuite;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author  rkubacki@netbeans.org, mmirilovic@netbeans.org
 */
public class EPMeasuringSetup extends NbTestSuite {

    public EPMeasuringSetup (java.lang.String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("UI Responsiveness Setup suite for Enterprise Pack");

        suite.addTest(new EnterpriseSetupTest("closeMemoryToolbar"));
        suite.addTest(new EnterpriseSetupTest("closeUIGesturesToolbar"));
        
        suite.addTest(new EnterpriseSetupTest("closeWelcome"));
        
        // need for some Menu tests
	// opened from build script suite.addTest(new EnterpriseSetupTest("openJeditProject"));
        suite.addTest(new EnterpriseSetupTest("openDataProject"));
//TODO no tomcat - see issue 101104        suite.addTest(new EnterpriseSetupTest("openWebProject"));

        suite.addTest(new EnterpriseSetupTest("openReservationPartnerServicesProject"));
        suite.addTest(new EnterpriseSetupTest("openTravelReservationServiceProject"));
        suite.addTest(new EnterpriseSetupTest("openTravelReservationServiceApplicationProject"));

        suite.addTest(new EnterpriseSetupTest("openSoaTestProject"));
        suite.addTest(new EnterpriseSetupTest("openBPELTestProject"));
        
        suite.addTest(new EnterpriseSetupTest("closeAllDocuments"));
        
        return suite;
    }
    
}
