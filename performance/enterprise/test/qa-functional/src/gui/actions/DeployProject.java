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

import org.netbeans.junit.ide.ProjectSupport;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class DeployProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
   private String  project_name;
   
    /** Creates a new instance of DeployProject */
    public DeployProject(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = 60000;
        WAIT_AFTER_OPEN=4000;    
        project_name = "ReservationPartnerServices";    
    }

    public DeployProject(String testName, String  performanceDataName) {
        super(testName,performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = 60000;
        WAIT_AFTER_OPEN=4000;                
        project_name = "ReservationPartnerServices";    
    }
    
    public void initialize(){
        log(":: initialize");
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/TravelReservationService/ReservationPartnerServices");
        RuntimeTabOperator rto = new RuntimeTabOperator().invoke();
        TreePath path = rto.tree().findPath("Servers|Sun Java System Application Server");
        rto.tree().selectPath(path);
        new EventTool().waitNoEvent(5000);
        new Node(rto.tree(),path).performPopupAction("Start");
        new EventTool().waitNoEvent(80000);

        OutputOperator oot = new OutputOperator();
        oot.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",300000);
        OutputTabOperator asot = oot.getOutputTab("Sun Java System Application Server 9");
        asot.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",300000);

        asot.waitText("Application server startup complete");

        new CloseAllDocumentsAction().performAPI(); 
    }
   
    public void prepare() {
        log(":: prepare");
    }

    public ComponentOperator open() {
        log("::open");
     
       Node ProjectNode = new ProjectsTabOperator().getProjectRootNode(project_name);        
        ProjectNode.performPopupActionNoBlock("Deploy Project");
        new EventTool().waitNoEvent(10000);
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project_name);

        return null;
    }
    
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(project_name);
    }

  public void close(){
        log("::close");
        new CloseAllDocumentsAction().performAPI();
     } 
    
}