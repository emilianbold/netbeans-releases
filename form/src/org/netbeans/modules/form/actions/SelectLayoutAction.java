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

import java.util.Vector;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.Node;
//import com.netbeans.ide.nodes.Cookies;
//import com.netbeans.developer.modules.loaders.form.palette.*;
import com.netbeans.developer.modules.loaders.form.*;

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
    return new Class[] { FormNodeCookie.class, FormLayoutCookie.class };
  }

  /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName() {
    return com.netbeans.ide.util.NbBundle.getBundle (SelectLayoutAction.class).getString ("ACT_SelectLayout");
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

  /** Returns a JMenuItem that presents the Action, that implements this
  * interface, in a MenuBar.
  * @return the JMenuItem representation for the Action
  * /
  public JMenuItem getMenuPresenter() {
    return getPopupPresenter ();
  }

  private PaletteNode[] getAllLayouts () {
    Vector layouts = new Vector ();
    Node[] categories = PaletteContext.getPaletteContext().getPaletteCategories ();
    for (int i = 0; i < categories.length; i++) {
      Node[] paletteNodes = ((PaletteCategory)categories[i]).getPaletteNodes ();
      for (int j = 0; j < paletteNodes.length; j++)
        if ((paletteNodes[j] instanceof PaletteNode) &&
            (((PaletteNode)paletteNodes[j]).isDesignLayout ()))
          layouts.addElement (paletteNodes[j]);
    }
    PaletteNode[] ret = new PaletteNode[layouts.size ()];
    layouts.copyInto (ret);
    return ret;
  }
    
  /** Returns a JMenuItem that presents the Action, that implements this
  * interface, in a Popup Menu.
  * @return the JMenuItem representation for the Action
  * /
  public JMenuItem getPopupPresenter() {
    JMenu popupMenu = new JMenu (FormEditor.getFormBundle ().getString ("ACT_SelectLayout"));
    popupMenu.setEnabled (isEnabled ());
    popupMenu.addMenuListener(new MenuListener() {
        public void menuSelected(MenuEvent e) {
          JMenu menu = (JMenu)e.getSource ();
          if (menu.getMenuComponentCount () > 0) // [IAN - Patch for Swing 1.1, which throws NullPointerException if removeAll is called on empty uninitialized JMenu]
            menu.removeAll ();
          Node[] nodes = getActivatedNodes ();
          if (nodes.length == 0) return;

          PaletteNode[] layouts = getAllLayouts ();
          
          for (int i = 0; i < layouts.length; i++) {
            JMenuItem mi = new JMenuItem (layouts[i].getDisplayName ());
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
    private PaletteNode paletteNode;
    
    LayoutActionListener (Node[] activatedNodes, PaletteNode paletteNode) {
      this.activatedNodes = activatedNodes;
      this.paletteNode = paletteNode;
    }
    
    public void actionPerformed (java.awt.event.ActionEvent evt) {
      if (activatedNodes == null) return; // due to the swing's bug with popup menus, it can be null
      RADContainerNode cont;
      for (int i = 0; i < activatedNodes.length; i++) {
        Node.Cookie cookie = activatedNodes[i].getCookie ();
        if (Cookies.isInstanceOf (cookie , FormNodeCookie.class))
          cont = (RADContainerNode)Cookies.getInstanceOf (cookie, FormNodeCookie.class);
        else if (Cookies.isInstanceOf (cookie, FormLayoutCookie.class))
          cont = (RADContainerNode) (((FormLayoutCookie)Cookies.getInstanceOf (cookie, FormLayoutCookie.class)).getLayoutNode ()).getParentNode ();
        else continue;

        // set the selected layout on the activated container 
        // (or activated layout's parent container)
        cont.getFormManager ().setDesignLayout (cont, paletteNode);
      }
    }
  }


  /** Instance of this class must be given to the constructor of NodeAction.
  * In this class all the functionality of the NodeAction is stored.
  * User of node action may automatically enable / disable this action
  * dependently on the activated nodes.
  * /
  public static class SelectLayoutControl extends CookieAction.CookieControl {
    public boolean enable (Node[] activatedNodes) {
      boolean ret = super.enable(activatedNodes);

      if (ret) {
        Node[] supported = resolveSupported(activatedNodes);
        if (Cookies.isInstanceOf (supported[0].getCookie (), FormNodeCookie.class))
          if (!(((FormNodeCookie)Cookies.getInstanceOf (
              supported[0].getCookie(), FormNodeCookie.class)).getFormNode() instanceof RADContainerNode))
            return false;
        return true;
      }
      else {
        return false;
      }
    }

    /**
    * Standart perform action extended by actually activated nodes.
    * @see CallableSystemAction#performAction
    *
    * @param activatedNodes gives array of actually activated nodes.
    * /
    public void performAction (Node[] activatedNodes) {
    }
  }
  */
}
/*
 * Log
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  2    Gandalf   1.1         3/28/99  Ian Formanek    Introduced changes done 
 *       in X2 after this class was copied to Gandalf
 *  1    Gandalf   1.0         3/26/99  Ian Formanek    
 * $
 */
