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



package org.netbeans.test.uml.cdfs.utils;

import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;

public class CDFSUtil {
    
    public static String CDFS_XTEST_PROJECT_DIR = UMLTestCase.XTEST_PROJECT_DIR + "/Projects-CDFS";
    
    private String projectName = "";
    public final String CDFS_MENU = "Create Diagram From Selected Elements...";
    public final String CDFS_COMPLEX_OBJ_TTL = "Create Diagram From Selected";
    public final String YES_BTN = "Yes";
    public final String NO_BTN = "No";
    public final String OK_BTN = "OK";
    private final static String FINISH_BTN = "Finish";
    public final String EXCEPTION_DLG = "Exception";
    
    private EventTool eventTool = new EventTool();
    public UMLTestCase tc = null;
    
    private final static String IMPLEMENTATION_MODE = "Implementation";
    
    public CDFSUtil(String projectName){
        this.projectName = projectName;
    }
    
    public CDFSUtil(String projectName, UMLTestCase ts){
        this.projectName = projectName;
        this.tc = ts;
    }
    
    
    public Node getNode(String nodePath){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projectName);
        
        Node node = new Node(root, nodePath);
        return node;
    }
    
    public void selectNode(String nodePath){
        Node node = getNode(nodePath);
        node.select();
    }
    
    public void createDiagram(Node[] nodes, String diagramType, String diagramName){
        eventTool.waitNoEvent(1000);
        new ActionNoBlock(null, CDFS_MENU).performPopup(nodes);
        NewDiagramWizardOperator wizard = new NewDiagramWizardOperator();
        wizard.setDiagramType(diagramType);
        wizard.setDiagramName(diagramName);
        wizard.clickFinish();
    }
    
    
    public boolean diagramHasExactElements(String[] elementNames, DiagramOperator dia){
        try{
            DiagramElementOperator[] els = new DiagramElementOperator[elementNames.length];
            for (int i=0;i<elementNames.length; i++){
                els[i] = new DiagramElementOperator(dia, elementNames[i]);
            }
            return diagramHasExactElements(els, dia);
        }catch(Exception e){
            e.printStackTrace(tc.getLog());
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
                tc.getLog().println("could not find an element."+diaAl.size()+" "+elements.length);
                return false;
            }
            al.remove(index);
        }
        
        if (al.size()>0){
            tc.getLog().println("element is left"+elements);
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
    
    
    public boolean allNodesExist(String parentPath, String[] nodeLabels){
        for(int i=0; i<nodeLabels.length; i++){
            if (!nodeExists(parentPath+"|"+nodeLabels[i])){
                return false;
            }
        }
        return true;
    }
    
    
    public boolean isPopupMenuItemExist(JPopupMenuOperator popup, String menuItem ){
        boolean isAvailable = false;
        
        MenuElement [] elements = popup.getSubElements();
        for (int i=0; i<elements.length; i++){
            if (elements[i] instanceof JMenuItem){
                //PopupConstants
                if ( ((JMenuItem)elements[i]).getText().equalsIgnoreCase(menuItem) ){
                    isAvailable = true;
                    break;
                }
            }
        }
        return isAvailable;
    }
    
    
    
}
