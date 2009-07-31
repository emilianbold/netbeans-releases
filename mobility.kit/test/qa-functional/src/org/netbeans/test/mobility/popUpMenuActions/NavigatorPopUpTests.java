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

package org.netbeans.test.mobility.popUpMenuActions;

//<editor-fold desc="imports">
import javax.swing.JCheckBoxMenuItem;
import javax.swing.MenuElement;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.jellytools.modules.web.NavigatorOperator;
import org.netbeans.jemmy.operators.JCheckBoxMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.ide.ProjectSupport;
//</editor-fold>

/**
 *
 * @author tester
 */
public class NavigatorPopUpTests extends JellyTestCase {
    public static final String ACTION_DELETE = "Delete";
    public static final String ACTION_EDIT = "Edit";
    public static final String ACTION_GOTOSRC = "Go To Source";
    public static final String ACTION_RENAME = "Rename";
    public static final String DIALOG_CONFIRM_DELETION = "Confirm Object Deletion";
    public static final String FORM = "Form";
    public static final String FORM_RENAMED = "myNewForm";
    public static final String MENU_FLOW = "Flow";
    public static final String MENU_VIEW_EDITORS = "View|Editors|";
    public static final String MIDP_VISUAL_DESIGN = "MIDP Visual Design";
    public static final String NAV_DISPLAYABLES = "MIDP Visual Design|Displayables";
    public static final String NAV_DISP_ITEMS = "|Items|";
    public static final String NAV_DISP_COMMS = "|Assigned Commands|";
    public static final String POPUPMENU_NEW_ADD = "New/Add|";
    public static final String PROJECT_NAME_MIDP = "MobileApplication";
//    public static final String WIZARD_BUNDLE = "org.netbeans.modules.mobility.project.ui.wizard.Bundle";
//    public static final String PROJECT_MIDP = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/Project/J2ME/MobileApplication");
    public static final String CATEGORY_OTHER = "Other";
    
    //<editor-fold desc="Test Suite - base">
    /** Constructor required by JUnit */
    public NavigatorPopUpTests(String tname, boolean init) {
        super(tname);
        if (init) init();
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        // Prepare some projects
        suite.addTest(new NavigatorPopUpTests("testGoToSourceFromNavigator", true));
        suite.addTest(new NavigatorPopUpTests("testEditFromNavigator", false));
        suite.addTest(new NavigatorPopUpTests("testDeleteFromNavigator", false));
        suite.addTest(new NavigatorPopUpTests("testNewScreenFromNavigator", false));
        suite.addTest(new NavigatorPopUpTests("testNewCommandFromNavigator", false));
        suite.addTest(new NavigatorPopUpTests("testNewComponentFromNavigator", false));
        suite.addTest(new NavigatorPopUpTests("testRenameFromNavigator", false));
        suite.addTest(new NavigatorPopUpTests("testSVGFiltersFromNavigator", false));
        return suite;
    }
    
     /*/public void createProject(String projectType, String projectName) {
        NewProjectWizardOperator npwop = NewProjectWizardOperator.invoke();
        npwop.selectCategory(Bundle.getStringTrimmed(WIZARD_BUNDLE,"Templates/Project/J2ME")); 
        npwop.selectProject(projectType); 
        npwop.next();
        NewProjectNameLocationStepOperator step = new NewProjectNameLocationStepOperator();
        step.txtProjectLocation().setText(getWorkDirPath());
        step.txtProjectName().setText(projectName); //NOI18N
        sleep(20);
        step.finish();
        ProjectSupport.waitScanFinished();
    }//*/
    
    //</editor-fold>
    
    public void init() {
        new Action(null, "Open").perform(new Node(ProjectsTabOperator.invoke().tree(), PROJECT_NAME_MIDP + "|Source Packages|hello|HelloMIDlet.java"));
    }
        
    public void testGoToSourceFromNavigator() {
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|form", ACTION_GOTOSRC);
    }
    
    public void testEditFromNavigator() {
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" + FORM, ACTION_EDIT);
    }
    
    public void testDeleteFromNavigator() {
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" + FORM, ACTION_DELETE);
        NbDialogOperator confirmDel = new NbDialogOperator(DIALOG_CONFIRM_DELETION);
        confirmDel.btYes().push();
    }
    
    public void testNewScreenFromNavigator() {
        ActionFromWhereTo(MENU_FLOW,NAV_DISPLAYABLES, POPUPMENU_NEW_ADD + FORM);
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES +"|" + FORM).select();
    }
    
    public void testNewCommandFromNavigator() {
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Cancel Command");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_COMMS + "cancelCommand").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Help Command");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_COMMS + "helpCommand").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Screen Command");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_COMMS + "screenCommand").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Stop Command");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_COMMS + "stopCommand").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Ok Command");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_COMMS + "okCommand").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Back Command");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_COMMS + "backCommand").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Item Command");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_COMMS + "itemCommand").select();
    }
    
    public void testNewComponentFromNavigator() {
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "String Item");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_ITEMS + "stringItem").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Image Item");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_ITEMS + "imageItem").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Table Item");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_ITEMS + "tableItem").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Spacer");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_ITEMS + "spacer").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Text Field");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_ITEMS + "textField").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Choice Group");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_ITEMS + "choiceGroup").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Date Field");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_ITEMS + "dateField").select();
        
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" +  FORM, POPUPMENU_NEW_ADD + "Gauge");
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES).select();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM +  NAV_DISP_ITEMS + "gauge").select();
    }
    
    public void testRenameFromNavigator() {
        ActionFromWhereTo(MENU_FLOW, NAV_DISPLAYABLES + "|" + FORM, ACTION_RENAME);
        NbDialogOperator renameDialog = new NbDialogOperator(ACTION_RENAME);
        new JTextFieldOperator(renameDialog,FORM).setText(FORM_RENAMED);
        renameDialog.btOK().push();
        new Node(new NavigatorOperator().getTree(), NAV_DISPLAYABLES + "|" + FORM_RENAMED).select();
    }
    
     public void createNewFile(String category, String template, String name, String extension, String packageName, String projectName, boolean tryCompile) {
        new ProjectsTabOperator().getProjectRootNode(projectName).select(); // NOI18N
        NewFileWizardOperator newFile = NewFileWizardOperator.invoke(); 
        newFile.selectCategory(category);
        newFile.selectFileType(template);
        newFile.next();
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        //op.setObjectName(name); //TODO !!! doesn't work with some file types. It doesn;t change the name
        JTextFieldOperator tfo = new JTextFieldOperator(op, 0);
        tfo.setText(name);
        if(packageName != null) {
            op.setPackage(packageName);
        }
        op.finish();
        ProjectSupport.waitScanFinished();
        if (packageName != null) {
            new ProjectsTabOperator().getProjectRootNode(projectName).select();
            new Node(new ProjectsTabOperator().tree(), PROJECT_NAME_MIDP + "|Source Packages|" + packageName + "|" + name + "." + extension).select();
        } else {
            FilesTabOperator.invoke();
            new Node(new FilesTabOperator().tree(), PROJECT_NAME_MIDP + "|" + name + "." + extension);
            ProjectsTabOperator.invoke();
        }
        if (tryCompile) {
            CompileJavaAction ca = new CompileJavaAction();
            MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
            stt.start();
            ProjectsTabOperator.invoke();
            ca.perform();
            stt.waitText("Finished building " + projectName);
        }
    }
    
    public void testSVGFiltersFromNavigator() {
        createNewFile(CATEGORY_OTHER, "SVG File", "newSVGFile", "svg", null, PROJECT_NAME_MIDP, false);
        NavigatorOperator.invokeNavigator().clickForPopup();
        JPopupMenuOperator jp = new JPopupMenuOperator();
        MenuElement Elems[] = jp.getSubElements();
        JCheckBoxMenuItemOperator jpi;
        
        for (int i = 0; i < Elems.length; i++) {
            jpi = new JCheckBoxMenuItemOperator((JCheckBoxMenuItem)Elems[i]);
            if (!jpi.getState()) {
                jpi.push();
                assertEquals(jpi.getState(), true);
                sleep(500);
                jpi.push();
                assertEquals(jpi.getState(), false);
                sleep(500);
            } else {
                jpi.push();
                assertEquals(jpi.getState(), false);
                sleep(500);
                jpi.push();
                assertEquals(jpi.getState(), true);
                sleep(500);
            }
        }
    }
    
    private void ActionFromWhereTo(String From, String Where, String ActionName) {
        sleep(2000);
        //new Action("Window|Editor", null).perform();
        //new Action(MENU_VIEW_EDITORS + From, null).perform();
        new Action("Window|Editor", null).perform();
        new Action(MENU_VIEW_EDITORS + From, null).perform();
        sleep(2000);
        //NavigatorOperator.invokeNavigator();
        Node n = new Node(new NavigatorOperator().getTree(), Where);
        new Action(null, ActionName).perform(n);
        sleep(1000);
    }
   

    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
}
