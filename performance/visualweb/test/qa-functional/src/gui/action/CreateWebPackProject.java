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

package gui.action;

import footprint.VWPFootprintUtilities;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test create Web Pack projects
 *
 * @author  mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateWebPackProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name, project_type;
    
    private int index;

    /**
     * Creates a new instance of CreateWebPackProject
     * @param testName the name of the test
     */
    public CreateWebPackProject(String testName) {
        super(testName);
        expectedTime = 25000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of CreateWebPackProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateWebPackProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 25000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void testCreateWebPackProject() {
        category = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web"); // Web
        project = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.visualweb.project.jsfproject.ui.wizards.Bundle", "Templates/Project/JsfWeb/emptyJsf.xml"); // Visual Web Application
        project_type="JSFWebProject";
        index=1;
	doMeasurement();
    }

    public void initialize(){
	log("::initialize::");
    }

    public void prepare(){
        createProject();
    }

    private void createProject() {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir");
        log("================= Destination directory={"+directory+"}");
        wizard_location.txtProjectLocation().setText("");
        waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        project_name = project_type + "_" + (index++);
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
    }

    public ComponentOperator open(){
        wizard_location.finish();
        return null;
    }
    
    public void close(){
        VWPFootprintUtilities.deleteProject(project_name);
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
       
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateWebPackProject("testCreateWebPackProject"));
    }    	

}
