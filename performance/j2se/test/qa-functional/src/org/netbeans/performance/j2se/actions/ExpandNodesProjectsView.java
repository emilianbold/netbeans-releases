/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.performance.j2se.actions;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.modules.performance.utilities.CommonUtilities;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ExpandNodesProjectsView extends PerformanceTestCase {

    /** Name of the folder which test creates and expands */
    protected String project;
    
    /** Path to the folder which test creates and expands */
    protected String pathToFolderNode;
    
    /** Node represantation of the folder which test creates and expands */
    protected Node nodeToBeExpanded;
    
    /** Projects tab */
    protected ProjectsTabOperator projectTab;
    
    public static final String suiteName="UI Responsiveness J2SE Actions";    
    
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
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES;
        doMeasurement();
    }
    
    public void testExpandFolderWith50JavaFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.javaFolder50";
        doMeasurement();
    }
    
    public void testExpandFolderWith100JavaFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.javaFolder100";
        doMeasurement();
    }

    public void testExpandFolderWith1000JavaFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.javaFolder1000";
        doMeasurement();
    }

    public void testExpandFolderWith100XmlFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 500;
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.xmlFolder100";
        doMeasurement();
    }
    
    public void testExpandFolderWith100TxtFiles(){
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 500;
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.txtFolder100";
        doMeasurement();
    }
    
    
    @Override
    public void initialize(){
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);

        projectTab.getProjectRootNode("jEdit").collapse();
        projectTab.getProjectRootNode("PerformanceTestFoldersData").collapse();
        
        turnBadgesOff();
//        repaintManager().addRegionFilter(repaintManager().EXPLORER_FILTER);
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
        repaintManager().addRegionFilter(repaintManager().EXPLORER_FILTER);
nodeToBeExpanded.tree().doExpandPath(nodeToBeExpanded.getTreePath());
//        nodeToBeExpanded.tree().clickMouse(2);
//        nodeToBeExpanded.waitExpanded();
        nodeToBeExpanded.expand();
        return null;
    }
    
    @Override
    public void close(){
        nodeToBeExpanded.collapse();
    }
    
    @Override
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
