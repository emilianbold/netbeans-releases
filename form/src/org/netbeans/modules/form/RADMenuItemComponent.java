/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.form;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.text.MessageFormat;

/** The RADMenuItemComponent represents one menu item component placed on the Form.
*
* @author Petr Hamernik, Ian Formanek
*/
public class RADMenuItemComponent extends RADComponent {

  /** A JDK 1.1 serial version UID */
//  static final long serialVersionUID = -6333847833552116543L;

  /** Type of container */
  private int type;

  /** Possible constants for type variable */
  static final int T_MENUBAR             = 0x1110;
  static final int T_MENUITEM            = 0x0011;
  static final int T_CHECKBOXMENUITEM    = 0x0012;
  static final int T_MENU                = 0x0113;
  static final int T_POPUPMENU           = 0x1114;
  
  static final int T_JPOPUPMENU          = 0x1125;
  static final int T_JMENUBAR            = 0x1126;
  static final int T_JMENUITEM           = 0x0027;
  static final int T_JCHECKBOXMENUITEM   = 0x0028;
  static final int T_JMENU               = 0x0129;
  static final int T_JRADIOBUTTONMENUITEM= 0x002A;

  /** Masks for the T_XXX constants */
  static final int MASK_AWT              = 0x0010;
  static final int MASK_SWING            = 0x0020;
  static final int MASK_CONTAINER        = 0x0100;
  static final int MASK_ROOT             = 0x1000;

  /** Icons for java data objects. * /
  static protected Image iconMenuBar;
  static protected Image iconPopupMenu;
  static protected Image iconMenu;
  static protected Image iconMenuItem;
  static protected Image iconCheckBoxMenuItem;
  static protected Image iconRadioBoxMenuItem;

  static {
    Toolkit t = java.awt.Toolkit.getDefaultToolkit();
    Class cl = Object.class;
    
    iconMenuBar = t.getImage(cl.getResource("/com/netbeans/developerx/resources/palette/menubar.gif"));
    iconPopupMenu = t.getImage(cl.getResource("/com/netbeans/developerx/resources/palette/popupmenu.gif"));
    iconMenu = t.getImage(cl.getResource("/com/netbeans/developerx/resources/form/menu.gif"));
    iconMenuItem = t.getImage(cl.getResource("/com/netbeans/developerx/resources/form/menuItem.gif"));
    iconCheckBoxMenuItem = t.getImage(cl.getResource("/com/netbeans/developerx/resources/form/menuCheckItem.gif"));
    iconRadioBoxMenuItem = t.getImage(cl.getResource("/com/netbeans/developerx/resources/form/menuRadioItem.gif"));
  }
*/
  /** Names of the properties */
  private static final String PROP_TEXT = "text";
  private static final String PROP_LABEL = "label";
  
  /** The MessageFormat for component names */
  private static MessageFormat menuNameFormat =
    new MessageFormat(FormEditor.getFormBundle().getString("FMT_MenuName"));
  
// -----------------------------------------------------------------------------
// Private properties

  transient private RADMenuComponent parent;

// -----------------------------------------------------------------------------
// Initialization

  void initParent (RADMenuComponent parent) {
    this.parent = parent;
  }
  
// -----------------------------------------------------------------------------
// Public interface

  public void setComponent (Class beanClass) {
    super.setComponent (beanClass);
    type = recognizeType(beanClass);

    Object o = getBeanInstance();
    if (o instanceof MenuItem) {
      ((MenuItem)o).addActionListener(getDefaultActionListener());
    }
    else if (o instanceof JMenuItem) {
      ((JMenuItem)o).addActionListener(getDefaultActionListener());
    }
  }

  int getMenuItemType () {
    return type;
  }

  String getItemLabel () {
    return "label"; // [PENDING]
  }

  /** Finds an icon for this component.
  * @see java.bean.BeanInfo
  * @param type constants from <CODE>java.bean.BeanInfo</CODE>
  * @return icon to use to represent the bean
  * /
  public java.awt.Image getIcon (int type) {
    switch (this.type) {
      case T_MENUBAR: return iconMenuBar;
      case T_MENUITEM: return iconMenuItem;
      case T_CHECKBOXMENUITEM: return iconCheckBoxMenuItem;
      case T_MENU: return iconMenu;
      case T_POPUPMENU: return iconPopupMenu;
      case T_JPOPUPMENU: return iconPopupMenu;
      case T_JMENUBAR: return iconMenuBar;
      case T_JMENUITEM: return iconMenuItem;
      case T_JCHECKBOXMENUITEM: return iconCheckBoxMenuItem;
      case T_JMENU: return iconMenu;
      case T_JRADIOBUTTONMENUITEM: return iconRadioBoxMenuItem;
      default: return super.getIcon(type);
    }
  }

  public java.awt.Image getOpenedIcon (int type) {
    return getIcon(type);
  }

  /** Test the given class if is is subclass of one of four basic classes and
  * return adequate T_XXX constant.
  */
  static int recognizeType(Class cl) {
    if (PopupMenu.class.isAssignableFrom(cl)) return T_POPUPMENU;
    if (Menu.class.isAssignableFrom(cl)) return  T_MENU;
    if (CheckboxMenuItem.class.isAssignableFrom(cl)) return T_CHECKBOXMENUITEM;
    if (MenuItem.class.isAssignableFrom(cl)) return  T_MENUITEM;
    if (MenuBar.class.isAssignableFrom(cl)) return T_MENUBAR;
    if (JRadioButtonMenuItem.class.isAssignableFrom(cl)) return T_JRADIOBUTTONMENUITEM;
    if (JMenu.class.isAssignableFrom(cl)) return T_JMENU;
    if (JCheckBoxMenuItem.class.isAssignableFrom(cl)) return T_JCHECKBOXMENUITEM;
    if (JMenuItem.class.isAssignableFrom(cl)) return T_JMENUITEM;
    if (JMenuBar.class.isAssignableFrom(cl)) return T_JMENUBAR;
    if (JPopupMenu.class.isAssignableFrom(cl)) return T_JPOPUPMENU;

    throw new InternalError ("Cannot create RADMenuItemComponent for nonmenu class");
  }


  private ActionListener getDefaultActionListener() {
    return new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        if (hasDefaultEvent ()) attachDefaultEvent ();
      }
    };
  }

  protected String formatName() {
    String beanClassName = getBeanClass().getName();
    String clName = beanClassName;
    int index = beanClassName.lastIndexOf(".");
    if (index != -1)
      clName = beanClassName.substring(index + 1);
    return menuNameFormat.format(new Object[] { getName(), clName, getItemLabel() });
  }


}

/*
 * Log
 *  3    Gandalf   1.2         8/6/99   Ian Formanek    setComponent is public
 *  2    Gandalf   1.1         7/16/99  Ian Formanek    default action
 *  1    Gandalf   1.0         7/5/99   Ian Formanek    
 * $
 */
