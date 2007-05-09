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

import gui.Utils;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test create projects
 *
 * @author  lmartinek@netbeans.org
 */
public class CreateJ2EEProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name;
    private boolean createSubProjects = false;
    
    /**
     * Creates a new instance of CreateJ2EEProject
     * @param testName the name of the test
     */
    public CreateJ2EEProject(String testName) {
        super(testName);
        expectedTime = 20000;
        WAIT_AFTER_OPEN=30000;
    }
    
    /**
     * Creates a new instance of CreateJ2EEProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateJ2EEProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 20000;
        WAIT_AFTER_OPEN=30000;
    }
    
    public void testCreateEnterpriseApplicationProject(){
        category = "Enterprise";
        project = "Enterprise Application";
        project_name = "MyApp";
        createSubProjects = true;
        doMeasurement();
    }
   
    public void testCreateStandaloneEnterpriseApplicationProject(){
        category = "Enterprise";
        project = "Enterprise Application";
        project_name = "MyStandaloneApp";
        createSubProjects = false;
        doMeasurement();
    }

    public void testCreateEJBModuleProject(){
        category = "Enterprise";
        project = "EJB Module";
        project_name = "MyEJBModule";
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
        wizard_location.txtProjectLocation().setText(System.getProperty("xtest.tmpdir"));
        project_name += Utils.getTimeIndex();
        wizard_location.txtProjectName().setText(project_name);
        //project_name = wizard_location.txtProjectName().getText();
        if (project.equals("Enterprise Application")) {
            JCheckBoxOperator createEjb = new JCheckBoxOperator(wizard_location, "Ejb");
            JCheckBoxOperator createWeb = new JCheckBoxOperator(wizard_location, "Web");
            createEjb.setSelected(createSubProjects);
            createWeb.setSelected(createSubProjects);
        }
    }
    
    public ComponentOperator open(){
        wizard_location.finish();
        return null;
    }
    
    public void close(){
        ProjectSupport.closeProject(project_name);
        ProjectSupport.closeProject(project_name+"-EJBModule");
        ProjectSupport.closeProject(project_name+"-WebModule");
    }
    
    
}
