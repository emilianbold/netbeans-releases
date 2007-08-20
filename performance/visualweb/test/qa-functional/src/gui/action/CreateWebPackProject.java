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
import gui.window.WebFormDesignerOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * Test create Web Pack projects
 *
 * @author  mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateWebPackProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewProjectNameLocationStepOperator wizard_location;
    
    private String category, project, project_name, project_type;
    
    
    
    /**
     * Creates a new instance of CreateWebPackProject
     * @param testName the name of the test
     */
    public CreateWebPackProject(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of CreateWebPackProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateWebPackProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void testCreateWebPackProject() {
        category = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web"); // Web
        project = "Web Application";
        project_type="JSFWebProject";
        
        doMeasurement();
    }
    
    public void initialize(){
        log("::initialize::");
        long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",120000);
        
        waitProjectCreatingDialogClosed();
        
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",oldTimeout);                
                
    }
    
    public void prepare(){
        log("::prepare");
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
        
        project_name = project_type + "_" + System.currentTimeMillis();
        log("================= Project name="+project_name+"}");
        wizard_location.txtProjectName().setText("");
        waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
        
        wizard_location.next();
        
        JTableOperator frameworkselector = new JTableOperator(wizard);
        frameworkselector.selectCell(0,0);
        
        
        
    }
    
    public ComponentOperator open(){
        log("::open");    
        wizard_location.finish();
        
        long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",120000);
        wizard_location.waitClosed();
        waitProjectCreatingDialogClosed();

        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",oldTimeout);        
        

        WebFormDesignerOperator.findWebFormDesignerOperator("Page1");

        return null;
    }
    private void waitProjectCreatingDialogClosed()
    {
       String dlgName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.project.jsf.ui.Bundle", "CAP_Opening_Projects");
       try {
           NbDialogOperator dlg = new NbDialogOperator(dlgName);
           dlg.waitClosed();
       } catch(TimeoutExpiredException tex) {
           //
       }
       
    }    
    public void close(){
        log("::close");

        try {
            new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
        } catch (Exception ex) {
            log("Exception catched on CloseAllDocuments action: "+ex.getMessage());
        }
        VWPFootprintUtilities.deleteProject(project_name);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CreateWebPackProject("testCreateWebPackProject"));
    }
    
}
