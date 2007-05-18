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

import org.netbeans.jellytools.ProjectsTabOperator;

import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.test.web.performance.WebPerformanceTestCase;


/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ExpandNodesWebProjectsView extends WebPerformanceTestCase {
    /** Name of the folder which test creates and expands */
    private static String project;
    /** Path to the folder which test creates and expands */
    private static String pathToFolderNode;
    /** Node represantation of the folder which test creates and expands */
    private static Node nodeToBeExpanded;
    /** Projects tab */
    private static ProjectsTabOperator projectTab;
    /** Project with data for these tests */
    private static String testDataProject = "PerformanceTestFolderWebApp";
    /**
     * Creates a new instance of ExpandNodesInExplorer
     * @param testName the name of the test
     */
    public ExpandNodesWebProjectsView(String testName) {
        super(testName);
        init();
    }
    
    /**
     * Creates a new instance of ExpandNodesInExplorer
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandNodesWebProjectsView(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    protected void init() {
        super.init();
        project = testDataProject;
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
    }

    public void testExpandProjectNode(){
        pathToFolderNode = "";
        doMeasurement();
    }

    public void testExpandSourcePackagesNode(){
        pathToFolderNode = "Source Packages";
        doMeasurement();
    }
    
    public void testExpandFolderWith50JspFiles(){
        pathToFolderNode = "Web Pages|jsp50";
        doMeasurement();
    }
    
    public void testExpandFolderWith100JspFiles(){
        pathToFolderNode = "Web Pages|jsp100";
        doMeasurement();
    }
    
    public void initialize(){
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        projectTab.getProjectRootNode("TestWebProject").collapse();
        projectTab.getProjectRootNode(testDataProject).collapse();
        System.setProperty("perf.dont.resolve.java.badges", "true");
    }
        
    public void prepare() {
        if(pathToFolderNode.equals(""))
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        else
	    nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
        //repaintManager().setOnlyExplorer(true);
        repaintManager().addRegionFilter(repaintManager().EXPLORER_FILTER);
        nodeToBeExpanded.select();
    }
    
    public ComponentOperator open(){
        nodeToBeExpanded.expand();
        return null;
    }
    
    public void close(){
        //repaintManager().setOnlyExplorer(true);
        repaintManager().resetRegionFilters();
        nodeToBeExpanded.collapse();
    }
    
    public void shutdown() {
        super.shutdown();
        System.setProperty("perf.dont.resolve.java.badges", "false");
        projectTab.getProjectRootNode(testDataProject).collapse();
        new RestoreWindowAction().performAPI(projectTab);
    }
}
