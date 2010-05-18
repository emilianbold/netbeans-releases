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


/**
 * Utils.java
 *
 * Created on February 10, 2005, 6:38 PM
 *
 * This Utils is used for UML related functions (use qa.uml.util.Utils for common functions)
 */

package org.netbeans.test.umllib;


import java.awt.Component;
import java.awt.Container;
import javax.swing.SwingUtilities;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;

/**
 *
 * @author Alexei Mokeev
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    public Utils() {
    }
    
    private static EventTool eventTool = new EventTool();
    private final static String DONE_BTN = "Done";
    private final static String OK_BTN = "OK";
    private final static String FINISH_BTN = "Finish";
    private final static String CANCEL_BTN = "Cancel";
    
    public static void waitSwing() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * 
     * @deprecated use createProject from qa.uml.project.UMLProject class directly
     * @param projectName 
     * @param javaProject 
     * @param projectPath 
     */
    public static void createUMLProjectFromJavaProject(String projectName, String javaProject, String projectPath){
        UMLProject.createProject(projectName,ProjectType.UML_JAVA_REVERSE_ENGINEERING,projectPath,javaProject);
    }
    
    /**
     * 
     * @deprecated use createProject from qa.uml.project.UMLProject class directly
     * @param pName 
     * @param path 
     */
    public static void createJavaUMLProject(String pName, String path){
        UMLProject.createProject(pName,ProjectType.UML_JAVA_PLATFORM_MODEL,path);
    }
    
    /**
     * 
     * @deprecated use createProject from qa.uml.project.UMLProject class directly
     * @param pName 
     * @param path 
     */
    public static void createAnalysisUMLProject(String pName, String path){
        UMLProject.createProject(pName,ProjectType.UML_PLATFORM_INDEPENDET_MODEL,path);
    }
    
    
    /**
     * 
     * @deprecated use createProject from qa.uml.project.UMLProject class directly
     * @param projectName 
     * @param javaProject 
     * @param projectPath 
     */
    
    public static void createReverseEngineeringUMLProject(String projectName, String javaProject, String projectPath){
        UMLProject.createProject(projectName,ProjectType.UML_JAVA_REVERSE_ENGINEERING,projectPath,javaProject);
    }
    
    
    /**
     *
     * @deprecated it's common action and should be moved to qa.uml.util.Utils
     */
    
    public static void closeSaveDlg(){
        new Thread(new Runnable() {
            public void run() {
                long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
                try{
                    new EventTool().waitNoEvent(3000);
                    //Thread.sleep(3000);
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 10000);
                    JDialogOperator saveDlg = new JDialogOperator("Save");
                    new JButtonOperator(saveDlg, "Save All").pushNoBlock();
                }catch(Exception e){} finally{
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);
                }
            }
        }).start();
    }
    
    /**
     * creates diagram in top level package of project, create package if there is no package with name
     * 
     * @param project 
     * @param workPkg 
     * @param diagram 
     * @param diagramType 
     * @return 
     */
    public static RetAll createDiagram(String project,String workPkg, String diagram,String diagramType){
        return createOrOpenDiagram(project,workPkg,diagram,diagramType,true);
    }
    /**
     * creates diagram in top level package of project, create package if there is no package with name
     * 
     * @param project 
     * @param workPkg 
     * @param diagram 
     * @param diagramType 
     * @param createonly
     * @return 
     */
    private static RetAll createOrOpenDiagram(String project,String workPkg, String diagram,String diagramType,boolean createonly){
        //user can pass project name or name with |Model, we need prName|Model in method
        if(project.lastIndexOf("|Model")!=(project.length()-"|Model".length()) || project.lastIndexOf("|Model")==-1)project=project+"|Model";
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        new EventTool().waitNoEvent(1000);
        ProjectRootNode root = new ProjectRootNode(pto.tree(),project);
        //*workaround for some fails in jelly
        root.tree().waitVisible(root.getTreePath());
        root.tree().expandPath(root.getTreePath());
        root.tree().waitExpanded(root.getTreePath());
        try{Thread.sleep(1000);}catch(Exception ex){}
        java.awt.Rectangle pth=root.tree().getPathBounds(root.getTreePath());
        root.tree().moveMouse(pth.x+pth.width/3, pth.y+pth.height/2);
        new EventTool().waitNoEvent(1000);
        //*workaround finished
        root.select();
        root.tree().waitSelected(root.getTreePath());
        if(root.isChildPresent(workPkg))
        {
            if((!createonly) && new Node(root,workPkg).isChildPresent(diagram))
            {
                new Node(root,workPkg+"|"+diagram).callPopup().pushMenu("Open");
            }
            else
            {
                Node pkgN=new Node(root,workPkg);
                pkgN.callPopup().pushMenuNoBlock("New|Diagram");
                NewDiagramWizardOperator nw=new NewDiagramWizardOperator();
                nw.setDiagramType(diagramType);
                nw.setDiagramName(diagram);
                nw.clickFinish();
                }
        }
        else
        {
            JPopupMenuOperator pop=root.callPopup();
            pop.waitComponentVisible(true);
            pop.waitComponentShowing(true);
            pop.pushMenuNoBlock("New|Package");
            NewPackageWizardOperator nw=new NewPackageWizardOperator();
            nw.setScopedDiagram(workPkg,diagram,diagramType);
            nw.clickFinish();
        }
        new EventTool().waitNoEvent(500);
        try{Thread.sleep(500);}catch(Exception ex){}
        DiagramOperator ret=null;
        ret=new DiagramOperator(diagram);
        ret.waitComponentShowing(true);
        Node lastDiagramNode = new Node(root,workPkg+"|"+diagram);
        new EventTool().waitNoEvent(500);
        try{Thread.sleep(500);}catch(Exception ex){}
        return new RetAll(pto, lastDiagramNode,ret);
    }
    /**
     * creates diagram in top level package of project, create package if there is no package with name
     * 
     * @param project 
     * @param workPkg 
     * @param diagram 
     * @param diagramType 
     * @return 
     */
    public static RetAll createOrOpenDiagram(String project,String workPkg, String diagram,String diagramType){
        return createOrOpenDiagram(project,workPkg,diagram,diagramType,false);
    }
    /**
     * creates create top level package if there is no package with name
     * 
     * @param project 
     * @param workPkg 

     * @return 
     */
    public static Node createPackage(String project,String workPkg){
        //user can pass project name or name with |Model, we need prName|Model in method
        if(project.lastIndexOf("|Model")!=(project.length()-"|Model".length()))project=project+"|Model";
        //
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        new EventTool().waitNoEvent(1000);
        ProjectRootNode root = new ProjectRootNode(pto.tree(),project);
        //*workaround for some fails in jelly
        root.tree().waitVisible(root.getTreePath());
        root.tree().expandPath(root.getTreePath());
        root.tree().waitExpanded(root.getTreePath());
        try{Thread.sleep(1000);}catch(Exception ex){}
        java.awt.Rectangle pth=root.tree().getPathBounds(root.getTreePath());
        root.tree().moveMouse(pth.x+pth.width/3, pth.y+pth.height/2);
        new EventTool().waitNoEvent(1000);
        //*workaround finished
        root.select();
        root.tree().waitSelected(root.getTreePath());
        if(root.isChildPresent(workPkg))
        {
        }
        else
        {
            JPopupMenuOperator pop=root.callPopup();
            pop.waitComponentVisible(true);
            pop.waitComponentShowing(true);
            pop.pushMenuNoBlock("New|Package");
            NewPackageWizardOperator nw=new NewPackageWizardOperator();
            nw.setPackageName(workPkg);
            nw.clickFinish();
        }
        new EventTool().waitNoEvent(500);
        Node lastDiagramNode = new Node(root,workPkg);
        new EventTool().waitNoEvent(500);
        return lastDiagramNode;
    }
    /**
     * helper class to return several values from createDiagram
     */
    public static class RetAll
    {
        public ProjectsTabOperator pto;
        /**
         * diagram's node in project tree
         */
        public Node lastDiagramNode;
        /**
         * diagram operator
         */
        public DiagramOperator dOp;
        /**
         * 
         * @param pto 
         * @param lastDiagramNode 
         * @param dOp 
         */
        RetAll(ProjectsTabOperator pto,Node lastDiagramNode,DiagramOperator dOp)
        {
            this.pto=pto;
            this.lastDiagramNode=lastDiagramNode;
            this.dOp=dOp;
        }
    }  
    
     
    public static void log(String msg) {
          org.netbeans.jemmy.JemmyProperties.getCurrentOutput().print("------- "+msg+"\n");
          System.out.println(msg+"\n");
    }
    
     static String log = "LOG: ";


    public static void printComponentList(Container container, int tabCount) {
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];

            printLineToJemmyLog(log + "~~~PrintComponentList~~~~");
            System.out.println("~~~PrintComponentList~~~~");
            for (int j = 0; j < tabCount; j++) {
                printToJemmyLog("\t");
                System.out.print("\t");
            }
            printLineToJemmyLog(log + "+++ COMPONENT = " + component);
            System.out.println("+++ COMPONENT = " + component);
            // print next level
            if (component instanceof Container) {
                printComponentList((Container) component, tabCount + 1);
            }
        }
    }

    public static void printXmlDump(String loc) {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
        try {
            org.netbeans.jemmy.util.Dumper.dumpAll(loc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void printXmlDumpComponent(Component c, String loc) {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
        try {
            org.netbeans.jemmy.util.Dumper.dumpComponent(c, loc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send output with line return to jemmy log
     * @param out
     */
    public static void printLineToJemmyLog(String out) {
        org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine(out);
    }

    /**
     * Send output to jemmy log
     * @param out
     */
    public static void printToJemmyLog(String out) {
        org.netbeans.jemmy.JemmyProperties.getCurrentOutput().print(out);
    }

    /**
     * Wait for specified time.
     * @param millisec Amount of milliseconds.
     */
    public static void wait(int millisec) {
        System.out.println("Sleep to " + millisec / 1000 + " seconds");
        printLineToJemmyLog(log + "Sleep to " + millisec / 1000 + " seconds");
        EventDispatcher.waitQueueEmpty();
        try {
            Thread.sleep(millisec);
        } catch (Exception e) {
        }
        EventDispatcher.waitQueueEmpty();
    }
}
 
