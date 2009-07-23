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

package org.netbeans.test.mobility.editor;

//<editor-fold desc="imports">
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.junit.ide.ProjectSupport;
//</editor-fold>

/**
 *
 * @author joshis
 */
public class editorTestsWithNewProject extends JellyTestCase {
    public static final String ELIF_BLOCK = "//#elif CLDC";
    public static final String PROJECT_NAME_MIDP = "FileWizardTestProject_MIDP";
    public static final String WIZARD_BUNDLE = "org.netbeans.modules.mobility.project.ui.wizard.Bundle";
    public static final String PROJECT_MIDP = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/Project/J2ME/MobileApplication");
    public static final String CATEGORY_MIDP = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/MIDP");
    public static final String ITEM_MIDLET = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/MIDP/Midlet.java");
    public static final String FILENAME = "Midlet";
    public static final String PREPROCESSOR_TEXT_BEFORE = 
            "//#if CLDC\n" +
            "System.out.println(\"CLDC\");\n" +
            "//#else\n" +
            "System.out.println(\"ELSE\");\n" +
            "//#endif";
    public static final String PREPROCESSOR_TEXT_AFTER = 
            "//#if CLDC\n" +
            "System.out.println(\"CLDC\");\n" +
            "//#else\n" +
            "//# System.out.println(\"ELSE\");\n" +
            "//#endif";
    public static final String IF_ELSE_BLOCK =
            "//#if CLDC\n" +
            "    \n" +
            "//#else\n" +
            "    \n" +
            "//#endif";
    public static final String DEBUG_BLOCK = 
            "//#mdebug\n" +
            "    \n" +
            "//#enddebug";
    
    public editorTestsWithNewProject(String testname) {
        super(testname);
    }
    
    public editorTestsWithNewProject(String testname, boolean init) {
        super(testname);
        if (init) prepareForTests();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new editorTestsWithNewProject("preprocessorRecommentTest", true));
        suite.addTest(new editorTestsWithNewProject("preprocessorCreateIfElseBlock"));
        suite.addTest(new editorTestsWithNewProject("preprocessorCreateDebugBlock"));
        suite.addTest(new editorTestsWithNewProject("preprocessorCreateElifBlock"));
        return suite;
    }
    
    public void CheckConfigurationExists(String conf) {
        new Node(new ProjectsTabOperator().tree(), PROJECT_NAME_MIDP + "|Project Configuration|" + conf).select();
    }
    
    public void CreateConfigurationInAddConfDialog(String strName, String DialogTitle) {
        sleep(1000);
        NbDialogOperator addConfigDialog = new NbDialogOperator(DialogTitle);
        JTextFieldOperator confNameTextField = new JTextFieldOperator(addConfigDialog, 0);
        confNameTextField.setText(strName);
        addConfigDialog.btOK().push();
    }
    
    public void newConfigurationPopUpTest() {
        EditorOperator editorPane = new EditorOperator(FILENAME + ".java");
        ActionNoBlock a = new ActionNoBlock(null, "Preprocessor Blocks|Add Configurations To Project");
        a.perform(editorPane);
        CreateConfigurationInAddConfDialog("configFromEditorPopUp", "Add Configuration");
        NbDialogOperator projPropDialog = new NbDialogOperator(PROJECT_NAME_MIDP);
        projPropDialog.btOK().push();
        CheckConfigurationExists("configFromEditorPopUp");
    }
    
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
 
    public void prepareForTests() {
        createProject(PROJECT_MIDP, PROJECT_NAME_MIDP);
        createNewFile(CATEGORY_MIDP, ITEM_MIDLET, FILENAME, "java", "hello", PROJECT_NAME_MIDP, false);
        newConfigurationPopUpTest();
    }
    
    public void preprocessorRecommentTest() {
        EditorOperator editorPane = new EditorOperator(FILENAME + ".java");
        editorPane.setCaretPosition("startApp() {", false);
        editorPane.typeKey('\n');
        int Line = editorPane.getLineNumber();
        editorPane.setCaretPosition(Line, 1);
        editorPane.insert(PREPROCESSOR_TEXT_BEFORE);
        Action a = new Action(null, "Preprocessor Blocks|Re-Comment");
        assertEquals(editorPane.contains(PREPROCESSOR_TEXT_AFTER), false);
        a.perform(editorPane);
        assertEquals(editorPane.contains(PREPROCESSOR_TEXT_AFTER), true);
        editorPane.select(PREPROCESSOR_TEXT_AFTER);
        editorPane.pushKey(KeyEvent.VK_DELETE);
    }
    
    public void preprocessorCreateIfElseBlock() {
        EditorOperator editorPane = new EditorOperator(FILENAME + ".java");
        editorPane.setCaretPosition("public void pauseApp(", true);
        int Line = editorPane.getLineNumber();
        editorPane.pushKey(KeyEvent.VK_ENTER);
        editorPane.pushKey(KeyEvent.VK_ENTER);
        editorPane.setCaretPosition(Line, 1);
        assertEquals(editorPane.contains(IF_ELSE_BLOCK), false);
        Action a = new Action("Source|Preprocessor Blocks|Create If / Else Block", null);
        a.perform();
        sleep(1000);
        editorPane.pushKey(KeyEvent.VK_ENTER);
        assertEquals(editorPane.contains(IF_ELSE_BLOCK), true);
    }
    
    public void preprocessorCreateDebugBlock() {
        EditorOperator editorPane = new EditorOperator(FILENAME + ".java");
        editorPane.setCaretPosition("public void pauseApp(", true);
        int Line = editorPane.getLineNumber();
        editorPane.pushKey(KeyEvent.VK_ENTER);
        editorPane.pushKey(KeyEvent.VK_ENTER);
        editorPane.setCaretPosition(Line, 1);
        assertEquals(editorPane.contains(DEBUG_BLOCK), false);
        Action a = new Action("Source|Preprocessor Blocks|Create Debug Block", null);
        a.perform();
        assertEquals(editorPane.contains(DEBUG_BLOCK), true);
    }
    
    public void preprocessorCreateElifBlock() {
        EditorOperator editorPane = new EditorOperator(FILENAME + ".java");
        editorPane.setCaretPosition("//#if CLDC", false);
        editorPane.pushKey(KeyEvent.VK_ENTER);
        editorPane.pushKey(KeyEvent.VK_ENTER);
        sleep(500);
        int Line = editorPane.getLineNumber();
        editorPane.setCaretPosition(Line, 1);
        sleep(500);
        assertEquals(editorPane.contains(ELIF_BLOCK), false);
        Action a = new Action("Source|Preprocessor Blocks|Add Elif Block Section", null);
        a.perform();
        sleep(1000);
        editorPane.pushKey(KeyEvent.VK_ENTER);
        assertEquals(editorPane.contains(ELIF_BLOCK), true);
    }
    
}


































