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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.actions;

import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;

/**
 * Test create CreateMobilityProject
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateMobilityProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name, project_type;
    
    private int index;
    
    /**
     * Creates a new instance of CreateMobilityProject
     * @param testName the name of the test
     */
    public CreateMobilityProject(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }
    
    /**
     * Creates a new instance of CreateMobilityProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateMobilityProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }
    
    public void testCreateMobilityProject(){
        category = "Mobility"; // NOI18N
        project = "MIDP Application"; // NOI18N
        project_type="MobileApp";
        index=1;
        doMeasurement();
    }
    
      public void testCreateMobilityLibrary(){
        category = "Mobility"; // NOI18N
        project = "Mobile Class Library"; // NOI18N
        project_type="MobileLib";
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
        
        String directory = System.getProperty("xtest.tmpdir") + java.io.File.separator + "createdProjects";
        log("================= Destination directory={"+directory+"}");
      //  wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        project_name = project_type + "_" + (index++);
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        
        wizard_location.txtProjectName().typeText(project_name);
    }
    
    public ComponentOperator open(){
        wizard_location.finish();
        return null;
    }
    
    public void close(){
        ProjectSupport.closeProject(project_name);
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateMobilityProject("testCreateMobilityProject"));
    }
}
