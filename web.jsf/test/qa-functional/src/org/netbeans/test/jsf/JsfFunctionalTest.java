/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.test.jsf;

import org.netbeans.test.web.*;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JComboBox;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentPaletteOperator;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/** Test JSF support.
 *
 * @author Lukasz Grela
 * @author Jiri Skrivanek
 * @author Jindrich Sedek
 */
public class JsfFunctionalTest extends WebProjectValidationEE5 {

    public static final String WELCOME_JSP = "welcomeJSF.jsp";
    public static final String INDEX_JSP = "index.jsp";
    public static final String FROM_ACTION1 = "FromAction1";
    public static final String FROM_ACTION2 = "FromAction2";
    public static final String FROM_OUTCOME1 = "FromOutcome1";
    public static final String FROM_OUTCOME2 = "FromOutcome2";
    public static final String DESCRIPTION_BEAN = "DescriptionBean";
    public static final String DESCRIPTION_RULE = "DescriptionRule";
    public static final String DESCRIPTION_CASE1 = "DescriptionCase1";
    public static final String DESCRIPTION_CASE2 = "DescriptionCase2";

    static {
        PROJECT_NAME = "WebJSFProject";
    }

    protected static String URL_PATTERN_NULL = "The URL Pattern has to be entered.";
    protected static String URL_PATTERN_INVALID = "The URL Pattern is not valid.";
    // folder of sample project

    /** Need to be defined because of JUnit */
    public JsfFunctionalTest(String name) {
        super(name);
    }

    /** Need to be defined because of JUnit */
    public JsfFunctionalTest() {
        super();
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(JsfFunctionalTest.class);
        conf = addServerTests(Server.GLASSFISH, conf,
                "testPreconditions",
                "testNewJSFWebProject",
                "testRedeployProject",
                "testCleanAndBuildProject",
                "testCompileAllJSP",
                "testCleanAndBuildProject",
                "testCompileAllJSP",
                "testStopServer",
                "testManagedBeanWizard",
                "testManagedBeanDelete",
                "testAddManagedBean",
                "testAddNavigationRule",
                "testAddNavigationCase",
                "testAddNavigationCaseWithNewRule",
                "testAddJSFToProject",
                "testJSFPalette",
                "testCreateEntityClassAndPU",
                "testShutdownDb"
                );
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    }
    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - in next panel set server to Glassfish and J2EE version to Java EE 5
     * - in Frameworks panel set JSF framework
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.jsp is opened
     */
    public void testNewJSFWebProject() throws IOException {
        File projectFolder = new File (getProjectFolder(PROJECT_NAME));
        if (projectFolder.exists()){
            FileObject fo = FileUtil.createData(projectFolder);
            fo.delete();
        }
        final String serverNodeName = getServerNode(Server.ANY).getText();
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        String category = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(category);
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep = new NewWebProjectNameLocationStepOperator();
        nameStep.txtProjectName().setText("");
        nameStep.txtProjectName().typeText(PROJECT_NAME);
        nameStep.txtProjectLocation().setText("");
        String sFolder = getProjectFolder(PROJECT_NAME);
        nameStep.txtProjectLocation().typeText(sFolder);
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectServer(serverNodeName);
        serverStep.selectJavaEEVersion("JAVA EE 5");
        serverStep.next();

        NewWebProjectJSFFrameworkStepOperator frameworkStep = new NewWebProjectJSFFrameworkStepOperator();
        assertTrue("JSF framework not present!", frameworkStep.setJSFFrameworkCheckbox());
//TODO enable this code while framework configuration is fixed
//        frameworkStep.txtServletURLMapping().setText("");
//        assertEquals(URL_PATTERN_NULL, frameworkStep.lblTheURLPatternHasToBeEntered().getText());
//        frameworkStep.txtServletURLMapping().typeText("hhhhhh*", 0);
//        assertEquals(URL_PATTERN_INVALID, frameworkStep.lblTheURLPatternIsNotValid().getText());
//        frameworkStep.txtServletURLMapping().setText("");
//        frameworkStep.txtServletURLMapping().typeText("/faces/*", 0);
//        frameworkStep.selectPageLibraries();
//        frameworkStep.rbCreateNewLibrary().push();
//        assertEquals("\"\" is not valid path for a folder.", frameworkStep.lblIsNotValidPathForAFolder().getText());
//        // Can be uncommented after fixing Issue 157766
//        //frameworkStep.rbRegisteredLibraries().push();
//        frameworkStep.rbDoNotAppendAnyLibrary().push();

        frameworkStep.finish();
        sleep(5000);
        ProjectSupport.waitScanFinished();
        verifyWebPagesNode("welcomeJSF.jsp");
        verifyWebPagesNode("WEB-INF|web.xml");
        verifyWebPagesNode("WEB-INF|sun-web.xml");//NOI18N
        verifyWebPagesNode("WEB-INF|faces-config.xml");//NOI18N
        // Check project contains all needed files.
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        Node welcomeJSF = new Node(webPages, "welcomeJSF.jsp");
        new OpenAction().perform(welcomeJSF);
        new EditorOperator("welcomeJSF.jsp").close();
        Node facesconfig = new Node(webPages, "WEB-INF|faces-config.xml");
        new OpenAction().perform(facesconfig);
        // open faces-config.xml is used in next test cases
        getFacesConfig();
    }

    @Override
    protected File getProjectFolder() {
        try {
            File dataDir = new JsfFunctionalTest().getWorkDir();
            return Manager.normalizeFile(dataDir);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    /** Test JSF Managed Bean Wizard. */
    public void testManagedBeanWizard(){
        NewFileWizardOperator projectWizard = NewFileWizardOperator.invoke();
        // "Java Server Faces"
        String category = Bundle.getStringTrimmed(
                "org/netbeans/modules/web/jsf/resources/Bundle",
                "Templates/JSF");
        // "JSF Managed Bean"
        String filetype = Bundle.getStringTrimmed(
                "org/netbeans/modules/web/jsf/resources/Bundle",
                "Templates/JSF/JSFManagedBean.java");
        projectWizard.selectCategory(category);
        projectWizard.selectFileType(filetype);
        projectWizard.next();
        NewJSFBeanStepOperator bean = new NewJSFBeanStepOperator();
        bean.setClassName("MyManagedBean");
        bean.selectScope("session");
        bean.cboPackage().getTextField().setText("mypackage");
        bean.finish();
        // verify
        new EditorOperator("MyManagedBean.java").close();
        EditorOperator facesEditor = getFacesConfig();
        String expected = "<managed-bean>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        expected = "<managed-bean-name>MyManagedBean</managed-bean-name>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        expected = "<managed-bean-class>mypackage.MyManagedBean</managed-bean-class>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        expected = "<managed-bean-scope>session</managed-bean-scope>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
    }
    
    /** Test that delete safely bean removes record from faces-config.xml. */
    public void testManagedBeanDelete() {
        Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "mypackage|MyManagedBean.java");
        new ActionNoBlock(null, "Refactor|Safely Delete...").perform(node);
        NbDialogOperator safeDeleteDialog = new NbDialogOperator("Safely Delete");
        new JButtonOperator(safeDeleteDialog, "Refactor").push();
        node.waitNotPresent();
        // verify
        EditorOperator facesEditor = getFacesConfig();
        String expected = "<managed-bean>";
        assertFalse("faces-config.xml should not contain "+expected, facesEditor.contains(expected));
    }
    
    /** Test adding JSF Managed Bean from faces-config.xml. */
    public void testAddManagedBean(){
        EditorOperator editor = getFacesConfig();
        Action addBeanAction = new ActionNoBlock(null, "JavaServer Faces|Add Managed Bean...");
        addBeanAction.perform(editor);
        AddManagedBeanOperator addBeanOper = new AddManagedBeanOperator();
        addBeanOper.setBeanName("SecondBean");
        addBeanOper.setBeanClass("mypackage.MyManagedBean");
        addBeanOper.selectScope("application");
        addBeanOper.setBeanDescription(DESCRIPTION_BEAN);
        addBeanOper.add();
        // verify
        try{
        EditorOperator facesEditor = getFacesConfig();
        String expected = "<managed-bean>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        expected = "<managed-bean-name>SecondBean</managed-bean-name>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        expected = "<managed-bean-class>mypackage.MyManagedBean</managed-bean-class>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        expected = "<managed-bean-scope>application</managed-bean-scope>";
        assertTrue("faces-config.xml should contain "+expected, facesEditor.contains(expected));
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
    
    /** Test adding navigation rule from faces-config.xml. */
    public void testAddNavigationRule() throws IOException {
        EditorOperator editor = getFacesConfig();
        Action addRule = new ActionNoBlock(null, "JavaServer Faces|Add Navigation Rule...");
        addRule.perform(editor);
        AddNavigationRuleDialogOperator rule = new AddNavigationRuleDialogOperator();
        rule.setRuleFromView("/"+WELCOME_JSP);
        rule.setRuleDescription(DESCRIPTION_RULE);
        rule.add();
        editor.waitModified(true);
        editor.save();
        // verify
        String expected = "<from-view-id>/welcomeJSF.jsp</from-view-id>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "<navigation-rule>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "</navigation-rule>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = DESCRIPTION_RULE;
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
    }
    
    /** Test adding navigation case from faces-config.xml. */
    public void testAddNavigationCase() throws IOException {
        EditorOperator editor = getFacesConfig();
        Action addCase = new ActionNoBlock(null, "JavaServer Faces|Add Navigation Case...");
        addCase.perform(editor);
        AddNavigationCaseDialogOperator caseOper = new AddNavigationCaseDialogOperator();
        caseOper.selectFromView("/"+WELCOME_JSP);
        caseOper.selectToView("/"+WELCOME_JSP);
        caseOper.setFromAction(FROM_ACTION1);
        caseOper.setFromOutcome(FROM_OUTCOME1);
        caseOper.setRuleDescription(DESCRIPTION_CASE1);
        caseOper.add();
        editor.waitModified(true);
        editor.save();
        // verify
        String expected = "<from-action>"+FROM_ACTION1+"</from-action>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "<from-outcome>"+FROM_OUTCOME1+"</from-outcome>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "<to-view-id>/"+WELCOME_JSP+"</to-view-id>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "<navigation-case>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "</navigation-case>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = DESCRIPTION_CASE1;
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
    }
    
    /** Test adding navigation case with new rule from faces-config.xml. */
    public void testAddNavigationCaseWithNewRule() throws IOException {
        EditorOperator editor = getFacesConfig();
        Action addCase = new ActionNoBlock(null, "JavaServer Faces|Add Navigation Case...");
        addCase.perform(editor);
        AddNavigationCaseDialogOperator caseOper = new AddNavigationCaseDialogOperator();
        caseOper.cboFromView().getTextField().setText("/"+INDEX_JSP);
        caseOper.setFromAction(FROM_ACTION2);
        caseOper.setFromOutcome(FROM_OUTCOME2);
        caseOper.setRuleDescription(DESCRIPTION_CASE2);
        caseOper.checkRedirect(true);
        caseOper.cboToView().getTextField().setText("/"+INDEX_JSP);
        caseOper.add();
        editor.waitModified(true);
        editor.save();
        // verify
        String expected = "<from-view-id>/"+INDEX_JSP+"</from-view-id>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "<from-action>"+FROM_ACTION2+"</from-action>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "<from-outcome>"+FROM_OUTCOME2+"</from-outcome>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "<to-view-id>/"+INDEX_JSP+"</to-view-id>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = "<redirect/>";
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
        expected = DESCRIPTION_CASE2;
        assertTrue("faces-config.xml should contain "+expected, editor.contains(expected));
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
        lop.next();
        lop.finish();
        
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
        // select "JavaServer Faces" but item is instance of org.netbeans.modules.web.jsf.JSFFrameworkProvider which we need to select
        new JListOperator(addFrameworkOper).selectItem("org.netbeans.modules.web.jsf.JSFFrameworkProvider");
        addFrameworkOper.ok();
// TODO add after ui fixes
//        new JCheckBoxOperator(propertiesDialogOper, "Validate XML").setSelected(false);
//        new JCheckBoxOperator(propertiesDialogOper, "Verify Objects").setSelected(true);
//        // do not append any library
//        new JTabbedPaneOperator(propertiesDialogOper).setSelectedIndex(1);
//        new JRadioButtonOperator(propertiesDialogOper, "Do not append any library.").doClick();
        // confirm properties dialog
        propertiesDialogOper.ok();
        
        // Check project contains all needed files.
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME+"2");
//        Node welcomeJSF = new Node(webPages, "welcomeJSF.jsp");
        assertNotNull(new Node(webPages, "WEB-INF|faces-config.xml"));
        webPages.setComparator(new DefaultStringComparator(true, true));
        Node webXML = new Node(webPages, "WEB-INF|web.xml");
        new EditAction().performAPI(webXML);
// TODO add after ui fixes
//        EditorOperator webXMLEditor = new EditorOperator("web.xml");
//        webXMLEditor.select("validateXml");
//        assertTrue("Validate XML should be false.", webXMLEditor.getText(webXMLEditor.getLineNumber()+1).indexOf("false") > -1);
//        webXMLEditor.select("verifyObjects");
//        assertTrue("Verify Objects should be true.", webXMLEditor.getText(webXMLEditor.getLineNumber()+1).indexOf("true") > -1);
//        webXMLEditor.close();
    }
    
    /** Test JSF Palette. */
    public void testJSFPalette() {
        EditorOperator editorOper = new EditorOperator(INDEX_JSP);
        editorOper.select(17);
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
        editorOper.makeComponentVisible();
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
        
        editorOper.makeComponentVisible();
        paletteOper.selectComponent("JSF Data Table");
        paletteOper.pushKey(KeyEvent.VK_ENTER);
        new NbDialogOperator("Insert JSF Data Table").ok();
        expected = "<h:dataTable value=\"#{}\" var=\"item\">";
        assertTrue("index.jsp should contain "+expected+".", editorOper.contains(expected));
        expected = "</h:dataTable>";
        assertTrue("index.jsp should contain "+expected+".", editorOper.contains(expected));
    }

        /** Create Entity class and persistence unit. */
    public void testCreateEntityClassAndPU(){
        NewFileWizardOperator entity = NewFileWizardOperator.invoke();
        entity.selectProject(PROJECT_NAME);
        entity.selectCategory("Persistence");
        entity.selectFileType("Entity Class");
        entity.next();
        NewJavaFileNameLocationStepOperator locationOper = new NewJavaFileNameLocationStepOperator();
        locationOper.setPackage("mypackage");
        new JButtonOperator(locationOper, "Create Persistence Unit").pushNoBlock();
        
        NbDialogOperator persistenceDialog = new NbDialogOperator("Create Persistence Unit");
        new JComboBoxOperator(
                (JComboBox)new JLabelOperator(persistenceDialog, "Data Source").getLabelFor()).selectItem("jdbc/sample");
        new JButtonOperator(persistenceDialog, "Create").push();
        
        locationOper.finish();
    }

    /** Shutdown databases */
    public void testShutdownDb(){
        DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes();
        for (DatabaseRuntime runtime : runtimes) {
            if (runtime.isRunning()) {
                runtime.stop();
            }
        }
    }

    /** If installed visualweb cluster in IDE, switch from PageFlow to XML view of faces-config.xml. 
     * @return EditorOperator instance of faces-config.xml
     */
    public static EditorOperator getFacesConfig() {
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        Node facesconfig = new Node(webPages, "WEB-INF|faces-config.xml");
        new OpenAction().perform(facesconfig);
        TopComponentOperator tco = new TopComponentOperator("faces-config.xml");
        if(JToggleButtonOperator.findJToggleButton((Container)tco.getSource(), ComponentSearcher.getTrueChooser("Toggle button")) != null) {
            // "XML"
            String xmlLabel = Bundle.getStringTrimmed("org.netbeans.modules.xml.multiview.Bundle", "LBL_XML_TAB");
            JToggleButtonOperator tbo = new JToggleButtonOperator(tco, xmlLabel);
            tbo.push();
        }
        return new EditorOperator("faces-config.xml");
    }
}
