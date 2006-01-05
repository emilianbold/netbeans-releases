/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.wizard;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.tomcat5.TomcatFactory55;
import org.netbeans.modules.tomcat5.TomcatFactory55Test;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.util.Utilities;

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
        InstallPanelVisual inst = new InstallPanelVisual(TomcatManager.TOMCAT_55);
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
