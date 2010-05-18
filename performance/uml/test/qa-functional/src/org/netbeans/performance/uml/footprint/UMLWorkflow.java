/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.performance.uml.footprint;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OutputWindowViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.modules.performance.utilities.MemoryFootprintTestCase;

/**
 * Measure UML Project Workflow Memory footprint
 *
 * @author  mmirilovic@netbeans.org
 */
public class UMLWorkflow extends MemoryFootprintTestCase {
    
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
    
    @Override
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
    
    @Override
    public void close(){
//        UMLFootprintUtilities.deleteProject(j2seprojectmodel.getText());
//        UMLFootprintUtilities.deleteProject(j2seproject.getText());
    }
    
//    public static void main(java.lang.String[] args) {
//        junit.textui.TestRunner.run(new UMLWorkflow("measureMemoryFooprint"));
//    }
    
}
