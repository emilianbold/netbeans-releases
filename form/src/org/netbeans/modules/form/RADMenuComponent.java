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

  /** Default icons for menu separators. */
//  static protected Image sepIcon;
//  static protected Image sepIcon32;

  /** Init icons * /
  static {
    sepIcon = Toolkit.getDefaultToolkit ().getImage (
      Object.class.getResource ("/com/netbeans/developerx/resources/form/menuSeparator.gif"));
    sepIcon32 = Toolkit.getDefaultToolkit ().getImage (
      Object.class.getResource ("/com/netbeans/developerx/resources/form/menuSeparator32.gif"));
  }
*/
// -----------------------------------------------------------------------------
// Private variables

  private ArrayList subComponents;

// -----------------------------------------------------------------------------
// Initialization

  /** Support for new types that can be created in this node.
  * @return array of new type operations that are allowed
  */
  public NewType[] getNewTypes () {
    Class[] classes = (Class []) supportedNewMenu.get(new Integer(getMenuItemType ()));

    if (classes == null)
      return RADComponent.NO_NEW_TYPES;

    NewType separator = createSeparatorNewType();
    NewType[] types = new NewType[classes.length + ((separator != null) ? 1 : 0)];

    for (int i = 0; i < classes.length; i++) {
      types[i] = new NewMenuType(classes[i]);
    }
    if (separator != null)
      types[types.length - 1] = separator;
    
    return types;
  }

// -----------------------------------------------------------------------------
// SubComponents Management

  public RADComponent[] getSubBeans () {
    RADComponent[] components = new RADComponent [subComponents.size ()];
    subComponents.toArray (components);
    return components;
  }
  
  public void initSubComponents (RADComponent[] initComponents) {
    subComponents = new ArrayList (initComponents.length);
    for (int i = 0; i < initComponents.length; i++) {
      subComponents.add (initComponents[i]);
      ((RADMenuItemComponent)initComponents[i]).initParent (this);
      addVisualMenu (initComponents[i]);
    }
  }

  public void reorderSubComponents (int[] perm) {
    for (int i = 0; i < perm.length; i++) {
      int from = i;
      int to = perm[i];
      if (from == to) continue;
      Object value = subComponents.remove (from);
      if (from < to) {
        subComponents.add (to - 1, value);
      } else {
        subComponents.add (to, value);
      }
    }
    getFormManager ().fireComponentsReordered (this);
  }

  public void add (RADComponent comp) {
    if (!(comp instanceof RADMenuItemComponent)) throw new IllegalArgumentException ();
    subComponents.add (comp);
    ((RADMenuItemComponent)comp).initParent (this);
    ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
  }

  public void remove (RADComponent comp) {
    if (!(comp instanceof RADMenuItemComponent)) throw new IllegalArgumentException ();
    // [PENDING]
    ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
  }

  public int getIndexOf (RADComponent comp) {
    if (!(comp instanceof RADMenuItemComponent)) throw new IllegalArgumentException ();
    return subComponents.indexOf (comp);
  }

  /**  Adds the menu represented by the node */
  private void addVisualMenu (RADComponent comp) {
    Object o = getBeanInstance();
    if (comp instanceof RADMenuItemComponent) {
      Object m = ((RADMenuItemComponent) comp).getBeanInstance();

      switch (getMenuItemType ()) {
        case T_MENUBAR:
          ((MenuBar)o).add((Menu)m);
          break;
        case T_MENU:
        case T_POPUPMENU:
          ((Menu)o).add((MenuItem)m);
          break;
        case T_JMENUBAR:
          ((JMenuBar)o).add((JMenu)m);
          break;
        case T_JMENU:
          ((JMenu)o).add((JMenuItem)m);
          break;
        case T_JPOPUPMENU:
          ((JPopupMenu)o).add((JMenuItem)m);
          break;
      }
    } else {
/*      switch (type) {
        case T_MENU:
        case T_POPUPMENU:
          ((Menu)o).addSeparator();
          break;
        case T_JMENU:
          ((JMenu)o).addSeparator();
          break;
        case T_JPOPUPMENU:
          ((JPopupMenu)o).addSeparator();
          break;
      } */
    }
  }
  
// -----------------------------------------------------------------------------
// Debug methods

// -----------------------------------------------------------------------------
// Innerclasses

  /** @return NewType for creating separator or null if this node doesn't support it.
  */
  private NewType createSeparatorNewType() {
    int type = getMenuItemType ();
    return ((type == T_MENU) || (type == T_POPUPMENU) ||
            (type == T_JMENU) || (type == T_JPOPUPMENU)) ? new NewSeparatorType() : null;
  }

  /** NewType class for creating the separator */
  private class NewSeparatorType extends NewType {

    /** Help context for the creation action.
    * @return the help context
    */
    public org.openide.util.HelpCtx getHelpCtx() {
      return new org.openide.util.HelpCtx (getClass ());
    }

    /** Display name for the creation action. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
      return FormEditor.getFormBundle ().getString("CTL_separator");
    }

    /** Create the object.
    * @exception IOException if something fails
    */
    public void create () throws IOException {
      RADComponent newNonVisualComp = new RADComponent ();
      newNonVisualComp.initialize (getFormManager ());
      newNonVisualComp.setComponent (JSeparator.class); // [PENDING]
      getFormManager ().addNonVisualComponent (newNonVisualComp, RADMenuComponent.this);
      getFormManager ().selectComponent (newNonVisualComp, false);
      return;
      
/*      MenuSeparatorNode sep = new MenuSeparatorNode(RADMenuNode.this);
      RADMenuNode.this.add(sep);
      RADMenuNode.this.getFormManager().regenerateInitializer(); */
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
      if (FormEditor.getFormSettings ().getShortBeanNames()) {
        int index = s.lastIndexOf('.');
        if (index != -1)
          return s.substring(index + 1);
      }
      return s;
    }

    /** Create the object.
    * @exception IOException if something fails
    */
    public void create () throws IOException {
      RADMenuItemComponent newMenuComp;
      
      if ((RADMenuItemComponent.recognizeType(item) & MASK_CONTAINER) == 0) {
        newMenuComp = new RADMenuItemComponent();
      }
      else {
        newMenuComp = new RADMenuComponent();
      }

      newMenuComp.initialize (RADMenuComponent.this.getFormManager());
      newMenuComp.setComponent (item);
      if (newMenuComp instanceof RADMenuComponent) {
        ((RADMenuComponent)newMenuComp).initSubComponents (new RADComponent[0]);
      }
      RADMenuComponent.this.getFormManager().addNonVisualComponent (newMenuComp, RADMenuComponent.this);

      // for some components, we initialize their properties with some non-default values
      // e.g. a label on buttons, checkboxes
      FormEditor.defaultMenuInit (newMenuComp);
      addVisualMenu (newMenuComp);
      
      RADMenuComponent.this.getFormManager().selectComponent (newMenuComp, false);
    } 
  }
}

/*
 * Log
 *  4    Gandalf   1.3         7/14/99  Ian Formanek    Fixed problem with 
 *       appearance of loaded menus
 *  3    Gandalf   1.2         7/9/99   Ian Formanek    Menu editor improvements
 *  2    Gandalf   1.1         7/5/99   Ian Formanek    improved
 *  1    Gandalf   1.0         7/3/99   Ian Formanek    
 * $
 */
