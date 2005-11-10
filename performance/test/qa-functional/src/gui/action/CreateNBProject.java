/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
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
        category = "NetBeans Plug-in Modules";
        project = "Module Project";
        project_type="moduleProject";
        index=1;
        doMeasurement();
    }

    public void testCreateModuleSuiteProject(){
        category = "NetBeans Plug-in Modules";
        project = "Module Suite Project";
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
        return null;
    }
    
    public void close(){
        ProjectSupport.closeProject(project_name);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateNBProject("testCreateModuleProject"));
    }
}
