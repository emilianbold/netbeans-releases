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


package gui.actions;


import java.awt.event.InputEvent;
import java.io.File;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.performance.test.guitracker.ActionTracker;
/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class SelectingMultipleNodes extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String testProjectName = "jEdit-Model";
    private TreePath path1, path2;
    private Node pNode;
   
    /** Creates a new instance of SelectingMultipleNodes */
    public SelectingMultipleNodes(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = 2000;
        WAIT_AFTER_OPEN=4000;  
        HEURISTIC_FACTOR = -1;
    }
    public SelectingMultipleNodes(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = 2000;
        WAIT_AFTER_OPEN=4000;                
        HEURISTIC_FACTOR = -1;
    }
    
    public void initialize(){
        log(":: initialize");
        
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;        
        
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+File.separator+testProjectName);
//        new CloseAllDocumentsAction().performAPI();
        
    }
   
    public void prepare() {
        log(":: prepare");
        pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        Node diag1 = new Node(pNode,"Model|ClassDiagram");        
        Node diag2 = new Node(pNode,"Imported Elements");        

        path1 = diag1.getTreePath();
        path2 = diag2.getTreePath();
        
        new EventTool().waitNoEvent(1000);
    }

    public ComponentOperator open() {
        log("::open");

        JTreeOperator projectTree = new ProjectsTabOperator().tree();
  
        projectTree.clickOnPath(path1, 1, InputEvent.BUTTON1_MASK);
        new EventTool().waitNoEvent(500);
        projectTree.clickOnPath(path2, 1, InputEvent.BUTTON1_MASK, InputEvent.SHIFT_MASK);
           
        return null;
    }
    
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(testProjectName);
//        new CloseAllDocumentsAction().performAPI();
    }
   

    public void close(){
        log("::close");
        pNode.select();
 //     new CloseAllDocumentsAction().performAPI();
 
    } 
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new SelectingMultipleNodes("measureTime"));
    }      


}
