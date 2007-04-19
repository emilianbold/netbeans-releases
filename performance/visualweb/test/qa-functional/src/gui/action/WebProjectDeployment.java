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

import gui.VWPUtilities;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

import footprint.VWPFootprintUtilities;
import javax.swing.tree.TreePath;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.TaskModel;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
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
        WAIT_AFTER_OPEN=60000;
        targetProject = "VisualWebProject";  // This is for internal running purpose
    }
    
    public WebProjectDeployment(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=60000;
    }
    
    public void testDeploySmallProject() {
        targetProject = "VisualWebProject";
        doMeasurement();
    }
    
    public void testDeployLargeProject() {
        targetProject = "HugeApp";
        doMeasurement();
    }
    
    public void initialize() {
        long start = System.currentTimeMillis();
        performServerStartup();
        long stop = System.currentTimeMillis();
        
        log("App server started in "+(stop-start)+" ms");
        
        new ProjectsTabOperator().invoke();
        VWPFootprintUtilities.buildproject(targetProject);
        waitForNetBeansTask(targetProject+" (dist)");
        
    }
    
    private void performServerStartup() {

        String taskName = "Starting"+" "+performServerCommand("Start");
        waitForNetBeansTask(taskName);
    }
    private void performServerShutdown() {
        String taskName = "Stopping"+" "+performServerCommand("Stop");
        waitForNetBeansTask(taskName);        
    }
    private String performServerCommand(String cmdName) {
        RuntimeTabOperator rto = new RuntimeTabOperator().invoke();
        
        TreePath path = null;
        
        try {
            path = rto.tree().findPath("Servers|Sun Java System Application Server"); // NOI18N
        } catch (TimeoutExpiredException exc) {
            exc.printStackTrace(System.err);
            throw new Error("Cannot find Application Server Node");
        }

        Node asNode = new Node(rto.tree(),path);
        asNode.select(); 
        
        JPopupMenuOperator popup = asNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for Application server node ");
        }
        
        boolean startEnabled = popup.showMenuItem(cmdName).isEnabled(); // NOI18N
        if(startEnabled) {
            popup.pushMenuNoBlock(cmdName); // NOI18N
        }
        return asNode.getText();
    }
    public void prepare() {
        log(":: prepare");
        proj = null;
        try {
            proj = new ProjectsTabOperator().getProjectRootNode(targetProject);
            proj.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }
        projectMenu = proj.callPopup();
    }
    
    public ComponentOperator open() {
        log(":: open");
        projectMenu.pushMenuNoBlock(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_RedeployAction_Name"));
        
        VWPUtilities.waitForPendingBackgroundTasks();
        String statusText = "Finished building " + targetProject + " (run-deploy)."; // NOI18N
        log("Waiting for: '"+statusText+"' text in status bar");  
        MainWindowOperator.getDefault().waitStatusText(statusText); 
        
        return null;
    }
    
    public void close() {
        //Undeploy?
    }
    public void shutdown() {
        log(":: shutdown ");
        performServerShutdown();
    }
    private void waitForNetBeansTask(String taskName) {
        Controller controller = Controller.getDefault();
        TaskModel model = controller.getModel();
        
        InternalHandle task = waitNetBeansTaskHandle(model,taskName);
        long taskTimestamp = task.getTimeStampStarted();
        
        log("task started at : "+taskTimestamp);
        
        while(true) {
            int state = task.getState();
            if(state == task.STATE_FINISHED) { return; }
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
                return;
            }
        }
    }
    
    private InternalHandle waitNetBeansTaskHandle(TaskModel model, String serverIDEName) {
        while(true) {
            InternalHandle[] handles =  model.getHandles();
            InternalHandle  serverTask = getTaskHandle(handles,serverIDEName);
            if(serverTask != null) {
                log("Returning task handle");
                return serverTask;
            }
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
            }
        }
    }
    
    private InternalHandle getTaskHandle(InternalHandle[] handles, String taskName) {
        if(handles.length == 0)  {
            log("Empty tasks queue");
            return null;
        }
        
        for (InternalHandle internalHandle : handles) {
            if(internalHandle.getDisplayName().equals(taskName)) {
                log("Expected task found...");
                return internalHandle;
            }
        }
        return null;
    }    
}
