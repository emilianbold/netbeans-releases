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


package org.netbeans.performance.languages.menus;


import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.Projects;

/**
 *
 * @author mkhramov@netbeans.org
 */

public class ScriptingNodePopup extends PerformanceTestCase {
    public static final String suiteName="Scripting UI Responsiveness Menus suite";
    private String testProject;
    private String docName;
    private String pathName;
    
    protected static Node dataObjectNode;
    protected static ProjectsTabOperator projectsTab = null;
    
    public ScriptingNodePopup(String testName) {
        super(testName);
        expectedTime = 100;
    }
    public ScriptingNodePopup(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 100;        
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ScriptingNodePopup("test_RB_NodePopup"," Ruby file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_RHTML_NodePopup","RHTML file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_YML_NodePopup","YML file node popup test"));        
        suite.addTest(new ScriptingNodePopup("test_JS_NodePopup","Java Script file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_JSON_NodePopup","JSON file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_CSS_NodePopup","CSS file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_BAT_NodePopup","Batch script file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_DIFF_NodePopup","Diff file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_MANIFEST_NodePopup","Manifest file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_SH_NodePopup","Shell Script file node popup test"));
        return suite;
    }
    

    /**
     * Selects node whose popup menu will be tested.
     */
    public void prepare() {
        dataObjectNode.select();
    }
    
    /**
     * Directly sends mouse events causing popup menu displaying to the selected node.
     * <p>Using Jemmy/Jelly to call popup can cause reselecting of node and more events
     * than is desirable for this case.
     */
    public ComponentOperator open(){
        /* it stopped to work after a while, see issue 58790
        java.awt.Point p = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
        JPopupMenu menu = callPopup(dataObjectNode.tree(), p.x, p.y, java.awt.event.InputEvent.BUTTON3_MASK);
        return new JPopupMenuOperator(menu);
         */
        
        java.awt.Point point = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
        int button = JTreeOperator.getPopupMouseButton();
        dataObjectNode.tree().clickMouse(point.x, point.y, 1, button);
        return new JPopupMenuOperator();
    }

    /**
     * Closes the popup by sending ESC key event.
     */
    @Override
    public void close(){
        //testedComponentOperator.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        // Above sometimes fails in QUEUE mode waiting to menu become visible.
        // This pushes Escape on underlying JTree which should be always visible
        dataObjectNode.tree().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }
    
    public void test_RB_NodePopup() {
        testProject = Projects.RUBY_PROJECT;
        pathName = "";
        docName = "ruby20kb.rb";
        testNode(new Node(getProjectNode(testProject),"Source Files"+"|"+docName));
    }
    public void test_PHP_NodePopup() {
        testProject = Projects.PHP_PROJECT;
        pathName = "";
        docName = "php20kb.php";
        testNode(new Node(getProjectNode(testProject),"Source Files"+"|"+docName));        
    }
    
    public void test_RHTML_NodePopup() {
        testProject = Projects.RAILS_PROJECT;
        pathName = "";
        docName = "rhtml20kb.rhtml";
        testNode(new Node(getProjectNode(testProject),"Views"+"|"+docName));
    }
    public void test_YML_NodePopup() {
        testProject = Projects.RAILS_PROJECT;
        pathName = "";
        docName = "yaml20kb.yml";        
        testNode(new Node(getProjectNode(testProject),"Configuration"+"|"+docName));
    }    
    public void test_JS_NodePopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "";
        docName = "javascript20kb.js";        
        testNode(new Node(getProjectNode(testProject),"Web Pages"+"|"+docName));
    }
    public void test_JSON_NodePopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "";
        docName = "json20kb.json";        
        testNode(new Node(getProjectNode(testProject),"Web Pages"+"|"+docName));
    }
    public void test_CSS_NodePopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "";
        docName = "css20kb.css";        
        testNode(new Node(getProjectNode(testProject),"Web Pages"+"|"+docName));
    }
    public void test_BAT_NodePopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "";
        docName = "bat20kb.bat";        
        testNode(new Node(getProjectNode(testProject),"Web Pages"+"|"+docName));
    }
    public void test_DIFF_NodePopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "";
        docName = "diff20kb.diff";        
        testNode(new Node(getProjectNode(testProject),"Web Pages"+"|"+docName));
    }
    public void test_MANIFEST_NodePopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "";
        docName = "manifest20kb.mf";        
        testNode(new Node(getProjectNode(testProject),"Web Pages"+"|"+docName));
    }
    public void test_SH_NodePopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "";
        docName = "sh20kb.sh";        
        testNode(new Node(getProjectNode(testProject),"Web Pages"+"|"+docName));
    }
    
    public void testNode(Node node){
        dataObjectNode = node;
        doMeasurement();
    }
    
    protected Node getProjectNode(String projectName) {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode(projectName);
    }    
}
