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

import java.awt.*;
import javax.swing.*;

/**
 * RADMenuItemComponent represents one menu item component in the Form.
 *
 * @author Petr Hamernik, Ian Formanek
 */

public class RADMenuItemComponent extends RADComponent {

    /** Type of menu */
    private int type;

    /** Possible constants for type variable */
    static final int T_MENUBAR              = 0x01110;
    static final int T_MENUITEM             = 0x00011;
    static final int T_CHECKBOXMENUITEM     = 0x00012;
    static final int T_MENU                 = 0x00113;
    static final int T_POPUPMENU            = 0x01114;

    static final int T_JPOPUPMENU           = 0x01125;
    static final int T_JMENUBAR             = 0x01126;
    static final int T_JMENUITEM            = 0x00027;
    static final int T_JCHECKBOXMENUITEM    = 0x00028;
    static final int T_JMENU                = 0x00129;
    static final int T_JRADIOBUTTONMENUITEM = 0x0002A;

    static final int T_SEPARATOR            = 0x1001B;
    static final int T_JSEPARATOR           = 0x1002C;

    /** Masks for the T_XXX constants */
    static final int MASK_AWT               = 0x00010;
    static final int MASK_SWING             = 0x00020;
    static final int MASK_CONTAINER         = 0x00100;
    static final int MASK_ROOT              = 0x01000;
    static final int MASK_SEPARATOR         = 0x10000;


    public Object initInstance(Class beanClass) throws Exception {
        type = recognizeType(beanClass);
        return super.initInstance(beanClass);
    }

    /** No synthetic properties for AWT Separator */
    protected org.openide.nodes.Node.Property[] createSyntheticProperties() {
        if (type == T_SEPARATOR) return RADComponent.NO_PROPERTIES;
        else return super.createSyntheticProperties();
    }

    int getMenuItemType() {
        return type;
    }

    /** Recognizes type of the menu from its class.
     * @return adequate T_XXX constant
     */
    static int recognizeType(Class cl) {
        if (JSeparator.class.isAssignableFrom(cl))
            return T_JSEPARATOR;
        if (org.netbeans.modules.form.Separator.class.isAssignableFrom(cl))
            return T_SEPARATOR;
        if (PopupMenu.class.isAssignableFrom(cl))
            return T_POPUPMENU;
        if (Menu.class.isAssignableFrom(cl))
            return  T_MENU;
        if (CheckboxMenuItem.class.isAssignableFrom(cl))
            return T_CHECKBOXMENUITEM;
        if (MenuItem.class.isAssignableFrom(cl))
            return  T_MENUITEM;
        if (MenuBar.class.isAssignableFrom(cl))
            return T_MENUBAR;
        if (JRadioButtonMenuItem.class.isAssignableFrom(cl))
            return T_JRADIOBUTTONMENUITEM;
        if (JMenu.class.isAssignableFrom(cl))
            return T_JMENU;
        if (JCheckBoxMenuItem.class.isAssignableFrom(cl))
            return T_JCHECKBOXMENUITEM;
        if (JMenuItem.class.isAssignableFrom(cl))
            return T_JMENUITEM;
        if (JMenuBar.class.isAssignableFrom(cl))
            return T_JMENUBAR;
        if (JPopupMenu.class.isAssignableFrom(cl))
            return T_JPOPUPMENU;

        throw new IllegalArgumentException("Cannot create RADMenuItemComponent for class: "+cl.getName()); // NOI18N
    }
}
