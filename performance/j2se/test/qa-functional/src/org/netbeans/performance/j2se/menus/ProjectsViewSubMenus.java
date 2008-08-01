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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.performance.j2se.menus;

import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

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
public class ProjectsViewSubMenus extends PerformanceTestCase {
    
    private static ProjectsTabOperator projectsTab = null;
    
    private String testedSubmenu;
    
    protected static Node dataObjectNode;
    
    private static final int repeat_original = Integer.getInteger("org.netbeans.performance.repeat", 1).intValue(); // initialize original value
    
    private JMenuItemOperator mio;
    private MouseDriver mdriver;

    public static final String suiteName="UI Responsiveness J2SE Menus";
    
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
    
    @Override
    public void close() {

        mio.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        mio.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }
    
    @Override
    public void setUp () {
        super.setUp();
        repeat = 1; // only first use is interesting
    }
    
    @Override
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
