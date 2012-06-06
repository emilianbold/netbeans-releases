/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author ivansidorkin@netbeans.org
 * @author stezeb@netbeans.org
 */
public class ValidationTest extends JellyTestCase {

    public static final String MESDK_WIN_LOCATION = "C:\\space\\hudson\\mesdk";
    public static final String MESDK_WIN_VERSION = "3.0.5";
    
    public static final String MESDK_LINUX_LOCATION = "/space/hudson/mesdk";
    public static final String MESDK_LINUX_VERSION = "2.5.2";
    
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

    private void locateEmulator() {
        String sdkpath = null;
        String osarch = System.getProperty("os.name", null);
        if (osarch.toLowerCase().indexOf("windows") != -1) {
            sdkpath = MESDK_WIN_LOCATION;
        } else if (osarch.toLowerCase().indexOf("linux") != -1) {
            sdkpath = MESDK_LINUX_LOCATION;
        }
        System.setProperty("platform.home", sdkpath);
        //System.out.println("platform.home for tests set to " + sdkpath);
    }

    /**
     * Adding ME emulator.
     * NOTE: Previous version of this test used zipped WTK 2.2.
     */
    public void testAddEmulator() {
        locateEmulator();

        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        JMenuBarOperator menubar = mainWindow.menuBar();

        menubar.pushMenuNoBlock("Tools|Java Platforms"); //TODO I18N

        DialogOperator jpm = new DialogOperator("Java Platform Manager"); //TODO I18N

        new JButtonOperator(jpm, "Add Platform...").pushNoBlock();

        WizardOperator ajpw = new WizardOperator("Add Java Platform");
        ajpw.stepsWaitSelectedValue("Select platform type");
        //System.out.println("current step: " + ajpw.stepsGetSelectedIndex() + " - " + ajpw.stepsGetSelectedValue());

        new JRadioButtonOperator(ajpw, "Java ME MIDP Platform Emulator").clickMouse();

        ajpw.next();
        ajpw.stepsWaitSelectedValue("Platform Folders");
        //System.out.println("current step: " + ajpw.stepsGetSelectedIndex() + " - " + ajpw.stepsGetSelectedValue());
        
        new EventTool().waitNoEvent(2000);
        (new JButtonOperator(ajpw, "Find More Java ME Platform Folders...")).pushNoBlock(); //TODO I18N
        new EventTool().waitNoEvent(2000);
        
        DialogOperator cdtsfp = new DialogOperator("Choose directory to search for platforms"); //TODO I18N
        new JTextFieldOperator(cdtsfp, 0).setText(System.getProperty("platform.home"));
        (new JButtonOperator(cdtsfp, "Open")).pushNoBlock();
        cdtsfp.waitClosed();
        
        DialogOperator sfjmep = new DialogOperator("Searching for Java ME platforms"); //TODO I18N
        sfjmep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        sfjmep.waitClosed();

        ajpw.next();
        ajpw.stepsWaitSelectedValue("Detected Platforms");
        //System.out.println("current step: " + ajpw.stepsGetSelectedIndex() + " - " + ajpw.stepsGetSelectedValue());

        DialogOperator djmep = new DialogOperator("Detecting Java ME platforms"); //TODO I18N
        djmep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 200000);
        djmep.waitClosed();
        
        ajpw.finish();
        ajpw.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        ajpw.waitClosed();
        
        (new JButtonOperator(jpm, "Close")).push();
    }

    public String createNewFile(String category, String template, String name, String packageName) {
        new EventTool().waitNoEvent(3000);
        NewFileWizardOperator newFile = NewFileWizardOperator.invoke();
        newFile.selectCategory(category);
        newFile.selectFileType(template);
        newFile.next();
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
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
        createNewFile(CATEGORY_MIDP, ITEM_MIDLET, "NewMIDlet", "myPackage"); // NOI18N
        createNewFile(CATEGORY_MIDP, ITEM_MIDPCANVAS, "MIDPCanvas", "myPackage"); // NOI18N

        new EventTool().waitNoEvent(5000);
        
        /* unstable
        //test that files are created and opened in editor
        new EditorOperator("NewMIDlet.java").close(); // NOI18N
        new EventTool().waitNoEvent(2000);
        new EditorOperator("MIDPCanvas.java").close();    // NOI18N
        new EventTool().waitNoEvent(5000);
        */
    }

    public void testCreateMIDPApplication() {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory("Java ME"); // XXX use Bundle.getString instead
        wizard.selectProject("Mobile Application");
        wizard.next();


        NewJavaProjectNameLocationStepOperator step = new NewJavaProjectNameLocationStepOperator();
        step.txtProjectLocation().setText(getWorkDirPath());
        step.txtProjectName().setText(PROJECT_TO_BE_CREATED);//NOI18N
        step.finish();

        new EventTool().waitNoEvent(20000);
        
        new ProjectsTabOperator().getProjectRootNode(PROJECT_TO_BE_CREATED);

    }
}
