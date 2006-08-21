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

package org.openide.awt;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.swing.JPopupMenu;
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
        Utilities.loadImage("org/openide/loaders/empty.gif")); // NOI18N            
    
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
                Actions.MenuItem item = new Actions.MenuItem(a, true);
                // check icon
                isWithIcons = checkIcon(item, isWithIcons);
                actionToMenuMap.put(item, new JComponent[] {item});
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
            if (curItem == null) {
                // null means separator
                curItem = new JSeparator();
            }
            m.add(curItem);
            boolean isSeparator = curItem instanceof JSeparator;
            if (isSeparator && wasSeparator) {
                curItem.setVisible(false);
            }
            if (!(curItem instanceof InvisibleMenuItem)) {
                wasSeparator = isSeparator;
            }
        }
    }
    
    
    private boolean checkIcon(Object obj, boolean isWithIconsAlready) {
        if (isWithIconsAlready) {
            return isWithIconsAlready;
        }
        if (obj instanceof JMenuItem) {
            if (((JMenuItem)obj).getIcon() != null && !BLANK_ICON.equals(((JMenuItem)obj).getIcon())) {
                return true;
            }
        }
        return false;
    }
    
    public void checkSubmenu(JMenu menu) {
        Iterator it = actionToMenuMap.entrySet().iterator();
        boolean oldisWithIcons = isWithIcons;
        boolean changed = false;
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            DynamicMenuContent pres = (DynamicMenuContent)entry.getKey();
            JComponent[] old = (JComponent[])entry.getValue();
            int oldIndex = 0;
            Component[] menuones = menu.getPopupMenu().getComponents();
            int menuIndex = old.length > 0 ? findFirstItemIndex(old[0], menuones) : -1;
            JComponent[] newones = convertArray(pres.synchMenuPresenters(unconvertArray(old)));
            if (!compareEqualArrays(old, newones)) {
                if (menuIndex < 0) {
                    menuIndex = 0;
                } else {
                    for (int i = 0; i < old.length; i++) {
                        if (old[i] != null) {
                            menu.getPopupMenu().remove(old[i]);
                            menuItems.remove(old[i]);
                        }
                    }
                }
                for (int i = 0; i < newones.length; i++) {
                    ///TODO now what to do with icon alignments..
                    JComponent one = newones[i];
                    menu.getPopupMenu().add(one, i + menuIndex);
                    changed = true;
                    menuItems.add(one);
                    boolean thisOneHasIcon = checkIcon(one, false);
                    if (!thisOneHasIcon && isWithIcons) {
                        alignVertically(Collections.singletonList(one));
                    }
                    if (thisOneHasIcon && !isWithIcons) {
                        isWithIcons = true;
                    }
                }
                entry.setValue(newones);
            }
            
        }
        boolean hasAnyIcons = false;
        Component[] menuones = menu.getPopupMenu().getComponents();
        for (int i = 0; i < menuones.length; i++) {
            if (menuones[i] != null) {
                hasAnyIcons = checkIcon(menuones[i], hasAnyIcons);
                if (hasAnyIcons) {
                    break;
                }
            }
        }
        checkSeparators(menuones, menu.getPopupMenu());
        if (!hasAnyIcons && isWithIcons) {
            isWithIcons = false;
        }
        if (oldisWithIcons != isWithIcons) {
            menuItems = alignVertically(menuItems);
        }
        if (changed && Utilities.isWindows()) {
            //#67847 on windows, we need revalidation otherwise strange effects kick in..
            menu.getPopupMenu().revalidate();
        }
    }
    
    static void checkSeparators(Component[] menuones, JPopupMenu parent) {
        boolean wasSeparator = false;
        for (int i = 0; i < menuones.length; i++) {
            Component curItem = menuones[i];
            if (curItem != null) {
                boolean isSeparator = curItem instanceof JSeparator;
                if (isSeparator) {
                    boolean isVisible = curItem.isVisible();
                    if (isVisible != !wasSeparator) {
                        //MACOSX whenever a property like enablement or visible is changed, need to remove and add.
                        // could be possibly split to work differetly on other platform..
                        parent.remove(i);
                        JSeparator newOne = new JSeparator();
                        newOne.setVisible(!wasSeparator);
                        parent.add(newOne, i);
                    }
                }
                if (!(curItem instanceof InvisibleMenuItem)) {
                    wasSeparator = isSeparator;
                }
            }
        }
    }
    
    private JComponent[] convertArray(JComponent[] arr) {
        if (arr == null || arr.length == 0) {
            return new JComponent[] { new InvisibleMenuItem() };
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
    
    private JComponent[] unconvertArray(JComponent[] arr) {
        if (arr.length == 1 && arr[0] instanceof InvisibleMenuItem) {
            return new JComponent[0];
        } else {
            return arr;
        }
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
                if (isWithIcons && curItem != null && curItem.getIcon() == null) {
                    curItem.setIcon(BLANK_ICON);
                } else if (!isWithIcons && curItem != null) {
                    curItem.setIcon(null);
                }
            }
            result.add(obj);
        }
        return result;
    }
    
    static final class InvisibleMenuItem extends JMenuItem {
        
        public boolean isVisible() {
            return false;
        }
        
    }
}
