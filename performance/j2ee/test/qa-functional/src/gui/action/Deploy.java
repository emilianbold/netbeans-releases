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

package gui.action;

import gui.Utils;
import java.io.InputStream;
import java.net.URL;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;


/**
 * Test create projects
 *
 * @author  lmartinek@netbeans.org
 */
public class Deploy extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node node;
    
    /**
     * Creates a new instance of CreateJ2EEProject
     * @param testName the name of the test
     */
    public Deploy(String testName) {
        super(testName);
        expectedTime = 60000;
        WAIT_AFTER_OPEN=240000;
    }
    
    /**
     * Creates a new instance of CreateJ2EEProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public Deploy(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 60000;
        WAIT_AFTER_OPEN=240000;
    }
    
    
    public void initialize(){
        Utils.startStopServer(true);
        
        JTreeOperator tree = ProjectsTabOperator.invoke().tree();
        tree.setComparator(new Operator.DefaultStringComparator(true, true));
        node = new ProjectRootNode(tree, "DeployTest");
        node.performPopupAction("Build");
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 120000);
        MainWindowOperator.getDefault().waitStatusText("Finished building build.xml (dist)");
    }
    
    public void shutdown() {
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        Node node = new Node(runtimeTab.getRootNode(), Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE")
                +"|Glassfish V2|"
                + Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_Applications") + "|"
                + Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_AppModules") + "|"
                + "DeployTest");
        node.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_Undeploy"));
        node.waitNotPresent();
        
        Utils.startStopServer(false);
    }
    
    public void prepare(){
        
    }
    
    public ComponentOperator open(){
        node.performPopupAction("Undeploy and Deploy");
        return null;
    }
    
    public void close() {
        MainWindowOperator.getDefault().waitStatusText("Finished building build.xml (run-deploy).");
        try {
            URL url = new URL("http://localhost:8080/DeployTest-WebModule/TestServlet");
            InputStream stream = url.openStream();
            stream.close();
        } catch (Exception e) {
            throw new RuntimeException("Deployed application unavailable.",e);
        }
    }
    
    
}
