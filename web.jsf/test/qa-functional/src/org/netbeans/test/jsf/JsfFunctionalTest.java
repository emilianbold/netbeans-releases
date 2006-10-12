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
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.config.model.FacesConfig;
import org.netbeans.modules.web.jsf.config.model.NavigationCase;
import org.netbeans.modules.web.jsf.config.model.NavigationRule;

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
public class JsfFunctionalTest extends DbJellyTestCase{
    public static String PROJECT_LOCATION=System.getProperty("xtest.tmpdir");
    public static String PROJECT_NAME="myjsfproject";
    Action open=new ActionNoBlock(null,"Open");
    Action addRule=new ActionNoBlock(null,"Java Server Faces|Add Navigation Rule...");
    Action addCase=new ActionNoBlock(null,"Java Server Faces|Add Navigation Case...");
    public static String JSP_PAGE="welcomeJSF.jsp";
    public static String VALUE_1="value_1";
    public static String VALUE_2="value_2";
    public JsfFunctionalTest(String s) {
        super(s);
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
        nameStep.txtProjectName().typeText(PROJECT_NAME);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(PROJECT_LOCATION);
        nameStep.next();
        NewWebProjectFrameworkStep frameworkStep=new NewWebProjectFrameworkStep();
        frameworkStep.selectJsf();
        frameworkStep.finish();
        sleep(1000);
        ProjectSupport.waitScanFinished();
    }
    
    public void testCheckFiles(){
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        Node welcomeJSF=new Node(webPages,"welcomeJSF.jsp");
        
        open.perform(welcomeJSF);
        new EditorOperator("welcomeJSF.jsp").close();
        
        Node facesconfig= new Node(webPages,"WEB-INF|faces-config.xml");
        open.perform(facesconfig);
        new EditorOperator("faces-config.xml").close();
       // ref(Util.dumpProjectView(PROJECT_NAME));
    }
    
    
    public void testJsfBeanWizzard(){
        NewFileWizardOperator projectWizard = NewFileWizardOperator.invoke();
        String filetype = Bundle.getStringTrimmed(
                "org/netbeans/modules/web/jsf/resources/Bundle",
                "Templates/JSP_Servlet/JSFManagedBean.java");
        projectWizard.selectCategory("Web");
        projectWizard.selectFileType(filetype);
        projectWizard.next();
        NewJSFBeanStepOperator bean=new NewJSFBeanStepOperator();
        bean.setClassName("MyManagedBean");
        bean.selectScope("session");
        bean.finish();
        new EditorOperator("MyManagedBean.java").close();
        
        
    }
    
    public void testJsfFacesSupport(){
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Node facesconfigNode= new Node(webPages,"WEB-INF|faces-config.xml");
        open.perform(facesconfigNode);
        EditorOperator editor=new EditorOperator("faces-config.xml");
        addRule.performPopup(editor);
        AddNavigationRuleDialogOperator rule=new AddNavigationRuleDialogOperator();
        rule.setRuleFromView("/"+JSP_PAGE);
        rule.add();
        addCase.performPopup(editor);
        AddNavigationCaseDialogOperator caze=new AddNavigationCaseDialogOperator();
        caze.selectFromView("/"+JSP_PAGE);
        caze.selectToView("/"+JSP_PAGE);
        caze.setFromAction(VALUE_1);
        caze.setFromOutcome(VALUE_2);
        caze.add();
                
        editor.save();
        
        
        
    }
    
    public void testIfJsfFacesContainsValidEntries() throws Exception{
        EditorOperator editor=new EditorOperator("faces-config.xml");
        Document document=editor.txtEditorPane().getDocument();
        JSFConfigDataObject  data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(document);        
        FacesConfig facesConfig=data.getFacesConfig(); 
        NavigationRule[] rules=facesConfig.getNavigationRule();
        assertEquals("/"+JSP_PAGE,rules[0].getFromViewId());
        assertTrue("there should be one navigation rule",rules.length==1);
        NavigationCase[] cases=rules[0].getNavigationCase();
        System.out.println(cases[0].getFromAction());
        System.out.println(cases[0].getFromOutcome());
        assertEquals(VALUE_1,cases[0].getFromAction());
        assertEquals(VALUE_2,cases[0].getFromOutcome());
        assertTrue("there should be one navigation case",cases.length==1);
        
    }
    
    public void testJSFConfigUtilities() throws Exception{
        EditorOperator editor=new EditorOperator("faces-config.xml");
        Document document=editor.txtEditorPane().getDocument();
        JSFConfigDataObject  data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(document);   
        List mb=JSFConfigUtilities.getAllManagedBeans(data);
        assertTrue("there should be one managed bean",mb.size()==1);
        NavigationRule rule=JSFConfigUtilities.findNavigationRule(data.getFacesConfig(),"/"+JSP_PAGE);
        assertNotNull("the navigation rule can't be null",rule);
                
        
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new JsfFunctionalTest("testCreateWebProjectWithJsf"));
        suite.addTest(new JsfFunctionalTest("testCheckFiles"));
        suite.addTest(new JsfFunctionalTest("testJsfBeanWizzard"));
        suite.addTest(new JsfFunctionalTest("testJsfFacesSupport"));
        suite.addTest(new JsfFunctionalTest("testIfJsfFacesContainsValidEntries"));
        suite.addTest(new JsfFunctionalTest("testJSFConfigUtilities"));
        return suite;
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
