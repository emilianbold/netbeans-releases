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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.junit.Manager;
import org.netbeans.modules.mobility.cldcplatform.startup.PostInstallJ2meAction;
import org.openide.filesystems.FileUtil;

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

    private static void createPlatform() {
        //See original version of this in TestUtil.java in unit tests
        String wtkZipPath = null;
        String cp = System.getProperty ("java.class.path");
        String[] x = cp.split(File.pathSeparator);
        String oneModule = x[0];
        int ix = oneModule.indexOf ("nbbuild" + File.separatorChar + "netbeans");
        String srcPath = oneModule.substring(0, ix);
        File userBuildProps = new File (new File (srcPath),
                File.separator + "nbbuild" +
                File.separator + "user.build.properties");
        userBuildProps = FileUtil.normalizeFile(userBuildProps);
        if (userBuildProps.exists()) {
            InputStream in = null;
            try {
                Properties p = new Properties();
                in = new BufferedInputStream(new FileInputStream(userBuildProps));
                try {
                    p.load(in);
                } finally {
                    in.close();
                }
                wtkZipPath = p.getProperty("wtk.zip");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    throw new RuntimeException (ex);
                }
            }
        }

        if (wtkZipPath == null) {
            return;
        }
        File wtkZip = new File (wtkZipPath);
        File parent = wtkZip.getAbsoluteFile();
        String osarch = System.getProperty("os.name", null);
        if (!wtkZip.exists() || !wtkZip.isFile()) {
            if (parent.isDirectory() && parent.exists()) {
                String ossuf = null;
                NbTestCase.assertNotNull(osarch);
                for (int i=20; i < 40; i++) {
                    String ver = Integer.toString(i);
                    if (osarch.toLowerCase().indexOf("windows") != -1) {
                        ossuf = ver + "_win";
                    } else if (osarch.toLowerCase().indexOf("linux") != -1) {
                        ossuf = ver + "_linux";
                    } else if (osarch.toLowerCase().indexOf("sunos") != -1) {
                        /* For Solaris we have just wtk21 */
                        ossuf = ver + "_sunos";
                    }
                    if (ossuf != null) {
                        wtkZip = new File (parent, "wtk_" + ossuf);
                        System.err.println("Try " + wtkZip.getPath());
                        if (wtkZip.isFile() && wtkZip.exists()) {
                            break;
                        } else {
                            wtkZip = null;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        //For new emulator install test
        System.setProperty ("wtk.zip", wtkZip.getPath());
        String destPath=Manager.getWorkDirPath();

        System.out.println("Unzipping wireless toolkit into " + destPath);
        ZipFile zip = null;
        String root = null;
        try {
            zip = new ZipFile(wtkZip);
            Enumeration files = zip.entries();
            while (files.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) files.nextElement();
                if (entry.isDirectory()){
                    if (root == null || entry.getName().length() < root.length()) {
                        root = entry.getName();
                    }
                    new File(destPath, entry.getName()).mkdirs();
                } else {
                    /* Extract only if not already present */
                    File test=new File(destPath+"/"+entry.getName());
                    if (!(test.isFile() && test.length() == entry.getSize()))
                        copy(zip.getInputStream(entry), entry.getName(), new File(destPath));
                }
            }
        } catch (IOException ex) {
            throw new Error (ex);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    throw new RuntimeException (e);
                }
            }
        }

        String home = destPath + File.separatorChar + root + File.separatorChar;
        try {
            PostInstallJ2meAction.installAction(home);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("platform.home for unit tests set to " + home);
        System.setProperty ("platform.home", home);
        System.setProperty("platform.type","UEI-1.0");
    }

    private static void copy(InputStream input, String file, File target) throws IOException {
        if (input == null  ||  file == null  ||  "".equals(file)) //NOI18N
            return;
        File output = new File(target, file);
        output.getParentFile().mkdirs();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
            FileUtil.copy(input, fos);
        } finally {
            if (input != null) try { input.close(); } catch (IOException e) {}
            if (fos != null) try { fos.close(); } catch (IOException e) {}
        }
    }

    private String initWtk() {
        //Try new way, with wtk location specified in nbbuild/user.build.properties
        createPlatform();
        String wtkZip = System.getProperty ("wtk.zip");
        if (wtkZip == null) {
            String wtkPath = System.getProperty("wtk.dir");
            //perhaps automated tests still need this?
            String osarch = System.getProperty("os.name", null);
            String ossuf = null;
            if (osarch.toLowerCase().indexOf("windows") != -1) {
                ossuf = "22_win";
            } else if (osarch.toLowerCase().indexOf("linux") != -1) {
                ossuf = "22_linux";
            }

            wtkZip = wtkPath + File.separator + "wtk" + ossuf + ".zip";
        }
        return wtkZip;
    }

    /**
     * test of adding ME emulator
     */
    public void testAddEmulator() {
        String wtkZip = initWtk();

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
        initWtk();
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
        initWtk();
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory("Java ME"); // XXX use Bundle.getString instead
        wizard.selectProject("Mobile Application");
        wizard.next();


        NewJavaProjectNameLocationStepOperator step = new NewJavaProjectNameLocationStepOperator();
        step.txtProjectLocation().setText(getWorkDirPath());
        step.txtProjectName().setText(PROJECT_TO_BE_CREATED);//NOI18N
//        String projectLocation = step.txtProjectFolder().getText();
//        sleep(1000);
        step.finish();

        new ProjectsTabOperator().getProjectRootNode(PROJECT_TO_BE_CREATED);

    }
}
