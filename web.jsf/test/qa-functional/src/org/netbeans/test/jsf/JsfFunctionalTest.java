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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.jsf;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentPaletteOperator;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.modules.editor.NbEditorUtilities;
/* TODO - fix to use new implementation
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.config.model.FacesConfig;
import org.netbeans.modules.web.jsf.config.model.ManagedBean;
import org.netbeans.modules.web.jsf.config.model.NavigationCase;
import org.netbeans.modules.web.jsf.config.model.NavigationRule;
 */

/** Test JSF support.
 *
 * @author Lukasz Grela
 * @author Jiri Skrivanek
 */
public class JsfFunctionalTest extends JellyTestCase{
    
    public static final String PROJECT_NAME = "myjsfproject";
    public static final String WELCOME_JSP = "welcomeJSF.jsp";
    public static final String INDEX_JSP = "index.jsp";
    public static final String FROM_ACTION = "FromAction";
    public static final String FROM_ACTION1 = "FromAction1";
    public static final String FROM_OUTCOME = "FromOutcome";
    public static final String DESCRIPTION = "Description";
    
    public JsfFunctionalTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new JsfFunctionalTest("testCreateWebProjectWithJSF"));
        suite.addTest(new JsfFunctionalTest("testManagedBeanWizard"));
        suite.addTest(new JsfFunctionalTest("testManagedBeanDelete"));
        suite.addTest(new JsfFunctionalTest("testAddManagedBean"));
        suite.addTest(new JsfFunctionalTest("testAddNavigationRule"));
        suite.addTest(new JsfFunctionalTest("testAddNavigationCase"));
        suite.addTest(new JsfFunctionalTest("testAddNavigationCaseWithNewRule"));
        suite.addTest(new JsfFunctionalTest("testAddJSFToProject"));
        suite.addTest(new JsfFunctionalTest("testJSFPalette"));
        return suite;
    }
    
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public void testCreateWebProjectWithJSF() throws IOException {
        // "Web"
        String web = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "OpenIDE-Module-Display-Category");
        // "Web Application"
        String webApplication = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyWeb.xml");
        NewProjectWizardOperator nop = NewProjectWizardOperator.invoke();
        nop.selectCategory(web);
        nop.selectProject(webApplication);
        nop.next();
        NewWebProjectNameLocationStepOperator lop = new NewWebProjectNameLocationStepOperator();
        lop.setProjectName(PROJECT_NAME);
        lop.setProjectLocation(getDataDir().getCanonicalPath());
        lop.next();
        NewProjectWizardOperator frameworkStep = new NewProjectWizardOperator();
        // select JavaServer Faces
        new JTableOperator(frameworkStep).selectCell(0, 0);;
        frameworkStep.finish();
        // Opening Projects
        String openingProjectsTitle = Bundle.getString(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_Opening_Projects_Progress");
        try {
            // wait at most 60 second until progress dialog dismiss
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000);
            new NbDialogOperator(openingProjectsTitle).waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
        ProjectSupport.waitScanFinished();
        // Check project contains all needed files.
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        Node welcomeJSF = new Node(webPages, "welcomeJSF.jsp");
        new OpenAction().perform(welcomeJSF);
        new EditorOperator("welcomeJSF.jsp").close();
        Node facesconfig = new Node(webPages, "WEB-INF|faces-config.xml");
        new OpenAction().perform(facesconfig);
        // open faces-config.xml is used in next test cases
        new EditorOperator("faces-config.xml");
    }
    
    /** Test JSF Managed Bean Wizard. */
    public void testManagedBeanWizard(){
        NewFileWizardOperator projectWizard = NewFileWizardOperator.invoke();
        String filetype = Bundle.getStringTrimmed(
                "org/netbeans/modules/web/jsf/resources/Bundle",
                "Templates/JSP_Servlet/JSFManagedBean.java");
        projectWizard.selectCategory("Web");
        projectWizard.selectFileType(filetype);
        projectWizard.next();
        NewJSFBeanStepOperator bean = new NewJSFBeanStepOperator();
        bean.setClassName("MyManagedBean");
        bean.selectScope("session");
        bean.cboPackage().getTextField().setText("mypackage");
        bean.finish();
        // verify
        new EditorOperator("MyManagedBean.java").close();
        EditorOperator facesEditor = new EditorOperator("faces-config.xml");
        String expected = "<managed-bean>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        expected = "<managed-bean-name>MyManagedBean</managed-bean-name>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        expected = "<managed-bean-class>mypackage.MyManagedBean</managed-bean-class>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        expected = "<managed-bean-scope>session</managed-bean-scope>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        
        /* TODO - fix to use new implementation
        Document document = facesEditor.txtEditorPane().getDocument();
        JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(document);
        List mbeans = JSFConfigUtilities.getAllManagedBeans(data);
        assertEquals("There should be one managed bean", 1, mbeans.size());
        ManagedBean managedBean = (ManagedBean)mbeans.get(0);
        assertEquals("Wrong managed-bean-name.", "MyManagedBean", managedBean.getManagedBeanName());
        assertEquals("Wrong managed-bean-class.", "mypackage.MyManagedBean", managedBean.getManagedBeanClass());
        assertEquals("Wrong managed-bean-scope.", "session", managedBean.getManagedBeanScope());
         */
    }
    
    /** Test that delete safely bean removes record from faces-config.xml. */
    public void testManagedBeanDelete() {
        Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "mypackage|MyManagedBean.java");
        new ActionNoBlock(null, "Refactor|Safely Delete...").perform(node);
        NbDialogOperator safeDeleteDialog = new NbDialogOperator("Safe Delete");
        new JButtonOperator(safeDeleteDialog, "Refactor").push();
        node.waitNotPresent();
        // verify
        EditorOperator facesEditor = new EditorOperator("faces-config.xml");
        /* TODO - fix to use new implementation
        Document document = facesEditor.txtEditorPane().getDocument();
        JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(document);
        List mbeans = JSFConfigUtilities.getAllManagedBeans(data);
         */
        //TODO - fails because of issue 77310
        //assertEquals("There should be none managed bean.", 0, mbeans.size());
    }
    
    /** Test adding JSF Managed Bean from faces-config.xml. */
    public void testAddManagedBean(){
        EditorOperator editor = new EditorOperator("faces-config.xml");
        Action addBeanAction = new ActionNoBlock(null, "JavaServer Faces|Add Managed Bean...");
        addBeanAction.perform(editor);
        AddManagedBeanOperator addBeanOper = new AddManagedBeanOperator();
        addBeanOper.setBeanName("SecondBean");
        addBeanOper.setBeanClass("mypackage.MyManagedBean");
        addBeanOper.selectScope("application");
        addBeanOper.setBeanDescription(DESCRIPTION);
        addBeanOper.add();
        // verify
        /* TODO - fix to use new implementation
        Document document = editor.txtEditorPane().getDocument();
        JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(document);
        List mbeans = JSFConfigUtilities.getAllManagedBeans(data);
        for (int i = 0; i < mbeans.size(); i++) {
            ManagedBean managedBean = (ManagedBean)mbeans.get(i);
            if(managedBean.getManagedBeanName().equals("SecondBean")) {
                assertEquals("Wrong managed-bean-class.", "mypackage.MyManagedBean", managedBean.getManagedBeanClass());
                assertEquals("Wrong managed-bean-scope.", "application", managedBean.getManagedBeanScope());
                return;
            }
        }
        fail("Managed bean record not added to faces-config.xml.");
        */ 
    }
    
    /** Test adding navigation rule from faces-config.xml. */
    public void testAddNavigationRule() throws IOException {
        EditorOperator editor = new EditorOperator("faces-config.xml");
        Action addRule = new ActionNoBlock(null, "JavaServer Faces|Add Navigation Rule...");
        addRule.perform(editor);
        AddNavigationRuleDialogOperator rule = new AddNavigationRuleDialogOperator();
        rule.setRuleFromView("/"+WELCOME_JSP);
        rule.setRuleDescription(DESCRIPTION);
        rule.add();
        editor.save();
        // verify
        Document document = editor.txtEditorPane().getDocument();
        /* TODO - fix to use new implementation
        JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(document);
        FacesConfig facesConfig = data.getFacesConfig();
        NavigationRule[] rules = facesConfig.getNavigationRule();
        assertEquals("Wrong From View.", "/"+WELCOME_JSP, rules[0].getFromViewId());
        assertTrue("Wrong rule description.", rules[0].getDescription()[0].indexOf(DESCRIPTION) > -1);
        assertEquals("There should be one navigation rule", 1, rules.length);
         */
    }
    
    /** Test adding navigation case from faces-config.xml. */
    public void testAddNavigationCase() throws IOException {
        EditorOperator editor = new EditorOperator("faces-config.xml");
        Action addCase = new ActionNoBlock(null, "JavaServer Faces|Add Navigation Case...");
        addCase.perform(editor);
        AddNavigationCaseDialogOperator caseOper = new AddNavigationCaseDialogOperator();
        caseOper.selectFromView("/"+WELCOME_JSP);
        caseOper.selectToView("/"+WELCOME_JSP);
        caseOper.setFromAction(FROM_ACTION);
        caseOper.setFromOutcome(FROM_OUTCOME);
        caseOper.setRuleDescription(DESCRIPTION);
        caseOper.add();
        editor.save();
        // verify
        Document document = editor.txtEditorPane().getDocument();
        /* TODO - fix to use new implementation
        JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(document);
        FacesConfig facesConfig = data.getFacesConfig();
        NavigationRule[] rules = facesConfig.getNavigationRule();
        assertEquals("There should be one navigation rule.", 1, rules.length);
        NavigationCase[] cases = rules[0].getNavigationCase();
        assertTrue("There should be one navigation case.", cases.length==1);
        assertEquals("Wrong From Action.", FROM_ACTION, cases[0].getFromAction());
        assertEquals("Wrong From OutCome.", FROM_OUTCOME, cases[0].getFromOutcome());
         */
        // TODO - fails because of issue 91329
        //assertFalse("Should not be redirected.", cases[0].isRedirected());
    }
    
    /** Test adding navigation case with new rule from faces-config.xml. */
    public void testAddNavigationCaseWithNewRule() throws IOException {
        EditorOperator editor = new EditorOperator("faces-config.xml");
        Action addCase = new ActionNoBlock(null, "JavaServer Faces|Add Navigation Case...");
        addCase.perform(editor);
        AddNavigationCaseDialogOperator caseOper = new AddNavigationCaseDialogOperator();
        caseOper.cboFromView().getTextField().setText("/"+INDEX_JSP);
        caseOper.setFromAction(FROM_ACTION);
        caseOper.setFromOutcome(FROM_OUTCOME);
        caseOper.setRuleDescription(DESCRIPTION);
        caseOper.checkRedirect(true);
        caseOper.cboToView().getTextField().setText("/"+INDEX_JSP);
        caseOper.add();
        editor.save();
        // verify
        Document document = editor.txtEditorPane().getDocument();
        /* TODO - fix to use new implementation
        JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(document);
        FacesConfig facesConfig = data.getFacesConfig();
        NavigationRule[] rules = facesConfig.getNavigationRule();
        assertEquals("There should be two navigation rules.", 2, rules.length);
        assertEquals("Wrong From View.", "/"+INDEX_JSP, rules[1].getFromViewId());
        NavigationCase[] cases = rules[1].getNavigationCase();
        assertEquals("There should be one navigation case.", 1, cases.length);
        assertEquals("Wrong From Action.", FROM_ACTION, cases[0].getFromAction());
        assertEquals("Wrong From OutCome.", FROM_OUTCOME, cases[0].getFromOutcome());
         */
        // TODO - fails because of issue 91329
        //assertTrue("Should be redirected.", cases[0].isRedirected());
    }

    /** Test adding JSF framework to existing web application. */
    public void testAddJSFToProject() throws IOException {
        // "Web"
        String web = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "OpenIDE-Module-Display-Category");
        // "Web Application"
        String webApplication = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyWeb.xml");
        NewProjectWizardOperator nop = NewProjectWizardOperator.invoke();
        nop.selectCategory(web);
        nop.selectProject(webApplication);
        nop.next();
        NewWebProjectNameLocationStepOperator lop = new NewWebProjectNameLocationStepOperator();
        lop.setProjectName(PROJECT_NAME+"2");
        lop.setProjectLocation(getDataDir().getCanonicalPath());
        lop.finish();
        // Opening Projects
        String openingProjectsTitle = Bundle.getString(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_Opening_Projects_Progress");
        try {
            // wait at most 60 second until progress dialog dismiss
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000);
            new NbDialogOperator(openingProjectsTitle).waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
   
        // add JSF framework using project properties
        // open project properties
        ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME+"2").properties();
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Frameworks" category
        new Node(new JTreeOperator(propertiesDialogOper), "Frameworks").select();
        new JButtonOperator(propertiesDialogOper, "Add").pushNoBlock();
        NbDialogOperator addFrameworkOper = new NbDialogOperator("Add a Framework");
        // select "JavaServer Faces" but item is instance of JSFFrameworkProvider which we need to select
        new JListOperator(addFrameworkOper).selectItem("JSF");
        addFrameworkOper.ok();
        new JCheckBoxOperator(propertiesDialogOper, "Validate XML").setSelected(false);
        new JCheckBoxOperator(propertiesDialogOper, "Verify Objects").setSelected(true);
        // confirm properties dialog
        propertiesDialogOper.ok();

        // Check project contains all needed files. 
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME+"2");
        Node welcomeJSF = new Node(webPages, "welcomeJSF.jsp");
        Node facesconfig = new Node(webPages, "WEB-INF|faces-config.xml");
        webPages.setComparator(new DefaultStringComparator(true, true));
        Node webXML = new Node(webPages, "WEB-INF|web.xml");
        new EditAction().performAPI(webXML);
        EditorOperator webXMLEditor = new EditorOperator("web.xml");
        webXMLEditor.select("validateXml");
        assertTrue("Validate XML should be false.", webXMLEditor.getText(webXMLEditor.getLineNumber()+1).indexOf("false") > -1);
        webXMLEditor.select("verifyObjects");
        assertTrue("Verify Objects should be true.", webXMLEditor.getText(webXMLEditor.getLineNumber()+1).indexOf("true") > -1);
        webXMLEditor.close();
    }
    
    /** Test JSF Palette. */
    public void testJSFPalette() {
        EditorOperator editorOper = new EditorOperator(INDEX_JSP);
        ComponentPaletteOperator paletteOper = new ComponentPaletteOperator();
        // collapse HTML category
        JCheckBoxOperator htmlCategoryOper = new JCheckBoxOperator(paletteOper, "HTML");
        if(htmlCategoryOper.isSelected()) {
            htmlCategoryOper.push();
        }
        // expand JSF category
        JCheckBoxOperator jsfCategoryOper = new JCheckBoxOperator(paletteOper, "JSF");
        if(!jsfCategoryOper.isSelected()) {
            jsfCategoryOper.push();
        }
        paletteOper.selectComponent("JSF Form");
        paletteOper.pushKey(KeyEvent.VK_ENTER);
        new NbDialogOperator("Insert JSF Form").ok();
        String expected = "<f:view>";
        assertTrue("index.jsp should contain "+expected+".", editorOper.contains(expected));
        expected = "<h:form>";
        assertTrue("index.jsp should contain "+expected+".", editorOper.contains(expected));
        expected = "</h:form>";
        assertTrue("index.jsp should contain "+expected+".", editorOper.contains(expected));
        expected = "</f:view>";
        assertTrue("index.jsp should contain "+expected+".", editorOper.contains(expected));
        
        paletteOper.selectComponent("JSF Data Table");
        paletteOper.pushKey(KeyEvent.VK_ENTER);
        new NbDialogOperator("Insert JSF Data Table").ok();
        expected = "<h:dataTable value=\"#{arrayOrCollectionOf}\" var=\"item\">";
        assertTrue("index.jsp should contain "+expected+".", editorOper.contains(expected));
        expected = "</h:dataTable>";
        assertTrue("index.jsp should contain "+expected+".", editorOper.contains(expected));
    }
}
