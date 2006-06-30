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

package org.netbeans.modules.form;

import org.openide.util.datatransfer.NewType;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import org.netbeans.modules.form.project.ClassSource;

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
        if (subComponents == null)
            subComponents = new ArrayList(initComponents.length);
        else {
            subComponents.clear();
            subComponents.ensureCapacity(initComponents.length);
        }

        for (int i = 0; i < initComponents.length; i++) {
            RADComponent comp = initComponents[i];
            if (comp instanceof RADMenuItemComponent) {
                subComponents.add(comp);
                comp.setParentComponent(this);
            }
        }
    }

    public void reorderSubComponents(int[] perm) {
        RADComponent[] components = new RADComponent[subComponents.size()];
        for (int i=0; i < perm.length; i++)
            components[perm[i]] = (RADComponent) subComponents.get(i);

        subComponents.clear();
        subComponents.addAll(Arrays.asList(components));
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
                .createComponent(new ClassSource(item.getName(), null, null),
                                 RADMenuComponent.this,
                                 null);
        }
    }
}
