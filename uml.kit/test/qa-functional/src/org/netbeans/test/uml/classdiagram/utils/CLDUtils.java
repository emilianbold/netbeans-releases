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


/*
 * CLDUtils.java
 *
 * Created on April 20, 2005, 11:26 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.uml.classdiagram.utils;

import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.RenameAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.NewUMLProjectStepOperator;
import org.netbeans.test.umllib.Utils;
import org.netbeans.test.umllib.actions.AddDiagramAction;
import org.netbeans.test.umllib.actions.CloseDiagramAction;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.util.OptionsOperator;
import org.netbeans.test.umllib.util.PopupConstants;


/**
 *
 * @author VijayaBabu Mummaneni
 */
public class CLDUtils {

        public static String CDFS_XTEST_PROJECT_DIR = UMLTestCase.XTEST_PROJECT_DIR + "/Projects-Class";

    /**
     * Creates a new instance of CLDUtils
     */
    public CLDUtils() {
    }
    
    public static void setProjectCreationPreferences()
   {
        OptionsOperator op=OptionsOperator.invoke();
        op=op.invokeAdvanced();
        TreeTableOperator tr=op.treeTable();
        tr.tree().selectPath(tr.tree().findPath("UML|New Project"));
        tr.tree().waitSelected(tr.tree().findPath("UML|New Project"));
        new EventTool().waitNoEvent(1000);
        PropertySheetOperator ps=new PropertySheetOperator(op);
        Property pr=new Property(ps,"Create New Diagram");
        pr.setValue(1);
        if(pr.getValue().equalsIgnoreCase("yes"))pr.setValue(0);
        new JButtonOperator(op,"Close").push();
   }
    
    public static ProjectRootNode createNewUMLProject(String projName, String folderName) {
        //setProjectCreationPreferences();
        ProjectRootNode root = null;
        int repCount = 5;
        try {
            UMLProject umlProject   = UMLProject.createProject(projName, ProjectType.UML_JAVA_PLATFORM_MODEL);
//            NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
//            while(repCount>0) { //Very unstable due to Tootip NPE (From Collab ????)
//                try {
//                    wizard.selectCategory(LabelsAndTitles.UML_PROJECTS_CATEGORY);
//                    repCount = 0;
//                }catch(Exception e) {
//                    repCount--;
//                }
//            }
//            wizard.selectProject(LabelsAndTitles.JAVA_UML_PROJECT_LABEL);
//            wizard.next();
//            NewUMLProjectStepOperator umlWizard = new NewUMLProjectStepOperator();
//            umlWizard.txtProjectName().setText(projName);
//            umlWizard.txtProjectLocation().setText(folderName);
//            //umlWizard.cbSetAsMainProject().changeSelection(false);
//            umlWizard.finish();
//            new JButtonOperator(new JDialogOperator("Create New Diagram"), "Cancel").pushNoBlock();
//            Thread.sleep(1000);
//            
            ProjectsTabOperator pto = ProjectsTabOperator.invoke();
            root = new ProjectRootNode(pto.tree(),projName) ;
            
        }catch(Exception e) {
            e.printStackTrace();
	        throw new RuntimeException(e);
        }
        
        return root;
    }
    
    
    public static Node createNewClassDiagram(String classDiagramName, String projName) throws Exception {
        Node cldNode = null;
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projName+"|Model");
        new AddDiagramAction().performPopup(root);
       
        NewDiagramWizardOperator newDWO = new NewDiagramWizardOperator();
         System.out.println("Set type = "+ NewDiagramWizardOperator.CLASS_DIAGRAM);
        newDWO.setDiagramType(NewDiagramWizardOperator.CLASS_DIAGRAM);
        newDWO.setDiagramName(classDiagramName);
        newDWO.clickOK();
        Thread.sleep(1000);
        cldNode = new ProjectRootNode(pto.tree(),projName+"|Diagrams|" + classDiagramName);
        return cldNode;
    }
    
    
    public static Node openDiagram(String classDiagramName, String projName, String howToOpen)  {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        new EventTool().waitNoEvent(1000);
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projName);
        Node node = new Node(root, "Diagrams|" + classDiagramName );
        if(howToOpen.equals("pop-up")){
            new OpenAction().performPopup(node);
        }else if(howToOpen.equals("double-click")){
            pto.tree().clickOnPath(node.getTreePath(), 2);
        }else if(howToOpen.equals("short-cut")){
            new OpenAction().performShortcut(node);
        }
        return node;
    }
    
    public static Node closeDiagram(Node node, String howToClose)  {
        return closeDiagram(node, howToClose, "");
    }
    
    public static Node closeDiagram(Node node, String howToClose, String diagramName)  {
        if(howToClose.equals("pop-up")){
            // new CloseDiagramAction().performPopup(node); "Close" popup item doesn't exist

            new DiagramOperator(diagramName).close();
            //new TopComponentOperator(diagramName).close(); 
        }else if(howToClose.equals("double-click")){
            node.tree().clickOnPath(node.getTreePath(), 2);
        }else if(howToClose.equals("short-cut")){
            new CloseDiagramAction().performShortcut(node);
        }
        return node;
    }
    
    public static Node renameNode(Node node, String newName, String howToRename)  {
        if(howToRename.equals("pop-up")){
            new RenameAction().performPopup(node);
        }else if(howToRename.equals("double-click")){
            node.tree().clickOnPath(node.getTreePath(), 2);
        }else if(howToRename.equals("short-cut")){
            new RenameAction().performShortcut(node);
        }
        JDialogOperator jdo = new JDialogOperator("Rename");
        JTextFieldOperator tfOper = new JTextFieldOperator(jdo, 0);
        // clear text field
        tfOper.clearText();
       // tfOper.setText("");
        tfOper.typeText(newName);
        JButtonOperator btOper = new JButtonOperator(jdo, "OK");
        btOper.push();
        
        return node;
    }
    public static Node deleteNode(Node node, String howToDelete)  {
        if(howToDelete.equals("pop-up")){
            new DeleteAction().performPopup(node);
        }else if(howToDelete.equals("double-click")){
            node.tree().clickOnPath(node.getTreePath(), 2);
        }else if(howToDelete.equals("short-cut")){
            new DeleteAction().performShortcut(node);
        }
        return node;
    }
    
    public static DiagramOperator openDiagram(String pName, String dName, String dType, String path){
        long timeout = JemmyProperties.getCurrentTimeout("DiagramOperator.WaitDiagramOperator");
        JemmyProperties.setCurrentTimeout("DiagramOperator.WaitDiagramOperator", 3000);
        try{
            DiagramOperator diagram = new DiagramOperator(dName);
            return diagram;
        }catch(Exception e){}
        finally{
            JemmyProperties.setCurrentTimeout("DiagramOperator.WaitDiagramOperator", timeout);
        }

        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = null;
        timeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
        try{
            root = new ProjectRootNode(pto.tree(),pName);
        }catch(Exception e){
            try{
                Utils.createAnalysisUMLProject(pName, path);
            }catch(Exception e1){
                JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
                return null;
            }
            root = new ProjectRootNode(pto.tree(),pName);
        }
        
        try{
            Node nodeDiagrams = new Node(root,"Diagrams");
            Node nodeDiagram = new Node(nodeDiagrams, dName);
            pto.tree().clickOnPath(nodeDiagram.getTreePath(), 2);
        }catch(Exception e){
            Node nodeModel = new Node(root,"Model");
            nodeModel.performPopupActionNoBlock(PopupConstants.ADD_DIAGRAM);
            NewDiagramWizardOperator wiz = new NewDiagramWizardOperator();
            wiz.setDiagramType(dType);        
            wiz.setDiagramName(dName);
            wiz.clickOK();
        }
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        try{Thread.sleep(500);}catch(Exception ex){}
        return new DiagramOperator(dName);
    }
    
    
    public static ElementTypes[] getExpectedCLDPaletteElementTypes(){
        ElementTypes[] et = {
            ElementTypes.CLASS,
                    ElementTypes.INTERFACE,
                    ElementTypes.PACKAGE,
//                    ElementTypes.COLLABORATION_LIFELINE,
//                    ElementTypes.ENUMERATION,
//                    ElementTypes.NODE,
                    ElementTypes.DATATYPE,
//                    ElementTypes.ARTIFACT,
//                    ElementTypes.ALIASED,
//                    ElementTypes.UTILITY_CLASS,
//                    ElementTypes.ACTOR_LIFELINE,
                    
                    ElementTypes.BOUNDARY_CLASS,
                    ElementTypes.CONTROL_CLASS,
                    ElementTypes.ENTITY_CLASS,
                    
                    ElementTypes.TEMPLATE_CLASS,
                    ElementTypes.DERIVATION_CLASSIFIER,
                    
                    ElementTypes.DESIGN_PATTERN,
                    ElementTypes.ROLE,
                    ElementTypes.INTERFACE_ROLE,
                    ElementTypes.ACTOR_ROLE,
                    ElementTypes.CLASS_ROLE,
                    ElementTypes.USE_CASE_ROLE,
//                    ElementTypes.ROLE_BINDING,
                    
                    ElementTypes.COMMENT,
//                    ElementTypes.LINK_COMMENT
        };  
        
        return et ;
    }
    
    public static LinkTypes[] getExpectedCLDPaletteLinkTypes(){
        LinkTypes[] lt = {
            LinkTypes.GENERALIZATION,
                    LinkTypes.IMPLEMENTATION,
                    LinkTypes.NESTED_LINK,
                    
                    LinkTypes.DEPENDENCY,
                    LinkTypes.REALIZE,
                    LinkTypes.USAGE,
                    LinkTypes.PERMISSION,
                    LinkTypes.ABSTRACTION,
                    
                    LinkTypes.DERIVATION_EDGE,
                    
                    LinkTypes.ASSOCIATION,
                    LinkTypes.COMPOSITION,
                    LinkTypes.NAVIGABLE_COMPOSITION,
                    LinkTypes.AGGREGATION,
                    LinkTypes.NAVIGABLE_AGGREGATION,
                    LinkTypes.NAVIGABLE_ASSOCIATION,
                    LinkTypes.ASSOCIATION_CLASS,
        };       
        
        return lt;
    }
    
     public static Enum[] getExpectedCLDPaletteElements(){
        Enum[] et = {
            ElementTypes.CLASS,
                    ElementTypes.INTERFACE,
                    ElementTypes.PACKAGE,
                    ElementTypes.COLLABORATION_LIFELINE,
                    ElementTypes.ENUMERATION,
                    ElementTypes.NODE,
                    ElementTypes.DATATYPE,
                    ElementTypes.ARTIFACT,
                    ElementTypes.ALIASED,
                    ElementTypes.UTILITY_CLASS,
                    ElementTypes.ACTOR,
                    LinkTypes.GENERALIZATION,
                    LinkTypes.IMPLEMENTATION,
                    LinkTypes.NESTED_LINK,                    
                    
                    ElementTypes.BOUNDARY_CLASS,
                    ElementTypes.CONTROL_CLASS,
                    ElementTypes.ENTITY_CLASS,
                    
                    LinkTypes.DEPENDENCY,
                    LinkTypes.REALIZE,
                    LinkTypes.USAGE,
                    LinkTypes.PERMISSION,
                    LinkTypes.ABSTRACTION,
                    
                    ElementTypes.TEMPLATE_CLASS,
                    ElementTypes.DERIVATION_CLASSIFIER,
                    LinkTypes.DERIVATION_EDGE,
                    
                    LinkTypes.ASSOCIATION,
                    LinkTypes.COMPOSITION,
                    LinkTypes.NAVIGABLE_COMPOSITION,
                    LinkTypes.AGGREGATION,
                    LinkTypes.NAVIGABLE_AGGREGATION,
                    LinkTypes.NAVIGABLE_ASSOCIATION,
                    LinkTypes.ASSOCIATION_CLASS,
                    
                    ElementTypes.DESIGN_PATTERN,
                    ElementTypes.ROLE,
                    ElementTypes.INTERFACE_ROLE,
                    ElementTypes.ACTOR_ROLE,
                    ElementTypes.CLASS_ROLE,
                    ElementTypes.USE_CASE_ROLE,
                    ElementTypes.ROLE_BINDING,
                    
                    ElementTypes.COMMENT,
                    ElementTypes.LINK_COMMENT
        };  
        
        return et ;
    }
}
