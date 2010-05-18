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



package org.netbeans.test.uml.sqd.utils;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;



public class Util {
    
    private String projectName = "";
    private EventTool eventTool = new EventTool();
    
    public final String ADD_DIAGRAM_MENU = "New|Diagram";
    public final String EXCEPTION_DLG = "Exception";
    
    public final String YES_BTN = "Yes";
    public final String NO_BTN = "No";
    public final String OK_BTN = "OK";
    private final static String FINISH_BTN = "Finish";
    private final static String DONE_BTN = "Done";
    
    private final static String RVRS_ENGINEERING_DLG_TTL = "Reverse Engineering";
    private final static String IMPLEMENTATION_MODE = "Implementation";
    
    public Util(String projectName) {
        this.projectName = projectName;
    }
    
      
    
    
    public void openDiagram(String projectRelativePath){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projectName);
        
        Node node = new Node(root, projectRelativePath);        
        pto.tree().clickOnPath(node.getTreePath(), 2);        
    }
    
    public DiagramOperator addDiagram(String name, String pathToInvokationPoint){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projectName);
        
        Node node = new Node(root, pathToInvokationPoint);        
        node.performPopupActionNoBlock(ADD_DIAGRAM_MENU);
        
        NewDiagramWizardOperator wiz = new NewDiagramWizardOperator();
        wiz.setDiagramType(NewDiagramWizardOperator.SEQUENCE_DIAGRAM);        
        wiz.setDiagramName(name);
        wiz.clickOK();
        return new DiagramOperator(name);
    }
    
    public Node getNode(String nodePath){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projectName);
        
        Node node = new Node(root, nodePath);        
        return node;
    }
    
    public boolean diagramHasExactElements(String[] elementNames, DiagramOperator dia){
        try{
            DiagramElementOperator[] els = new DiagramElementOperator[elementNames.length];
            for (int i=0;i<elementNames.length; i++){
                els[i] = new DiagramElementOperator(dia, elementNames[i]);
            }
            return diagramHasExactElements(els, dia);
        }catch(Exception e){
            return false;
        }
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
    
    
    public boolean nodeExists(String path){
        long waitNodeTime = JemmyProperties.getCurrentTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 2000);
        try{
            Node node = getNode(path);
            node.select();
            return true;
        }catch(Exception e){
            return false;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", waitNodeTime);
        }
    }
    
    
    /**
     * @return previous value of color
     */
    public Color setColor(JDialogOperator dlg, int red, int green, int blue){
        JTabbedPaneOperator tabbedPane =  new JTabbedPaneOperator(dlg);
        tabbedPane.selectPage("RGB");
        JTextComponentOperator tmp=new JTextComponentOperator(tabbedPane, 0);
        int r=Integer.parseInt(tmp.getText());
        tmp.clearText();
        tmp.typeText(String.valueOf(red));
        
        tmp=new JTextComponentOperator(tabbedPane, 1);
        int g=Integer.parseInt(tmp.getText());
        tmp.clearText();
        tmp.typeText(String.valueOf(green));
        
        tmp=new JTextComponentOperator(tabbedPane, 2);
        int b=Integer.parseInt(tmp.getText());
        tmp.clearText();
        tmp.typeText(String.valueOf(blue));
        
        eventTool.waitNoEvent(3500);
        JButtonOperator btn = new JButtonOperator(dlg, "OK");
        //btn.push();
        new MouseRobotDriver(new Timeout("",10)).clickMouse(btn, btn.getCenterXForClick(), btn.getCenterYForClick(), 1, InputEvent.BUTTON1_MASK, 0, new Timeout("",10)); 
        return new Color(r,g,b);
    }
    
    
    public void closeStartupException(){
        long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try{                        
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 2000);
            new JDialogOperator(EXCEPTION_DLG).close();                        
        }catch(Exception excp){
        }finally{
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);                        
        }        
    }
    
    public void closeSaveDlg(){
        new Thread(new Runnable() {
            public void run() {
                long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
                try{
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
                    JDialogOperator saveDlg = new JDialogOperator("Save");
                    new JButtonOperator(saveDlg, "Save All").pushNoBlock();
                }catch(Exception e){}
                finally{
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);
                }
            }
        }).start();
    }
}
