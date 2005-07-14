/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.awt;

import com.sun.rsasign.m;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.openide.util.Utilities;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;

/**
 *
 * @author mkleint
 */
class DynaMenuModel {
    private static final Icon BLANK_ICON = new ImageIcon(
        Utilities.loadImage("org/openide/resources/empty.gif")); // NOI18N            
    
    private List menuItems;
    private HashMap actionToMenuMap;
    private boolean isWithIcons = false;
    /** Creates a new instance of DynaMenuModel */
    public DynaMenuModel() {
        actionToMenuMap = new HashMap();
    }
    
    public void loadSubmenu(List cInstances, JMenu m) {
        // clear first - refresh the menu's content
        boolean addSeparator = false;
        Icon curIcon = null;
        Iterator it = cInstances.iterator();
        menuItems = new ArrayList(cInstances.size());
        actionToMenuMap.clear();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof Presenter.Menu) {
                // does this still apply??
                obj = ((Presenter.Menu)obj).getMenuPresenter();
            }
            if (obj instanceof DynamicMenuContent) {
                if(addSeparator) {
                    menuItems.add(null);
                    addSeparator = false;
                }
                DynamicMenuContent mn = (DynamicMenuContent)obj;
                JComponent[] itms = convertArray(mn.getMenuPresenters());
                actionToMenuMap.put(mn, itms);
                Iterator itx = Arrays.asList(itms).iterator();
                while (itx.hasNext()) {
                    JComponent comp = (JComponent)itx.next();
                    menuItems.add(comp);
                    // check icon
                    isWithIcons = checkIcon(comp, isWithIcons);
                }
                continue;
            } 
            
            
            if (obj instanceof JMenuItem) {
                if(addSeparator) {
                    menuItems.add(null);
                    addSeparator = false;
                }
                // check icon
                isWithIcons = checkIcon(obj, isWithIcons);
                menuItems.add((JMenuItem)obj);
            } else if (obj instanceof JSeparator) {
                addSeparator = menuItems.size() > 0;
            } else if (obj instanceof Action) {
                if(addSeparator) {
                    menuItems.add(null);
                    addSeparator = false;
                }
                Action a = (Action)obj;
                JMenuItem item = new JMenuItem();
                Actions.connect(item, a, false);
                // check icon
                isWithIcons = checkIcon(item, isWithIcons);
                menuItems.add(item);
            }
        }
        
        if (isWithIcons) {
            menuItems = alignVertically(menuItems);
        }
        
        // fill menu with built items
        JComponent curItem = null;
        boolean wasSeparator = false;
        for (Iterator iter = menuItems.iterator(); iter.hasNext(); ) {
            curItem = (JComponent)iter.next();
            if (curItem != null) {
                m.add(curItem);
                wasSeparator = curItem instanceof JSeparator;
            } else {
                // null means separator
                if (!wasSeparator) {
                    m.addSeparator();
                }
            }
        }
    }
    
    
    private boolean checkIcon(Object obj, boolean isWithIconsAlready) {
        if (isWithIconsAlready) {
            return isWithIconsAlready;
        }
        if (obj instanceof JMenuItem) {
            if (((JMenuItem)obj).getIcon() != null) {
                return true;
            }
        }
        return false;
    }
    
    public void checkSubmenu(JMenu menu) {
        Iterator it = actionToMenuMap.entrySet().iterator();
        boolean oldisWithIcons = isWithIcons;
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            DynamicMenuContent pres = (DynamicMenuContent)entry.getKey();
            JComponent[] old = (JComponent[])entry.getValue();
            int oldIndex = 0;
            Component[] menuones = menu.getPopupMenu().getComponents();
            int menuIndex = old.length > 0 ? findFirstItemIndex(old[0], menuones) : -1;
            JComponent[] newones = convertArray(pres.synchMenuPresenters(old));
            if (!compareEqualArrays(old, newones)) {
                if (menuIndex < 0) {
                    //warning
                    menuIndex = 0;
                    System.out.println("what's up menu index is  not found..");
                } else {
                    for (int i = 0; i < old.length; i++) {
                        if (old[i] != null) {
                            menu.getPopupMenu().remove(old[i]);
                        }
                    }
                }
                for (int i = 0; i < newones.length; i++) {
                    ///TODO now what to do with icon alignments..
                    JComponent one = newones[i];
                    menu.getPopupMenu().add(one, i + menuIndex);
                }
                entry.setValue(newones);
            }
            
        }
        
        if (isWithIcons != oldisWithIcons) {
            menuItems = alignVertically(menuItems);
        }
        
    }
    
    private JComponent[] convertArray(JComponent[] arr) {
        if (arr == null || arr.length == 0) {
            return new JComponent[0];
        }
        JComponent[] toRet = new JComponent[arr.length];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null) {
                toRet[i] = new JSeparator();
            } else {
                toRet[i] = arr[i];
            }
        }
        return toRet;
    }
    
    private int findFirstItemIndex(JComponent first, Component[] menuItems) {
        for (int i = 0; i < menuItems.length; i++) {
            if (first == menuItems[i]) {
                return i;
            }
        }
        return -1;
    }
    
    private boolean compareEqualArrays(JComponent[] one, JComponent[] two) {
        if (one.length != two.length) {
            return false;
        }
        for (int i = 0; i < one.length; i++) {
            if (one[i] != two[i]) {
                return false;
            }
        }
        return true;
    }
    
    /** Removes icons from all direct menu items of this menu.
     * Not recursive, */
    private List alignVertically(List menuItems) {
        List result = new ArrayList(menuItems.size());
        JMenuItem curItem = null;
        for (Iterator iter = menuItems.iterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            if (obj instanceof JMenuItem) {
                curItem = (JMenuItem)obj;
                if (curItem != null && curItem.getIcon() == null) {
                    curItem.setIcon(BLANK_ICON);
                }
            }
            result.add(obj);
        }
        return result;
    }
}
