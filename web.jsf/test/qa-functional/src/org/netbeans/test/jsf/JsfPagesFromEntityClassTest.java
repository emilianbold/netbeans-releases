/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.jsf;

import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.Action;

/*
 * JsfFunctionalTest.java
 *
 * Created on 2 April 2006, 15:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author luke
 */
public class JsfPagesFromEntityClassTest extends JellyTestCase{
    public static String PROJECT_LOCATION=System.getProperty("xtest.tmpdir");
    public static String PROJECT_NAME="myjsfproject";
    public static String J2EE_VERSION="Java EE 5";
    public static String SERVER="Sun Java System Application Server";
    Action open=new ActionNoBlock(null,"Open");
    private static final String BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";

    public JsfPagesFromEntityClassTest(String s) {
        super(s);
    }
    
    
    public void testAddServer(){
        String path = System.getProperty("server.path");
        System.out.println("ulalala  "+path);
        if (path == null) {
            throw new RuntimeException("Cannot setup appserver, property j2ee.appserver.path is not set.");
        }
        String username = System.getProperty("j2ee.appserver.username","admin");
        String password = System.getProperty("j2ee.appserver.password","adminadmin");
        
        Node node = new Node(new RuntimeTabOperator().getRootNode(),Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE"));
        node.performPopupActionNoBlock(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"));
        NbDialogOperator dialog = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"));
        new JComboBoxOperator(dialog).selectItem("Sun Java System Application Server");
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT")).push();
        
        new JTextFieldOperator(dialog).setText("");
        new JTextFieldOperator(dialog).setText(path);
        
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH")).push();
        new ProjectsTabOperator();
    }
    
    public void testCreateWebProjectWithJsf() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        String category = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(category);
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep =
                new NewWebProjectNameLocationStepOperator();
        nameStep.cbServer().selectItem(1);
        nameStep.txtProjectName().typeText(PROJECT_NAME);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(PROJECT_LOCATION);
        nameStep.selectJ2EEVersion(J2EE_VERSION);
        nameStep.next();
        NewWebProjectFrameworkStep frameworkStep=new NewWebProjectFrameworkStep();
        frameworkStep.selectJsf();
        frameworkStep.finish();
        ProjectSupport.waitScanFinished();
    }
    
    
    public void testCreateEntityClassAndPU(){
        NewFileWizardOperator entity = NewFileWizardOperator.invoke();
        String filetype = "Entity Class";
        entity.selectCategory("Persistence");
        entity.selectFileType(filetype);
        entity.next();
        NbDialogOperator dialog = new NbDialogOperator("New Entity Class");
        new JComboBoxOperator(dialog,1).typeText("mypackage");
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH")).push();
        
        NewFileWizardOperator persistence_unit = NewFileWizardOperator.invoke();
        filetype = "Persistence Unit";
        persistence_unit.selectCategory("Persistence");
        persistence_unit.selectFileType(filetype);
        persistence_unit.next();
        NbDialogOperator persistenceDialog = new NbDialogOperator("New Persistence Unit");
        new JButtonOperator(persistenceDialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH")).push();
        
        
    }
    
    public void testCreateJSFPagesFromEntityClass(){
        NewFileWizardOperator jsf_pages = NewFileWizardOperator.invoke();
        String filetype = "JSF Pages from Entity Class";
        jsf_pages.selectCategory("Persistence");
        jsf_pages.selectFileType(filetype);
        jsf_pages.next();
        NbDialogOperator dialog = new NbDialogOperator("New JSF Pages from Entity Class");
        new JButtonOperator(dialog,2).push();
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT")).push();
        new JButtonOperator(dialog,Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH")).push();
        
    }
    
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new JsfPagesFromEntityClassTest("testAddServer"));
        suite.addTest(new JsfPagesFromEntityClassTest("testCreateWebProjectWithJsf"));
        suite.addTest(new JsfPagesFromEntityClassTest("testCreateEntityClassAndPU"));
        suite.addTest(new JsfPagesFromEntityClassTest("testCreateJSFPagesFromEntityClass"));
        suite.addTest(new JsfPagesFromEntityClassTest("checkFiles"));
        return suite;
    }
    
    public void checkFiles(){
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        Node detail=new Node(webPages,"newEntity|Detail.jsp");
        open.perform(detail);
        new EditorOperator("Detail.jsp").close();
        Node edit=new Node(webPages,"newEntity|Edit.jsp");
        open.perform(edit);
        new EditorOperator("Edit.jsp").close();
        Node list=new Node(webPages,"newEntity|List.jsp");
        open.perform(list);
        new EditorOperator("List.jsp").close();
        Node _new=new Node(webPages,"newEntity|New.jsp");
        open.perform(_new);
        new EditorOperator("New.jsp").close();
        
    }
    
    public void runProject(){
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Action(null,"Run Project").perform(rootNode);
        OutputTabOperator console = new OutputTabOperator(PROJECT_NAME);
        console.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000);
        console.waitText(BUILD_SUCCESSFUL);
    }
    
    static class NewWebProjectFrameworkStep extends NewProjectWizardOperator {
      private JTableOperator _tabFrameworks; 
      
      public JTableOperator tabFrameworks() {
        if (_tabFrameworks==null) {
            _tabFrameworks = new JTableOperator(this);
        }
        return _tabFrameworks;
      }
      
      public void selectJsf(){
          tabFrameworks().selectCell(0,0);
      }
        
        
    }
}

