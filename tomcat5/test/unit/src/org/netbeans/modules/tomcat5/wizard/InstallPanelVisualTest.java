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

package org.netbeans.modules.tomcat5.wizard;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;

/**
 *
 * @author sherold
 */
public class InstallPanelVisualTest extends NbTestCase {

    private File datadir;

    public InstallPanelVisualTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new InstallPanelVisualTest("testIsServerXmlValid"));
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp ();
        datadir = getDataDir();
    }
    
    public void testIsServerXmlValid() {
        InstallPanelVisual inst = new InstallPanelVisual(TomcatVersion.TOMCAT_55);
        for (int i = 0; true; i++) {
            File serverXml = new File(datadir, "conf/valid/server_" + i + ".xml");
            if (!serverXml.exists()) {
                break;
            }
            assertTrue("Tomcat configuration file " + serverXml.getAbsolutePath() + " is supposed to be valid", 
                       inst.isServerXmlValid(serverXml));
        }

        for (int i = 0; true; i++) {
            File serverXml = new File(datadir, "conf/invalid/server_" + i + ".xml");
            if (!serverXml.exists()) {
                break;
            }
            assertFalse("Tomcat configuration file " + serverXml.getAbsolutePath() + " is supposed to be invalid", 
                        inst.isServerXmlValid(serverXml));
        }
    }
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
}
