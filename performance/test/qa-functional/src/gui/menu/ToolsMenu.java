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

import org.netbeans.performance.test.guitracker.ActionTracker;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.junit.NbTestSuite;

/**
 * Performance test for tools menu invoked when a various node is selected.</p>
 * <p>Each test method reads the label of tested menu.
 * During @link prepare given node is selected and menu is pushed using mouse.
 * The menu is then closed using escape key.
 * @author Radim Kubacki
 */
public class ToolsMenu extends MainMenu {
    
    protected static Node dataObjectNode;
    private static ProjectsTabOperator projectsTab = null;
    
    /** Creates a new instance of ToolsMenu */
    public ToolsMenu(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    /** Creates a new instance of ToolsMenu */
    public ToolsMenu(String testName, String performanceDataName) {
        this(testName);
        expectedTime = UI_RESPONSE;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
        setTestCaseName(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ToolsMenu("testJavaToolsMenu"));
        suite.addTest(new ToolsMenu("testXmlToolsMenu"));
        suite.addTest(new ToolsMenu("testTxtToolsMenu"));
        return suite;
    }
    
    public void testJavaToolsMenu(){
        testToolsMenu("Main.java");
    }
    
    public void testXmlToolsMenu(){
        testToolsMenu("xmlfile.xml");
    }
    
    public void testTxtToolsMenu(){
        testToolsMenu("textfile.txt");
    }
    
    public void prepare() {
        dataObjectNode.select();
    }
    
    private void testToolsMenu(String file) {
        dataObjectNode = new Node(getProjectNode(), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|" + file);
        super.testMenu("org.netbeans.core.Bundle","Menu/Tools");
    }

    private Node getProjectNode() {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode("PerformanceTestData");
    }
    
}
