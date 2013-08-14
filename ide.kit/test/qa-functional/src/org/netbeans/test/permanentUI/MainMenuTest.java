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
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.permanentUI.utils.NbMenuItem;
import org.netbeans.test.permanentUI.utils.Utilities;
import org.netbeans.test.permanentUI.utils.MenuChecker;
import org.netbeans.test.permanentUI.utils.ProjectContext;

/**
 *
 * @author Lukas Hasik, Jan Peska
 */
public class MainMenuTest extends JellyTestCase {

    //context says what kind of project is open
    public static ProjectContext context = ProjectContext.NONE;
    private static final boolean screen = false;
    // array of tests to be executed
    public static final String[] TESTS = new String[]{
        // here you test main-menu bar
        "testFileMenu",
        "testEditMenu",
        "testViewMenu",
        "testNavigateMenu",
        "testSourceMenu",
        "testRefactorMenu",
        "testDebugMenu",
        "testRunMenu",
        "testHelpMenu",
        "testToolsMenu",
        "testTeamMenu",
        "testWindowMenu",
        "testProfileMenu",
        // here you test sub-menus in each menu.
        "testFile_ProjectGroupSubMenu",
        "testFile_ImportProjectSubMenu",
        "testFile_ExportProjectSubMenu",
        "testNavigate_InspectSubMenu",
        "testView_CodeFoldsSubMenu",
        "testView_ToolbarsSubMenu",
        "testProfile_AdvancedCommandsSubMenu",
        "testDebug_StackSubMenu",
        "testSource_PreprocessorBlocksSubMenu",
        "testTools_InternationalizationSubMenu",
        "testTools_PaletteSubMenu",
        "testTeam_GitSubMenu",
        "testTeam_MercurialSubMenu",
        "testTeam_SubversionSubMenu",
        "testTeam_HistorySubMenu",
        "testWindow_DebuggingSubMenu",
        "testWindow_IDE_ToolsSubMenu",
        "testWindow_Configure_WindowSubMenu",
        "testWindow_ProfilingSubMenu"
    };

    /**
     * Need to be defined because of JUnit
     */
    public MainMenuTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(MainMenuTest.class).clusters(".*").enableModules(".*");
        conf.addTest(TESTS);
        //conf = conf.addTest("testView_CodeFoldsSubMenu");
        return conf.suite();
    }

    /**
     * Setup called before every test case.
     */
    @Override
    public void setUp() {
        System.out.println("########  " + ProjectContext.NONE.toString() + " CONTEXT - " + getName() + "  #######");
        try {
            clearWorkDir();
            getWorkDir();
            if (isInit()) {
                openDataProjects("SampleProject");
                setInit(false);
                initSources();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void initSources() {
        setContext();
    }

    public boolean isInit() {
        return false;
    }

    public void setInit(boolean init) {
    }

    /**
     * Tear down called after every test case.
     */
    @Override
    public void tearDown() {
    }

    public void testFileMenu() {
        oneMenuTest("File");
    }

    public void testEditMenu() {
        oneMenuTest("Edit");
    }

    public void testViewMenu() {
        oneMenuTest("View");
    }

    public void testNavigateMenu() {
        oneMenuTest("Navigate");
    }

    public void testSourceMenu() {
        oneMenuTest("Source");
    }

    public void testRefactorMenu() {
        oneMenuTest("Refactor");
    }

    public void testRunMenu() {
        oneMenuTest("Run");
    }

    public void testDebugMenu() {
        oneMenuTest("Debug");
    }

    public void testHelpMenu() {
        oneMenuTest("Help");
    }

    public void testToolsMenu() {
        oneMenuTest("Tools");
    }

    public void testTeamMenu() {
        oneMenuTest("Team");
    }

    public void testWindowMenu() {
        oneMenuTest("Window");
    }

    public void testProfileMenu() {
        oneMenuTest("Profile");
    }

    public void testFile_ProjectGroupSubMenu() {
        oneSubMenuTest("File|Project Group", false);
    }

    public void testFile_ImportProjectSubMenu() {
        oneSubMenuTest("File|Import Project", true);
    }

    public void testFile_ExportProjectSubMenu() {
        oneSubMenuTest("File|Export Project", false);//here
    }

    public void testNavigate_InspectSubMenu() {
        oneSubMenuTest("Navigate|Inspect", false);
    }

    public void testView_CodeFoldsSubMenu() {
        //Submenu disabled - do nothing
    }

    public void testView_ToolbarsSubMenu() {
        oneSubMenuTest("View|Toolbars", false);
    }

    public void testProfile_AdvancedCommandsSubMenu() {
        oneSubMenuTest("Profile|Advanced Commands", true);
    }

    public void testDebug_StackSubMenu() {
        oneSubMenuTest("Debug|Stack", false);
    }

    public void testSource_PreprocessorBlocksSubMenu() {
        //Submenu disabled - do nothing
    }

    public void testTools_InternationalizationSubMenu() {
        oneSubMenuTest("Tools|Internationalization", false);//here
    }

    public void testTools_PaletteSubMenu() {
        oneSubMenuTest("Tools|Palette", false);
    }

    public void testTeam_GitSubMenu() {
        oneSubMenuTest("Team|Git", true);//here
    }

    public void testTeam_MercurialSubMenu() {
        oneSubMenuTest("Team|Mercurial", true);
    }

    public void testTeam_SubversionSubMenu() {
        //Submenu initializing - do nothing
    }

    public void testTeam_HistorySubMenu() {
        oneSubMenuTest("Team|History", true);
    }

    public void testWindow_DebuggingSubMenu() {
        oneSubMenuTest("Window|Debugging", false);
    }

    public void testWindow_IDE_ToolsSubMenu() {
        oneSubMenuTest("Window|IDE Tools", false);
    }

    public void testWindow_Configure_WindowSubMenu() {
        oneSubMenuTest("Window|Configure Window", false);
    }

    public void testWindow_ProfilingSubMenu() {
        oneSubMenuTest("Window|Profiling", false);
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
     * to pre-initialize the sub menu. TRUE = pre-init. FALSE by default. The
     * item HAS TO BE JAVAX.SWING item !!!
     */
    void oneSubMenuTest(String submenuPath, boolean preInitSubMenu) {
        String fileName = submenuPath.replace('|', '-').replace(' ', '_');
        oneSubMenuTest(submenuPath, getMainMenuGoldenFile(fileName, context), preInitSubMenu);
    }

    /**
     * Tests submenu items including mnemonics.
     *
     * @param submenuName to be tested
     * @param mainmenuName to be tested
     * @param goldenFileName to be tested
     * @return difference between submenuName and goldenFileName
     */
    private void oneSubMenuTest(String submenuPath, String goldenFileName, boolean preInitSubMenu) throws IllegalArgumentException {
        NbMenuItem testedSubMenuItem = Utilities.readSubmenuStructureFromFile(goldenFileName);
        assertNotNull("Nothing read from " + goldenFileName, testedSubMenuItem); //was the file read correctly?

        // when sub-menu has time out exception problems. It can Helps.
        if (preInitSubMenu) {
            String firstSubMenuItem = testedSubMenuItem.getSubmenu().get(0).getName();
            MainWindowOperator.getDefault().menuBar().showMenuItem(submenuPath + "|" + firstSubMenuItem);
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

            Utilities.printMenuStructure(goldenFileStream, testedSubMenuItem, "   ", 1);
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
        if (screen) {
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
        MainMenuTest.context = ProjectContext.NONE;
    }
}
