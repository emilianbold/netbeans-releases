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

import gui.Utils;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of popup menu on nodes in Runtime View
 * @author  juhrik@netbeans.org, mmirilovic@netbeans.org
 */


public class AppServerPopupMenu extends ValidatePopupMenuOnNodes{
    
    private static RuntimeTabOperator runtimeTab;
    
    private final String SERVER_REGISTRY = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE");
    
    /**
     * Creates a new instance of AppServerPopupMenu 
     */
    public AppServerPopupMenu(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of AppServerPopupMenu 
     */
    public AppServerPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    
    public void testAppServerPopupMenuRuntime(){
        testMenu(SERVER_REGISTRY + "|" + "GlassFish V2");
    }
    
    private void testMenu(String path){
        try {
            runtimeTab = new RuntimeTabOperator();
            dataObjectNode = new Node(runtimeTab.getRootNode(), path);
            doMeasurement();
        } catch (Exception e) {
            throw new Error("Exception thrown",e);
        }
    }
    
    public void initialize() {
        //Utils.startStopServer(true);
    }
    
    public void shutdown() {
        //Utils.startStopServer(false);
    }

   
    
}