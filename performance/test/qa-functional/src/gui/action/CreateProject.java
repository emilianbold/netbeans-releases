/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;

import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test create projects
 *
 * @author  mmirilovic@netbeans.org
 */
public class CreateProject extends testUtilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name;
    
    /**
     * Creates a new instance of CreateProject
     * @param testName the name of the test
     */
    public CreateProject(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of CreateProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void testCreateJavaApplicationProject(){
        category = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle","Templates/Project/Standard"); // "Standard"
        project = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle","Templates/Project/Standard/emptyJ2SE.xml"); // "Java Application"
        doMeasurement();
    }
    
    public void testCreateJavaLibraryProject(){
        category = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle","Templates/Project/Standard"); // "Standard"
        project = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle","Templates/Project/Standard/emptyJ2SElibrary.xml"); // "Java Class Library"
        doMeasurement();
    }
    
    public void testCreateWebApplicationProject(){
        category = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.projects.Bundle",""); //"Web"
        project = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle","Templates/Project/Web/emptyWeb.xml"); //"Web Application"
        doMeasurement();
    }

    /* TODO 
    public void testCreateJavaProjectWithExistingSources(){
        category="Standard";
        project="Java Project with Existing Sources";
        doMeasurement();
    }*/

    public void initialize(){
    }
    
    public void prepare(){
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        project_name = wizard_location.txtProjectName().getText();
    }
    
    public ComponentOperator open(){
        wizard_location.finish();
        return null;
    }
    
    public void close(){
        ProjectSupport.closeProject(project_name);
    }
    
}
