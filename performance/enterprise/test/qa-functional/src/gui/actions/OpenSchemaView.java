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
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author mkhramov@netbeans.org
 *
 */
public class OpenSchemaView extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String ParentPath;
    private Node schemaNode;
    
    private static String category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA"); // "Service Oriented Architecture";
    private static String project = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA/emptyBpelpro.xml"); // "BPEL Module"
    private static String testProjectName = "SOATestProject";
    private static String testSchemaName = "XMLTestSchema";

    /** Creates a new instance of OpenSchemaView */
    public OpenSchemaView(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;        
    }

    public OpenSchemaView(String testName, String  performanceDataName) {
        super(testName,performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;                
    }
    
    public void initialize(){
        log(":: initialize");
        ParentPath = System.getProperty("xtest.tmpdir")+java.io.File.separator+"createdProjects";

        createProject(ParentPath,testProjectName);
        addSchemaDoc(testProjectName,testSchemaName);
        
        new CloseAllDocumentsAction().performAPI();
    }

    private void createProject(String path, String projectName) {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);

        wizard.next();
        NewProjectNameLocationStepOperator wizard_location = new NewProjectNameLocationStepOperator();

        wizard_location.txtProjectName().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(projectName);
        new EventTool().waitNoEvent(1000);
        
        wizard_location.txtProjectLocation().setText("");
        new EventTool().waitNoEvent(1000);
        wizard_location.txtProjectLocation().typeText(path);
        
        new EventTool().waitNoEvent(1000);
        wizard_location.finish();
    }    

    public void prepare() {
        log(":: prepare");
    }

    public ComponentOperator open() {
        log("::open");
        String schemaDocPath = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.bpel.project.ui.Bundle", "LBL_Node_Sources")+"|"+testSchemaName+".xsd";
        schemaNode = new Node(new ProjectsTabOperator().getProjectRootNode(testProjectName),schemaDocPath);
        schemaNode.performPopupActionNoBlock("Open");
        return new XMLSchemaComponentOperator(testSchemaName+".xsd");
    }
    
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(testProjectName);
    }

    private void addSchemaDoc( String projectName, String SchemaName) {
        Node pfn =  new Node(new ProjectsTabOperator().getProjectRootNode(projectName), org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.bpel.project.ui.Bundle", "LBL_Node_Sources"));
  //      NewFileWizardOperator wizard = NewFileWizardOperator.invoke(pfn,"XML","XML Schema");
        pfn.select();

        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();

        wizard.selectCategory("XML");
        wizard.selectFileType("XML Schema"); 

   
        wizard.next();
       new EventTool().waitNoEvent(1000);
        NewFileNameLocationStepOperator location = new NewFileNameLocationStepOperator();
        location.setObjectName(SchemaName);
 //       wizard.finish();
            location.finish();

    }

  public void close(){
//        ProjectSupport.closeProject(project_name);
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
}
