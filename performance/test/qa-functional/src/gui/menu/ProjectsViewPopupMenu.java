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

package gui.menu;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of popup menu on nodes in Projects View.
 * @author  mmirilovic@netbeans.org
 */
public class ProjectsViewPopupMenu extends ValidatePopupMenuOnNodes {
    
    private static ProjectsTabOperator projectsTab = null;
    
    /** Creates a new instance of ProjectsViewPopupMenu */
    public ProjectsViewPopupMenu(String testName) {
        super(testName);
    }
    
    /** Creates a new instance of ProjectsViewPopupMenu */
    public ProjectsViewPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ProjectsViewPopupMenu("testProjectNodePopupMenuProjects", "Project node popup in Projects View"));
        
        suite.addTest(new ProjectsViewPopupMenu("testSourcePackagesPopupMenuProjects", "Source Packages node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testTestPackagesPopupMenuProjects", "Test Packages node popup in Projects View"));
        
        suite.addTest(new ProjectsViewPopupMenu("testPackagePopupMenuProjects", "Package node popup in Projects View"));
        
        suite.addTest(new ProjectsViewPopupMenu("testJavaFilePopupMenuProjects", "Java file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testTxtFilePopupMenuProjects", "Txt file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testPropertiesFilePopupMenuProjects", "Properties file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testXmlFilePopupMenuProjects", "Xml file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testJspFilePopupMenuProjects", "Jsp file node popup in Projects View"));
        
        suite.addTest(new ProjectsViewPopupMenu("testNBProjectNodePopupMenuProjects", "NB Project node popup in Projects View"));

        return suite;
    }
    
    public void testProjectNodePopupMenuProjects() {
        testNode(getProjectNode("PerformanceTestData"));
    }
    
    public void testSourcePackagesPopupMenuProjects(){
        testNode(new Node(getProjectNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES));
    }
    
    public void testTestPackagesPopupMenuProjects(){
        testNode(new Node(getProjectNode("PerformanceTestData"), gui.Utilities.TEST_PACKAGES));
    }
    
    public void testPackagePopupMenuProjects(){
        testNode(new Node(getProjectNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance"));
    }
    
    public void testJavaFilePopupMenuProjects(){
        testNode(new Node(getProjectNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Main.java"));
    }
    
    public void testTxtFilePopupMenuProjects(){
        testNode(new Node(getProjectNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|textfile.txt"));
    }
    
    public void testPropertiesFilePopupMenuProjects(){
        testNode(new Node(getProjectNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|Bundle.properties"));
    }
    
    public void testXmlFilePopupMenuProjects(){
        testNode(new Node(getProjectNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|xmlfile.xml"));
    }
    
    public void testJspFilePopupMenuProjects(){
        testNode(new Node(getProjectNode("PerformanceTestWebApplication"), org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_DocBase") + "|Test.jsp"));
    }
    
    public void testNBProjectNodePopupMenuProjects() {
        testNode(getProjectNode("SystemProperties"));
    }
    
    
    public void testNode(Node node){
        dataObjectNode = node;
        doMeasurement();
    }
    
    private Node getProjectNode(String projectName) {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode(projectName);
    }

    
}
