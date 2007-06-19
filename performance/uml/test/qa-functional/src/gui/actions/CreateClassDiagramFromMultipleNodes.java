/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package gui.actions;

import java.io.File;

import javax.swing.tree.TreePath;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.junit.ide.ProjectSupport;

import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class CreateClassDiagramFromMultipleNodes extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String testProjectName = "jEdit-Model";
    private static String testDiagramName = "AbbrevEditor";    
    private Node diag;
    private NbDialogOperator create_diag ;
   
    /** Creates a new instance of CreateClassDiagramFromMultipleNodes */
    public CreateClassDiagramFromMultipleNodes(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = 5000;
        WAIT_AFTER_OPEN=4000;        
    }
    public CreateClassDiagramFromMultipleNodes(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = 5000;
        WAIT_AFTER_OPEN=4000;                
    }
    
    public void initialize(){
        log(":: initialize");
        
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+File.separator+testProjectName);
        new CloseAllDocumentsAction().performAPI();
        
    }
   
    public void prepare() {
        log(":: prepare");
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        diag = new Node(pNode,"Model"+"|"+"org"+"|"+"gjt"+"|"+"sp"+"|"+"jedit"+"|"+"gui"+"|"+testDiagramName);
        diag.select();
        new EventTool().waitNoEvent(1000);
        JTreeOperator projectTree = new ProjectsTabOperator().tree();
 
        TreePath[] arrTreePath ;
        arrTreePath=new TreePath[40] ;
        for (int i=0; i<arrTreePath.length ;i++) {
        arrTreePath[i]=projectTree.getPathForRow(22+i);
    }  
         projectTree.callPopupOnPaths(arrTreePath);   
         JPopupMenuOperator selectMenu = new JPopupMenuOperator();
         selectMenu.pushMenu("Create Diagram From Selected Elements");

        create_diag = new NbDialogOperator("Create New Diagram");
        new EventTool().waitNoEvent(1000);
	JListOperator diag_type = new JListOperator(create_diag,1);
        diag_type.selectItem("Class Diagram");
        JComboBoxOperator spaceCombo = new JComboBoxOperator(create_diag);
        spaceCombo.selectItem("jEdit-Model");
        
    }

    public ComponentOperator open() {
        log("::open");
 
       JButtonOperator finishButton = new JButtonOperator(create_diag,"Finish");
        finishButton.push();

         return null;
    }
    
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(testProjectName);
        new CloseAllDocumentsAction().performAPI();
    }
   

    public void close(){
        log("::close");
     new CloseAllDocumentsAction().performAPI();
 
    } 
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateClassDiagramFromMultipleNodes("measureTime"));
    }      


}