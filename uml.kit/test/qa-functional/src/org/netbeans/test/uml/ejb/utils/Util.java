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




package org.netbeans.test.uml.ejb.utils;

import java.util.ArrayList;
import javax.swing.JTextField;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.util.OptionsOperator;


public class Util {
    
    public Util() {
    }
    
    
    public boolean diagramHasExactElements(String[] elementNames, DiagramOperator dia){
        DiagramElementOperator[] els = new DiagramElementOperator[elementNames.length];
        for (int i=0;i<elementNames.length; i++){
            els[i] = new DiagramElementOperator(dia, elementNames[i]);
        }
        return diagramHasExactElements(els, dia);
    }
    
    public boolean diagramHasExactElements(DiagramElementOperator[] elements, DiagramOperator dia){
        ArrayList<DiagramElementOperator> al = new ArrayList<DiagramElementOperator>();
        for(int i=0;i<elements.length;i++){
            al.add(elements[i]);
        }
        
        ArrayList<DiagramElementOperator> diaAl = dia.getDiagramElements();
        for(int i=0;i<diaAl.size();i++){
            int index = al.indexOf(diaAl.get(i));
            if (index<0){
                return false;
            }
            al.remove(index);
        }
        
        if (al.size()>0){
            return false;
        }
        return true;
    }
    
    
    public Node getNode(String nodePath, String projectName){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projectName);
        
        Node node = new Node(root, nodePath);
        return node;
    }
    
    
    public boolean nodeExists(String path, String projectName){
        long waitNodeTime = JemmyProperties.getCurrentTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 2000);
        try{
            Node node = getNode(path, projectName);
            node.select();
            return true;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", waitNodeTime);
        }
    }
    
    
    public boolean allNodesExist(String projectName, String parentPath, String[] nodeLabels){
        for(int i=0; i<nodeLabels.length; i++){
            if (!nodeExists(parentPath+"|"+nodeLabels[i], projectName)){
                return false;
            }
        }
        return true;
    }
    
    
    public void closeSaveDlg(){
        new Thread(new Runnable() {
            public void run() {
                long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
                try{
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
                    JDialogOperator saveDlg = new JDialogOperator("Save");
                    new JButtonOperator(saveDlg, "Save All").pushNoBlock();
                }catch(Exception e){} finally{
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);
                }
            }
        }).start();
    }
    //
    public void setDefaultPreferences() {
        OptionsOperator op=OptionsOperator.invoke();
        op=op.invokeAdvanced();
        TreeTableOperator tr=op.treeTable();
        //
        tr.tree().selectPath(tr.tree().findPath("UML"));
        tr.tree().waitSelected(tr.tree().findPath("UML"));
        new EventTool().waitNoEvent(1000);
        PropertySheetOperator ps=new PropertySheetOperator(op);
        Property pr=new Property(ps,"Prompt to Save Diagram");
        pr.setValue(1);
        if(!pr.getValue().equalsIgnoreCase("No"))pr.setValue(0);
        //
        tr.tree().selectPath(tr.tree().findPath("UML|New Project"));
        tr.tree().waitSelected(tr.tree().findPath("UML|New Project"));
        new EventTool().waitNoEvent(1000);
        ps=new PropertySheetOperator(op);
        pr=new Property(ps,"Create New Diagram");
        pr.setValue(1);
        if(pr.getValue().equalsIgnoreCase("yes"))pr.setValue(0);
        new JButtonOperator(op,"Close").push();
    }
    public void commonSetup(String workdir,String prName) {
        //setDefaultPreferences();
        new NewProjectAction().performMenu();
        NewProjectWizardOperator newWizardOper=new NewProjectWizardOperator();
        new EventTool().waitNoEvent(500);
        try{Thread.sleep(1000);}catch(Exception ex){}
        //newWizardOper.selectCategory(qa.uml.util.LabelsAndTitles.UML_PROJECTS_CATEGORY);
        JTreeOperator catTree=new JTreeOperator(newWizardOper);
        java.awt.Rectangle pth=catTree.getPathBounds(catTree.findPath(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY));
        catTree.moveMouse(pth.x+pth.width/3, pth.y+pth.height/2);
        catTree.selectPath(catTree.findPath(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY));
        catTree.waitSelected(catTree.findPath(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY));
        new EventTool().waitNoEvent(500);
        newWizardOper.selectProject(org.netbeans.test.umllib.util.LabelsAndTitles.JAVA_UML_PROJECT_LABEL);
        newWizardOper.next();
        JLabelOperator ploL=new JLabelOperator(newWizardOper,"Project Location:");
        JTextFieldOperator ploT=new JTextFieldOperator((JTextField)(ploL.getLabelFor()));
        ploT.clearText();
        ploT.typeText(workdir);
        JLabelOperator pnmL=new JLabelOperator(newWizardOper,"Project Name:");
        JTextFieldOperator pnmT=new JTextFieldOperator((JTextField)(pnmL.getLabelFor()));
        pnmT.clearText();
        pnmT.typeText(prName);
        //newWizardOper.finish();
        new JButtonOperator(newWizardOper, "Finish").push();
        new JButtonOperator(new JDialogOperator("Create New Diagram"), "Cancel").push();
        //properties
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Window|Properties");
        new PropertySheetOperator();
        new EventTool().waitNoEvent(500);
        //add package
        org.netbeans.test.umllib.Utils.createPackage(prName,"pkg");
    }
    
}
