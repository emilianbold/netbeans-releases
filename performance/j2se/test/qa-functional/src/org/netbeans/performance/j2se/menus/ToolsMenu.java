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

package org.netbeans.performance.j2se.menus;

import org.netbeans.modules.performance.guitracker.ActionTracker;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.junit.NbTestSuite;

/**
 * Performance test for tools menu invoked when a various node is selected.</p>
 * <p>Each test method reads the label of tested menu.
 * During @link prepare given node is selected and menu is pushed using mouse.
 * The menu is then closed using escape key.
 * @author Radim Kubacki, mmirilovic@netbeans.org
 */
public class ToolsMenu extends MainMenu {
    
    protected static Node dataObjectNode;
    
    public static final String suiteName="UI Responsiveness J2SE Menus";
    
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
    
    @Override
    public void prepare() {
        dataObjectNode.select();
    }
    
    private void testToolsMenu(String file) {
        dataObjectNode = new Node(new SourcePackagesNode("PerformanceTestData"),"org.netbeans.test.performance|" + file);
        super.testMenu("org.netbeans.core.ui.resources.Bundle","Menu/Tools");
    }

}
