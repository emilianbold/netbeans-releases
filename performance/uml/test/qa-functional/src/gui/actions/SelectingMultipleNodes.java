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
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class SelectingMultipleNodes extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String testProjectName = "jEdit-Model";
    private static String testDiagramName = "ClassDiagram";    
    private Node diag;
   
    /** Creates a new instance of SelectingMultipleNodes */
    public SelectingMultipleNodes(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = 2000;
        WAIT_AFTER_OPEN=4000;        
    }
    public SelectingMultipleNodes(String testName, String  performanceDataName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 2000;
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
        diag = new Node(pNode,"Model"+"|"+testDiagramName);
        diag.select();
        new EventTool().waitNoEvent(1000);
    }

    public ComponentOperator open() {
        log("::open");

        JTreeOperator projectTree = new ProjectsTabOperator().tree();
  
        projectTree.addSelectionInterval(2,120);
           
         return null;
    }
    
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(testProjectName);
        new CloseAllDocumentsAction().performAPI();
    }
   

    public void close(){
        log("::close");
        diag.select();
 //     new CloseAllDocumentsAction().performAPI();
 
    } 
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new SelectingMultipleNodes("measureTime"));
    }      


}