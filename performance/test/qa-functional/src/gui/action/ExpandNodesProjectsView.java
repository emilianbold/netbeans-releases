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

package gui.action;

import org.netbeans.jellytools.ProjectsTabOperator;

import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.modules.java.settings.JavaSettings;


/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ExpandNodesProjectsView extends testUtilities.PerformanceTestCase {
    
    /** Name of the folder which test creates and expands */
    private static String project;
    
    /** Path to the folder which test creates and expands */
    private static String pathToFolderNode;
    
    /** Node represantation of the folder which test creates and expands */
    private static Node nodeToBeExpanded;
    
    /** Projects tab */
    private static ProjectsTabOperator projectTab;
    
    /** Turn On/Off prescan sources */
    private boolean prescanSources;
    
    /** Project with data for these tests */
    private static String testDataProject = "PerformanceTestFoldersData";
    
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
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = "jEdit";
        pathToFolderNode = "";
        doMeasurement();
    }

    public void testExpandSourcePackagesNode(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = "jEdit";
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES;
        doMeasurement();
    }
    
    public void testExpandFolderWith50JavaFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = testDataProject;
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES + "|javaFolder50";
        doMeasurement();
    }
    
    public void testExpandFolderWith100JavaFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = testDataProject;
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES + "|javaFolder100";
        doMeasurement();
    }
    
    public void testExpandFolderWith100XmlFiles(){
        WAIT_AFTER_OPEN = 2000;
        WAIT_AFTER_PREPARE = 500;
        project = testDataProject;
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES + "|xmlFolder100";
        doMeasurement();
    }
    
    public void testExpandFolderWith100TxtFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 500;
        project = testDataProject;
        pathToFolderNode = gui.Utilities.SOURCE_PACKAGES + "|txtFolder100";
        doMeasurement();
    }
    
    
    public void initialize(){
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        
        projectTab.getProjectRootNode("jEdit").collapse();
        //projectTab.getProjectRootNode(testDataProject).collapse();
        
        turnOff();
        addClassNameToLookFor("explorer.view");
        setPrintClassNames(true);
    }
        
        
    public void prepare() {
        if(pathToFolderNode.equals(""))
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        else
            nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
    }
    
    public ComponentOperator open(){
        nodeToBeExpanded.tree().clickOnPath(nodeToBeExpanded.getTreePath(), 2);
//        nodeToBeExpanded.tree().clickMouse(2);
//        nodeToBeExpanded.waitExpanded();
        nodeToBeExpanded.expand();
        return null;
    }
    
    public void close(){
        nodeToBeExpanded.collapse();
    }
    
    public void shutdown() {
        turnBack();
        projectTab.getProjectRootNode(testDataProject).collapse();
        new RestoreWindowAction().performAPI(projectTab);
    }

    protected void turnOff() {
        // turn off prescanning of sources
        prescanSources = JavaSettings.getDefault().getPrescanSources();
        JavaSettings.getDefault().setPrescanSources(false);
    }
    
    protected void turnBack() {
        // turn back on prescanning of sources
        JavaSettings.getDefault().setPrescanSources(prescanSources);
    }
    
}
