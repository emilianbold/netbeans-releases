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
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test Add New Bpel Process
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class AddNewEnterprise extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    protected NewProjectNameLocationStepOperator projectName_wizardLocation; // wizard_location
    protected NewFileNameLocationStepOperator fileName_wizardLocation; // location
    
    protected static final String BUNDLE = "org.netbeans.modules.bpel.project.wizards.Bundle";
    protected static final String PROJECT_CATEGORY = Bundle.getStringTrimmed(BUNDLE,"Templates/Project/SOA"); // "Service Oriented Architecture"
    protected static final String PROJECT_TYPE = Bundle.getStringTrimmed(BUNDLE,"Templates/Project/SOA/emptyBpelpro.xml"); // "BPEL Module"
    
    protected String project_name, file_category, file_type, file_name;
    protected int index=0;
    
    /**
     * Creates a new instance of AddNewBpelProcess
     * @param testName the name of the test
     */
    public AddNewEnterprise(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of AddNewBpelProcess
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public AddNewEnterprise(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public void testAddNewBpelProcess(){
        project_name = "BPELModule";
        file_category = "Service Oriented Architecture"; // NOI18N
        file_type = "BPEL Process"; // NOI18N
        file_name = "BPELProcess";
        doMeasurement();
    }
    
    public void testAddNewWSDLDocument(){
        project_name = "BPELModule";
        file_category = "XML"; // NOI18N
        file_type = "WSDL Document"; // NOI18N
        file_name = "WSDLDoc";
        doMeasurement();
    }
    
    public void testAddNewXMLDocument(){
        project_name = "BPELModule";
        file_category = "XML"; // NOI18N
        file_type = "XML Document"; // NOI18N
        file_name = "XMLDoc";
        doMeasurement();
    }
    
    public void testAddNewXMLSchema(){
        project_name = "BPELModule";
        file_category = "XML"; // NOI18N
        file_type = "XML Schema"; // NOI18N
        file_name = "XMLSchema";
        doMeasurement();
    }
    
    protected void initialize() {
        //create bpel project
        NewProjectWizardOperator wizardP = NewProjectWizardOperator.invoke();
        wizardP.selectCategory(PROJECT_CATEGORY);
        wizardP.selectProject(PROJECT_TYPE);
        wizardP.next();
        projectName_wizardLocation = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir")+java.io.File.separator+"createdProjects";
        log("================= Destination directory={"+directory+"}");
        
        new EventTool().waitNoEvent(1000);
        projectName_wizardLocation.txtProjectLocation().setText(directory);
        
        log("================= Project name="+project_name+"}");
        projectName_wizardLocation.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        projectName_wizardLocation.txtProjectName().typeText(project_name);
        new EventTool().waitNoEvent(1000);
        projectName_wizardLocation.finish();
        //bpel end
    }
    
    public void prepare(){
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        wizard.selectCategory(file_category);
        wizard.selectFileType(file_type);
        
        wizard.next();
        
        new EventTool().waitNoEvent(1000);
        fileName_wizardLocation = new NewFileNameLocationStepOperator();
        fileName_wizardLocation.txtObjectName().setText(file_name + "_" + (index++));
    }
    
    public ComponentOperator open(){
        fileName_wizardLocation.finish();
        return null;
    }
    
    public void close(){
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    protected void shutdown() {
        ProjectSupport.closeProject(project_name);
    }
    
}
