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

package gui.actions;


import javax.swing.tree.TreePath;

import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class DeployProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String  project_name = "ReservationPartnerServices";
    private static String TIMEOUT_NAME = "ComponentOperator.WaitStateTimeout";
    private static long timeout;
    
    /** Creates a new instance of DeployProject */
    public DeployProject(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 60000;
        WAIT_AFTER_OPEN=60000;
    }
    
    public DeployProject(String testName, String  performanceDataName) {
        super(testName,performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = 60000;
        WAIT_AFTER_OPEN=60000;
    }
    
    public void initialize(){
        log(":: initialize");
        RuntimeTabOperator rto = new RuntimeTabOperator().invoke();
        TreePath path = rto.tree().findPath("Servers|Sun Java System Application Server"); // NOI18N
        rto.tree().selectPath(path);
        new EventTool().waitNoEvent(5000);
        
        new Node(rto.tree(),path).performPopupAction("Start"); // NOI18N
        new EventTool().waitNoEvent(80000);
        
        OutputOperator oot = new OutputOperator();
        timeout = JemmyProperties.getCurrentTimeout(TIMEOUT_NAME);
        JemmyProperties.setCurrentTimeout(TIMEOUT_NAME,300000);
        
        OutputTabOperator asot = oot.getOutputTab("Sun Java System Application Server 9"); // NOI18N
        asot.waitText("Application server startup complete"); // NOI18N
        
        new CloseAllDocumentsAction().performAPI();
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log("::open");
        
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(project_name);
        projectNode.performPopupActionNoBlock("Deploy Project"); // NOI18N
        new EventTool().waitNoEvent(60000);
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project_name); // NOI18N
        
        return null;
    }
    
    protected void shutdown() {
        log("::shutdown");
        JemmyProperties.setCurrentTimeout(TIMEOUT_NAME,timeout);
    }
    
    public void close(){
        log("::close");
        new CloseAllDocumentsAction().performAPI();
    }
    
}