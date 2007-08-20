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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import org.netbeans.jellytools.ProjectsTabOperator;

import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ExpandNodesProjectsView extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    /** Name of the folder which test creates and expands */
    protected String project;
    
    /** Path to the folder which test creates and expands */
    protected String pathToFolderNode;
    
    /** Node represantation of the folder which test creates and expands */
    protected Node nodeToBeExpanded;
    
    /** Projects tab */
    protected ProjectsTabOperator projectTab;
    
    /**
     * Creates a new instance of ExpandNodesInExplorer
     * @param testName the name of the test
     */
    public ExpandNodesProjectsView(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of ExpandNodesInExplorer
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandNodesProjectsView(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    

    public void testExpandProjectNode(){
        WAIT_AFTER_OPEN = 3000;
        WAIT_AFTER_PREPARE = 2000;
        project = "jEdit";
        pathToFolderNode = "";
        doMeasurement();
    }

    public void testExpandSourcePackagesNode(){
        WAIT_AFTER_OPEN = 3000;
        WAIT_AFTER_PREPARE = 2000;
        project = "jEdit";
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES;
        doMeasurement();
    }
    
    public void testExpandFolderWith50JavaFiles(){
        WAIT_AFTER_OPEN = 3000;
        WAIT_AFTER_PREPARE = 2000;
        project = "PerformanceTestFoldersData";
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES + "|folders.javaFolder50";
        doMeasurement();
    }
    
    public void testExpandFolderWith100JavaFiles(){
        WAIT_AFTER_OPEN = 3000;
        WAIT_AFTER_PREPARE = 2000;
        project = "PerformanceTestFoldersData";
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES + "|folders.javaFolder100";
        doMeasurement();
    }
    
    public void testExpandFolderWith100XmlFiles(){
        WAIT_AFTER_OPEN = 3000;
        WAIT_AFTER_PREPARE = 500;
        project = "PerformanceTestFoldersData";
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES + "|folders.xmlFolder100";
        doMeasurement();
    }
    
    public void testExpandFolderWith100TxtFiles(){
        WAIT_AFTER_OPEN = 3000;
        WAIT_AFTER_PREPARE = 500;
        project = "PerformanceTestFoldersData";
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES + "|folders.txtFolder100";
        doMeasurement();
    }
    
    
    public void initialize(){
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);

        projectTab.getProjectRootNode("jEdit").collapse();
        projectTab.getProjectRootNode("PerformanceTestFoldersData").collapse();
        
        turnBadgesOff();
        repaintManager().addRegionFilter(repaintManager().EXPLORER_FILTER);
    }
        
        
    public void prepare() {
        log("====== Path to folder: {"+project+"|"+pathToFolderNode+"}");
        if(pathToFolderNode.equals(""))
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        else
            nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
        log("====== Node to be expanded: {"+nodeToBeExpanded.getPath()+"}");
    }
    
    public ComponentOperator open(){
//        nodeToBeExpanded.tree().clickOnPath(nodeToBeExpanded.getTreePath(), 2);
nodeToBeExpanded.tree().doExpandPath(nodeToBeExpanded.getTreePath());
//        nodeToBeExpanded.tree().clickMouse(2);
//        nodeToBeExpanded.waitExpanded();
        nodeToBeExpanded.expand();
        return null;
    }
    
    public void close(){
        nodeToBeExpanded.collapse();
    }
    
    public void shutdown() {
        repaintManager().resetRegionFilters();
        turnBadgesOn();
        projectTab.getProjectRootNode(project).collapse();
        new RestoreWindowAction().performAPI(projectTab);
    }

    /**
     * turn badges off
     */
    protected void turnBadgesOff() {
        System.setProperty("perf.dont.resolve.java.badges", "true");
    }

    /**
     * turn badges on
     */
    protected void turnBadgesOn() {
        System.setProperty("perf.dont.resolve.java.badges", "false");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new ExpandNodesProjectsView("testExpandFolderWith100XmlFiles"));
    }
    
}
