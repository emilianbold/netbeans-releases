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
package org.netbeans.test.permanentUI.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.MenuElement;

/**
 *
 * @author Lukas Hasik
 */
public class Utilities {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final String mainmenu_file = "F:\\prace\\hg\\main\\ide.kit\\test\\qa-functional\\src\\org\\netbeans\\test\\permanentUI\\data\\mainmenu-file.txt";

        NbMenuItem item = Utilities.readMenuStructureFromFile(mainmenu_file);
        Utilities.printMenuStructure(System.out, item, " ");
    }

    /**
     * reads menus like http://wiki.netbeans.org/MainMenu
     * 
     * 
     * | View |                       V (mnemonics)
     * ========
     *     Editors                >   E
     *     Code Folds             >   C
     * ============================
     *     Web Browser                W
     *     IDE Log                    L
     * ============================
     *     Toolbars               >   T
     * [x] Show Editor Toolbar        h
     * [ ] Show Line Numbers          S
     * [x] Show Diff Sidebar          D
     * [ ] Show Versioning Labels     V
     * ============================
     *     Full Screen                F
     * 
     * 
     * @param filename
     * @return parsed menu structure
     */
    public static NbMenuItem readMenuStructureFromFile(String filename) {
        NbMenuItem parsedMenu = new NbMenuItem();
        try {
            //first use a Scanner to get each line
            Scanner scanner = new Scanner(new File(filename));
            //starts "| Item |"
            String menuName = scanner.nextLine();
            int from;
            if ((from = menuName.indexOf("| ")) != -1) {
                parsedMenu.setName(menuName.substring(from + "| ".length(), menuName.lastIndexOf(" |")));
                parsedMenu.setMnemo(menuName.substring(menuName.lastIndexOf(" |") + "| ".length()).trim().charAt(0));
            } else {
                System.out.println("Wrong file: missing header - menu name as | menuName |");
                return null;
            }
            //skip ====== bellow menu name
            menuName = scanner.nextLine();
            if (!(menuName.matches("^={5,}+"))) {
                System.out.println("Wrong file: missing ===== - bellow  menu name");
                return null;
            }
            //parse the menu items structure
            ArrayList<NbMenuItem> submenu = new ArrayList<NbMenuItem>();
            while (scanner.hasNextLine()) {
                submenu.add(parseMenuLineText(scanner.nextLine()));
            }
            parsedMenu.setSubmenu(submenu);
            scanner.close();
        } catch (IOException ex) {
            //log(ex.getMessage());
        }

        return parsedMenu;
    }

    /**
     * reads submenus like http://wiki.netbeans.org/MainMenu
    
     *     Toolbars                 > [x] Build                  B
     *                                [ ] Debug                  D
     *                                [x] Edit                   E
     *                                [x] File                   F
     *                                [ ] Memory                 M
     *                               ========================
     *                                    Small Toolbar Icons    S
     *                                ========================
     *                                    Reset Toolbars         R
     *                                    Customize...           C
     * @param filename
     * @return parsed submenu structure
     */
    public static NbMenuItem readSubmenuStructureFromFile(String filename) {
        NbMenuItem parsedMenu = new NbMenuItem();
        try {
            //first use a Scanner to get each line
            Scanner scanner = new Scanner(new File(filename));
            //starts "Toolbars                 > [x] Build                  B"
            String submenuName = scanner.nextLine();
            int to;
            if ((to = submenuName.indexOf("> ")) != -1) {
                parsedMenu.setName(submenuName.substring(0, submenuName.lastIndexOf("   > ")));
            } else {
                System.out.println("Wrong file: missing header - submenu name                 > [x] submenu item                  B");
                return null;
            }

            ArrayList<NbMenuItem> submenu = new ArrayList<NbMenuItem>();
            submenu.add(parseMenuLineText(submenuName.substring(to)));
            while (scanner.hasNextLine()) {
                submenu.add(parseMenuLineText(scanner.nextLine()));
            }
            parsedMenu.setSubmenu(submenu);
            scanner.close();
        } catch (IOException ex) {
            //log(ex.getMessage());
        }

        return parsedMenu;

    }

    /**
     * Parses menu line like
     *     IDE Log                    L
     * ============================
     *     Toolbars               >   T
     * [x] Show Editor Toolbar        h
     * [ ] Show Line Numbers          S
     * (x) Show Diff Sidebar          D
     * 
     * @param lineText
     * @return parsed menu item from line
     */
    public static NbMenuItem parseMenuLineText(String lineText) {
        //parse line
        Scanner line = new Scanner(lineText);
        NbMenuItem menuitem = new NbMenuItem();

        //is it separator? "======="
        if (line.hasNext("^={5,}+")) { //at least 5x =

            menuitem.setSeparator(true);
        } else {
            //does the line start with ( ?
            String isRadio = line.findInLine("\\(.\\)");
            if (isRadio != null) {
                //System.out.println("parsing radiobutton: " + isRadio);
                menuitem.setRadiobutton(true);
                menuitem.setChecked(isRadio.indexOf("o") != -1);
            } else {
                //does the line start with [ ?
                String isCheck = line.findInLine("\\[.\\]");
                if (isCheck != null) {
                    //System.out.println("parsing checkbox: " + isCheck);
                    menuitem.setCheckbox(true);
                    menuitem.setChecked(isCheck.indexOf("x") != -1);
                }
            }

            //read menu item text
            StringBuffer text = new StringBuffer();
            boolean read = true;
            while (read && line.hasNext()) {
                String partOfText = line.next();
                if (partOfText.length() == 1) {
                    if (partOfText.charAt(0) == '>') {
                        menuitem.setSubmenu(new ArrayList<NbMenuItem>());
                    } else { //it must be the mnemonic

                        menuitem.setMnemo(partOfText.charAt(0));
                        read = false;
                    }
                } else {
                    text.append(partOfText);
                    text.append(" ");
                }
            }
            menuitem.setName(text.substring(0, text.length() - 1)); //remove the last " "

        }

        return menuitem;
    }

    /**
     * Prints NbMenuItem to the printstream 
     * @param out PrintStream
     * @param menu NbMenuItem
     * @param separator
     */
    public static void printMenuStructure(PrintStream out, NbMenuItem menu, String separator) {
        String checked = " ";
        String output = separator;
        if (menu == null) {
            return;
        }
        if (menu.isChecked()) {
            checked = "x";
        }
        if (menu.isRadiobutton()) {
            output += "(" + checked + ") ";
        }
        if (menu.isCheckbox()) {
            output += "[" + checked + "] ";
        }

        output += menu.getName() + " ";
        ArrayList<NbMenuItem> submenu = menu.getSubmenu();
        if (submenu != null) {
            output += "> ";
        }
        output += "   " + menu.getMnemo();

        System.out.println(output);

        //print submenu
        if (submenu != null) {
            Iterator<NbMenuItem> sIt = submenu.iterator();
            while (sIt.hasNext()) {
                printMenuStructure(out,/*(NbMenuItem)*/ sIt.next(), separator + separator);
            }
        }
    }

    /**
     * Parses files like http://wiki.netbeans.org/NewProjectWizard
     * Java
     *    Java Application
     *    Java Desktop Application
     *    Java Class Library
     *    Java Project with Existing Sources
     *    Java Free-form Project
     * 
     * @param filename
     * @return
     */
    public static ArrayList<String> parseFileByLines(String filename) {
        ArrayList<String> textLines = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                textLines.add(trimTextLine(scanner.nextLine()));
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return textLines;
    }

    /**
     * trims unnecessary spaces from text line
     * @param line
     * @return
     */
    public static String trimTextLine(String line) {
        StringBuffer trimmedText = new StringBuffer();
        String words[] = line.split(" ");
        Scanner lineScan = new Scanner(line);
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                trimmedText.append(words[i]);
                trimmedText.append(" ");
            }
        }

        return trimmedText.substring(0, trimmedText.length() - 1);//remove the last space

    }

    /**
     * Compares two NbMenuItems. Return a String with description of all the differences
     * @param menuOrigin
     * @param menuCompare
     * @param submenuLevel
     * @return
     */
    public static String compareNbMenuItems(NbMenuItem menuOrigin, NbMenuItem menuCompare, int submenuLevel) {
        String returnText = "";
        System.out.println(menuOrigin.toString()+" comparing with \n"+menuCompare); //DEBUG
        if (!menuOrigin.equals(menuCompare)) {
            returnText = menuOrigin.findDifference(menuCompare);
        }
        if (submenuLevel > 0) {
            if ((menuOrigin.getSubmenu() != null) && (menuCompare.getSubmenu() != null)) {
                //lets traverse thought the submenu
                Iterator<NbMenuItem> itOrigin = menuOrigin.getSubmenu().iterator();
                Iterator<NbMenuItem> itCompare = menuCompare.getSubmenu().iterator();
                NbMenuItem originItem = null;
                NbMenuItem compareItem = null;
                while (itOrigin.hasNext() || itCompare.hasNext()) {
                    try {
                        originItem = itOrigin.next();
                    } catch (NoSuchElementException e) {
                        originItem = null;
                    }
                    try {
                        compareItem = itCompare.next();
                    } catch (NoSuchElementException e) {
                        compareItem = null;
                    }
                    if(originItem == null) {
                        if(compareItem == null) {
                            returnText += "BOTH ITEMS ARE NULL. THIS STATE SHOULDN'T HAPPEN";
                        } else { //only originItem is null
                            returnText += compareItem.getName() + " is missing in the menu. ["+compareItem.toString()+"]";
                        }
                    } else {
                        if(compareItem == null) {
                            returnText += originItem.getName() + " shoul NOT be in the menu. ["+originItem.toString()+"]";                          
                        } else { 
                        }
                    }
                    returnText += compareNbMenuItems(originItem, compareItem, submenuLevel - 1);
                }
            }
        }
        return returnText;
    }

    /**
     * Filter out all NbMenuItem.separators from array
     * @param array
     * @return
     */
    public static ArrayList<NbMenuItem> filterOutSeparators(ArrayList<NbMenuItem> array) {
        ArrayList<NbMenuItem> newArray = new ArrayList<NbMenuItem>();
        for (Iterator<NbMenuItem> it = array.iterator(); it.hasNext();) {
            NbMenuItem nbMenuItem = it.next();
            if (!nbMenuItem.isSeparator()) { //it is not separator {
                newArray.add(nbMenuItem);
//                System.out.println("adding - " + nbMenuItem.getName()); //DEBUG
            }
        }
        return newArray;
    }
}
