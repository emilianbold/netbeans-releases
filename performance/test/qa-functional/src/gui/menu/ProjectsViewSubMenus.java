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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of submenu in popup menu on nodes in Projects View.
 * @author  mmirilovic@netbeans.org
 */
public class ProjectsViewSubMenus extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static ProjectsTabOperator projectsTab = null;
    
    private String testedSubmenu;
    
    protected static Node dataObjectNode;
    
    private static final int repeat_original = Integer.getInteger("org.netbeans.performance.repeat", 1).intValue(); // initialize original value
    
    private JMenuItemOperator mio;
    private MouseDriver mdriver;
    
    /** Creates a new instance of ProjectsViewPopupMenu */
    public ProjectsViewSubMenus(String testName) {
        super(testName);
        expectedTime = 250;
        WAIT_AFTER_OPEN = 1000;
        track_mouse_event = ActionTracker.TRACK_MOUSE_MOVED;
    }
    
    /** Creates a new instance of ProjectsViewPopupMenu */
    public ProjectsViewSubMenus(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 250;
        WAIT_AFTER_OPEN = 1000;
        track_mouse_event = ActionTracker.TRACK_MOUSE_MOVED;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ProjectsViewSubMenus("testProjectNodeCVSsubmenu", "CVS Submenu over projects node in Projects View"));
        suite.addTest(new ProjectsViewSubMenus("testProjectNodeNewSubmenu", "New Submenu over projects node in Projects View"));
        return suite;
    }
    
    public void testProjectNodeCVSsubmenu() {
        testedSubmenu = Bundle.getStringTrimmed("org.netbeans.modules.versioning.system.cvss.ui.actions.Bundle","CTL_MenuItem_CVSCommands_Label"); //CVS
        testNode(getProjectNode("PerformanceTestData"));
    }
    
    public void testProjectNodeNewSubmenu(){
        testedSubmenu = Bundle.getStringTrimmed("org.openide.actions.Bundle","NewFromTemplate"); //New
        testNode(getProjectNode("PerformanceTestData"));
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

    public void prepare(){
        JPopupMenuOperator popupMenu = dataObjectNode.callPopup();
        mio = new JMenuItemOperator(popupMenu,testedSubmenu);
        assertNotNull("Can not find "+testedSubmenu+" menu item in pop up menu over ["+dataObjectNode.getPath()+"]", mio);
        
        mdriver = org.netbeans.jemmy.drivers.DriverManager.getMouseDriver(mio);
    }
    
    public ComponentOperator open(){
        mdriver.moveMouse(mio, mio.getCenterXForClick(), mio.getCenterYForClick());
//        mio.pushKey(java.awt.event.KeyEvent.VK_RIGHT);
        return mio;
    }
    
    public void close() {
        testedComponentOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        testedComponentOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }
    
    public void setUp () {
        super.setUp();
        repeat = 1; // only first use is interesting
    }
    
    public void tearDown() {
        super.tearDown();
        repeat = repeat_original; // initialize original value
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new ProjectsViewSubMenus("testProjectNodeNewSubmenu"));
    }
    
}
