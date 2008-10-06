/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.test.permanentUI;

import java.io.*;
import java.util.ArrayList;
import javax.swing.MenuElement;
import javax.swing.JMenuItem;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import junit.textui.TestRunner;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.junit.NbModuleSuite;

import org.netbeans.test.permanentUI.utils.NbMenuItem;
import org.netbeans.test.permanentUI.utils.Utilities;
import org.netbeans.test.permanentUI.utils.MenuChecker;

/**
 *
 * @author Lukas Hasik
 */
public class MainMenuTest extends JellyTestCase {

    /** Need to be defined because of JUnit */
    public MainMenuTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
                MainMenuTest.class).clusters(".*").enableModules(".*");

          conf.addTest(
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
                  "testVersioningMenu",
                  "testWindowMenu",

                  "testFile_ProjectGroupSubMenu",
                  "testMnemonicsCollision",
                  "testNavigate_InspectSubMenu",
                  "testView_CodeFoldsSubMenu",
                  "testView_ToolbarsSubMenu",
                  "testProfile_AdvancedCommandsSubMenu",
                  "testProfile_ProfileOtherSubMenu",
                  "testRun_SetMainProjectSubMenu",
                  "testDebug_StackSubMenu",
                  "testSource_PreprocessorBlocksSubMenu",
                  "testTools_InternationalizationSubMenu",
                  "testTools_PaletteSubMenu",
                  "testVersioning_CVSSubMenu",
                  "testVersioning_CVS_BranchesSubMenu",
                  "testVersioning_LocalHistorySubMenu",
                  "testVersioning_Mercurial_MergeSubMenu",
                  "testVersioning_Mercurial_RecoverSubMenu",
                  "testVersioning_Mercurial_ShareSubMenu",
                  "testVersioning_Mercurial_ShowSubMenu",
                  "testWindow_DebuggingSubMenu",
                  "testWindow_NavigatingSubMenu",
                  "testWindow_OtherSubMenu",
                  "testWindow_OutputSubMenu",
                  "testWindow_ProfilingSubMenu",
                  "testWindow_VersioningSubMenu"
                  );
        return NbModuleSuite.create(conf);
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    // run only selected test case
    //junit.textui.TestRunner.run(new IDEValidation("testMainMenu"));
    }

    /** Setup called before every test case. */
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        try {
            getWorkDir();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /** Tear down called after every test case. */
    @Override
    public void tearDown() {
    }

    /**
     * Tests if *File* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-File
     */
    public void testFileMenu() {
        oneMenuTest("File");
    }

    /**
     * Tests if *Edit* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Edit
     */
    public void testEditMenu() {
        oneMenuTest("Edit");
    }

    /**
     * Tests if *View* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-View
     */
    public void testViewMenu() {
        oneMenuTest("View");
    }

    /**
     * Tests if *Navigate* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Navigate
     */
    public void testNavigateMenu() {
        oneMenuTest("Navigate");
    }

    /**
     * Tests if *Source* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Source
     */
    public void testSourceMenu() {
        oneMenuTest("Source");
    }

    /**
     * Tests if *Refactor* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Refactor
     */
    public void testRefactorMenu() {
        oneMenuTest("Refactor");
    }

    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testRunMenu() {
        oneMenuTest("Run");
    }
    
        /**
     * Tests if *Build* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Build
     */
    public void testDebugMenu() {
        oneMenuTest("Debug");
    }

    /**
     * Tests if *Help* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Help
     */
    public void testHelpMenu() {
        oneMenuTest("Help");
    }

    /**
     * Tests if *Tools* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Tools
     */
    public void testToolsMenu() {
        oneMenuTest("Tools");
    }

    /**
     * Tests if *Versioning* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Versioning
     */
    public void testVersioningMenu() {
        oneMenuTest("Versioning");
    }

    /**
     * Tests if *Window* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Window
     */
    public void testWindowMenu() {
        oneMenuTest("Window");
    }

    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testFile_ProjectGroupSubMenu() {
        String goldenFile = getMainMenuGoldenFile("File-Project_Group");
        oneSubMenuTest("File|Project Group", goldenFile);
    }

    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testNavigate_InspectSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Navigate-Inspect");
        oneSubMenuTest("Navigate|Inspect",  goldenFile);
    }

    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testView_CodeFoldsSubMenu() {
        String goldenFile = getMainMenuGoldenFile("View-Code_Folds");
        oneSubMenuTest("View|Code Folds", goldenFile);
    }

    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testView_ToolbarsSubMenu() {
        String goldenFile = getMainMenuGoldenFile("View-Toolbars");
        oneSubMenuTest("View|Toolbars", goldenFile);
    }

    public void testProfile_AdvancedCommandsSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Profile-Advanced_Commands");
        oneSubMenuTest("Profile|Advanced Commands", goldenFile);
    }

    public void testProfile_ProfileOtherSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Profile-Profile_Other");
        oneSubMenuTest("Profile|Profile Other", goldenFile);
    }

    public void testRun_SetMainProjectSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Run-Set_Main_Project");
        oneSubMenuTest("Run|Set Main Project", goldenFile);
    }

    public void testDebug_StackSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Debug-Stack");
        oneSubMenuTest("Debug|Stack", goldenFile);
    }

    public void testSource_PreprocessorBlocksSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Source-Preprocessor_Blocks");
        oneSubMenuTest("Source|Preprocessor Blocks", goldenFile);
    }

    public void testTools_InternationalizationSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Tools-Internationalization");
        oneSubMenuTest("Tools|Internationalization", goldenFile);
    }

    public void testTools_PaletteSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Tools-Palette");
        oneSubMenuTest("Tools|Palette", goldenFile);
    }

    public void testVersioning_CVSSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-CVS");
        oneSubMenuTest("Versioning|CVS", goldenFile);
    }

    public void testVersioning_CVS_BranchesSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-CVS-Branches");
        oneSubMenuTest("Versioning|CVS|Branches", goldenFile);
    }

    public void testVersioning_LocalHistorySubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Local_History");
        oneSubMenuTest("Versioning|Local History", goldenFile);
    }

    public void testVersioning_Mercurial_MergeSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Mercurial-Merge");
        oneSubMenuTest("Versioning|Mercurial|Merge", goldenFile);
    }

    public void testVersioning_Mercurial_RecoverSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Mercurial-Recover");
        oneSubMenuTest("Versioning|Mercurial|Recover", goldenFile);
    }

    public void testVersioning_Mercurial_ShareSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Mercurial-Share");
        oneSubMenuTest("Versioning|Mercurial|Share", goldenFile);
    }

    public void testVersioning_Mercurial_ShowSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Mercurial-Show");
        oneSubMenuTest("Versioning|Mercurial|Show", goldenFile);
    }

    public void testWindow_DebuggingSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Debugging");
        oneSubMenuTest("Window|Debugging",  goldenFile);
    }

    public void testWindow_NavigatingSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Navigating");
        oneSubMenuTest("Window|Navigating", goldenFile);
    }

    public void testWindow_OtherSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Other");
        oneSubMenuTest("Window|Other", goldenFile);
    }

    public void testWindow_OutputSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Output");
        oneSubMenuTest("Window|Output", goldenFile);
    }

    public void testWindow_ProfilingSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Profiling");
        oneSubMenuTest("Window|Profiling", goldenFile);
    }

    public void testWindow_VersioningSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Versioning");
        oneSubMenuTest("Window|Versioning", goldenFile);
    }

    /**
     *
     * @param menuName to be tested
     * @return difference between menuName and golden file with the same name
     */
    private void oneMenuTest(String menuName) {
        oneMenuTest(menuName, getMainMenuGoldenFile(menuName));
    }

    /**
     *
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
            ideFile = new PrintStream(menuItemsLogFile);
            Utilities.printMenuStructure(ideFile, menuItem, "---", 1);
            assertNotNull("Cannot find menu " + menuName, menuItem);//is there such menu?
            assertFile(Utilities.compareNbMenuItems(menuItem, permanentMenu, 1),
                    //"[+]missing in IDE, [-] missing in spec\n"+Utilities.readFileToString(diffFile),
                    permuiLogsFile, menuItemsLogFile, diffFile);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            ideFile.close();
            goldenFile.close();
        }
    }

//
    /**
     *
     * @param submenuName to be tested
     * @param mainmenuName to be tested
     * @param goldenFileName to be tested
     * @return difference between submenuName and goldenFileName
     */
    private void oneSubMenuTest(String submenuPath, String goldenFileName) throws IllegalArgumentException {
        NbMenuItem permanentMenu = Utilities.readSubmenuStructureFromFile(goldenFileName);
        assertNotNull("Nothing read from " + goldenFileName, permanentMenu); //was the file read correctly?
        PrintStream ideFile = null;
        PrintStream goldenFile = null;
        final String menuItemsLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";
        final String permuiLogsFile = getWorkDirPath() + File.separator + getName() + "_golden.txt";
        final String diffFile = getWorkDirPath() + File.separator + getName() + ".diff";
        ArrayList<NbMenuItem> newSubmenu = Utilities.filterOutSeparators(permanentMenu.getSubmenu()); //TODO: fix the getMainMenuItem(.) to return even separators
        permanentMenu.setSubmenu(newSubmenu); //TODO: remove when getMainMenuItem(.) fixed
//        System.out.println("GOLDEN FILE:");
//        Utilities.printMenuStructure(System.out, permanentMenu, "  ", 100);
        try {
            goldenFile = new PrintStream(permuiLogsFile);

            Utilities.printMenuStructure(goldenFile, permanentMenu, "   ", 1);

            ideFile = new PrintStream(menuItemsLogFile);
            //pushMainMenuItem(submenuPath);
            String submenuItems[] = submenuPath.split("\\|");
            assertTrue("submenuPath must be >= 2. - " + submenuPath, submenuItems.length >= 2); //check the size
            NbMenuItem mainM = getMainMenuItem(submenuItems[0]);
            //System.out.println("-------------MENU-----------------");
            //Utilities.printMenuStructure(System.out, mainM, "---", 100);

            NbMenuItem submenuItem = Utilities.getMenuByName(submenuItems[submenuItems.length-1], mainM);
            assertNotNull("Cannot find submenu " + submenuPath, submenuItem);//is there such submenu?
            submenuItem.setMnemo((char)0); //remove the mnemonic of the submenu item because it is not in the perm ui spec too
            //System.out.println("IDE MENU:");
            //Utilities.printMenuStructure(System.out, submenuItem, "   ", 100);
            Utilities.printMenuStructure(ideFile, submenuItem, "   ", 2);
            //System.out.println("-------------SUBMENU-----------------");
            //Utilities.printMenuStructure(System.out, submenuItem, "---", 100);
            assertFile(Utilities.compareNbMenuItems(submenuItem, permanentMenu, 1), menuItemsLogFile, permuiLogsFile, diffFile);
        } catch (FileNotFoundException ex) {
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
    private String getMainMenuGoldenFile(String menuName) {        
        String dataDir = "";
        try {
            dataDir = getDataDir().getCanonicalPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return dataDir +File.separator+ "permanentUI"+File.separator+"mainmenu"+File.separator+ menuName + ".txt";
    }

    public JMenuBarOperator pushMainMenuItem(String mainMenuItem) {
        ///open menu to let it create sucesfully
        JMenuBarOperator mainmenuOp = MainWindowOperator.getDefault().menuBar();
        mainmenuOp.pushMenu(mainMenuItem);

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
}
