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

import java.awt.Component;
import java.io.File;
import org.netbeans.core.windows.view.ui.slides.SlideBar;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JRadioButtonMenuItemOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.TopComponentOperator;

/**
 *
 * @author ivansidorkin@netbeans.org
 */
public class ValidationTest extends JellyTestCase {

    public static final String ITEM_VISUALMIDLET = Bundle.getStringTrimmed("org.netbeans.modules.vmd.midp.resources.Bundle", "Templates/MIDP/VisualMIDlet.java");
    public static final String ITEM_MIDLET = Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.ui.wizard.Bundle", "Templates/MIDP/Midlet.java");
    public static final String ITEM_MIDPCANVAS = Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.ui.wizard.Bundle", "Templates/MIDP/MIDPCanvas.java");
    public static final String CATEGORY_MIDP = Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.ui.wizard.Bundle", "Templates/MIDP");
    public static final String PROJECT_TO_BE_CREATED = "NewCreatedMobileProject";
    static final String[] tests = {
        "testAddEmulator",
        "testCreateMIDPApplication",
        "testCreateNewFiles"
    };

    public ValidationTest(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ValidationTest.class).addTest(tests).clusters(".*").enableModules(".*").gui(true));
    }

    /**
     * test of adding ME emulator
     */
    public void testAddEmulator() {
        String wtkPath = System.getProperty("wtk.dir");

        String wtkZip = "";

        String osarch = System.getProperty("os.name", null);
        String ossuf = null;
        if (osarch.toLowerCase().indexOf("windows") != -1) {
            ossuf = "22_win";
        } else if (osarch.toLowerCase().indexOf("linux") != -1) {
            ossuf = "22_linux";
        }

        wtkZip = wtkPath + File.separator + "wtk" + ossuf + ".zip";

        String unzipDir = System.getProperty("nbjunit.workdir") + File.separator + "wtkemulator";

        if (!Util.unzipFile(wtkZip, unzipDir)) {
            fail("Failed to unzip emulator");
        }

        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        JMenuBarOperator menubar = mainWindow.menuBar();

        menubar.pushMenuNoBlock("Tools|Java Platforms"); //TODO I18N

        DialogOperator jpm = new DialogOperator("Java Platform Manager"); //TODO I18N

        new JButtonOperator(jpm, "Add Platform...").pushNoBlock();

        DialogOperator ajp = new DialogOperator("Add Java Platform"); //TODO I18N

        new JRadioButtonOperator(ajp, "Custom Java ME MIDP Platform Emulator").clickMouse();

        (new JButtonOperator(ajp, "Next")).pushNoBlock();

        new JTextFieldOperator(ajp, 0).setText(unzipDir);

        (new JButtonOperator(ajp, "Next")).pushNoBlock();

        (new JButtonOperator(ajp, "Finish")).pushNoBlock();

        (new JButtonOperator(jpm, "Close")).push();


    }

    public String createNewFile(String category, String template, String name, String packageName) {
        NewFileWizardOperator newFile = NewFileWizardOperator.invoke();
        newFile.selectCategory(category);
        newFile.selectFileType(template);
        newFile.next();
        NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
        op.setObjectName(name); //TODO doesn't work with New > MIDP Canvas. It doesn;t change the name
        if (packageName != null) {
            op.setPackage(packageName);
        }
        String fileLocation = op.txtCreatedFile().getText();
        op.finish();
        return fileLocation;
    }

    public void testCreateNewFiles() {
        //select the project in project view
        new ProjectsTabOperator().getProjectRootNode(PROJECT_TO_BE_CREATED).select();
        //create all new files in the project
        createNewFile(CATEGORY_MIDP, ITEM_VISUALMIDLET, "NewVisualMidlet", "myPackage"); // NOI18N
        createNewFile(CATEGORY_MIDP, ITEM_MIDLET, "NewMIDlet", "myPackage"); // NOI18N
        createNewFile(CATEGORY_MIDP, ITEM_MIDPCANVAS, "MIDPCanvas", "myPackage"); // NOI18N


        //test that files are created and opened in editor
        new TopComponentOperator("NewVisualMidlet.java").close(); // NOI18N
        new EditorOperator("NewMIDlet.java").close(); // NOI18N
        new EditorOperator("MIDPCanvas.java").close();    // NOI18N

    }

    public void testCreateMIDPApplication() {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory("Java ME"); // XXX use Bundle.getString instead
        wizard.selectProject("Mobile Application");
        wizard.next();


        NewProjectNameLocationStepOperator step = new NewProjectNameLocationStepOperator();
        step.txtProjectLocation().setText(getWorkDirPath());
        step.txtProjectName().setText(PROJECT_TO_BE_CREATED);//NOI18N
//        String projectLocation = step.txtProjectFolder().getText();
//        sleep(1000);
        step.finish();

        new ProjectsTabOperator().getProjectRootNode(PROJECT_TO_BE_CREATED);

    }
}
