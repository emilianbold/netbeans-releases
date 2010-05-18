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



package org.netbeans.test.uml.dependencydiagram.utils;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.customelements.CollaborationDiagramOperator;
import org.netbeans.test.umllib.customelements.SequenceDiagramOperator;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.util.OptionsOperator;
import org.netbeans.test.umllib.util.PopupConstants;

public class DependencyUtils {
    
    private EventTool eventTool = new EventTool();
    
    public DependencyUtils(){
    }
    
    public static DiagramOperator openDiagram(String pName, String dName, String dType, String path){
        long timeout = JemmyProperties.getCurrentTimeout("DiagramOperator.WaitDiagramOperator");
        JemmyProperties.setCurrentTimeout("DiagramOperator.WaitDiagramOperator", 3000);
        try{
            DiagramOperator diagram = new DiagramOperator(dName);
            return diagram;
        }catch(Exception e){
        } finally{
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
                UMLProject.createProject(pName, ProjectType.UML_JAVA_PLATFORM_MODEL, path);
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
            timeout = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
            NewDiagramWizardOperator wiz = new NewDiagramWizardOperator();
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeout);
            wiz.setDiagramType(dType);
            wiz.setDiagramName(dName);
            try{Thread.sleep(100);}catch(Exception ex){}
            org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("createDiagram_");
            wiz.clickFinish();
            try{Thread.sleep(500);}catch(Exception ex){}
        }
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        if (dType.equals(NewDiagramWizardOperator.SEQUENCE_DIAGRAM))
            return new SequenceDiagramOperator(dName);
        else if (dType.equals(NewDiagramWizardOperator.COLLABORATION_DIAGRAM))
            return new CollaborationDiagramOperator(dName);
        else
            return new DiagramOperator(dName);
    }
    
    public static void setDefaultPreferences() {
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
        //autosave diagrams
        tr.tree().selectPath(tr.tree().findPath("UML"));
        tr.tree().waitSelected(tr.tree().findPath("UML"));
        pr=new Property(ps,"Prompt to Save Diagram");
        pr.setValue(1);
        if(!pr.getValue().equalsIgnoreCase("No"))pr.setValue(0);
        //
        new JButtonOperator(op,"Close").push();
    }
    
}
