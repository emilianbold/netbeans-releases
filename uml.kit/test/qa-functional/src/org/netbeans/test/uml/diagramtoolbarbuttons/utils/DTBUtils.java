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


package org.netbeans.test.uml.diagramtoolbarbuttons.utils;

import java.awt.Container;
import java.io.PrintStream;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.metal.MetalComboBoxButton;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.Utils;
import org.netbeans.test.umllib.util.PopupConstants;


/**
 * @author yaa
 */
public class DTBUtils {
    
    public DTBUtils() {
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
            try{Thread.sleep(100);}catch(Exception e3){}
            wiz.clickOK();
        }
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        try{Thread.sleep(500);}catch(Exception e){}
        return new DiagramOperator(dName);
    }

    public static boolean findAndCloseDialog(String dialogTitle){
        long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            JDialogOperator dlg = new JDialogOperator(dialogTitle);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
            dlg.close();
            dlg.waitClosed();
            return true;
     } 
    
    public static boolean checkUnnecessaryButtons(DiagramOperator diagram, String[] buttonNames, PrintStream log){
        MyToolbarChooser chooser = new MyToolbarChooser(buttonNames, log);
        JButtonOperator.findJButton((Container)diagram.getSource(), chooser);
        return chooser.result;
    }

    static class MyToolbarChooser implements ComponentChooser {
         private String[] btnNecessary;
         private PrintStream aLog;
         public boolean result;
        
         MyToolbarChooser(String[] buttonsNecessary, PrintStream log){
             btnNecessary = buttonsNecessary;
             aLog = log;
             result = true;
         }

         public boolean checkComponent(java.awt.Component arg0)  {
                if(arg0 instanceof JButton)
                {
                    //check combobox, skip arrow button
                    JButton tmp=(JButton)arg0;
                    if(tmp.getParent() instanceof JComboBox)return false;
                }
             
                if (arg0 instanceof BasicArrowButton){
                } else if (arg0 instanceof MetalComboBoxButton){
                } else if ((arg0 instanceof JButton) || (arg0 instanceof JToggleButton)){
                        JButton button = (JButton)arg0;
                        String tooltip = button.getToolTipText();
                        boolean flag = false;
                        for(int i = 0; i < btnNecessary.length; i++){
                            if(btnNecessary[i].equals(tooltip)){
                                flag = true;
                                break;
                            }
                        }
                        if (!flag){
                            result = false;
                            aLog.println("Unnecessary toolbar button '" + tooltip + "' >> " + button.toString());
                        }
                }
                //
                return false ;
         } 
         
	 public String getDescription() {
			return "Chooser for checking unnecessary buttons on diagram toolbar";
	 }         
    }

    public interface DialogTitles{
        public final static String EXPORT_AS_IMAGE = "Export as image";
        public final static String OVERVIEW = "Overview";
        public final static String PRINT_PREVIEW = "Print Preview";
    }
    
}


