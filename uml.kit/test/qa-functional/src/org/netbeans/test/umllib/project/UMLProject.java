/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Versio3n 2 only ("GPL") or the Common
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


package org.netbeans.test.umllib.project;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.test.umllib.DiagramTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.NewPackageWizardOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.util.PopupConstants;
import org.netbeans.test.umllib.util.Utils;

public class UMLProject extends Project {
    
    /** Creates a new instance of JavaProject */
    private static long TIME_WAIT = 1000;
    
    private static String PROJECT_NAME_LABEL="Project Name:";
    private static String PROJECT_LOCATION_LABEL="Project Location:";
    
    String javaProjectName;
    Node projectNode;
    
    /**
     *
     * @param name
     * @param type
     */
    public UMLProject(String name, ProjectType type) {
        this(name, type, null);
    }
    
    /**
     *
     * @param name
     * @param type
     * @param location
     */
    public UMLProject(String name, ProjectType type, String location) {
        this(name, type, location, null);
    }
    
    /**
     *
     * @param name
     * @param type
     * @param location
     * @param javaProject
     */
    public UMLProject(String name, ProjectType type, String location, String javaProject) {
        super(name, type, location);
        this.javaProjectName = javaProject;
        try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
        projectNode = new Node( ProjectsTabOperator.invoke().tree(), name);
        
    }
    
    
    /**
     *
     * @return
     */
    public String  getJavaProjectName(){
        return javaProjectName;
    }
    
    
    /**
     *
     * @return
     */
    public Node getProjectNode(){
        return projectNode;
    }
    
    
    /**
     *
     * @param name
     * @param type
     * @return
     */
    public static UMLProject createProject(String name, ProjectType type) {
        return createProject(name, type, Utils.WORK_DIR , null);
    }
    
    
    /**
     *
     * @param name
     * @param type
     * @param javaProject
     * @return
     */
    public static UMLProject createProject(String name, ProjectType type, JavaProject javaProject) {
        return createProject(name, type, javaProject.getLocation() , javaProject.getName());
    }
    
    /**
     *
     * @param name
     * @param type
     * @param location
     * @return
     */
    public static UMLProject createProject(String name, ProjectType type, String location) {
        return createProject(name, type, location, null);
    }
    
    /**
     *
     * @param name
     * @param type
     * @param location
     * @param javaProjectName
     * @return
     */
    public static UMLProject createProject(String name, ProjectType type, String location, String javaProjectName ) {
        
        location = (location == null) ?  Utils.WORK_DIR : location;
        
        
        NewProjectWizardOperator newProject = NewProjectWizardOperator.invoke();
        try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
        try{ Thread.sleep(TIME_WAIT * 4); } catch(Exception e){}
        
        newProject.selectCategory(LabelsAndTitles.PROJECT_CATEGORY_UML);
        newProject.selectProject(type.toString());
        
        newProject.next();
        
        try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
        
        //newProject.setName(name);
        
        //new JTextFieldOperator(newProject, 0).setText(name);
        JLabelOperator prNmLabl=new JLabelOperator(newProject,PROJECT_NAME_LABEL);
        new JTextFieldOperator((JTextField)(prNmLabl.getLabelFor())).setText(name);
        
        try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
        
        //JTextFieldOperator projectLocation = new JTextFieldOperator(newProject, 1);
        JLabelOperator prLocLabl=new JLabelOperator(newProject,PROJECT_LOCATION_LABEL);
        new JTextFieldOperator((JTextField)(prLocLabl.getLabelFor())).setText(location);
        
        // projectLocation.setText(location);
        
        
        if( javaProjectName!= null ){
            
            switch (type){
                
                case UML_JAVA_PLATFORM_MODEL :
                    
                    throw new UMLCommonException("Unsupported request: java platform uml project connected to java project");
                    
                case UML_JAVA_REVERSE_ENGINEERING :
                    
                    JLabelOperator javaPrjLbl=new JLabelOperator(newProject,"Java Project:");
                    JComboBoxOperator javaPrj=new JComboBoxOperator((JComboBox)(javaPrjLbl.getLabelFor()));
                    int ind=javaPrj.findItemIndex(javaProjectName,javaPrj.getDefaultStringComparator());
                    if(ind<0)
                    {
                        //org.netbeans.modules.java.j2seproject.J2SEProject tmp2;
                       // tmp2.getLookup();
                        int count=javaPrj.getItemCount();
                        String tmp="";
                        for(int i=0;i<count;i++)
                        {
                            org.netbeans.api.project.Project pr=((org.netbeans.api.project.Project)(javaPrj.getItemAt(i)));
                            ProjectInformation pi =  ProjectUtils.getInformation(pr);
                            tmp+=javaPrj.getItemAt(i)+"////\\\\"+pi.getName()+"; ";
                            if(javaPrj.getDefaultStringComparator().equals(javaProjectName,pi.getName()))
                            {
                                ind=i;
                                break;
                            }
                        }
                        if(ind<0)throw new NotFoundException("Can't find java project with name \""+javaProjectName+"\" in project's combobox, current: "+javaPrj+", all items: "+tmp);
                    }
                    javaPrj.selectItem(ind);
                    javaPrj.waitItemSelected(ind);
                    
                    break;
                    
            }
            
            
        }
        
        
        new JButtonOperator(newProject, "Finish").push();
        //
        
        if(type.equals(ProjectType.UML_JAVA_PLATFORM_MODEL) || type.equals(ProjectType.UML_PLATFORM_INDEPENDET_MODEL)) {
            try{ Thread.sleep(TIME_WAIT); } catch(Exception e){}
            try {
                NewDiagramWizardOperator wizardDialog = new NewDiagramWizardOperator();
                wizardDialog.clickCancel();
            } catch(Exception ex) {
                
            }
        }
        //
        newProject.waitClosed();
        //may be very fast
        try{
            JDialogOperator opPr=new JDialogOperator("Opening Project");
            opPr.waitClosed();
        }catch(Exception ex){}
        //
        
        //check for output window fo re
        if(type.equals(ProjectType.UML_JAVA_REVERSE_ENGINEERING)) {
            ContainerOperator reOut=null;
            try
            {
                reOut=new OutputTabOperator("Reverse Engineering Log");
            }
            catch(Exception ex)
            {
                try
                {
                    reOut=new TopComponentOperator("Output - Reverse Engineering Log");
                }
                catch(Exception ex2)
                {
                    throw new UMLCommonException("Can't initialize/find output window/tab: "+ex+";"+ex2);
                }
            }
            new JEditorPaneOperator(reOut).waitText("Task Successful");
        }
        //
        //
        
        return new UMLProject(name, type, location, javaProjectName);
        
    }
    
    
    public void reverseEngineerOperation(String node, String diagramName, DiagramTypes diagramType){
        reverseEngineerOperation(new Node(getProjectNode(), node), diagramName, diagramType);
    }
    
    public void reverseEngineerOperation(Node node, String diagramName, DiagramTypes diagramType){
        
        node.performPopupActionNoBlock(PopupConstants.REVERSE_ENGINEER_OPERATION);
        
        NewDiagramWizardOperator dw = new NewDiagramWizardOperator();
        dw.setDiagramType(diagramType.toString());
        dw.setDiagramName(diagramName);
        dw.clickFinish();
        
    }
    
    
    public void addPackage(Node node, String packageName){
        addPackage(node, packageName, null, null);
    }
    
    public void addPackage(Node node, String packageName, String diagramName, DiagramTypes diagramType){
        
        node.performPopupActionNoBlock(PopupConstants.ADD_PACKAGE);
        NewPackageWizardOperator pw = new NewPackageWizardOperator();
        pw.setPackageName(packageName);
        
        if( diagramName != null ){
            
            pw.setCreateDiagram(true);
            pw.setDiagramType(diagramType.toString());
            pw.setDiagramName(diagramName);
        }
        
        pw.clickFinish();
        
    }
    
    public void generateCode(JavaProject javaProject){
        generateCode(getProjectNode(), javaProject);
    }
    
   /* public void generateCode(Node node, JavaProject javaProject){
        generateCode(node, javaProject.getLocation() + "/" + javaProject.getName() + "/src");
    }*/
    
     public void generateCode(Node node, JavaProject javaProject){
        generateCode(node,  javaProject.getName());
    }
    
    public void generateCode(Node node, String javaProjectName){
       
        node.performPopupActionNoBlock("Generate Code...");
        JDialogOperator codeGenDialog = new JDialogOperator("Generate Code");

        JLabelOperator targetPrjLbl = new JLabelOperator(codeGenDialog, "Target Project:");
        JComboBoxOperator targetPrj = new JComboBoxOperator((JComboBox) (targetPrjLbl.getLabelFor()));
        int ind =  findItemIndex(targetPrj, javaProjectName, targetPrj.getDefaultStringComparator());     
         if (ind < 0) {
            int count = targetPrj.getItemCount();
            String tmp = "";
            for (int i = 0; i < count; i++) {
                org.netbeans.api.project.Project pr = (org.netbeans.api.project.Project) (targetPrj.getItemAt(i));
                ProjectInformation pi = ProjectUtils.getInformation(pr);
                tmp += targetPrj.getItemAt(i) + "////\\\\" + pi.getName() + "; ";
                if (targetPrj.getDefaultStringComparator().equals(javaProjectName, pi.getName())) {
                    ind = i;
                    break;
                }
            }
            if (ind < 0) {
                throw new NotFoundException("Can't find target project with name \"" + javaProjectName + "\" in project's combobox, current: " + targetPrj + ", all items: " + tmp);
            }
        }
        targetPrj.selectItem(ind);
        targetPrj.waitItemSelected(ind);       
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        JButtonOperator button = new JButtonOperator(codeGenDialog, "OK");
        button.pushNoBlock();
    }    
    
    // same as JComboBoxOperator.findItemIndex, plus handles blank option
    public int findItemIndex(JComboBoxOperator comboBoxOperator, String item, StringComparator comparator) {
        ComboBoxModel model = comboBoxOperator.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i) != null) {
                if (comparator.equals(model.getElementAt(i).toString(), item)) {
                    return (i);
                }
            }
        }
        return (-1);
    }
}
