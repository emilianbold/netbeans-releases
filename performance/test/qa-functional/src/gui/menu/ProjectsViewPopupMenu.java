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
import org.netbeans.jellytools.nodes.ProjectRootNode;

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
        suite.addTest(new ProjectsViewPopupMenu("testProjectNodePopupMenu", "Project node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testSourcePackagesPopupMenu", "Source Packages node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testTestPackagesPopupMenu", "Test Packages node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testPackagePopupMenu", "Package node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testJavaFilePopupMenu", "Java file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testTxtFilePopupMenu", "Txt file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testPropertiesFilePopupMenu", "Properties file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testXmlFilePopupMenu", "Xml file node popup in Projects View"));
        suite.addTest(new ProjectsViewPopupMenu("testJspFilePopupMenu", "Jsp file node popup in Projects View"));
        return suite;
    }
    
    public void testProjectNodePopupMenu() {
        testNode(getProjectNode());
    }
    
    public void testSourcePackagesPopupMenu(){
        testNode(new Node(getProjectNode(), "Source Packages"));
    }
    
    public void testTestPackagesPopupMenu(){
        testNode(new Node(getProjectNode(), "Test Packages"));
    }
    
    public void testPackagePopupMenu(){
        testNode(new Node(getProjectNode(), "Source Packages" + '|' + "org.netbeans.test.performance"));
    }
    
    public void testJavaFilePopupMenu(){
        testNode(new Node(getProjectNode(), "Source Packages" + '|' + "org.netbeans.test.performance" + '|' + "Main.java"));
    }
    
    public void testTxtFilePopupMenu(){
        testNode(new Node(getProjectNode(), "Source Packages" + '|' + "org.netbeans.test.performance" + '|' + "textfile.txt"));
    }
    
    public void testPropertiesFilePopupMenu(){
        testNode(new Node(getProjectNode(), "Source Packages" + '|' + "org.netbeans.test.performance" + '|' + "Bundle.properties"));
    }
    
    public void testXmlFilePopupMenu(){
        testNode(new Node(getProjectNode(), "Source Packages" + '|' + "org.netbeans.test.performance" + '|' + "xmlfile.xml"));
    }
    
    public void testJspFilePopupMenu(){
        testNode(new Node(getWebProjectNode(), "Web Pages" + '|' + "Test.jsp"));
    }
    
    public void testNode(Node node){
        dataObjectNode = node;
        doMeasurement();
    }
    
    private Node getProjectNode() {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode("PerformanceTestData");
    }

    private Node getWebProjectNode() {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode("PerformanceTestWebApplication");
    }
    
}
