/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import org.openide.util.datatransfer.*;

import java.awt.*;
import javax.swing.*;
import java.util.*;

/**
 *
 * @author Ian Formanek
 */

public class RADMenuComponent extends RADMenuItemComponent implements ComponentContainer {

    /** Map of possible combinations of menus in menus. Menu types (as Integer)
     * are mapped to supported (sub)menu types (as Class[]).
     */
    static Map supportedMenus;
    
    /** Initialization of supportedMenus map. */
    static {
        supportedMenus = new HashMap();
        supportedMenus.put(new Integer(T_MENUBAR),
                           new Class[] { Menu.class });
        supportedMenus.put(new Integer(T_MENU),
                           new Class[] { MenuItem.class,
                                         CheckboxMenuItem.class,
                                         Menu.class,
                                         Separator.class });
        supportedMenus.put(new Integer(T_POPUPMENU),
                           new Class[] { MenuItem.class,
                                         CheckboxMenuItem.class,
                                         Menu.class,
                                         Separator.class });
        supportedMenus.put(new Integer(T_JMENUBAR),
                           new Class[] { JMenu.class });
        supportedMenus.put(new Integer(T_JMENU),
                           new Class[] { JMenuItem.class,
                                         JCheckBoxMenuItem.class,
                                         JRadioButtonMenuItem.class,
                                         JMenu.class,
                                         JSeparator.class });
        supportedMenus.put(new Integer(T_JPOPUPMENU),
                           new Class[] { JMenuItem.class,
                                         JCheckBoxMenuItem.class,
                                         JRadioButtonMenuItem.class,
                                         JMenu.class,
                                         JSeparator.class });
    }

    // -----------------------------------------------------------------------------
    // Private variables

    private ArrayList subComponents;

    // -----------------------------------------------------------------------------
    // Initialization

    /** Support for new types that can be created in this node.
     * @return array of new type operations that are allowed
     */
    public NewType[] getNewTypes() {
        if (isReadOnly())
            return RADComponent.NO_NEW_TYPES;

        Class[] classes = (Class[])
                          supportedMenus.get(new Integer(getMenuItemType()));

        if (classes == null)
            return RADComponent.NO_NEW_TYPES;

        NewType[] types = new NewType[classes.length];
        for (int i = 0; i < types.length; i++)
            types[i] = new NewMenuType(classes[i]);

        return types;
    }

    public boolean canAddItem(Class itemType) {
        Class[] classes = (Class[])
                          supportedMenus.get(new Integer(getMenuItemType()));

        if (classes != null)
            for (int i=0; i < classes.length; i++)
                if (classes[i] == itemType) // or more general isAssignableFrom ??
                    return true;

        return false;
    }

    // -----------------------------------------------------------------------------
    // SubComponents Management

    public RADComponent[] getSubBeans() {
        RADComponent[] components = new RADComponent [subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    public void initSubComponents(RADComponent[] initComponents) {
        subComponents = new ArrayList(initComponents.length);
        for (int i = 0; i < initComponents.length; i++) {
            RADComponent comp = initComponents[i];
            if (comp instanceof RADMenuItemComponent) {
                subComponents.add(comp);
                comp.setParentComponent(this);
            }
        }
    }

    public void reorderSubComponents(int[] perm) {
        // XXX(-tdt) must make a copy of the component list, otherwise removing
        // menu separator will break

        // make a copy of item list
        ArrayList list = new ArrayList(subComponents.size());
        list.addAll(subComponents);

        // shuffle the copy
        for (int i = 0; i < perm.length; i++) {
            int from = i;
            int to = perm[i];
            if (from == to) continue;
            Object value = list.remove(from);
            if (from < to) {
                list.add(to - 1, value);
            } else {
                list.add(to, value);
            }
        }

        // remove and re-add in new order
        for (int i = 0, n = list.size(); i < n; i++)
            remove((RADMenuItemComponent) list.get(i));
        for (int i = 0, n = list.size(); i < n; i++)
            add((RADMenuItemComponent) list.get(i));
//        getFormModel().fireComponentsReordered(this);
    }

    public void add(RADComponent comp) {
        if (comp instanceof RADMenuItemComponent) {
            subComponents.add(comp);
            comp.setParentComponent(this);
//            getNodeReference().updateChildren();
        }
    }

    public void remove(RADComponent comp) {
        if (subComponents.remove(comp))
            comp.setParentComponent(null);
//        getNodeReference().updateChildren();
    }

    public int getIndexOf(RADComponent comp) {
        return subComponents.indexOf(comp);
    }

    // -------------
    // Innerclasses

    /** NewType for creating sub-MenuItem. */
    class NewMenuType extends NewType {
        /** Class which represents the menu class for this NewType */
        Class item;

        /** Constructs new NewType for the given menu class */
        public NewMenuType(Class item) {
            this.item = item;
        }

        /** Display name for the creation action. This should be
         * presented as an item in a menu.
         *
         * @return the name of the action
         */
        public String getName() {
            String s = item.getName();

            int index = s.lastIndexOf('.');
            if (index != -1)
                return s.substring(index + 1);
            else
                return s;
        }

        /** Create the object.
         * @exception IOException if something fails
         */
        public void create() throws java.io.IOException {
            getFormModel().getComponentCreator()
                .createComponent(item, RADMenuComponent.this, null);
        }
    }
}
