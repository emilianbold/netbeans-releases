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

package gui.setup;

import gui.VWPUtilities;

import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * Test suite that actually does not perform any test but sets up servers
 * for Visual Web Pack tests (and opens required test projects...)
 *
 * @author  mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class WebSetupTest extends IDESetupTest {
    
    private RuntimeTabOperator rto = null;
    
    public WebSetupTest(String testName) {
        super(testName);
    }

    public void openWebPackProject() {
        // we'll open this project, creating is expensive and not so stable
        //createTestProject("VisualWebProject");
        
        VWPUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir")+ java.io.File.separator +"VisualWebProject");
        VWPUtilities.waitForPendingBackgroundTasks();
    }
    
    public void setupAppServer() {
        rto = RuntimeTabOperator.invoke();
        try {
            VWPUtilities.getApplicationServerNode();
        } catch (TimeoutExpiredException ex) {
            //App server node not found -(
            installAppServer();
        }
        
        try {
            VWPUtilities.getApplicationServerNode();
        } catch (TimeoutExpiredException ex) {
            //App server node not found -(
            fail("Application Server Installation failed");
        }
        ProjectsTabOperator.invoke();
    }
    
    private void installAppServer() {
        Node n = null;
        try {
            n = new Node(rto.tree(),"Servers"); // NOI18N
        } catch(TimeoutExpiredException e) {
            fail("Cannot select serves node");
        }
        n.select();
        n.callPopup().pushMenuNoBlock("Add Server"); // NOI18N
        processInstall();
    }
    
    private void processInstall() {
        WizardOperator wizard = new WizardOperator("Add Server Instance"); // NOI18N
        org.netbeans.jemmy.operators.JComboBoxOperator SrvTypeList;
        SrvTypeList = new JComboBoxOperator(wizard,0);
        SrvTypeList.selectItem("Sun Java System Application Server"); // NOI18N
        String ASPath = System.getProperty("com.sun.aas.installRoot");
        wizard.next();
        
        wizard = new WizardOperator("Add Server Instance"); // NOI18N
        JTextComponentOperator domainpath = null;
        try {
            domainpath = new JTextComponentOperator(wizard,0);
        } catch (TimeoutExpiredException tex) {
            fail("Cannot take a TextBox");
        }
        
        log("Actual path id: "+domainpath.getText());
        
        domainpath.setText(ASPath);
        log("Actual path id: "+domainpath.getText());
        
        new WizardOperator("Add Server Instance").next(); // NOI18N
        new JTextComponentOperator(new WizardOperator("Add Server Instance"),1).setText("adminadmin"); // NOI18N
        new WizardOperator("Add Server Instance").finish(); // NOI18N
        new EventTool().waitNoEvent(15000);
        
    }
    
    private void createTestProject(String ProjectName) {
        String category = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web"); // Web
        String project = "Web Application"; // NOI18N
        
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        NewProjectNameLocationStepOperator wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = System.getProperty("xtest.tmpdir");
        
        wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(ProjectName);
        
        wizard_location.next();
        
        JTableOperator frameworkselector = new JTableOperator(wizard);
        frameworkselector.selectCell(0,0);
        
        wizard_location.finish();
    }
    
}