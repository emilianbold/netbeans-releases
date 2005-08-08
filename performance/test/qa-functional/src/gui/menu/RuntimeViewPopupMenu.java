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

package gui.menu;

import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of popup menu on nodes in Runtime View
 * @author  juhrik@netbeans.org, mmirilovic@netbeans.org
 */


public class RuntimeViewPopupMenu extends ValidatePopupMenuOnNodes{
    
    private static RuntimeTabOperator runtimeTab;
    
    private final String SERVER_REGISTRY = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE");
    
    /** Creates a new instance of RuntimeViewPopupMenu */
    public RuntimeViewPopupMenu(String testName) {
        super(testName);
    }
    
    /** Creates a new instance of RuntimeViewPopupMenu */
    public RuntimeViewPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new RuntimeViewPopupMenu("testServerRegistryPopupMenuRuntime", "Server Registry node popup in Runtime View"));
        suite.addTest(new RuntimeViewPopupMenu("testTomcatPopupMenuRuntime", "Tomcat node popup in Runtime View"));
        return suite;
    }
    

    public void testServerRegistryPopupMenuRuntime(){
        testMenu(SERVER_REGISTRY);
    }
    
    public void testTomcatPopupMenuRuntime(){
        testMenu(SERVER_REGISTRY + "|Bundled Tomcat");
    }
    
    private void testMenu(String path){
        runtimeTab = new RuntimeTabOperator();
        dataObjectNode = new Node(runtimeTab.getRootNode(), path);
        doMeasurement();
    }

    public void shutdown(){
        // do nothing runtimeTab.close();
    } 

}