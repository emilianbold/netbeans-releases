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
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.NbDialogOperator;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class CodeGenerationFromUMLProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String testProjectName = "jEdit-Model";
    private NbDialogOperator codeDialog ;   
   
    /** Creates a new instance of GenerateModelReport */
    public CodeGenerationFromUMLProject(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;        
    }
    public CodeGenerationFromUMLProject(String testName, String  performanceDataName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
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
        pNode.performPopupActionNoBlock("Generate Code...");
    }

    public ComponentOperator open() {
        log("::open");
        codeDialog = new NbDialogOperator("Generate Code");
        codeDialog.ok();
        OutputOperator oot = new OutputOperator();
        oot.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",600000);
        OutputTabOperator asot = oot.getOutputTab("Generate Code Log");
        asot.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",600000);
        asot.waitText("Task Successful");
      
        return null;
    }
    
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(testProjectName);
    }
   

    public void close(){
        log("::close");
      new CloseAllDocumentsAction().performAPI();
 
    } 
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CodeGenerationFromUMLProject("measureTime"));
    }      


}