
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
 *
 * @author mkhramov@netbeans.org
 */
public class SchemaViewSwitchTest extends org.netbeans.performance.test.utilities.PerformanceTestCase  {

    private XMLSchemaComponentOperator schema;
    private static String category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA"); // "Service Oriented Architecture";
    private static String project = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle","Templates/Project/SOA/emptyBpelpro.xml"); // "BPEL Module"
    private static String testProjectName = "TestProject2";
    private static String testSchemaName = "XMLTestSchema2";    
    
    
    /** Creates a new instance of SchemaViewSwitch */
    public SchemaViewSwitchTest(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;           
    }
    
    public SchemaViewSwitchTest(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;                
    }    

    public void initialize(){
        log(":: initialize");
        String ParentPath = System.getProperty("xtest.tmpdir")+java.io.File.separator+"createdProjects";

        createProject(ParentPath,testProjectName);
        addSchemaDoc(testProjectName,testSchemaName);
    }

    private void createProject(String path, String projectName) {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        log(":: selecting category: "+category);
        wizard.selectCategory(category);

        log(":: selecting project: "+project);
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

    private void addSchemaDoc( String projectName, String SchemaName) {
        Node pfn =  new Node(new ProjectsTabOperator().getProjectRootNode(projectName), org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.bpel.project.ui.Bundle", "LBL_Node_Sources"));
        pfn.select();
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        wizard.selectCategory("XML");
        wizard.selectFileType("XML Schema");

        wizard.next();

        NewFileNameLocationStepOperator location = new NewFileNameLocationStepOperator();
        location.setObjectName(SchemaName);
        location.finish();
    }    

    public void prepare() {
        log(":: prepare");
        String schemaDocPath = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.bpel.project.ui.Bundle", "LBL_Node_Sources")+"|"+testSchemaName+".xsd";
        Node schemaNode = new Node(new ProjectsTabOperator().getProjectRootNode(testProjectName),schemaDocPath);        
        schemaNode.performPopupActionNoBlock("Open");
        schema = new XMLSchemaComponentOperator(testSchemaName+".xsd");        
    }

    public ComponentOperator open() {
        schema.getDesignButton().pushNoBlock();
        return schema;
    }

    public void close(){
        schema.getSchemaButton().pushNoBlock();
    }

    protected void shutdown() {
        log("::shutdown");
        
        new CloseAllDocumentsAction().performAPI();        
        ProjectSupport.closeProject(testProjectName);
    }
}