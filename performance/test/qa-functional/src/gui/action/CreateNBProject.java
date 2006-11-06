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

package gui.action;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test create projects
 *
 * @author  mmirilovic@netbeans.org
 */
public class CreateNBProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name, project_type;
    
    private int index;
    
    /**
     * Creates a new instance of CreateNBProject
     * @param testName the name of the test
     */
    public CreateNBProject(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of CreateNBProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateNBProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void testCreateModuleProject(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle","Templates/Project/APISupport"); //"NetBeans Plug-in Modules"
        project = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle","Templates/Project/APISupport/emptyModule"); //"Module Project"
        project_type="moduleProject";
        index=1;
        doMeasurement();
    }

    public void testCreateModuleSuiteProject(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle","Templates/Project/APISupport"); //"NetBeans Plug-in Modules"
        project = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle","Templates/Project/APISupport/emptySuite"); //"Module Suite Project"
        project_type="moduleSuiteProject";
        index=1;
        doMeasurement();
    }

    public void initialize(){
    }
    
    public void prepare(){
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir")+"/"+"createdProjects";
        log("================= Destination directory={"+directory+"}");
        wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        project_name = project_type + "_" + (index++);
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
    }
    
    public ComponentOperator open(){
        if(project_type.equalsIgnoreCase("moduleProject")){
            wizard_location.next();
        }
        wizard_location.finish();
        ProjectSupport.waitScanFinished();
        new EventTool().waitNoEvent(1000);
        return null;
    }
    
    public void close(){
        ProjectSupport.closeProject(project_name);
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateNBProject("testCreateModuleProject"));
    }
}
