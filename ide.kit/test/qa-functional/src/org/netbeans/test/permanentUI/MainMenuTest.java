/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
            MainMenuTest.class
        ).clusters(".*").enableModules(".*");
            
        conf = conf.addTest("testFileMenu");
        conf = conf.addTest("testEditMenu");
        conf = conf.addTest("testViewMenu");
        conf = conf.addTest("testNavigateMenu");
        conf = conf.addTest("testSourceMenu");
        conf = conf.addTest("testRefactorMenu");
        conf = conf.addTest("testBuildMenu");
        conf = conf.addTest("testRunMenu");
        conf = conf.addTest("testFile_ProjectGroupSubMenu");
        conf = conf.addTest("testMnemonicsCollision");
        conf = conf.addTest("testNavigate_InspectSubMenu");
        conf = conf.addTest("testView_CodeFoldsSubMenu");
        conf = conf.addTest("testView_ToolbarsSubMenu");
        
        
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
        String testedMenu = "File";
        String diff = oneMenuTest(testedMenu);
        assertTrue(diff, diff.length() == 0);  
    }
    /**
     * Tests if *Edit* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Edit
     */
    public void testEditMenu() {
        String testedMenu = "Edit";
        String diff = oneMenuTest(testedMenu);
        assertTrue(diff, diff.length() == 0);  
    }
    /**
     * Tests if *View* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-View
     */
    public void testViewMenu() {
        String testedMenu = "View";
        String diff = oneMenuTest(testedMenu);
        assertTrue(diff, diff.length() == 0);  
    }
    /**
     * Tests if *Navigate* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Navigate
     */
    public void testNavigateMenu() {
        String testedMenu = "Navigate";
        String diff = oneMenuTest(testedMenu);
        assertTrue(diff, diff.length() == 0);  
    }
    /**
     * Tests if *Source* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Source
     */
    public void testSourceMenu() {
        String testedMenu = "Source";
        String diff = oneMenuTest(testedMenu);
        assertTrue(diff, diff.length() == 0);  
    }
    /**
     * Tests if *Refactor* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Refactor
     */
    public void testRefactorMenu() {
        String testedMenu = "Refactor";
        String diff = oneMenuTest(testedMenu);
        assertTrue(diff, diff.length() == 0);  
    }
    /**
     * Tests if *Build* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Build
     */
    public void testBuildMenu() {
        String testedMenu = "Build";
        String diff = oneMenuTest(testedMenu);
        assertTrue(diff, diff.length() == 0);  
    }
    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testRunMenu() {
        String testedMenu = "Run";
        String diff = oneMenuTest(testedMenu);
        assertTrue(diff, diff.length() == 0);  
    }
    /**
     * Tests if *Run* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testFile_ProjectGroupSubMenu() {
        String testedMenu = "File";
        String testedSubmenu = "Project Group";
        String goldenFile = getMainMenuGoldenFile("File-Project_Group");
        String diff = oneSubMenuTest(testedSubmenu, testedMenu, goldenFile);
        assertTrue(diff, diff.length() == 0);  
    }
    
        /**
     * Tests if *Run* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testNavigate_InspectSubMenu() {
        String testedMenu = "Navigate";
        String testedSubmenu = "Inspect";
        String goldenFile = getMainMenuGoldenFile("Navigate-Inspect");
        String diff = oneSubMenuTest(testedSubmenu, testedMenu, goldenFile);
        assertTrue(diff, diff.length() == 0);  
    }
    
        /**
     * Tests if *Run* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testView_CodeFoldsSubMenu() {
        String testedMenu = "View";
        String testedSubmenu = "Code Folds";
        String goldenFile = getMainMenuGoldenFile("View-Code_Folds");
        String diff = oneSubMenuTest(testedSubmenu, testedMenu, goldenFile);
        assertTrue(diff, diff.length() == 0);  
    }
    
        /**
     * Tests if *Run* menu in main menu is same as permanent UI spec  
     * http://wiki.netbeans.org/MainMenu#section-MainMenu-Run
     */
    public void testView_ToolbarsSubMenu() {
        String testedMenu = "View";
        String testedSubmenu = "Toolbars";
        String goldenFile = getMainMenuGoldenFile("View-Toolbars");
        String diff = oneSubMenuTest(testedSubmenu, testedMenu, goldenFile);
        assertTrue(diff, diff.length() == 0);  
    }
    
    
    /**
     *  
     * @param menuName to be tested
     * @return difference between menuName and golden file with the same name
     */  
    private String oneMenuTest(String menuName) {        
        return oneMenuTest(menuName, getMainMenuGoldenFile(menuName));
    }
 
    /**
     *  
     * @param menuName to be tested
     * @param goldenFileName to be tested
     * @return difference between menuName and goldenFileName
     */ 
    private String oneMenuTest(String menuName, String goldenFileName) throws IllegalArgumentException {
        //System.out.println("===============permanent=====================");
        //System.out.println("===============menuItem=====================");
//        PrintStream ideFile = null;
//        PrintStream goldenFile = null;
//        try {
//            String WORKDIR = getWorkDirPath();            
            String filename = this.getClass().getResource(goldenFileName).getFile();
            NbMenuItem permanentMenu = Utilities.readMenuStructureFromFile(filename);
            ArrayList<NbMenuItem> newSubmenu = Utilities.filterOutSeparators(permanentMenu.getSubmenu());
            permanentMenu.setSubmenu(newSubmenu);
//            goldenFile = new PrintStream(WORKDIR + File.separator + goldenFileName + "_golden");
//            Utilities.printMenuStructure(goldenFile, permanentMenu, "---");
            
            NbMenuItem menuItem = getMainMenuItem(menuName);
//            ideFile = new PrintStream(WORKDIR + File.separator + goldenFileName + "_ide");
//            Utilities.printMenuStructure(ideFile, menuItem, "---");

//            compareReferenceFiles(WORKDIR + File.separator + goldenFileName + "_ide", WORKDIR + File.separator + goldenFileName + "_golden", WORKDIR + File.separator + goldenFileName + "_diff");
            return Utilities.compareNbMenuItems(menuItem, permanentMenu, 1);
//        } catch (FileNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        } finally {
//            ideFile.close();
//            goldenFile.close();
//        }
//        
//        return "FAILED";
    }
    
//    
    /**
     *  
     * @param submenuName to be tested
     * @param mainmenuName to be tested
     * @param goldenFileName to be tested
     * @return difference between submenuName and goldenFileName
     */ 
    private String oneSubMenuTest(String submenuName, String mainmenuName, String goldenFileName) throws IllegalArgumentException {
        String filename = this.getClass().getResource(goldenFileName).getFile();
        //System.out.println("FILENAME : " + filename);
        NbMenuItem permanentMenu = Utilities.readSubmenuStructureFromFile(filename);
        //Utilities.printMenuStructure(System.out, permanentMenu, "---");
        ArrayList<NbMenuItem> newSubmenu = Utilities.filterOutSeparators(permanentMenu.getSubmenu()); //TODO: fix the getMainMenuItem(.) to return even separators
        permanentMenu.setSubmenu(newSubmenu); //TODO: remove when getMainMenuItem(.) fixed
        //System.out.println("===============permanent=====================");
        
//        Utilities.printMenuStructure(System.out, permanentMenu, "---");        
        NbMenuItem submenuItem = Utilities.getMenuByName(submenuName, getMainMenuItem(mainmenuName));
        //System.out.println("===============menuItem=====================");
//        Utilities.printMenuStructure(System.out, submenuItem, "---");        
        
        return Utilities.compareNbMenuItems(submenuItem, permanentMenu, 1);
    }    
//////////////////////////////////////////////////////////////////////////////////////////        
    public void parseMainMenuItems(String mainMenuItem) {
        ///open menu to let it create sucesfully
        JMenuBarOperator mainmenuOp = MainWindowOperator.getDefault().menuBar();
        System.out.println("---opening menu " + mainMenuItem);

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
            items[k].list();
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
        return "data/mainmenu-"+menuName+".txt";
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
