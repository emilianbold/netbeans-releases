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

package com.netbeans.developer.modules.loaders.form.actions;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import com.netbeans.developer.modules.loaders.form.palette.*;
import com.netbeans.developer.modules.loaders.form.*;
import com.netbeans.developerx.loaders.form.formeditor.layouts.support.DesignSupportLayout;

/** SelectLayout action - subclass of NodeAction - enabled on RADContainerNodes and RADLayoutNodes.
*
* @author   Ian Formanek
*/
public class SelectLayoutAction extends CookieAction {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = -5280204757097896304L;

  /** @return the mode of action. Possible values are disjunctions of MODE_XXX
  * constants. */
  protected int mode() {
    return MODE_ALL;
  }
  
  /** Creates new set of classes that are tested by the cookie.
  *
  * @return list of classes the that the cookie tests
  */
  protected Class[] cookieClasses () {
    return new Class[] { RADComponentCookie.class, FormLayoutCookie.class };
  }

  /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName() {
    return org.openide.util.NbBundle.getBundle (SelectLayoutAction.class).getString ("ACT_SelectLayout");
  }

  /** Help context where to find more about the action.
  * @return the help context for this action
  */
  public HelpCtx getHelpCtx() {
    return new HelpCtx(SelectLayoutAction.class);
  }

  /** Icon resource.
  * @return name of resource for icon
  */
  protected String iconResource () {
    return "/com/netbeans/developer/modules/loaders/form/resources/selectLayout.gif";
  }

  /**
  * Standard perform action extended by actually activated nodes.
  *
  * @param activatedNodes gives array of actually activated nodes.
  */
  protected void performAction (Node[] activatedNodes) {
  }

  /*
  * In this method the enable / disable action logic can be defined.
  *
  * @param activatedNodes gives array of actually activated nodes.
  */
  protected boolean enable (Node[] activatedNodes) {
    if (super.enable (activatedNodes)) {
      for (int i = 0; i < activatedNodes.length; i++) {
        RADVisualContainer container = null;
        FormLayoutCookie layoutCookie = (FormLayoutCookie)activatedNodes[i].getCookie (FormLayoutCookie.class);
        if (layoutCookie != null) {
          container = layoutCookie.getLayoutNode ().getRADContainer ();
        } else {
          RADComponentCookie nodeCookie = (RADComponentCookie)activatedNodes[i].getCookie (RADComponentCookie.class);
          if (nodeCookie != null) {
            if (nodeCookie.getRADComponent () instanceof RADVisualContainer) {
              container = (RADVisualContainer)nodeCookie.getRADComponent ();
            }
          }
        }

        if ((container != null) && (!(container.getDesignLayout () instanceof DesignSupportLayout))) {
          return true;
        }

      }
    }
    return false;
  }

  /** Returns a JMenuItem that presents the Action, that implements this
  * interface, in a MenuBar.
  * @return the JMenuItem representation for the Action
  */
  public JMenuItem getMenuPresenter() {
    return getPopupPresenter ();
  }

  private PaletteItem[] getAllLayouts () {
    PaletteItem[] allItems = ComponentPalette.getDefault ().getAllItems ();
    ArrayList layoutsList = new ArrayList ();
    for (int i = 0; i < allItems.length; i++) {
      if (allItems[i].isDesignLayout ()) {
        layoutsList.add (allItems[i]);
      }
    }

    PaletteItem[] layouts = new PaletteItem[layoutsList.size ()];
    layoutsList.toArray (layouts);
    return layouts;
  }
    
  /** Returns a JMenuItem that presents the Action, that implements this
  * interface, in a Popup Menu.
  * @return the JMenuItem representation for the Action
  */
  public JMenuItem getPopupPresenter() {
    JMenu popupMenu = new org.openide.awt.JMenuPlus (getName ());
    popupMenu.setEnabled (isEnabled ());
    popupMenu.addMenuListener(new MenuListener() {
        public void menuSelected(MenuEvent e) {
          JMenu menu = (JMenu)e.getSource ();
          if (menu.getMenuComponentCount () > 0) { // [IAN - Patch for Swing 1.1, which throws NullPointerException if removeAll is called on empty uninitialized JMenu]
            menu.removeAll ();
          }
          Node[] nodes = getActivatedNodes ();
          if (nodes.length == 0) return;

          PaletteItem[] layouts = getAllLayouts ();
          
          for (int i = 0; i < layouts.length; i++) {
            JMenuItem mi = new JMenuItem (layouts[i].getName ());
            menu.add (mi);
            mi.addActionListener (new LayoutActionListener (nodes, layouts[i]));
          }
        }
        public void menuDeselected(MenuEvent e) {
        }
        public void menuCanceled(MenuEvent e) {
        }
      }
    );
    return popupMenu;
  }

  class LayoutActionListener implements java.awt.event.ActionListener {
    private Node[] activatedNodes;
    private PaletteItem paletteItem;
    
    LayoutActionListener (Node[] activatedNodes, PaletteItem paletteItem) {
      this.activatedNodes = activatedNodes;
      this.paletteItem = paletteItem;
    }
    
    public void actionPerformed (java.awt.event.ActionEvent evt) {
      if (activatedNodes == null) return; // due to the swing's bug with popup menus, it can be null
      for (int i = 0; i < activatedNodes.length; i++) {
        RADVisualContainer container = null;
        FormLayoutCookie layoutCookie = (FormLayoutCookie)activatedNodes[i].getCookie (FormLayoutCookie.class);
        if (layoutCookie != null) {
          container = layoutCookie.getLayoutNode ().getRADContainer ();
        } else {
          RADComponentCookie nodeCookie = (RADComponentCookie)activatedNodes[i].getCookie (RADComponentCookie.class);
          if (nodeCookie != null) {
            if (nodeCookie.getRADComponent () instanceof RADVisualContainer) {
              container = (RADVisualContainer)nodeCookie.getRADComponent ();
            }
          }
        }

        if (container == null) {
          continue;
        }

        // set the selected layout on the activated container 
        // (or activated layout's parent container)
        container.getFormManager ().setDesignLayout (container, paletteItem);
      }
    }
  }

}
/*
 * Log
 *  6    Gandalf   1.5         6/28/99  Ian Formanek    Fixed positioning 
 *       problems with popup menu
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/20/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  2    Gandalf   1.1         3/28/99  Ian Formanek    Introduced changes done 
 *       in X2 after this class was copied to Gandalf
 *  1    Gandalf   1.0         3/26/99  Ian Formanek    
 * $
 */
