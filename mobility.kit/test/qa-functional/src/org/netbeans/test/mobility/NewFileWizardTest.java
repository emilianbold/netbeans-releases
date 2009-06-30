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

package org.netbeans.test.mobility;

//<editor-fold desc="imports">
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.junit.ide.ProjectSupport;
//</editor-fold>

/**
 *
 * @author tester
 */
public class NewFileWizardTest extends JellyTestCase {
    
    //<editor-fold desc="Constants">
    public static final String PROJECT_NAME_MIDP = "MobileApplication";
    //public static final String PROJECT_NAME_CDC = "FileWizardTestProject_CDC";
    public static final String WIZARD_BUNDLE = "org.netbeans.modules.mobility.project.ui.wizard.Bundle";
    public static final String PROJECT_MIDP = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/Project/J2ME/MobileApplication");
    //public static final String PROJECT_CDC = "CDC Application";
    
    public static final String CATEGORY_MIDP = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/MIDP");
    //public static final String CATEGORY_CDC = "CDC"; //TODO I18N
    public static final String CATEGORY_OTHER = "Other";
    
    public static final String ITEM_VISUALMIDLET = "Visual MIDlet";
    public static final String ITEM_VISUALDESIGN = "Visual Design";
    public static final String ITEM_VISUALGAMEDESIGN = "Visual Game Design";
    public static final String ITEM_MIDLET = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/MIDP/Midlet.java");
    public static final String ITEM_MIDPCANVAS = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/MIDP/MIDPCanvas.java");
    /*public static final String ITEM_XLET = "Xlet";
    public static final String ITEM_PPXLETFORM = "Personal Profile Xlet Form";
    public static final String ITEM_AGUIXLETFORM  = "AGUI Xlet Form";
    public static final String ITEM_RICOHXLET = "Ricoh Xlet";
    public static final String ITEM_CREMEFROM = "CreMe JFrame Form";*/
    //</editor-fold>

    static final String[] tests = {
        "testCreateProjects",
        "testCreateVisualMIDlet",
        "testCreateVisualDesign",
        "testCreateVisualGameDesign",
        "testCreateMIDPCanvas",
        "testCreateSVG",
        "testCreateMIDlet"
    };

    //<editor-fold desc="Test Suite - base">
    /** Constructor required by JUnit */
    public NewFileWizardTest(String tname) {
        super(tname);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(NewFileWizardTest.class).addTest(tests).clusters(".*").enableModules(".*").gui(true));
    }

    /** Creates suite from particular test cases. */
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite();
//        // Prepare some projects
//        // suite.addTest(new NewFileWizardTest("testCreateProjects"));
//        // Create MIDP Files
//        suite.addTest(new NewFileWizardTest("testCreateMIDlet"));
//        suite.addTest(new NewFileWizardTest("testCreateVisualMIDlet"));
//        suite.addTest(new NewFileWizardTest("testCreateVisualDesign"));
//        suite.addTest(new NewFileWizardTest("testCreateVisualGameDesign"));
//        suite.addTest(new NewFileWizardTest("testCreateMIDPCanvas"));
//        suite.addTest(new NewFileWizardTest("testCreateSVG"));
//        /* Create CDC Files
//        suite.addTest(new NewFileWizardTest("testCreateXlet"));
//        suite.addTest(new NewFileWizardTest("testCreatePPXletForm"));
//        suite.addTest(new NewFileWizardTest("testCreateAGUIXletForm"));
//        suite.addTest(new NewFileWizardTest("testCreateRicohXlet"));
//        suite.addTest(new NewFileWizardTest("testCreateCremeJFrameForm"));
//        //*/
//        return suite;
//    }
    //</editor-fold>
    
    //<editor-fold desc="General Methods for Creating New File and Project">
    public void createProject(String projectType, String projectName) {
        NewProjectWizardOperator npwop = NewProjectWizardOperator.invoke();
        npwop.selectCategory(Bundle.getStringTrimmed(WIZARD_BUNDLE,"Templates/Project/J2ME")); 
        npwop.selectProject(projectType); 
        npwop.next();
        NewJavaProjectNameLocationStepOperator step = new NewJavaProjectNameLocationStepOperator();
        step.txtProjectLocation().setText(getWorkDirPath());
        step.txtProjectName().setText(projectName); //NOI18N
        sleep(20);
        step.finish();
        ProjectSupport.waitScanFinished();
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
    
    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    //</editor-fold>

    
    public void testCreateProjects() {
        createProject(PROJECT_MIDP, PROJECT_NAME_MIDP);
        //createProject(PROJECT_CDC, PROJECT_NAME_CDC);
        OutputOperator.invoke();
    }
 
    
    //<editor-fold desc="MIDP Files">
    public void testCreateMIDlet() {
        createNewFile(CATEGORY_MIDP, ITEM_MIDLET, "newMIDlet", "java", "testing", PROJECT_NAME_MIDP, true);
    }
    
    public void testCreateVisualMIDlet() {
        createNewFile(CATEGORY_MIDP, ITEM_VISUALMIDLET, "newVisualMIDlet", "java", "testing", PROJECT_NAME_MIDP, true);
    }
            
    public void testCreateVisualDesign() {
        createNewFile(CATEGORY_MIDP, ITEM_VISUALDESIGN, "newVisualDesign", "java", "testing", PROJECT_NAME_MIDP, true);
    }
    
    public void testCreateVisualGameDesign() {
        createNewFile(CATEGORY_MIDP, ITEM_VISUALGAMEDESIGN, "newVisualGameDesign", "java", "testing", PROJECT_NAME_MIDP, true);
    }
    
    public void testCreateMIDPCanvas() {
        createNewFile(CATEGORY_MIDP, ITEM_MIDPCANVAS, "newMIDPCanvas", "java", "testing", PROJECT_NAME_MIDP, true);
    }
    
    public void testCreateSVG() {
        createNewFile(CATEGORY_OTHER, "SVG File", "newSVGFile", "svg", null, PROJECT_NAME_MIDP, false);
    }
    //</editor-fold>
    
    /*/<editor-fold desc="CDC Files">
    public void testCreateXlet() {
        createNewFile(CATEGORY_CDC, ITEM_XLET, "newXlet", "java", "testing", PROJECT_NAME_CDC, true);
    }
    
    public void testCreatePPXletForm() {
        createNewFile(CATEGORY_CDC, ITEM_PPXLETFORM, "newPPXletForm", "java", "testing", PROJECT_NAME_CDC, true);
    }
        
    public void testCreateAGUIXletForm() {
        createNewFile(CATEGORY_CDC, ITEM_AGUIXLETFORM, "newAGUIXletForm", "java", "testing", PROJECT_NAME_CDC, true);
    }
    
    public void testCreateRicohXlet() {
        createNewFile(CATEGORY_CDC, ITEM_RICOHXLET, "newRicohXlet", "java", "testing", PROJECT_NAME_CDC, true);
    }
    
    public void testCreateCremeJFrameForm() {
        createNewFile(CATEGORY_CDC, ITEM_CREMEFROM, "newCremeForm", "java", "testing", PROJECT_NAME_CDC, true);
    }
    //</editor-fold>//*/
    
}




















