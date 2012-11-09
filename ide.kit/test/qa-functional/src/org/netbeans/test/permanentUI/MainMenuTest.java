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
import org.openide.util.Exceptions;

/**
 *
 * @author Lukas Hasik, Jan Peska
 */
public class MainMenuTest extends JellyTestCase {

    // array of tests to be executed
    public static final String[] ALL_TESTS = new String[]{
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
        "testWindow_DebuggingSubMenu",
        "testWindow_NavigatingSubMenu",
        "testWindow_OtherSubMenu",
        "testWindow_OutputSubMenu",
        "testWindow_ProfilingSubMenu",
        "testWindow_VersioningSubMenu",
        
        "testMnemonicsCollision"};

    protected boolean logging = false;
    private static final boolean screen = false;

    /** Need to be defined because of JUnit */
    public MainMenuTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(MainMenuTest.class).clusters(".*").enableModules(".*");
        conf.addTest(ALL_TESTS);
        return conf.suite();
    }

    /** Setup called before every test case. */
    @Override
    public void setUp() {
        System.out.println("########  " + getProjectContext().toString() + " CONTEXT - " + getName() + "  #######");
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
        //no sorces needed - NONE context
    }

    public boolean isInit() {
        return false;
    }

    public void setInit(boolean init) {
    }

    /** Tear down called after every test case. */
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
        oneSubMenuTest("File|Project Group");
    }

    public void testFile_ImportProjectSubMenu() {
        oneSubMenuTest("File|Import Project");
    }

    public void testFile_ExportProjectSubMenu() {
        oneSubMenuTest("File|Export Project");
    }

    public void testNavigate_InspectSubMenu() {
        oneSubMenuTest("Navigate|Inspect");
    }

    public void testView_CodeFoldsSubMenu() {
        //Submenu disabled - do nothing
    }

    public void testView_ToolbarsSubMenu() {
        oneSubMenuTest("View|Toolbars");
    }

    public void testProfile_AdvancedCommandsSubMenu() {
        oneSubMenuTest("Profile|Advanced Commands");
    }

    public void testDebug_StackSubMenu() {
        oneSubMenuTest("Debug|Stack");
    }

    public void testSource_PreprocessorBlocksSubMenu() {
        //Submenu disabled - do nothing
    }

    public void testTools_InternationalizationSubMenu() {
        oneSubMenuTest("Tools|Internationalization");
    }

    public void testTools_PaletteSubMenu() {
        oneSubMenuTest("Tools|Palette");
    }

    public void testTeam_GitSubMenu() {
        oneSubMenuTest("Team|Git");
    }

    public void testTeam_MercurialSubMenu() {
        oneSubMenuTest("Team|Mercurial");
    }

    public void testTeam_SubversionSubMenu() {
        //Submenu initializing - do nothing
    }

    public void testWindow_DebuggingSubMenu() {
        oneSubMenuTest("Window|Debugging");
    }

    public void testWindow_NavigatingSubMenu() {
        oneSubMenuTest("Window|Navigating");
    }

    public void testWindow_OtherSubMenu() {
        oneSubMenuTest("Window|Other");
    }

    public void testWindow_OutputSubMenu() {
        oneSubMenuTest("Window|Output");
    }

    public void testWindow_ProfilingSubMenu() {
        oneSubMenuTest("Window|Profiling");
    }

    public void testWindow_VersioningSubMenu() {
        oneSubMenuTest("Window|Versioning");
    }

    ProjectContext getProjectContext(){
        return ProjectContext.NONE;
    }

    /**
     * @param menuName to be tested
     * @return difference between menuName and golden file with the same name
     */
    void oneMenuTest(String menuName) {
        oneMenuTest(menuName, ProjectContext.NONE);
    }

    void oneMenuTest(String menuName, ProjectContext context) {
        oneMenuTest(menuName, getMainMenuGoldenFile(menuName, context));
    }

    /**
     * @param menuName to be tested
     * @param goldenFileName to be tested
     * @return difference between menuName and goldenFileName
     */
    private void oneMenuTest(String menuName, String goldenFileName) throws IllegalArgumentException {
        PrintStream ideFile = null;
        PrintStream goldenFile = null;
        final String menuItemsLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";
        final String permuiLogsFile = getWorkDirPath() + File.separator + getName() + "_golden.txt";
        final String diffFile = getWorkDirPath() + File.separator + getName() + ".diff";
        try {
            NbMenuItem permanentMenu = Utilities.readMenuStructureFromFile(goldenFileName);
            assertNotNull("Nothing read from " + goldenFileName, permanentMenu); //was the file read correctly?
            ArrayList<NbMenuItem> newSubmenu = Utilities.filterOutSeparators(permanentMenu.getSubmenu());//TODO: fix the getMainMenuItem(.) to return even separators
            permanentMenu.setSubmenu(newSubmenu);//TODO: fix the getMainMenuItem(.) to return even separators
            goldenFile = new PrintStream(permuiLogsFile);
            Utilities.printMenuStructure(goldenFile, permanentMenu, "---", 1);

            NbMenuItem menuItem = getMainMenuItem(menuName);
            captureScreen();
            ideFile = new PrintStream(menuItemsLogFile);
            Utilities.printMenuStructure(ideFile, menuItem, "---", 1);

            Manager.getSystemDiff().diff(menuItemsLogFile, permuiLogsFile, diffFile);
            //assert
            String message = Utilities.readFileToString(diffFile);

            assertNotNull("Cannot find menu " + menuName, menuItem);//is there such menu?

            assertFile(message, menuItemsLogFile, permuiLogsFile, diffFile);


//            assertFile(Utilities.compareNbMenuItems(menuItem, permanentMenu, 1),
                    //"[+]missing in IDE, [-] missing in spec\n"+Utilities.readFileToString(diffFile),
//                    permuiLogsFile, menuItemsLogFile, diffFile);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            ideFile.close();
            goldenFile.close();
        }
    }

    /**
     *
     * @param menuName to be tested
     * @return difference between menuName and golden file with the same name
     */
    void oneSubMenuTest(String submenuPath, boolean w84init) {
        oneSubMenuTest(submenuPath, w84init, ProjectContext.NONE);
    }
    
    void oneSubMenuTest(String submenuPath, ProjectContext context) {
        oneSubMenuTest(submenuPath, false, context);
    }

    void oneSubMenuTest(String submenuPath) {
        oneSubMenuTest(submenuPath, false, ProjectContext.NONE);
    }

    void oneSubMenuTest(String submenuPath, boolean w84init, ProjectContext context) {
        String fileName = submenuPath.replace('|', '-').replace(' ', '_');
        oneSubMenuTest(submenuPath, getMainMenuGoldenFile(fileName, context), w84init);
    }

    /**
     *
     * @param submenuName to be tested
     * @param mainmenuName to be tested
     * @param goldenFileName to be tested
     * @return difference between submenuName and goldenFileName
     */
    private void oneSubMenuTest(String submenuPath, String goldenFileName, boolean w84init) throws IllegalArgumentException {
        NbMenuItem permanentMenu = Utilities.readSubmenuStructureFromFile(goldenFileName);
        assertNotNull("Nothing read from " + goldenFileName, permanentMenu); //was the file read correctly?
        PrintStream ideFile = null;
        PrintStream goldenFile = null;
        final String menuItemsLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";
        final String permuiLogsFile = getWorkDirPath() + File.separator + getName() + "_golden.txt";
        final String diffFile = getWorkDirPath() + File.separator + getName() + ".diff";
        ArrayList<NbMenuItem> newSubmenu = Utilities.filterOutSeparators(permanentMenu.getSubmenu()); //TODO: fix the getMainMenuItem(.) to return even separators
        permanentMenu.setSubmenu(newSubmenu); //TODO: remove when getMainMenuItem(.) fixed
        try {
            goldenFile = new PrintStream(permuiLogsFile);

            Utilities.printMenuStructure(goldenFile, permanentMenu, "   ", 1);

            ideFile = new PrintStream(menuItemsLogFile);
            pushMainMenuItem(submenuPath);
            captureScreen();
            if(w84init) {
                waitForInit();
                pushMainMenuItem(submenuPath);
            }
            String submenuItems[] = submenuPath.split("\\|");
            assertTrue("submenuPath must be >= 2. - " + submenuPath, submenuItems.length >= 2); //check the size
            NbMenuItem mainM = getMainMenuItem(submenuItems[0]);
            NbMenuItem submenuItem = Utilities.getMenuByName(submenuItems[submenuItems.length-1], mainM);
            assertNotNull("Cannot find submenu " + submenuPath, submenuItem);//is there such submenu?
            submenuItem.setMnemo((char)0); //remove the mnemonic of the submenu item because it is not in the perm ui spec too
            Utilities.printMenuStructure(ideFile, submenuItem, "   ", 1);

            if (logging) {
                System.out.println("GOLDEN FILE:");
                Utilities.printMenuStructure(System.out, permanentMenu, "  ", 100);
                System.out.println("-------------MENU-----------------");
                Utilities.printMenuStructure(System.out, mainM, "---", 100);
                System.out.println("IDE MENU:");
                Utilities.printMenuStructure(System.out, submenuItem, "   ", 100);
                System.out.println("-------------SUBMENU-----------------");
                Utilities.printMenuStructure(System.out, submenuItem, "---", 100);
            }

            Manager.getSystemDiff().diff(menuItemsLogFile, permuiLogsFile, diffFile);
            //assert
            String message = Utilities.readFileToString(diffFile);

            assertFile(message, permuiLogsFile, menuItemsLogFile, diffFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void testMnemonicsCollision() {
        String collisions = MenuChecker.checkMnemonicCollision();
        assertFalse(collisions, collisions.length() > 0);
    }

    /**
     * constructs the relative path to the golden file with main menu permanent UI spec
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

    public JMenuBarOperator pushMainMenuItem(String mainMenuItem) {
        ///open menu to let it create sucesfully
        JMenuBarOperator mainmenuOp = MainWindowOperator.getDefault().menuBar();
        ///use string comparator with exact matching
        mainmenuOp.pushMenu(mainMenuItem, new DefaultStringComparator(true, false));

        return mainmenuOp;
    }

    private NbMenuItem getMainMenuItem(String mainMenuItem) {

        JMenuBarOperator mainmenuOp = pushMainMenuItem(mainMenuItem);
        //parse all the menu elements
        int position = MenuChecker.getElementPosition(mainMenuItem, mainmenuOp.getSubElements());
        MenuElement theMenuElement = mainmenuOp.getSubElements()[position];
        NbMenuItem theMenu = new NbMenuItem((JMenuItem) theMenuElement);
        theMenu.setSubmenu(MenuChecker.getMenuArrayList(mainmenuOp.getMenu(position)));

        return theMenu;
    }

    private void waitForInit() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void captureScreen() {
        if(screen) {
            try {
                String captureFile = getWorkDir().getAbsolutePath() + File.separator + "screen.png";
                PNGEncoder.captureScreen(captureFile, PNGEncoder.COLOR_MODE);
            } catch (Exception ex) {
                ex.printStackTrace(getLog());
            }
        }
    }
}
