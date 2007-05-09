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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of popup menu on nodes in Projects View.
 * @author  lmartinek@netbeans.org
 */
public class J2EEProjectsViewPopupMenu extends ValidatePopupMenuOnNodes {
    
    private static ProjectsTabOperator projectsTab = null;
    private static final String JAVA_EE_MODULES = Bundle.getStringTrimmed(
                "org.netbeans.modules.j2ee.earproject.ui.Bundle",
                "LBL_LogicalViewNode");
    
    /**
     * Creates a new instance of J2EEProjectsViewPopupMenu 
     */
    public J2EEProjectsViewPopupMenu(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of J2EEProjectsViewPopupMenu 
     */
    public J2EEProjectsViewPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    
    public void testEARProjectNodePopupMenu() {
        testNode(getEARProjectNode(), null);
    }
    
    public void testEARConfFilesNodePopupMenu(){
        testNode(getEARProjectNode(), "Configuration Files");
    }
    
    public void testApplicationXmlPopupMenu(){
        testNode(getEARProjectNode(), "Configuration Files|application.xml");
    }

    public void testSunApplicationXmlPopupMenu(){
        testNode(getEARProjectNode(), "Configuration Files|sun-application.xml");
    }
    
    public void testJ2eeModulesNodePopupMenu(){
        testNode(getEARProjectNode(), JAVA_EE_MODULES);
    }

    public void testJ2eeModulesEJBNodePopupMenu(){
        testNode(getEARProjectNode(), JAVA_EE_MODULES+"|TestApplication-EJBModule.jar");
    }

    public void testJ2eeModulesWebNodePopupMenu(){
        testNode(getEARProjectNode(), JAVA_EE_MODULES+"|TestApplication-WebModule.war");
    }

    public void testEJBProjectNodePopupMenu() {
        testNode(getEJBProjectNode(), null);
    }
    
    public void testEJBsNodePopupMenu() {
        testNode(getEJBProjectNode(), "Enterprise Beans");
    }
    
    public void testSessionBeanNodePopupMenu() {
        testNode(getEJBProjectNode(), "Enterprise Beans|TestSessionSB");
    }
    
    public void testEntityBeanNodePopupMenu() {
        testNode(getEJBProjectNode(), "Enterprise Beans|TestEntityEB");
    }
    
    public void testEjbJarXmlPopupMenu(){
        testNode(getEJBProjectNode(), "Configuration Files|ejb-jar.xml");
    }

    public void testSunEjbJarXmlPopupMenu(){
        testNode(getEJBProjectNode(), "Configuration Files|sun-ejb-jar.xml");
    }
    
    
    public void testNode(Node rootNode, String path){
        try {
            if (path == null)
                dataObjectNode = rootNode;
            else
                dataObjectNode = new Node(rootNode, path);
            doMeasurement();
        } catch (Exception e) {
            throw new Error("Exception thrown",e);
        }
        
    }
    
    private Node getEARProjectNode() {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode("TestApplication");
    }

    private Node getWebProjectNode() {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode("TestApplication-WebModule");
    }
    
    private Node getEJBProjectNode() {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode("TestApplication-EJBModule");
    }
    
}
