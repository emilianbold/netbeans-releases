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


package org.netbeans.test.uml.diagramcontextmenu.utils;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.umllib.*;
import org.netbeans.test.umllib.util.PopupConstants;

/**
 * @author yaa
 */
public class DCMUtils {
    
    public DCMUtils() {
    }

    public static DiagramOperator openDiagram(String pName, String dName, String dType, String path){
        long timeout = JemmyProperties.getCurrentTimeout("DiagramOperator.WaitDiagramOperator");
        JemmyProperties.setCurrentTimeout("DiagramOperator.WaitDiagramOperator", 5000);
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
                Utils.createJavaUMLProject(pName, path);
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
            try{Thread.sleep(100);}catch(Exception e2){}
            wiz.clickOK();
        }
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        try{Thread.sleep(500);}catch(Exception e){}
        return new DiagramOperator(dName);
    }

    public static boolean findAndCloseDialog(String dialogTitle){
            JDialogOperator dlg = new JDialogOperator(dialogTitle);
            try{Thread.sleep(500);}catch(Exception ex){}
            dlg.close();
            dlg.waitClosed();
            return true;
    } 
    
    public static JMenuItemOperator checkDiagramPopupMenuItem (DiagramOperator diagram, String itemName){
        JPopupMenuOperator popup = diagram.getDrawingArea().getPopup();
        JMenuItemOperator item = null;
        try{
            item = popup.showMenuItem(itemName);
        }catch(Exception e){}
        //Point p = diagram.getDrawingArea().getFreePoint();
        //diagram.getDrawingArea().clickMouse(10, 10, 1);
        popup.pushKey(KeyEvent.VK_ESCAPE);
        popup.waitComponentShowing(false);
        return item;
    }

    public static void pushDiagramPopupMenuItem (DiagramOperator diagram, String itemName){
        JPopupMenuOperator popup = diagram.getDrawingArea().getPopup();
        try{
            popup.pushMenuNoBlock(itemName);
        }catch(Exception e){}
    }
    
    public static String checkUnnecessaryItems(DiagramOperator diagram, String[] itemNames, PrintStream log){
        String result = "";
        JPopupMenuOperator popup = diagram.getDrawingArea().getPopup();
        String[] list = getMenuContext(popup.getSubElements());
        
        boolean flag;
        for(int i=0;i < list.length;i++){
            flag = false;
            for(int j=0;j < itemNames.length;j++){
                if(list[i].equals(itemNames[j])){
                    flag = true;
                    break;
                }
            }
            if (!flag){
                result += "Extra Item: "+list[i]+";\n";
                log.println("Unnecessary popup menu item '" + list[i] + "'");
            }
        }
        Point p = diagram.getDrawingArea().getFreePoint();
        diagram.getDrawingArea().clickMouse(10, 10, 1);
        
        return result;
    }

    private static String[] getMenuContext(MenuElement[] elements){
        return getMenuContext(elements, "");
    }

    private static String[] getMenuContext(MenuElement[] elements, String pathParent){
        String list = getMenuContextIt(elements, pathParent);
        list = list.substring(1);
        return list.split(",");
    }
    
    private static String getMenuContextIt(MenuElement[] elements, String pathParent){
        String list = "";
        for(int i = 0; i < elements.length; i++){
            if (elements[i] instanceof JMenu){
                JMenu menu = (JMenu)elements[i];
                if (menu != null){
                    list = list + getMenuContextIt(menu.getPopupMenu().getSubElements(), menu.getText() + "|");
                }
            }else if (elements[i] instanceof JMenuItem){
                JMenuItem item = (JMenuItem)elements[i];
                list = list + "," + pathParent + item.getText();
            }
        }
        
        return list;
    }

    public interface DiagramPopupConstants{
        public final static String LAYOUT = "Layout";
        public final static String LAYOUT_HIERARCHICAL = "Hierarchical";
        public final static String LAYOUT_ORTHOGONAL = "Orthogonal";
        public final static String LAYOUT_SYMMETRIC = "Symmetric";
        public final static String LAYOUT_SEQUENCE = "Sequence";
        public final static String LAYOUT_PROPERTIES = "Properties...";
        public final static String LAYOUT_INCREMENTAL = "Incremental";
        public final static String ZOOM = "Zoom...";
        public final static String ZOOM_IN = "Zoom In";
        public final static String ZOOM_OUT = "Zoom Out";
        public final static String PROPERTIES = "Properties...";
        public final static String SYNCHRONIZE = "Synchronize Element with Data";
        public final static String SHOW_MESSAGE_NUMBERS = "Show Message Numbers";
        public final static String SHOW_ALL_RETURN_MESSAGES = "Show All Return Messages";
        public final static String SHOW_INTERACTION_BOUNDARY = "Show Interaction Boundary";
        public final static String ASSOCIATE_WITH = "Associate With...";
        public final static String APPLY_DESIGN_PATTERN = "Apply Design Pattern...";
        public final static String SELECT_IN_MODEL = "Select in Model";
        public final static String SET_DIMENSIONS = "Set Dimensions...;";
    }
    
    public interface DialogTitles{
        public final static String LAYOUT_PROPERTIES = "Layout Properties";
        public final static String ZOOM = "Zoom";
        public final static String ASSOCIATE_WITH = "Associate";
        public final static String APPLY_DESIGN_PATTERN = "Design Pattern Apply Wizard";
        public final static String PROPERTIES = "Display and Drawing Preferences";
    }
}


