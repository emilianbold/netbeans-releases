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

package gui.menu;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.junit.NbTestSuite;

import gui.menu.*;

/**
 * Test of popup menu on nodes in Projects View.
 * @author  mmirilovic@netbeans.org
 */
public class WebProjectsViewPopupMenu extends ValidatePopupMenuOnNodes {
    
    private static ProjectsTabOperator projectsTab = null;
    
    /** Creates a new instance of ProjectsViewPopupMenu */
    public WebProjectsViewPopupMenu(String testName) {
        super(testName);
    }
    
    /** Creates a new instance of ProjectsViewPopupMenu */
    public WebProjectsViewPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        
    }
    
    public void testProjectNodePopupMenuProjects() {
        testNode(getProjectNode());
    }
    
    public void testSourcePackagesPopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Source Packages"));
    }
    
    public void testPackagePopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Source Packages" + '|' + "test"));
    }
    
    public void testServletPopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Source Packages" + '|' + "test" + '|' + "TestServlet.java"));
    }
    
    public void testWebPagesPopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Web Pages"));
    }
     
    public void testJspFilePopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "Test.jsp"));
    }

    public void testHtmlFilePopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "HTML.html"));
    }
    
    public void testWebInfPopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "WEB-INF"));
    }
    
    public void testMetaInfPopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "META-INF"));
    }

    public void testWebXmlFilePopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "WEB-INF" + '|' + "web.xml"));
    }

    public void testContextXmlFilePopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "META-INF" + '|' + "context.xml"));
    }

    public void testTagFilePopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "WEB-INF" + '|' + "tags" + '|' + "mytag.tag"));
    }

    public void testTldPopupMenuProjects(){
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "WEB-INF" + '|' + "MyTLD.tld"));
    }
    
    public void testNode(Node node){
        dataObjectNode = node;
        doMeasurement();
    }
    
    private Node getProjectNode() {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode("TestWebProject");
    }

    
}
