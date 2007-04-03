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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.NewFileAction;


import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test Add New Bpel Process
 *
 * @author  rashid@netbeans.org
 */
public class AddNewBpelProcess extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    private NewProjectWizardOperator wizardP; 
    private NewProjectNameLocationStepOperator wizard_location;
    private NewFileNameLocationStepOperator location;
    private NewFileWizardOperator wizard ;
    private String category, project, project_name, project_type, docname, doctype, doccategory ;
    
    private int index;
    private int indexI;
    /**
     * Creates a new instance of AddNewBpelProcess
     * @param testName the name of the test
     */
    public AddNewBpelProcess(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of AddNewBpelProcess
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public AddNewBpelProcess(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public void testAddNewBpelProcess(){
        doccategory = "Service Oriented Architecture";
        doctype = "BPEL Process";
        docname = "BPELProcess";
        category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA"); // "Service Oriented Architecture"
        project = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA/emptyBpelpro.xml"); // "BPEL Module"
        project_type="BPELModule_";
        index=1;
        indexI=1;
        doMeasurement();

    }
    
    
    
    public void initialize(){
//create bpel project
       NewProjectWizardOperator wizardP = NewProjectWizardOperator.invoke();
        wizardP.selectCategory(category);
        wizardP.selectProject(project);
        wizardP.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir")+"/"+"createdProjects";
        log("================= Destination directory={"+directory+"}");
      
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        project_name = project_type + "_" + (index++);
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
        new EventTool().waitNoEvent(1000);
        wizard_location.finish();
//bpel end

    }
    
    public void prepare(){
  
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        wizard.selectCategory(doccategory);
        wizard.selectFileType(doctype);
	
        wizard.next();
        
        new EventTool().waitNoEvent(1000);
        location = new NewFileNameLocationStepOperator();
        location.txtObjectName().setText(docname+"_"+(indexI++));
    
    }
    
    public ComponentOperator open(){
      location.finish();
       return null;
    }
    
    public void close(){
//        ProjectSupport.closeProject(project_name);
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }

    protected void shutdown() {
         ProjectSupport.closeProject(project_name);
//    new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport      
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new AddNewBpelProcess("testAddNewBpelProcess"));
    }
}
