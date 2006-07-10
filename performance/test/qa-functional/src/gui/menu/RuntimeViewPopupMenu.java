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
        suite.addTest(new RuntimeViewPopupMenu("testServerRegistryPopupMenuRuntime", "Servers node popup in Runtime View"));
        suite.addTest(new RuntimeViewPopupMenu("testTomcatPopupMenuRuntime", "Bundled Tomcat node popup in Runtime View"));
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

    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new RuntimeViewPopupMenu("testTomcatPopupMenuRuntime"));
    }

}