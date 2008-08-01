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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.test.permanentUI.utils;

import java.awt.Component;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.JSeparator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import javax.swing.JComponent;
import org.netbeans.jemmy.operators.JMenuBarOperator;

import org.netbeans.jellytools.MainWindowOperator;

/**
 * @author  lhasik@netbeans.org, mmirilovic@netbeans.org
 */
public class MenuChecker {

    /** Creates a new instance of MenuChecker */
    public MenuChecker() {
    }


    /** Open all menus in menubar
     * @param menu  to be visited */
    public static void visitMenuBar(JMenuBar menu) {
        MenuElement[] elements = menu.getSubElements();

        JMenuBarOperator op = new JMenuBarOperator(menu);

        for (int k = 0; k < elements.length; k++) {
            if (elements[k] instanceof JMenuItem) {
                op.pushMenu(((JMenuItem) elements[k]).getText(), "/", true, true);
                try {
                    op.wait(200);
                } catch (Exception e) {
                }
            }
        }
    }

    /** Get MenuBar and tranfer it to ArrayList.
     * @param menu menu to be tranfered
     * @return tranfered menubar  - !separator is ignored
     */
    public static ArrayList<NbMenuItem> getMenuBarArrayList(JMenuBar menu) {
 //       System.out.println("getMenuBarArrayList " + menu.getName());
        visitMenuBar(menu);

        MenuElement[] elements = menu.getSubElements();

        ArrayList<NbMenuItem> list = new ArrayList<NbMenuItem>();
        for (int k = 0; k < elements.length; k++) {
            if (elements[k] instanceof JPopupMenu.Separator) {
                NbMenuItem separator = new NbMenuItem();
                separator.setSeparator(true);
                list.add(separator);
            } else {
                if (elements[k] instanceof JMenuItem) {

                    NbMenuItem item = new NbMenuItem((JMenuItem) elements[k]);
                    JMenuBarOperator menuOp = new JMenuBarOperator(menu);
                    item.setSubmenu(getMenuArrayList(menuOp.getMenu(k)));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /** Get Menu and tranfer it to ArrayList.
     * @param menu menu to be tranfered
     * @return tranfered menu  - !separator is ignored
     */
    public static ArrayList<NbMenuItem> getMenuArrayList(JMenu menu) {
//        System.out.println("getMenuArrayList: " + menu.getText());//DEBUG
//        menu.list();//DEBUG
        MenuElement[] elements = menu.getSubElements();
        ArrayList<NbMenuItem> list = new ArrayList<NbMenuItem>();

        for (int k = 0; k < elements.length; k++) {
//            System.out.print("getMenuArrayList: ");
//            ((JComponent) elements[k]).list(System.out);
            if (elements[k] instanceof JSeparator) {
                NbMenuItem separator = new NbMenuItem();
                separator.setSeparator(true);
                list.add(separator);
            } else {
                if (elements[k] instanceof JPopupMenu) {
                    list.addAll(getPopupMenuArrayList((JPopupMenu) elements[k]));
                } else {
                    if (elements[k] instanceof JMenuItem) {
                        NbMenuItem item = new NbMenuItem((JMenuItem) elements[k]);
                        item.setName(item.getName());
                        list.add(item);
                    } else {
                        System.out.println("getMenu unknown:" + elements[k].toString());
                    }
                }
            }

        }
        return list;
    }

    /** Get PopupMenu and transfer it to ArrayList.
     * @param popup menu to be tranfered
     * @return transfered menu - !separator is ignored
     */
    public static ArrayList<NbMenuItem> getPopupMenuArrayList(JPopupMenu popup) {
        //System.out.print("getPopupMenuArrayList: "); popup.list(); //DEBUG
        MenuElement[] elements = popup.getSubElements();
        ArrayList<NbMenuItem> list = new ArrayList<NbMenuItem>();

        for (MenuElement menuElement : elements) {
            //System.out.print("getPopupMenuArrayList: ");
//            ((JComponent) menuElement).list();
            if (menuElement instanceof JSeparator) {
                //System.out.println("adding separator");//DEBUG
                NbMenuItem separator = new NbMenuItem();
                separator.setSeparator(true);
                list.add(separator);
            } else {
                if (menuElement instanceof JMenu) {
                    NbMenuItem mitem = new NbMenuItem((JMenuItem) menuElement);
                    mitem.setName(mitem.getName());
                    mitem.setSubmenu (getMenuArrayList((JMenu) menuElement));
                    list.add(mitem);
                } else if (menuElement instanceof JMenuItem) //if()
                {
                    NbMenuItem item = new NbMenuItem((JMenuItem) menuElement);
                    item.setName(item.getName());
                    list.add(item);
                } else {
                    System.out.println("getPopup unknown:" + menuElement.toString());
                }
            }
        }
        return list;
    }
    /**
     * 
     * @param component
     * @return all menu items in the menu component
     */
    public static ArrayList<NbMenuItem> getMenuItemsList(JComponent component) {
        Component items[] = component.getComponents();
        
        ArrayList<NbMenuItem> list = new ArrayList<NbMenuItem>();
        
        for (Component menuItem : items) {
            if (menuItem instanceof JSeparator) {
//                System.out.println("adding separator");//DEBUG
                NbMenuItem separator = new NbMenuItem();
                separator.setSeparator(true);
                list.add(separator);
            } else {
                if (menuItem instanceof JMenu) {
                    NbMenuItem mitem = new NbMenuItem((JMenuItem) menuItem);
                    mitem.setName(mitem.getName());
                    mitem.setSubmenu (getMenuItemsList((JComponent)menuItem));
                    list.add(mitem);
                } else if (menuItem instanceof JMenuItem) {//if()                
                    NbMenuItem item = new NbMenuItem((JMenuItem) menuItem);
                    item.setName(item.getName());
                    list.add(item);
                } else {
                    System.out.println("getMenuItemsList unknown:" + menuItem.toString());
                }
            }
        }
        return list;
    }

    public static String checkMnemonicCollision() {
        return checkMnemonicCollision(getMenuBarArrayList(MainWindowOperator.getDefault().getJMenuBar())).toString();
    }

    /** Check mnemonics in menu structure.
     * @param list
     * @return  
     */
    private static StringBuffer checkCollision(ArrayList list, boolean checkShortCuts) {
        StringBuffer collisions = new StringBuffer("");
        Iterator it = list.iterator();

        HashMap check = new HashMap();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof NbMenuItem) {
                NbMenuItem item = (NbMenuItem) o;

                if (checkShortCuts) {
                    if (item.getMnemo() != 0) {
                        Integer mnemonic = new Integer(item.getMnemo());
                        //stream.println("checking : " + item.name + " - " + item.mnemo);
                        if (check.containsKey(mnemonic)) {
                            char k = (char) item.getMnemo();
                            collisions.append("\n !!!!!! Collision! mnemonic='" + k + "' : " + item.getName() + " is in collision with " + check.get(mnemonic));
                        } else {
                            check.put(mnemonic, item.getName());
                        }

                    }
                } else {
                    if (item.getMnemo() != 0) {
                        Integer mnemonic = new Integer(item.getMnemo());
                        //stream.println("checking : " + item.name + " - " + item.mnemo);
                        if (check.containsKey(mnemonic)) {
                            char k = (char) item.getMnemo();
                            collisions.append("\n !!!!!! Collision! mnemonic='" + k + "' : " + item.getName() + " is in collision with " + check.get(mnemonic));
                        } else {
                            check.put(mnemonic, item.getName());
                        }

                    }
                }
            }

            if (o instanceof ArrayList) {
                collisions.append(checkMnemonicCollision((ArrayList) o));
            }

        }

        return collisions;
    }

    /** Check mnemonics in menu structure.
     * @param list
     * @return  
     */
    private static StringBuffer checkMnemonicCollision(ArrayList list) {
        StringBuffer collisions = new StringBuffer("");
        Iterator it = list.iterator();

        HashMap check = new HashMap();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof NbMenuItem) {
                NbMenuItem item = (NbMenuItem) o;
                if (item.getMnemo() != 0) {
                    Integer mnemonic = new Integer(item.getMnemo());
                    //stream.println("checking : " + item.name + " - " + item.mnemo);
                    if (check.containsKey(mnemonic)) {
                        char k = (char) item.getMnemo();
                        collisions.append("\n !!!!!! Collision! mnemonic='" + k + "' : " + item.getName() + " is in collision with " + check.get(mnemonic));
                    } else {
                        check.put(mnemonic, item.getName());
                    }

                }
            }

            if (o instanceof ArrayList) {
                collisions.append(checkMnemonicCollision((ArrayList) o));
            }

        }

        return collisions;
    }

    public static String checkShortCutCollision() {
        return checkShortCutCollision(getMenuBarArrayList(MainWindowOperator.getDefault().getJMenuBar())).toString();
    }

    /** check shortcuts in menu structure
     * @param a
     * @return  
     */
    private static StringBuffer checkShortCutCollision(ArrayList a) {
        StringBuffer collisions = new StringBuffer("");
        Iterator it = a.iterator();
        HashMap check = new HashMap();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof NbMenuItem) {
                NbMenuItem item = (NbMenuItem) o;

                if (item.getAccelerator() != null) {
                    //stream.println("checking : " + item.name + " - " + item.accelerator);
                    if (check.containsKey(item.getAccelerator())) {
                        collisions.append("\n !!!!!! Collision! accelerator ='" + item.getAccelerator() + "' : " + item.getName() + " is in collision with " + check.get(item.getAccelerator()));
                    } else {
                        check.put(item.getAccelerator(), item.getName());
                    }

                }
            }

            if (o instanceof ArrayList) {
                collisions.append(checkShortCutCollision((ArrayList) o));
            }

        }

        return collisions;
    }

/////////////////////////////////////////////////////////////////////////////////
    public static int getElementPosition(String element, MenuElement[] array) {
        int p = -1;
        for (int i = 0; i <
                array.length; i++) {
            if (element.equals(((JMenuItem) array[i]).getText())) {
                return i;
            }

        }
        return p;
    }

}


