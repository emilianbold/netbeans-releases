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

/* $Id$ */

package org.netbeans.modules.form;

import org.openide.util.datatransfer.*;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;

/**
 *
 * @author Ian Formanek
 */
public class RADMenuComponent extends RADMenuItemComponent implements ComponentContainer {

    /** Hashtable, where keys are Integer(T_XXX), and values are Class[] - supported NewTypes
     * for the different menu types */
    static HashMap supportedNewMenu;
    /** Init supportedNewMenu table. */
    static {
        supportedNewMenu = new HashMap();
        supportedNewMenu.put(new Integer(T_MENUBAR), new Class[] { Menu.class });
        supportedNewMenu.put(new Integer(T_MENU), new Class[] { MenuItem.class, CheckboxMenuItem.class, Menu.class });
        supportedNewMenu.put(new Integer(T_POPUPMENU), new Class[] { MenuItem.class, CheckboxMenuItem.class, Menu.class });
        supportedNewMenu.put(new Integer(T_JMENUBAR), new Class[] { JMenu.class });
        supportedNewMenu.put(new Integer(T_JMENU), new Class[] { JMenuItem.class, JCheckBoxMenuItem.class, JRadioButtonMenuItem.class, JMenu.class });
        supportedNewMenu.put(new Integer(T_JPOPUPMENU), new Class[] { JMenuItem.class, JCheckBoxMenuItem.class, JRadioButtonMenuItem.class, JMenu.class });
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
        Class[] classes =(Class []) supportedNewMenu.get(new Integer(getMenuItemType()));

        if (classes == null)
            return RADComponent.NO_NEW_TYPES;

        NewType separator = createSeparatorNewType();
        NewType[] types = new NewType[classes.length +((separator != null) ? 1 : 0)];

        for (int i = 0; i < classes.length; i++) {
            types[i] = new NewMenuType(classes[i]);
        }
        if (separator != null)
            types[types.length - 1] = separator;

        return types;
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
            subComponents.add(initComponents[i]);
            ((RADMenuItemComponent)initComponents[i]).initParent(this);
            addVisualMenu((RADMenuItemComponent)initComponents[i]);
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

        getFormManager().fireComponentsReordered(this);
    }

    public void add(RADComponent comp) {
        if (!(comp instanceof RADMenuItemComponent)) throw new IllegalArgumentException();
        subComponents.add(comp);
        ((RADMenuItemComponent)comp).initParent(this);
        addVisualMenu((RADMenuItemComponent) comp);
        ((RADChildren)getNodeReference().getChildren()).updateKeys();
    }

    public void remove(RADComponent comp) {
        if (!(comp instanceof RADMenuItemComponent))
            throw new IllegalArgumentException();
        removeVisualMenu((RADMenuItemComponent)comp);
        subComponents.remove(comp);
        ((RADChildren)getNodeReference().getChildren()).updateKeys();
    }

    public int getIndexOf(RADComponent comp) {
        return subComponents.indexOf(comp);
    }

    /**  Adds the menu represented by the node */
    private void addVisualMenu(RADMenuItemComponent comp) {
        Object o = getBeanInstance();
        Object m = comp.getBeanInstance();
        Object dto = getDesignTimeMenus(getFormManager()).getDesignTime(o);
        Object dtm = getDesignTimeMenus(getFormManager()).getDesignTime(m);

        switch (getMenuItemType()) {
            case T_MENUBAR:
                ((MenuBar)o).add((Menu)m);
                ((JMenuBar)dto).add((JMenu) dtm);
                ((JMenuBar)dto).validate();
                break;
            case T_MENU:
                if (comp.getMenuItemType() == T_SEPARATOR) {
                    ((Menu)o).addSeparator();
                    ((JMenu)dto).addSeparator();
                } else {
                    ((Menu)o).add((MenuItem)m);
                    ((JMenu)dto).add((JMenuItem)dtm);
                }
                break;
            case T_POPUPMENU:
                if (comp.getMenuItemType() == T_SEPARATOR) {
                    ((Menu)o).addSeparator();
                    ((JPopupMenu)dto).addSeparator();
                } else {
                    ((Menu)o).add((MenuItem)m);
                    ((JPopupMenu)dto).add((JMenuItem)dtm);
                }
                break;
            case T_JMENUBAR:
                ((JMenuBar)o).add((JMenu)m);
                ((JMenuBar)o).validate();
                break;
            case T_JMENU:
                if (comp.getMenuItemType() == T_JSEPARATOR) {
                    ((JMenu)o).addSeparator();
                } else {
                    ((JMenu)o).add((JMenuItem)m);
                }
                break;
            case T_JPOPUPMENU:
                if (comp.getMenuItemType() == T_JSEPARATOR) {
                    ((JPopupMenu)o).addSeparator();
                } else {
                    ((JPopupMenu)o).add((JMenuItem)m);
                }
                break;
        }
    }

    /**  Removes the menu represented by the node */
    private void removeVisualMenu(RADMenuItemComponent comp) {
        Object o = getBeanInstance();
        Object m = comp.getBeanInstance();
        Object dto = getDesignTimeMenus(getFormManager()).getDesignTime(o);
        Object dtm = getDesignTimeMenus(getFormManager()).getDesignTime(m);

        switch (getMenuItemType()) {
            case T_MENUBAR:
                ((MenuBar)o).remove((Menu)m);
                ((JMenuBar)dto).remove((JMenu)dtm);
                ((JMenuBar)dto).validate();
                break;
            case T_MENU:
                if (comp.getMenuItemType() == T_SEPARATOR) {
                    ((Menu)o).remove(subComponents.indexOf(comp));
                    ((JMenu)dto).remove(subComponents.indexOf(comp));
                } else {
                    ((Menu)o).remove((MenuItem)m);
                    ((JMenu)dto).remove((JMenuItem)dtm);
                }
                break;
            case T_POPUPMENU:
                if (comp.getMenuItemType() == T_SEPARATOR) {
                    ((Menu)o).remove(subComponents.indexOf(comp));
                    // PENDING - dont know how to get reference to JPopupMenu.Separator
                    // so it is not supported by getDesignTimeMenu() !!
                    //((JPopupMenu)dto).remove((JPopupMenu.Separator)dtm);
                } else {
                    ((Menu)o).remove((MenuItem)m);
                    ((JPopupMenu)dto).remove((JMenuItem)dtm);
                }
                break;
            case T_JMENUBAR:
                ((JMenuBar)o).remove((JMenu)m);
                ((JMenuBar)o).validate();
                break;
            case T_JMENU:
                if (comp.getMenuItemType() == T_JSEPARATOR) {
                    ((JMenu)o).remove(subComponents.indexOf(comp));
                } else {
                    ((JMenu)o).remove((JMenuItem)m);
                }
                break;
            case T_JPOPUPMENU:
                if (comp.getMenuItemType() == T_JSEPARATOR) {
                    //XXX(-tdt)((JPopupMenu)o).remove((JPopupMenu.Separator)m);
                    ((JPopupMenu)o).remove(subComponents.indexOf(comp));
                } else {
                    ((JPopupMenu)o).remove((JMenuItem)m);
                }
                break;
        }
    }

    void freeMenu() {
        for (int i=0, n=subComponents.size(); i < n; i++)
            ((RADMenuItemComponent)subComponents.get(i)).freeMenu();
        super.freeMenu();
    }

    // -----------------------------------------------------------------------------
    // Debug methods

    // -----------------------------------------------------------------------------
    // Innerclasses

    /** @return NewType for creating separator or null if this node doesn't support it.
     */
    private NewType createSeparatorNewType() {
        int type = getMenuItemType();
        return((type == T_MENU) ||(type == T_POPUPMENU) ||
               (type == T_JMENU) ||(type == T_JPOPUPMENU)) ? new NewSeparatorType() : null;
    }

    /** NewType class for creating the separator */
    private class NewSeparatorType extends NewType {

        /** Help context for the creation action.
         * @return the help context
         */
        public org.openide.util.HelpCtx getHelpCtx() {
            return new org.openide.util.HelpCtx(this.getClass());
        }

        /** Display name for the creation action. This should be
         * presented as an item in a menu.
         *
         * @return the name of the action
         */
        public String getName() {
            return FormEditor.getFormBundle().getString("CTL_separator");
        }

        /** Create the object.
         * @exception IOException if something fails
         */
        public void create() throws IOException {
            RADMenuItemComponent newSeparatorComp = new RADMenuItemComponent();
            newSeparatorComp.initialize(getFormManager());
            if ((getMenuItemType() == T_MENU) ||(getMenuItemType() == T_POPUPMENU)) {
                newSeparatorComp.setComponent(org.netbeans.modules.form.Separator.class);
            } else {
                newSeparatorComp.setComponent(JSeparator.class);
            }
            getFormManager().addNonVisualComponent(newSeparatorComp, RADMenuComponent.this);
            //XXX(-tdt) addVisualMenu(newSeparatorComp);
            getFormManager().selectComponent(newSeparatorComp, false);
            return;
        }
    }


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
            if (FormEditor.getFormSettings().getShortBeanNames()) {
                int index = s.lastIndexOf('.');
                if (index != -1)
                    return s.substring(index + 1);
            }
            return s;
        }

        /** Create the object.
         * @exception IOException if something fails
         */
        public void create() throws IOException {
            RADMenuItemComponent newMenuComp;

            if ((RADMenuItemComponent.recognizeType(item) & MASK_CONTAINER) == 0) {
                newMenuComp = new RADMenuItemComponent();
            }
            else {
                newMenuComp = new RADMenuComponent();
            }

            newMenuComp.initialize(RADMenuComponent.this.getFormManager());
            newMenuComp.setComponent(item);
            if (newMenuComp instanceof RADMenuComponent) {
                ((RADMenuComponent)newMenuComp).initSubComponents(new RADComponent[0]);
            }
            RADMenuComponent.this.getFormManager().addNonVisualComponent(newMenuComp, RADMenuComponent.this);

            // for some components, we initialize their properties with some non-default values
            // e.g. a label on buttons, checkboxes
            FormEditor.defaultMenuInit(newMenuComp);
            addVisualMenu(newMenuComp);

            RADMenuComponent.this.getFormManager().selectComponent(newMenuComp, false);
        }
    }
}
