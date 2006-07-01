/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.windowsystem;

import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import java.util.Collection;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import java.io.PrintStream;

import org.netbeans.jemmy.operators.JMenuBarOperator;

import org.netbeans.jellytools.MainWindowOperator;


/**
 * @author  lhasik@netbeans.org, mmirilovic@netbeans.org
 */
public class MenuChecker {
    
    /** Creates a new instance of MenuChecker */
    public MenuChecker() {
    }
    
    /** Check whether JPopupMenu <b>popup</b> contains <B>item</B> ?
     * @param popup looking for menu item in this popup menu
     * @param item looking for this item
     * @return true-popup contains item, false-doesn't contain item */
    public boolean containsMenuItem(javax.swing.JPopupMenu popup, String item) {
        MenuElement [] elements = popup.getSubElements();
        for(int k=0; k < elements.length; k++) {
            
            if(elements[k] instanceof JMenuItem) {
                if(item.equals(((JMenuItem)elements[k]).getText())) return true;
            }
        }
        return false;
    }
    
    /** Open all menus in menubar
     * @param menu  to be visited */
    public static void visitMenuBar(JMenuBar menu) {
        MenuElement [] elements = menu.getSubElements();
        
        JMenuBarOperator op = new JMenuBarOperator(menu);
        
        for(int k=0; k < elements.length; k++) {
            if(elements[k] instanceof JMenuItem) {
                op.pushMenu(((JMenuItem)elements[k]).getText(), "/", true, true);
                try {
                    op.wait(200);
                }catch(Exception e) {}
            }
        }
    }
    
    /** Get MenuBar and tranfer it to ArrayList.
     * @param menu menu to be tranfered
     * @return tranfered menubar */
    public static ArrayList getMenuBarArrayList(JMenuBar menu) {
        visitMenuBar(menu);
        
        MenuElement [] elements = menu.getSubElements();
        
        ArrayList list = new ArrayList();
        for(int k=0; k < elements.length; k++) {
            if(elements[k] instanceof JMenuItem) {
                list.add(NbMenu.getNbMenu((JMenuItem)elements[k]));
                JMenuBarOperator menuOp = new JMenuBarOperator(menu);
                list.add(getMenuArrayList(menuOp.getMenu(k)));
            }
            /*if(elements[k] instanceof JMenuBar) {
                JMenuBarOperator menuOp = new JMenuBarOperator(menu);
                list.add(getMenuArrayList(menuOp.getMenu(0)));
            }
             */
        }
        return list;
    }
    
    /** Get Menu and tranfer it to ArrayList.
     * @param menu menu to be tranfered
     * @return tranfered menu */
    public static ArrayList getMenuArrayList(JMenu menu) {
        MenuElement [] elements = menu.getSubElements();
        ArrayList list = new ArrayList();
        
        for(int k=0; k < elements.length; k++) {
            
            if(elements[k] instanceof JPopupMenu)
                list.add(getPopupMenuArrayList((JPopupMenu)elements[k]));
            
            if(elements[k] instanceof JMenuItem)
                list.add(NbMenu.getNbMenu((JMenuItem)elements[k]));
            
        }
        return list;
    }
    
    /** Get PopupMenu and transfer it to ArrayList.
     * @param popup menu to be tranfered
     * @return transfered menu */
    public static ArrayList getPopupMenuArrayList(JPopupMenu popup) {
        MenuElement [] elements = popup.getSubElements();
        ArrayList list = new ArrayList();
        
        for(int k=0; k < elements.length; k++) {
            if(elements[k] instanceof JMenu)
                list.add(getMenuArrayList((JMenu)elements[k]));
            
            if(elements[k] instanceof JMenuItem)
                list.add(NbMenu.getNbMenu((JMenuItem)elements[k]));
        }
        return list;
    }
    
    /**
     * @param a aarray to be printed
     * @param stream where
     * @param x level of array */
    public static void printArray(ArrayList a, PrintStream stream, int x) {
        Iterator it = a.iterator();
        while(it.hasNext()) {
            Object o = it.next();
            
            if(o instanceof NbMenu) {
                
                for(int i=0;i<x;i++)
                    stream.print("-");
                
                stream.println(((NbMenu)o).name);
            }
            
            if(o instanceof ArrayList) {
                printArray((ArrayList)o, stream, x + 1);
            }
        }
    }
    
    /**
     * @param menu
     * @return  */
    public static TreeSet getSortedMenuBar(JMenuBar menu, String menuToTest) {
        
        StringTokenizer menuT = new StringTokenizer(menuToTest, ", ");
        HashSet menuTT = new HashSet();
        
        while(menuT.hasMoreTokens())
            menuTT.add(menuT.nextToken());
        
        MenuElement [] elements = menu.getSubElements();
        TreeSet list = new TreeSet();
        
        for(int k=0; k < elements.length; k++) {
            if(elements[k] instanceof JMenuItem) {
                //NbMenu m = NbMenu.getNbMenu((JMenuItem)elements[k]);
                JMenuBarOperator menuOp = new JMenuBarOperator(menu);
                //m.addSubMenu(getMenuArrayList(menuOp.getMenu(k)));
                JMenu m = menuOp.getMenu(k);
                if(menuTT.contains(m.getLabel())){
                    list.addAll((Collection)getSortedMenu(menuOp.getMenu(k)));
                }
            }
            /*if(elements[k] instanceof JMenuBar) {
                JMenuBarOperator menuOp = new JMenuBarOperator(menu);
                list.add(getMenuArrayList(menuOp.getMenu(0)));
            }
             */
        }
        return list;
    }
    
    /**
     * @param menu
     * @return  */
    public static TreeSet getSortedMenu(JMenu menu) {
        menu.list();
        MenuElement [] elements = menu.getSubElements();
        TreeSet list = new TreeSet();
        NbMenu last = NbMenu.getNbMenu(menu);
        list.add(last);
        
        for(int k=0; k < elements.length; k++) {
            if(elements[k] instanceof JPopupMenu) {
                //NbMenu last = (NbMenu)list.get(list.size() - 1);
                last.addSubMenu(getSortedPopupMenu((JPopupMenu)elements[k], ""));
            }
            if(elements[k] instanceof JMenuItem) {
                last = NbMenu.getNbMenu((JMenuItem)elements[k]);
                list.add(last);
            }
            
        }
        return list;
    }
    
    /**
     * @param popup
     * @return  */
    public static TreeSet getSortedPopupMenu(JPopupMenu popup, String menuNotTest) {
        StringTokenizer menuT = new StringTokenizer(menuNotTest, ", ");
        HashSet menuTT = new HashSet();
        
        while(menuT.hasMoreTokens())
            menuTT.add(menuT.nextToken());
        
        MenuElement [] elements = popup.getSubElements();
        TreeSet list = new TreeSet();
        
        for(int k=0; k < elements.length; k++) {
            
            if(elements[k] instanceof JMenu) {
                JMenu m = (JMenu) elements[k];
                if(!menuTT.contains(m.getLabel()))
                    list.addAll(getSortedMenu(m));
            }
            
            if(elements[k] instanceof JMenuItem) {
                list.add(NbMenu.getNbMenu((JMenuItem)elements[k]));
            }
        }
        return list;
    }
    
    /** Print (unsorted) structure of menu - as it really looks
     * @param menu
     * @param stream  */
    public static void printMenuBarStructure(JMenuBar menu, PrintStream stream, String menuToBeTested, boolean printEnabledOnly, boolean sorted) {
        if(sorted){
            printSorted(getSortedMenuBar(menu, menuToBeTested), stream, 1, printEnabledOnly);
            stream.close();
        }else
            printArray(getMenuBarArrayList(menu), stream, 1);
    }
    
    /** Print (unsorted) structure of menu - as it really looks
     * @param menu
     * @param stream  */
    public static void printPopupMenuStructure(JPopupMenu menu, PrintStream stream, String menuNotToBeTested, boolean printEnabledOnly, boolean sorted) {
        if(sorted){
            printSorted(getSortedPopupMenu(menu, menuNotToBeTested), stream, 1, printEnabledOnly);
            stream.close();
        }else{
            printArray(getPopupMenuArrayList(menu), stream, 1);
        }
    }
    
    /** Print Sorted collection.
     * @param a Collection to be sorted.
     * @param stream output stream
     * @param x indentation */
    public static void printSorted(Collection a, PrintStream stream, int x, boolean printEnabledOnly) {
        Iterator it = a.iterator();
        
        while(it.hasNext()) {
            Object o = it.next();
            if(o instanceof NbMenu) {
                NbMenu item = (NbMenu)o;
                
                if(!(printEnabledOnly ^ item.enabled)){
                    for(int i=0;i<x;i++) stream.print("-");
                    stream.println(item.name);
                }
                
                if(item.submenu != null) {
                    printSorted(item.getSubMenu(), stream, x+1, printEnabledOnly);
                }
                
            }
        }
    }
    
    public static String checkMnemonicCollision() {
        return checkMnemonicCollision(getMenuBarArrayList(MainWindowOperator.getDefault().getJMenuBar())).toString();
    }
    
    
    /** Check mnemonics in menu structure.
     * @param list
     * @return  */
    private static StringBuffer checkCollision(ArrayList list, boolean checkShortCuts) {
        StringBuffer collisions = new StringBuffer("");
        Iterator it = list.iterator();
        
        HashMap check = new HashMap();
        
        while(it.hasNext()) {
            Object o = it.next();
            
            if(o instanceof NbMenu) {
                NbMenu item = (NbMenu)o;
                
                if(checkShortCuts){
                    if(item.mnemo != 0) {
                        Integer mnemonic = new Integer(item.mnemo);
                        //stream.println("checking : " + item.name + " - " + item.mnemo);
                        if(check.containsKey(mnemonic)) {
                            char k = (char) item.mnemo;
                            collisions.append("\n !!!!!! Collision! mnemonic='" + k +  "' : " + item.name + " is in collision with " + check.get(mnemonic));
                        } else {
                            check.put(mnemonic, item.name);
                        }
                    }
                }else{
                    if(item.mnemo != 0) {
                        Integer mnemonic = new Integer(item.mnemo);
                        //stream.println("checking : " + item.name + " - " + item.mnemo);
                        if(check.containsKey(mnemonic)) {
                            char k = (char) item.mnemo;
                            collisions.append("\n !!!!!! Collision! mnemonic='" + k +  "' : " + item.name + " is in collision with " + check.get(mnemonic));
                        } else {
                            check.put(mnemonic, item.name);
                        }
                    }
                }
            }
            
            if(o instanceof ArrayList) {
                collisions.append(checkMnemonicCollision((ArrayList)o));
            }
        }
        
        return collisions;
    }
    
    
    
    /** Check mnemonics in menu structure.
     * @param list
     * @return  */
    private static StringBuffer checkMnemonicCollision(ArrayList list) {
        StringBuffer collisions = new StringBuffer("");
        Iterator it = list.iterator();
        
        HashMap check = new HashMap();
        
        while(it.hasNext()) {
            Object o = it.next();
            
            if(o instanceof NbMenu) {
                NbMenu item = (NbMenu)o;
                if(item.mnemo != 0) {
                    Integer mnemonic = new Integer(item.mnemo);
                    //stream.println("checking : " + item.name + " - " + item.mnemo);
                    if(check.containsKey(mnemonic)) {
                        char k = (char) item.mnemo;
                        collisions.append("\n !!!!!! Collision! mnemonic='" + k +  "' : " + item.name + " is in collision with " + check.get(mnemonic));
                    } else {
                        check.put(mnemonic, item.name);
                    }
                }
            }
            
            if(o instanceof ArrayList) {
                collisions.append(checkMnemonicCollision((ArrayList)o));
            }
        }
        
        return collisions;
    }
    
    
    
    public static String checkShortCutCollision() {
        return checkShortCutCollision(getMenuBarArrayList(MainWindowOperator.getDefault().getJMenuBar())).toString();
    }
    
    /** check shortcuts in menu structure
     * @param a
     * @return  */
    private static StringBuffer checkShortCutCollision(ArrayList a) {
        StringBuffer collisions = new StringBuffer("");
        Iterator it = a.iterator();
        HashMap check = new HashMap();
        
        while(it.hasNext()) {
            Object o = it.next();
            
            if(o instanceof NbMenu) {
                NbMenu item = (NbMenu)o;
                
                if(item.accelerator != null) {
                    //stream.println("checking : " + item.name + " - " + item.accelerator);
                    if(check.containsKey(item.accelerator)) {
                        collisions.append("\n !!!!!! Collision! accelerator ='" + item.accelerator +  "' : " + item.name + " is in collision with " + check.get(item.accelerator));
                    } else {
                        check.put(item.accelerator, item.name);
                    }
                }
            }
            
            if(o instanceof ArrayList) {
                collisions.append(checkShortCutCollision((ArrayList)o));
            }
        }
        
        return collisions;
    }
    
}


class NbMenu implements Comparable {
    /** label of menuitem */
    public String name;
    /** mnemonic in int */
    public int mnemo;
    /** jasne ? */
    public String accelerator;
    public boolean enabled;
    TreeSet submenu = null;
    
    /**
     * @param it
     * @return instance of NbMenu constructed from parameter it */
    public static NbMenu getNbMenu(JMenuItem it) {
        NbMenu item = new NbMenu();
        item.name = it.getText();//getLabel();
        item.accelerator = (it.getAccelerator() == null) ? null : it.getAccelerator().toString();
        item.mnemo = it.getMnemonic();
        item.enabled = it.isEnabled();
        return item;
    }
    /**
     * @param m  */
    public void addSubMenu(TreeSet m) {
        submenu = m;
    }
    
    /**
     * @return  */
    public TreeSet getSubMenu() {
        return submenu;
    }
    
    /** needed for comparing in TreeSet
     * @param obj
     * @return  */
    public int compareTo(Object obj) {
        NbMenu n = (NbMenu)obj;
        return (name != null) ? name.compareTo(n.name) : n.name.compareTo(name);
    }
}

