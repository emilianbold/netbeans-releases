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
import java.awt.Container;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.MenuElement;
import javax.swing.JMenuItem;
import javax.swing.JMenu;

import org.netbeans.jellytools.JellyTestCase;
import junit.textui.TestRunner;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.junit.NbTestSuite;

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

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MainMenuTest("testFileMenuNoSeparators"));
//        suite.addTest(new MainMenuTest("testMnemonicsCollision"));
        return suite;
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

    public void testFileMenu() {
        //parseMainMenuItems("View");
//        MenuChecker.printMenuBarStructure(MainWindowOperator.getDefault().getJMenuBar(), System.out, null, false, false);
//        Utilities.printMenuStructure(System.out, getMainMenuItem("File"), " ");
//        Utilities.printMenuStructure(System.out, getMainMenuItem("View"), " ");
    }

    public void testFileMenuNoSeparators() {
        String filename = this.getClass().getResource(getMainMenuGoldenFile("File")).getFile();
        System.out.println("FILENAME : " + filename);
        NbMenuItem permanentMenu = Utilities.readMenuStructureFromFile(filename);
        ArrayList<NbMenuItem> newSubmenu = Utilities.filterOutSeparators(permanentMenu.getSubmenu());
        permanentMenu.setSubmenu(newSubmenu);
        System.out.println("===============permanent=====================");
        Utilities.printMenuStructure(System.out, permanentMenu, "---");
        
        NbMenuItem menuItem = getMainMenuItem("File");
        System.out.println("===============menuItem=====================");
        Utilities.printMenuStructure(System.out, menuItem, "---");        
        
        String diff = Utilities.compareNbMenuItems(menuItem, permanentMenu, 1);
        assertTrue(diff, diff.length() == 0);
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
                System.out.println("aaa " + items[k].toString());

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
