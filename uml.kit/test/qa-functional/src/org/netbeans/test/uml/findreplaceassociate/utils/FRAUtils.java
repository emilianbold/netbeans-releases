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


package org.netbeans.test.uml.findreplaceassociate.utils;

import java.util.LinkedList;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.FindDialogOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.PopupConstants;

/**
 * @author yaa
 */

public class FRAUtils {
    
    public static String FRA_XTEST_PROJECT_DIR = UMLTestCase.XTEST_PROJECT_DIR + "/Projects-FindReplaceAssociate";
    
    public FRAUtils() {
    }
    
    
    
    public static DiagramOperator openDiagram(String pName, String dName, String dType){
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
        
        root = new ProjectRootNode(pto.tree(),pName);
        
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
            wiz.clickFinish();
        }
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", timeout);
        try{Thread.sleep(500);}catch(Exception ex){}
        return new DiagramOperator(dName);
    }
    
    public static Node selectElementInProjectsTree(String prName, String elName){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = null;
        long timeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
        //try{
        root = new ProjectRootNode(pto.tree(),prName);
        Node nodeModel = new Node(root,"Model");
        Node nodeElem = new Node(nodeModel, elName);
        nodeElem.select();
        return nodeElem;
        //}catch(Exception e){
        //    return null;
        //}
    }
    
    public static LinkedList getTestData1(){
        LinkedList list = new LinkedList();
        list.add(new Object[]{"UMLProject1", "UMLProject1", "", FindDialogOperator.SearchTarget.PROJECT});
        
        list.add(new Object[]{"DClass", "classdiagram", "", FindDialogOperator.SearchTarget.DIAGRAM});
        
        list.add(new Object[]{"pkg1", "package", "", FindDialogOperator.SearchTarget.PACKAGE});
        
        list.add(new Object[]{"ClassC", "class", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassC", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        
        list.add(new Object[]{"Interface1", "interface", "", FindDialogOperator.SearchTarget.INTERFACE});
        list.add(new Object[]{"value", "attribute", "", FindDialogOperator.SearchTarget.ATTRIBUTE});
        list.add(new Object[]{"test", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        
        list.add(new Object[]{"ClassA", "class", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"data", "attribute", "", FindDialogOperator.SearchTarget.ATTRIBUTE});
        list.add(new Object[]{"ClassA", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"getData", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"setData", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"val", "parameter", "", FindDialogOperator.SearchTarget.PARAMETER});
        
        list.add(new Object[]{"ClassB", "class", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassB", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"getData", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"setData", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"val", "parameter", "", FindDialogOperator.SearchTarget.PARAMETER});
        
        list.add(new Object[]{"ClassE", "class", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassE", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        
        list.add(new Object[]{"ClassD", "class", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassD", "ClassD", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"test", "operation", "", FindDialogOperator.SearchTarget.OPERATION});
        
        list.add(new Object[]{"int", "datatype", "", FindDialogOperator.SearchTarget.DATATYPE});
        list.add(new Object[]{"void", "datatype", "", FindDialogOperator.SearchTarget.DATATYPE});
        
        list.add(new Object[]{"Gen1", "generalization", "", FindDialogOperator.SearchTarget.GENERALIZATION});
        list.add(new Object[]{"Impl1", "implementation", "", FindDialogOperator.SearchTarget.IMPLEMENTATION});
        
        return list;
    }
    
    public static LinkedList getTestData2(){
        LinkedList list = new LinkedList();
        list.add(new Object[]{"UMLProject2", "UMLProject2", "", FindDialogOperator.SearchTarget.PROJECT});
        
        list.add(new Object[]{"DClass1", "DClass1", "", FindDialogOperator.SearchTarget.DIAGRAM});
        list.add(new Object[]{"DClass2", "DClass2", "", FindDialogOperator.SearchTarget.DIAGRAM});
        list.add(new Object[]{"DClass3", "DClass3", "", FindDialogOperator.SearchTarget.DIAGRAM});
        
        list.add(new Object[]{"Class01", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class01", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class02", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class02", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class03", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class03", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class04", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class04", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class05", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class05", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class06", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class06", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class07", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class07", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class08", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class08", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class09", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class09", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class10", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class10", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class11", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class11", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"Class12", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"Class12", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        
        list.add(new Object[]{"ClassA", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassA", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassB", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassB", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassC", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassC", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassD", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassD", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassE", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassE", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassF", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassF", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassG", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassG", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassH", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassH", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassI", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassI", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassJ", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassJ", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"ClassK", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassK", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        
        list.add(new Object[]{"int", "datatype", "", FindDialogOperator.SearchTarget.DATATYPE});
        list.add(new Object[]{"void", "datatype", "", FindDialogOperator.SearchTarget.DATATYPE});
        
        list.add(new Object[]{"ClassTest1", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassTest1", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"data", "attribute", "", FindDialogOperator.SearchTarget.ATTRIBUTE});
        list.add(new Object[]{"test", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        list.add(new Object[]{"index", "parameter", "", FindDialogOperator.SearchTarget.PARAMETER});
        
        list.add(new Object[]{"ClassTest2", "AliasClass", "", FindDialogOperator.SearchTarget.CLASS});
        list.add(new Object[]{"ClassTest2", "AliasOperation", "", FindDialogOperator.SearchTarget.OPERATION});
        
        list.add(new Object[]{"Gen1", "AliasGen", "", FindDialogOperator.SearchTarget.GENERALIZATION});
        
        return list;
    }
    
}

