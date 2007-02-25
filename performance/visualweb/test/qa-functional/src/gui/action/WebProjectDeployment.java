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

package gui.action;

import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class WebProjectDeployment extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    private String targetProject;
    private Node proj;
    private JPopupMenuOperator projectMenu;
    
    /**
     * Creates a new instance of WebProjectDeployment
     */
    public WebProjectDeployment(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=2000;            
    }
    
    public WebProjectDeployment(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=2000;            
    }
    
    public void testDeploySmallProject() {
        targetProject = "VisualWebProject";
        doMeasurement();
    }
    
    public void testDeployLargeProject() {
        targetProject = "HugeApp";
        doMeasurement();
    }
    
    public void prepare() {
        proj = new ProjectsTabOperator().getProjectRootNode(targetProject);
        proj.select();
        
        projectMenu = proj.callPopup();
    }

    public ComponentOperator open() {
        projectMenu.pushMenu(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.project.jsfproject.ui.Bundle", "LBL_RedeployAction_Name"));
        MainWindowOperator.getDefault().waitStatusText("Finished building "+targetProject+" (run-deploy)");        
        return null;
    }
    
    public void close() {
       //Undeploy?  
    }
    
    public void initialize() {
        log(":: initialize");
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+ File.separator +targetProject);
        waitForPendingBackgroundTasks();
    }
    
    protected void shutdown() {
        log(":: shutdown");
        ProjectSupport.closeProject(targetProject);
        
    }
    
    //Copyed from footprint module FootprintUtilities.java class
    static void waitForPendingBackgroundTasks() {
        // wait maximum 5 minutes
        for (int i=0; i<5*60; i++) {
            if (org.netbeans.progress.module.Controller.getDefault().getModel().getSize()==0)
                return;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            
        } 
    }    
}
