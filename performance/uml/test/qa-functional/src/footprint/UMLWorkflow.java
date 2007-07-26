5/*
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

package footprint;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OutputWindowViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Measure UML Project Workflow Memory footprint
 *
 * @author  mmirilovic@netbeans.org
 */
public class UMLWorkflow extends org.netbeans.performance.test.utilities.MemoryFootprintTestCase {
    
    private ProjectRootNode j2seproject, j2seprojectmodel;
    
    /**
     * Creates a new instance of J2EEProjectWorkflow
     *
     * @param testName the name of the test
     */
    public UMLWorkflow(String testName) {
        super(testName);
        prefix = "UML Project Workflow |";
    }
    
    /**
     * Creates a new instance of J2EEProjectWorkflow
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public UMLWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "UML Project Workflow |";
    }
    
    @Override
    public void setUp() {
        //do nothing
    }
    
    public void prepare() {
    }
    
    public void initialize() {
        super.initialize();
        UMLFootprintUtilities.closeAllDocuments();
        UMLFootprintUtilities.closeMemoryToolbar();
    }
    
    public ComponentOperator open(){
        // Create, edit, build and execute a sample J2SE project
        String j2seprojectName = UMLFootprintUtilities.createproject("Samples|Java", "Anagram Game", true);
        j2seproject = new ProjectsTabOperator().getProjectRootNode(j2seprojectName);
        
        new OutputWindowViewAction().performAPI();
        UMLFootprintUtilities.buildproject(j2seprojectName);
        
        // reverse enginnering
        new ActionNoBlock(null,"Reverse Engineer...").performPopup(j2seproject); //NOI18N
        new NbDialogOperator("Reverse Engineer").ok(); //NOI18N
        
        new OutputOperator().getOutputTab("Reverse Engineering Log").waitText("Task Successful"); //NOI18N
        j2seprojectmodel = new ProjectsTabOperator().getProjectRootNode(j2seprojectName+"-Model");
        j2seprojectmodel.expand();
        
        Node modelNode = new Node(j2seprojectmodel, "Model");
        modelNode.expand();
        
        String modelNodeNames[] = modelNode.getChildren();
        Node modelNodes[] = new Node[modelNodeNames.length];
        
        for (int i = 0; i < modelNodeNames.length; i++) {
            modelNodes[i] = new Node(modelNode, modelNodeNames[i]);
        }
        
        new ActionNoBlock(null,"Create Diagram From Selected Elements...").performPopup(modelNodes); //NOI18N
        WizardOperator createNewDiagram = new WizardOperator("Create New Diagram"); //NOI18N
        new JListOperator(createNewDiagram, "Activity Diagram").selectItem("Class Diagram"); //NOI18N
        JTextFieldOperator textfield = new JTextFieldOperator(createNewDiagram);
        textfield.clearText();
        textfield.typeText("ClassDiagram");
        createNewDiagram.finish();
        
        return null;
    }
    
    public void close(){
//        UMLFootprintUtilities.deleteProject(j2seprojectmodel.getText());
//        UMLFootprintUtilities.deleteProject(j2seproject.getText());
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new UMLWorkflow("measureMemoryFooprint"));
    }
    
}
