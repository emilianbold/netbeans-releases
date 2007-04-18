/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package gui.scanclasspath;

import gui.setup.EnterpriseSetupTest;

import org.netbeans.junit.NbTestSuite;


/**
 * Test provide measured time of scanning classpth for some classpath roots.
 * We measure classpath scanning during openieng HugeApplication project.
 *
 * @author  mmirilovic@netbeans.org
 */
public class EPScanClasspath extends gui.scanclasspath.ScanClasspath {
    
    static {
        reportCPR.clear();
        reportCPR.add("rt.jar");                // JDK/jre/lib/rt.jar    
        reportCPR.add("src/java");              // HugeApp/src/java/
        reportCPR.add("webservices-rt.jar");    // appserver/lib/webservices-rt.jar
        reportCPR.add("webservices-tools.jar"); // appserver/lib/webservices-tools.jar
    }
    
    /**
     * Creates a new instance of WebScanClasspath
     * @param testName the name of the test
     */
    public EPScanClasspath(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of WebScanClasspath
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public EPScanClasspath(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new EnterpriseSetupTest("closeMemoryToolbar"));
        suite.addTest(new EnterpriseSetupTest("closeUIGesturesToolbar"));
        suite.addTest(new EnterpriseSetupTest("closeAllDocuments"));
        
        suite.addTest(new EnterpriseSetupTest("openReservationPartnerServicesProject"));
        suite.addTest(new EnterpriseSetupTest("openTravelReservationServiceProject"));
        suite.addTest(new EnterpriseSetupTest("openTravelReservationServiceApplicationProject"));

        suite.addTest(new EPScanClasspath("measureClassPathScan"));
        
        return suite;
    }
    
    public void measureClassPathScan() {
        super.measureClassPathScan();
    }
}