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


package org.netbeans.test.uml.multipleprojects.utils;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import javax.swing.SwingUtilities;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.MovingElementsOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.NewElementWizardOperator;
import org.netbeans.test.umllib.NewPackageWizardOperator;
import org.netbeans.test.umllib.Utils;
import org.netbeans.test.umllib.util.PopupConstants;


/**
 * @author yaa
 */
public class MUPUtils {
    
    public MUPUtils() {
    }
    
    public static DiagramOperator openDiagram(String pName, String dName, String dType, String path){
        long timeout = JemmyProperties.getCurrentTimeout("DiagramOperator.WaitDiagramOperator");
        JemmyProperties.setCurrentTimeout("DiagramOperator.WaitDiagramOperator", 3000);
        try{
            DiagramOperator diagram = new DiagramOperator(dName);
            return diagram;
        }catch(Exception e){} finally{
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
                Utils.createJavaUMLProject(pName, path);
            }catch(Exception e1){
                return null;
            }
            root = new ProjectRootNode(pto.tree(),pName);
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
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
            wiz.clickOK();
        }
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        try{Thread.sleep(500);}catch(Exception ex){}
        return new DiagramOperator(dName);
    }
    
    public static Node selectElementInModelTree(String prName, String elName){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = null;
        long timeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
        try{
            root = new ProjectRootNode(pto.tree(),prName);
            Node nodeModel = new Node(root,"Model");
            Node nodeElem = new Node(nodeModel, elName);
            nodeElem.select();
            return nodeElem;
        }catch(Exception e){
            return null;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        }
    }
    
    public static Node selectElementInImportTree(String prName, String elName){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = null;
        long timeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
        try{
            root = new ProjectRootNode(pto.tree(),prName);
            Node nodeImportedElements = new Node(root,"Imported Elements");
            Node nodeElem = new Node(nodeImportedElements, elName);
            nodeElem.select();
            return nodeElem;
        }catch(Exception e){
            return null;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        }
    }
    
    public static void createElement(String prName, String elName, String elType){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        new EventTool().waitNoEvent(1000);
            ProjectRootNode root = new ProjectRootNode(pto.tree(),prName);
            Node nodeModel = new Node(root,"Model");
            
            nodeModel.performPopupActionNoBlock(PopupConstants.ADD_ELEMENT);
            //JPopupMenuOperator popup = nodeModel.callPopup();
            //popup.pushMenuNoBlock(PopupConstants.ADD_ELEMENT);
            
            new EventTool().waitNoEvent(1000);
            
            NewElementWizardOperator ewo = new NewElementWizardOperator();
            ewo.setElementType(elType);
            ewo.setElementName(elName);
            ewo.clickFinish();

            new EventTool().waitNoEvent(1000);
            
            Node nodeElem = new Node(nodeModel, elName);
            nodeElem.select();
    }
    
    public static void createPackage(String prName, String pkgName) throws Exception{
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = null;
        long timeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
        try{
            root = new ProjectRootNode(pto.tree(),prName);
            Node nodeModel = new Node(root,"Model");
            
            nodeModel.performPopupActionNoBlock(PopupConstants.ADD_PACKAGE);
            //JPopupMenuOperator popup = nodeModel.callPopup();
            //popup.pushMenuNoBlock(PopupConstants.ADD_PACKAGE);

            NewPackageWizardOperator pwo = new NewPackageWizardOperator();
            pwo.setPackageName(pkgName);
            pwo.clickFinish();
            
            new EventTool().waitNoEvent(1000);
            
            Node nodePkg = new Node(nodeModel, pkgName);
            nodePkg.select();
        }catch(Exception e){
            throw e;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        }
    }
    
    public static boolean moveElementInTree(String srcProject, String tgtProject, String elName){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = null;
        long timeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
        try{
            Node nodeSrc = new Node(new ProjectRootNode(pto.tree(),srcProject), "Model|" + elName);
            Node nodeTgt = new Node(new ProjectRootNode(pto.tree(),tgtProject), "Model");
            
            nodeTgt.select();
            nodeSrc.select();
            
            Rectangle rec1 = pto.tree().getPathBounds(nodeSrc.getTreePath());
            Point point1 = new Point(rec1.x + rec1.width/2, rec1.y + rec1.height/2);
            SwingUtilities.convertPointToScreen(point1, pto.tree().getSource());
            
            Rectangle rec2 = pto.tree().getPathBounds(nodeTgt.getTreePath());
            Point point2 = new Point(rec2.x + rec2.width/2, rec2.y + rec2.height/2);
            SwingUtilities.convertPointToScreen(point2, pto.tree().getSource());
            
            dragNDrop(point1, point2);
            
            new MovingElementsOperator().clickMoveElement();
            
            return true;
        }catch(Exception e){
            return false;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        }
    }
    
    public static boolean importElementInTree(String srcProject, String tgtProject, String elName) throws FileNotFoundException{
        // moving element
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = null;
        long timeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
        try{
            Node nodeSrc = new Node(new ProjectRootNode(pto.tree(),srcProject), "Model|" + elName);
            Node nodeTgt = new Node(new ProjectRootNode(pto.tree(),tgtProject), "Model");
            
            nodeTgt.select();
            nodeSrc.select();
            
            Rectangle rec1 = pto.tree().getPathBounds(nodeSrc.getTreePath());
            Point point1 = new Point(rec1.x + rec1.width/2, rec1.y + rec1.height/2);
            SwingUtilities.convertPointToScreen(point1, pto.tree().getSource());
            
            Rectangle rec2 = pto.tree().getPathBounds(nodeTgt.getTreePath());
            Point point2 = new Point(rec2.x + rec2.width/2, rec2.y + rec2.height/2);
            SwingUtilities.convertPointToScreen(point2, pto.tree().getSource());
            
            dragNDrop(point1, point2);
            
            new MovingElementsOperator().clickImportElement();
            
            return true;
        }catch(Exception e){
            return false;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        }
    }
/*
    public static boolean moveElementToDiagram(String srcProject, String tgtProject, String elName, String diagramName){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        long timeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
        try{
            Node nodeDiagram = new Node(new ProjectRootNode(pto.tree(),tgtProject), "Model|" + diagramName);
            nodeDiagram.callPopup().pushMenuNoBlock("Open");
            DiagramOperator dia = new DiagramOperator(diagramName);
 
            Node nodeSrc = new Node(new ProjectRootNode(pto.tree(),srcProject), "Model|" + elName);
            nodeSrc.select();
 
            Rectangle rec1 = pto.tree().getPathBounds(nodeSrc.getTreePath());
            Point point1 = new Point(rec1.x + rec1.width/2, rec1.y + rec1.height/2);
            SwingUtilities.convertPointToScreen(point1, pto.tree().getSource());
 
            Point point2 = dia.getDrawingArea().getFreePoint();
            SwingUtilities.convertPointToScreen(point2, dia.getDrawingArea().getSource());
 
            dragNDrop(point1, point2);
 
            new MovingElementsOperator().clickMoveElement();
 
            return true;
        }catch(Exception e){
            return false;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        }
    }
 */
    public static boolean importElementToDiagram(String srcProject, String tgtProject, String elName, String diagramName){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        long timeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
        try{
            Node nodeDiagram = new Node(new ProjectRootNode(pto.tree(),tgtProject), "Model|" + diagramName);
            nodeDiagram.callPopup().pushMenuNoBlock("Open");
            DiagramOperator dia = new DiagramOperator(diagramName);
            
            Node nodeSrc = new Node(new ProjectRootNode(pto.tree(),srcProject), "Model|" + elName);
            nodeSrc.select();
            
            Rectangle rec1 = pto.tree().getPathBounds(nodeSrc.getTreePath());
            Point point1 = new Point(rec1.x + rec1.width/2, rec1.y + rec1.height/2);
            SwingUtilities.convertPointToScreen(point1, pto.tree().getSource());
            
            Point point2 = dia.getDrawingArea().getFreePoint();
            SwingUtilities.convertPointToScreen(point2, dia.getDrawingArea().getSource());
            
            dragNDrop(point1, point2);
            
            return true;
        }catch(Exception e){
            return false;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        }
    }
    
    private static void dragNDrop(Point p1, Point p2) throws AWTException, InterruptedException{
        Robot r = new Robot();
        r.mouseMove(p1.x, p1.y);
        r.mousePress(KeyEvent.BUTTON1_MASK);
        
        r.mouseMove(p2.x, p2.y);
        r.mouseRelease(KeyEvent.BUTTON1_MASK);
        Thread.sleep(1000);
        r.mouseMove(p2.x, p2.y + 5);
    }
    
    public static void pushKey(int key) throws AWTException, InterruptedException{
        Robot r = new Robot();
        r.keyPress(key);
        Thread.sleep(100);
        r.keyRelease(key);
    }
    
}

