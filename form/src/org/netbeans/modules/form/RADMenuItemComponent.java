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

  static final Object DUMMY_SEPARATOR_INSTANCE = new Object ();
  
  /** A JDK 1.1 serial version UID */
//  static final long serialVersionUID = -6333847833552116543L;

  /** Type of container */
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
  
  /** No synthetic properties for AWT Separator */
  protected org.openide.nodes.Node.Property[] createSyntheticProperties () {
    if (type == T_SEPARATOR) return RADComponent.NO_PROPERTIES;
    else return super.createSyntheticProperties ();
  }
  
  public RADMenuComponent getParentMenu () {
    return parent;
  }
  
// -----------------------------------------------------------------------------
// Public interface

  public void setComponent (Class beanClass) {
    type = recognizeType(beanClass);
    // to initialize the type before calling super.setComponent is crucial, 
    // as the type is used in various methods called from the setComponent
    // (e.g. the createSyntheticProperties () relies on this order to correctly
    //  provide no properties for AWT menu separators)

    super.setComponent (beanClass);

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

  /** Test the given class if is is subclass of one of four basic classes and
  * return adequate T_XXX constant.
  */
  static int recognizeType(Class cl) {
    if (JSeparator.class.isAssignableFrom(cl)) return T_JSEPARATOR;
    if (com.netbeans.developer.modules.loaders.form.Separator.class.isAssignableFrom(cl)) return T_SEPARATOR;
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
        if (!getFormManager ().isTestMode () && hasDefaultEvent ()) attachDefaultEvent ();
      }
    };
  }

}

/*
 * Log
 *  7    Gandalf   1.6         11/10/99 Pavel Buzek     while menu is selectedf 
 *       in form in test mode do not go to event handler
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/9/99  Ian Formanek    Fixed bug 4411 - Delete 
 *       of a jMenuItem does not work. (No action is performed.)
 *  4    Gandalf   1.3         9/6/99   Ian Formanek    Correctly works with 
 *       separators - fixes bug 3703 - When a new separator is created usng New 
 *       > Separator in a menu, an exception is thrown.
 *  3    Gandalf   1.2         8/6/99   Ian Formanek    setComponent is public
 *  2    Gandalf   1.1         7/16/99  Ian Formanek    default action
 *  1    Gandalf   1.0         7/5/99   Ian Formanek    
 * $
 */
