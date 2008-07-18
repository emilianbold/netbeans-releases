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

import java.awt.Component;
import java.io.*;
import java.util.ArrayList;
import javax.swing.MenuElement;
import javax.swing.JMenuItem;
import javax.swing.JMenu;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import junit.textui.TestRunner;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
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

        conf = conf.addTest("testFileMenu");
        conf = conf.addTest("testEditMenu");
        conf = conf.addTest("testViewMenu");
        conf = conf.addTest("testNavigateMenu");
        conf = conf.addTest("testSourceMenu");
        conf = conf.addTest("testRefactorMenu");
        conf = conf.addTest("testBuildMenu");
        conf = conf.addTest("testRunMenu");
        conf = conf.addTest("testHelpMenu");
        conf = conf.addTest("testToolsMenu");
        conf = conf.addTest("testVersioningMenu");
        conf = conf.addTest("testWindowMenu");

        conf = conf.addTest("testFile_ProjectGroupSubMenu");
        conf = conf.addTest("testMnemonicsCollision");
        conf = conf.addTest("testNavigate_InspectSubMenu");
        conf = conf.addTest("testView_CodeFoldsSubMenu");
        conf = conf.addTest("testView_ToolbarsSubMenu");
        conf = conf.addTest("testProfile_AdvancedCommandsSubMenu");
        conf = conf.addTest("testProfile_ProfileOtherSubMenu");
        conf = conf.addTest("testRun_RunFileSubMenu");
        conf = conf.addTest("testRun_StackSubMenu");
        conf = conf.addTest("testSource_PreprocessorBlocksSubMenu");
        conf = conf.addTest("testTools_InternationalizationSubMenu");
        conf = conf.addTest("testTools_PaletteSubMenu");
        conf = conf.addTest("testVersioning_CVSSubMenu");
        conf = conf.addTest("testVersioning_CVS_BranchesSubMenu");
        conf = conf.addTest("testVersioning_LocalHistorySubMenu");
        conf = conf.addTest("testVersioning_Mercurial_MergeSubMenu");
        conf = conf.addTest("testVersioning_Mercurial_RecoverSubMenu");
        conf = conf.addTest("testVersioning_Mercurial_ShareSubMenu");
        conf = conf.addTest("testVersioning_Mercurial_ShowSubMenu");
        conf = conf.addTest("testWindow_DebuggingSubMenu");
        conf = conf.addTest("testWindow_NavigatingSubMenu");
        conf = conf.addTest("testWindow_OtherSubMenu");
        conf = conf.addTest("testWindow_OutputSubMenu");
        conf = conf.addTest("testWindow_ProfilingSubMenu");
        conf = conf.addTest("testWindow_VersioningSubMenu");

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
     * Tests if *Build* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Build
     */
    public void testBuildMenu() {
        oneMenuTest("Build");
    }

    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testRunMenu() {
        oneMenuTest("Run");
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
        oneSubMenuTest("Project Group", "File", goldenFile);
    }

    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testNavigate_InspectSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Navigate-Inspect");
        oneSubMenuTest("Inspect", "Navigate", goldenFile);
    }

    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testView_CodeFoldsSubMenu() {
        String goldenFile = getMainMenuGoldenFile("View-Code_Folds");
        oneSubMenuTest("Code Folds", "View", goldenFile);
    }

    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testView_ToolbarsSubMenu() {
        String goldenFile = getMainMenuGoldenFile("View-Toolbars");
        oneSubMenuTest("Toolbars", "View", goldenFile);
    }

    public void testProfile_AdvancedCommandsSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Profile-Advanced_Commands");
        oneSubMenuTest("Advanced Commands", "Profile", goldenFile);
    }

    public void testProfile_ProfileOtherSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Profile-Profile_Other");
        oneSubMenuTest("Profile Other", "Profile", goldenFile);
    }

    public void testRun_RunFileSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Run-Run_File");
        oneSubMenuTest("Run File", "Run", goldenFile);
    }

    public void testRun_StackSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Run-Stack");
        oneSubMenuTest("Stack", "Run", goldenFile);
    }

    public void testSource_PreprocessorBlocksSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Source-Preprocessor_Blocks");
        oneSubMenuTest("Preprocessor Blocks", "Source", goldenFile);
    }

    public void testTools_InternationalizationSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Tools-Internationalization");
        oneSubMenuTest("Internationalization", "Tools", goldenFile);
    }

    public void testTools_PaletteSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Tools-Palette");
        oneSubMenuTest("Palette", "Tools", goldenFile);
    }

    public void testVersioning_CVSSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-CVS");
        oneSubMenuTest("CVS", "Versioning", goldenFile);
    }

    public void testVersioning_CVS_BranchesSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-CVS-Branches");
        oneSubMenuTest("Branches", "Versioning", goldenFile);
    }

    public void testVersioning_LocalHistorySubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Local_History");
        oneSubMenuTest("Local History", "Versioning", goldenFile);
    }

    public void testVersioning_Mercurial_MergeSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Mercurial-Merge");
        oneSubMenuTest("Merge", "Versioning", goldenFile);
    }

    public void testVersioning_Mercurial_RecoverSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Mercurial-Recover");
        oneSubMenuTest("Recover", "Versioning", goldenFile);
    }
    
    public void testVersioning_Mercurial_ShareSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Mercurial-Share");
        oneSubMenuTest("Share", "Versioning", goldenFile);
    }
    
    public void testVersioning_Mercurial_ShowSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Versioning-Mercurial-Show");
        oneSubMenuTest("Show", "Versioning", goldenFile);
    }

    public void testWindow_DebuggingSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Debugging");
        oneSubMenuTest("Debugging", "Window", goldenFile);
    }

    public void testWindow_NavigatingSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Navigating");
        oneSubMenuTest("Navigating", "Window", goldenFile);
    }

    public void testWindow_OtherSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Other");
        oneSubMenuTest("Other", "Window", goldenFile);
    }

    public void testWindow_OutputSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Output");
        oneSubMenuTest("Output", "Window", goldenFile);
    }

    public void testWindow_ProfilingSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Profiling");
        oneSubMenuTest("Profiling", "Window", goldenFile);
    }

    public void testWindow_VersioningSubMenu() {
        String goldenFile = getMainMenuGoldenFile("Window-Versioning");
        oneSubMenuTest("Versioning", "Window", goldenFile);
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
        //System.out.println("===============permanent=====================");
        //System.out.println("===============menuItem=====================");
        PrintStream ideFile = null;
        PrintStream goldenFile = null;
        final String menuItemsLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";
        final String permuiLogsFile = getWorkDirPath() + File.separator + getName() + "_golden.txt";
        final String diffFile = getWorkDirPath() + File.separator + getName() + ".diff";
        try {
            String filename = this.getClass().getResource(goldenFileName).getFile();
            NbMenuItem permanentMenu = Utilities.readMenuStructureFromFile(filename);
            ArrayList<NbMenuItem> newSubmenu = Utilities.filterOutSeparators(permanentMenu.getSubmenu());
            permanentMenu.setSubmenu(newSubmenu);
            goldenFile = new PrintStream(permuiLogsFile);
            Utilities.printMenuStructure(goldenFile, permanentMenu, "---", 1);

            NbMenuItem menuItem = getMainMenuItem(menuName);
            ideFile = new PrintStream(menuItemsLogFile);
            Utilities.printMenuStructure(ideFile, menuItem, "---", 1);

            assertFile(Utilities.compareNbMenuItems(menuItem, permanentMenu, 1), permuiLogsFile, menuItemsLogFile, diffFile);

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
    private void oneSubMenuTest(String submenuName, String mainmenuName, String goldenFileName) throws IllegalArgumentException {
        //System.out.println("===============permanent=====================");
        String filename = this.getClass().getResource(goldenFileName).getFile();
        NbMenuItem permanentMenu = Utilities.readSubmenuStructureFromFile(filename);
        PrintStream ideFile = null;
        PrintStream goldenFile = null;
        final String menuItemsLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";
        final String permuiLogsFile = getWorkDirPath() + File.separator + getName() + "_golden.txt";
        final String diffFile = getWorkDirPath() + File.separator + getName() + "_diff";
        ArrayList<NbMenuItem> newSubmenu = Utilities.filterOutSeparators(permanentMenu.getSubmenu()); //TODO: fix the getMainMenuItem(.) to return even separators
        permanentMenu.setSubmenu(newSubmenu); //TODO: remove when getMainMenuItem(.) fixed
        System.out.println("GOLDEN FILE:");            
        Utilities.printMenuStructure(System.out, permanentMenu, "--", 100);
        try {
            goldenFile = new PrintStream(permuiLogsFile);

            Utilities.printMenuStructure(goldenFile, permanentMenu, "---", 1);

            ideFile = new PrintStream(menuItemsLogFile);
            NbMenuItem submenuItem = Utilities.getMenuByName(submenuName, getMainMenuItem(mainmenuName));
            System.out.println("IDE MENU:");
            Utilities.printMenuStructure(System.out, submenuItem, "---", 100);
            Utilities.printMenuStructure(ideFile, submenuItem, "---", 2);

            assertFile(Utilities.compareNbMenuItems(submenuItem, permanentMenu, 1), menuItemsLogFile, permuiLogsFile, diffFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////

    public void parseMainMenuItems(String mainMenuItem) {
        ///open menu to let it create sucesfully
        JMenuBarOperator mainmenuOp = MainWindowOperator.getDefault().menuBar();
//        System.out.println("---opening menu " + mainMenuItem);

        mainmenuOp.pushMenu(mainMenuItem);
        try {
            mainmenuOp.wait(200);
        } catch (Exception e) {
        }
        MenuElement[] mmElements = mainmenuOp.getSubElements();
        //parse all the menu elements
        int position = MenuChecker.getElementPosition(mainMenuItem, mmElements);
        JMenu menu = mainmenuOp.getMenu(position);
        Component items[] = menu.getComponents();
        for (int k = 0; k < items.length; k++) {
//            items[k].list();
            if (items[k] instanceof JMenuItem) {
//                list.add(NbMenu.getNbMenu((JMenuItem)elements[k]));
//                JMenuBarOperator menuOp = new JMenuBarOperator(menu);
//                list.add(getMenuArrayList(menuOp.getMenu(k)));
            }
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
        return "data/mainmenu-" + menuName + ".txt";
    }

    private NbMenuItem getMainMenuItem(String mainMenuItem) {
        ///open menu to let it create sucesfully
        JMenuBarOperator mainmenuOp = MainWindowOperator.getDefault().menuBar();

        mainmenuOp.pushMenu(mainMenuItem);
        try {
            mainmenuOp.wait(200);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //parse all the menu elements
        int position = MenuChecker.getElementPosition(mainMenuItem, mainmenuOp.getSubElements());
        MenuElement theMenuElement = mainmenuOp.getSubElements()[position];
        NbMenuItem theMenu = new NbMenuItem((JMenuItem) theMenuElement);
        theMenu.setSubmenu(MenuChecker.getMenuArrayList(mainmenuOp.getMenu(position)));

        return theMenu;
    }
}
