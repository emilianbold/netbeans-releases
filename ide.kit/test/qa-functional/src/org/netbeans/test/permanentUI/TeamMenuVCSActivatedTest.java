/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.test.permanentUI;

import java.io.*;
import java.util.ArrayList;
import javax.swing.MenuElement;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbModuleSuite;
import static org.netbeans.junit.NbTestCase.assertFile;
import org.netbeans.test.permanentUI.utils.NbMenuItem;
import org.netbeans.test.permanentUI.utils.Utilities;
import org.netbeans.test.permanentUI.utils.MenuChecker;
import org.netbeans.test.permanentUI.utils.ProjectContext;
import org.openide.util.Exceptions;

/**
 *
 * @author Lukas Hasik, Jan Peska
 */
public class TeamMenuVCSActivatedTest extends JellyTestCase {

    //context says what kind of project is open
    public static ProjectContext context;
    private Node projectRootNode;
    private static boolean init = true;
    // array of tests to be executed
    public static final String[] TESTS = new String[]{
        "testTeamMenu",
        "testTeam_DiffSubMenu",
        "testTeam_IgnoreSubMenu",
        "testTeam_PatchesSubMenu",
        "testTeam_BranchTagSubMenu",
        "testTeam_QueuesSubMenu",
        "testTeam_RemoteSubMenu",
        "testTeam_RecoverSubMenu",
        "testTeam_OtherVCSSubMenu",
        "testTeam_HistorySubMenu"
    };

    /**
     * Need to be defined because of JUnit
     */
    public TeamMenuVCSActivatedTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(TeamMenuVCSActivatedTest.class).clusters(".*").enableModules(".*");
        conf.addTest(TESTS);
        //conf = conf.addTest("testView_CodeFoldsSubMenu");
        return conf.suite();
    }

    /**
     * Setup called before every test case.
     */
    @Override
    public void setUp() {
        System.out.println("########  " + " CONTEXT -> " + ProjectContext.VERSIONING_ACTIVATED.toString() + " - " + getName() + "  #######");
        try {
            clearWorkDir();
            getWorkDir();
            if (isInit()) {
                setInit(false);
                initSources();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void initSources() {
        String newProject = "SampleProject";
        setContext(); // must be called because of correct golden file name which has contained context in suffix.
        assertEquals("VCS activation of: \"" + newProject + "\" FAILURED",true, openVersioningJavaEEProject(newProject));
        
        
        
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    /**
     * Tear down called after every test case.
     */
    @Override
    public void tearDown() {
//        projectRootNode.callPopup().showMenuItem("Delete").push();
//        new WizardOperator("Delete Project").yes();
    }

    public void testTeamMenu() {
        oneMenuTest("Team");
    }

    public void testTeam_DiffSubMenu() {
        oneSubMenuTest("Team|Diff", false, false);
    }

    public void testTeam_IgnoreSubMenu() {
        oneSubMenuTest("Team|Ignore", false, false);
    }

    public void testTeam_PatchesSubMenu() {
        oneSubMenuTest("Team|Patches", false, false);
    }

    public void testTeam_BranchTagSubMenu() {
        oneSubMenuTest("Team|Branch/Tag", false, false);
    }

    public void testTeam_QueuesSubMenu() {
        oneSubMenuTest("Team|Queues", false, false);
    }

    public void testTeam_RemoteSubMenu() {
        oneSubMenuTest("Team|Remote", false, true);
    }

    public void testTeam_RecoverSubMenu() {
        oneSubMenuTest("Team|Recover", false, true);
    }

    public void testTeam_OtherVCSSubMenu() {
        oneSubMenuTest("Team|Other VCS", false, false);
    }

    public void testTeam_HistorySubMenu() {
        oneSubMenuTest("Team|History", true, false);
    }

    //=============================oneMenuTests=================================
    /**
     * @param menuName to be tested
     * @return difference between menuName and golden file with the same name
     */
    void oneMenuTest(String menuName) {
        oneMenuTest(menuName, getMainMenuGoldenFile(menuName, context));
    }

    /**
     * @param menuName to be tested
     * @param goldenFileName to be tested
     * @return difference between menuName and goldenFileName
     *
     * You shouldn't call directly this method.
     */
    private void oneMenuTest(String menuName, String goldenFileName) throws IllegalArgumentException {
        NbMenuItem testedMenu = Utilities.readMenuStructureFromFile(goldenFileName);
        assertNotNull("Nothing read from " + goldenFileName, testedMenu); //was the file read correctly?

        PrintStream ideFileStream = null;
        PrintStream goldenFileStream = null;
        final String pathToIdeLogFile = getLogFile("_ide.txt");
        final String pathToGoldenLogFile = getLogFile("_golden.txt");
        final String pathToDiffLogFile = getLogFile("_diff.txt");

        //filtering separators out from sub-menu
        testedMenu.setSubmenu(removeSeparators(testedMenu));

        try {
            ideFileStream = new PrintStream(pathToIdeLogFile);
            goldenFileStream = new PrintStream(pathToGoldenLogFile);

            Utilities.printMenuStructure(goldenFileStream, testedMenu, "   ", 1);
            captureScreen();

            NbMenuItem menuItem = getMainMenuItem(menuName);
            Utilities.printMenuStructure(ideFileStream, menuItem, "   ", 1);

            assertNotNull("Cannot find menu " + menuName, menuItem);//is there such menu?

            Manager.getSystemDiff().diff(pathToIdeLogFile, pathToGoldenLogFile, pathToDiffLogFile);
            String message = Utilities.readFileToString(pathToDiffLogFile);
            assertFile(message, pathToIdeLogFile, pathToGoldenLogFile, pathToDiffLogFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            ideFileStream.close();
            goldenFileStream.close();
        }
    }

    //===========================oneSubMenuTests================================
    /**
     * Tests submenu items including mnemonics.
     *
     * @param submenuPath Menu name e.g. Window|Projects.
     * @param context context e.g. Java, Php, none
     * @param preInitSubMenu when sub menu doesn't pop up in time, you can try
     * @param projectName has to be set, when there is project name in menu
     * structure. to pre-initialize the sub menu. TRUE = pre-init. FALSE by
     * default. The item HAS TO BE JAVAX.SWING item !!!
     * @param projectNameAppearInMenuItems if name of project appear in menu items
     * you can enable it, but the name hast to be set correctly in following main method.
     */
    void oneSubMenuTest(String submenuPath, boolean preInitSubMenu, boolean projectNameAppearInMenuItems) {
        String fileName = submenuPath.replace('|', '-').replace(' ', '_').replace("/", "#");
        oneSubMenuTest(submenuPath, getMainMenuGoldenFile(fileName, context), preInitSubMenu, projectNameAppearInMenuItems);
    }

    /**
     * Tests submenu items including mnemonics.
     *
     * @param submenuName to be tested
     * @param mainmenuName to be tested
     * @param goldenFileName to be tested
     * @return difference between submenuName and goldenFileName
     */
    private void oneSubMenuTest(String submenuPath, String goldenFileName, boolean preInitSubMenu, boolean projectNameAppearInMenuItems) throws IllegalArgumentException {
        NbMenuItem testedSubMenuItem;
        if (projectNameAppearInMenuItems) {
            testedSubMenuItem = Utilities.readSubmenuStructureFromFile(goldenFileName);
        } else {
            testedSubMenuItem = Utilities.readSubmenuStructureFromFile(goldenFileName, "core-main");
        }

        assertNotNull("Nothing read from " + goldenFileName, testedSubMenuItem); //was the file read correctly?

        // when sub-menu has time out exception problems. It can Helps.
        if (preInitSubMenu) {
            String firstSubMenuItem = testedSubMenuItem.getSubmenu().get(0).getName();
            MainWindowOperator.getDefault().menuBar().showMenuItem(submenuPath + "|" + firstSubMenuItem, new Operator.DefaultStringComparator(true, true));
        }

        PrintStream ideFileStream = null;
        PrintStream goldenFileStream = null;
        final String pathToIdeLogFile = getLogFile("_ide.txt");
        final String pathToGoldenLogFile = getLogFile("_golden.txt");
        final String pathToDiffLogFile = getLogFile("_diff.txt");

        //filtering separators out from sub-menu
        testedSubMenuItem.setSubmenu(removeSeparators(testedSubMenuItem));

        try {
            ideFileStream = new PrintStream(pathToIdeLogFile);
            goldenFileStream = new PrintStream(pathToGoldenLogFile);

            Utilities.printMenuStructure(goldenFileStream, testedSubMenuItem, "   ", 2);
            captureScreen();

            //TEST 1
            String submenuItems[] = submenuPath.split("\\|");
            assertTrue("submenuPath must be >= 2. - " + submenuPath, submenuItems.length >= 2); //check the size
            //TEST 2
            NbMenuItem mainM = getMainMenuItem(submenuItems[0]);
            NbMenuItem submenuItem = Utilities.getMenuByName(submenuItems[submenuItems.length - 1], mainM);
            assertNotNull("Cannot find submenu " + submenuPath, submenuItem);//is there such submenu?
            //remove the mnemonic of the submenu item because it is not in the perm ui spec too
            submenuItem.setMnemo((char) 0);

            Utilities.printMenuStructure(ideFileStream, submenuItem, "   ", 1);

            //TEST - menu structure
            Manager.getSystemDiff().diff(pathToIdeLogFile, pathToGoldenLogFile, pathToDiffLogFile);
            String message = Utilities.readFileToString(pathToDiffLogFile);
            assertFile(message, pathToGoldenLogFile, pathToIdeLogFile, pathToDiffLogFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            ideFileStream.close();
            goldenFileStream.close();
        }
    }

    //===========================Other Methods==================================
    /**
     * constructs the relative path to the golden file with main menu permanent
     * UI spec
     *
     * @param menuName
     * @return
     */
    private String getMainMenuGoldenFile(String menuName, ProjectContext context) {
        String dataDir = "";
        try {
            dataDir = getDataDir().getCanonicalPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return dataDir + File.separator + "permanentUI" + File.separator + "mainmenu" + File.separator + menuName + context.getPathSuffix() + ".txt";
    }

    /**
     * Press menu item.
     *
     * @param mainMenuItem the item.
     * @return Operator.
     */
    public JMenuBarOperator pushMainMenuItem(String mainMenuItem) {
        ///open menu to let it create sucesfully
        JMenuBarOperator mainmenuOp = MainWindowOperator.getDefault().menuBar();
        ///use string comparator with exact matching
        mainmenuOp.pushMenu(mainMenuItem, new DefaultStringComparator(true, false));

        return mainmenuOp;
    }

    /**
     * Construct path to menu item.
     *
     * @param mainMenuItem item in menu.
     * @return path.
     */
    private NbMenuItem getMainMenuItem(String mainMenuItem) {

        JMenuBarOperator mainmenuOp = pushMainMenuItem(mainMenuItem);
        //parse all the menu elements
        int position = MenuChecker.getElementPosition(mainMenuItem, mainmenuOp.getSubElements());
        MenuElement theMenuElement = mainmenuOp.getSubElements()[position];
        NbMenuItem theMenu = new NbMenuItem((JMenuItem) theMenuElement);
        theMenu.setSubmenu(MenuChecker.getMenuArrayList(mainmenuOp.getMenu(position)));

        return theMenu;
    }

    /**
     * Take a screen shot.
     */
    private void captureScreen() {
        if (true) {
            try {
                String captureFile = getWorkDir().getAbsolutePath() + File.separator + "screen.png";
                PNGEncoder.captureScreen(captureFile, PNGEncoder.COLOR_MODE);
            } catch (Exception ex) {
                ex.printStackTrace(getLog());
            }
        }
    }

    private ArrayList<NbMenuItem> removeSeparators(NbMenuItem item) {
        return Utilities.filterOutSeparators(item.getSubmenu());
    }

    /**
     * Build path to log file.
     *
     * @param fileType IDE or GOLDEN
     * @return path to file.
     */
    private String getLogFile(String fileType) {
        return getWorkDirPath() + File.separator + getName() + fileType;
    }

    public void setContext() {
        context = ProjectContext.VERSIONING_ACTIVATED;
    }

    /**
     * Create new Java EE project in default path.
     *
     * @param projectName Name of project.
     * @return name of currently created project.
     */
    public boolean openVersioningJavaEEProject(String projectName) {
        try {
            openDataProjects(projectName);
            waitScanFinished();
            new ProjectsTabOperator().getProjectRootNode(projectName).select();
            if (!isVersioningProject(projectName)) {
                versioningActivation(projectName);
            }
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    /**
     * Create repository (Mercurial) for given project.
     *
     * @param projectName
     */
    public void versioningActivation(String projectName) {
        ProjectRootNode pto = new ProjectsTabOperator().getProjectRootNode(projectName);
        projectRootNode = new Node(pto.tree(), projectName);
        projectRootNode.callPopup().showMenuItem("Versioning|Initialize Mercurial Repository...").push();
        WizardOperator wo2 = new WizardOperator("Initialize a Mercurial Repository");
        wo2.ok();
    }

    /**
     * Check if project has local repository activated.
     *
     * @param projectName
     * @return true - VCS activated, else false
     */
    public boolean isVersioningProject(String projectName) {
        int menuSize = getMainMenuItem("Team").getSubmenu().size();
        return menuSize > 8;
    }

// ======================Usefull, finally unused methods========================
    /**
     * Check if following project exists. If it does, It is tested for VCS
     * activation, which is required. Otherwise, old project is deleted. If old
     * project is unsuccessfully deleted, new project is renamed.
     *
     * @param projectName - name of new project.
     * @param pathToSave - path to folder with projects.
     * @return Name of project, which is not occupied.
     */
//    public String findUnusedProjectName(String projectNameApearedInMenuItems, String pathToSave) {
//        int projectSuffix = 2;
//        File oldProject;
//        boolean isDeleted = false;
//
//        while (true) {
//            if ((oldProject = new File(pathToSave + File.separator + projectNameApearedInMenuItems + projectSuffix)).exists()) {
//                isDeleted = oldProject.delete();
//            } else {
//                return projectNameApearedInMenuItems + projectSuffix;
//            }
//            if (!isDeleted) {
//                projectSuffix++;
//            } else {
//                return projectNameApearedInMenuItems + projectSuffix;
//            }
//        }
//
//    }
    /**
     * Check, if location of project already exists.
     *
     * @param projectName Name of project.
     * @param pathToSave Path to save. Has to be set.
     * @return True if exists, otherwise False.
     */
//    public boolean projectExistence(String projectNameApearedInMenuItems, String pathToSave) {
//        String pathAndNameOfProject = pathToSave + File.separator + projectNameApearedInMenuItems;
//        return (new File(pathAndNameOfProject).exists());
//    }
}
