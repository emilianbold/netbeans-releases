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

import org.openide.cookies.InstanceCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import com.netbeans.developer.modules.loaders.form.palette.BeanInstaller;

/** InstallToPalette action - enabled on RADContainerNodes and RADLayoutNodes.
*
* @author   Ian Formanek
*/
public class InstallToPaletteAction extends CookieAction {
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
    return new Class[] { InstanceCookie.class };
  }

  /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName() {
    return org.openide.util.NbBundle.getBundle (InstallToPaletteAction.class).getString ("ACT_InstallToPalette");
  }

  /** Help context where to find more about the action.
  * @return the help context for this action
  */
  public HelpCtx getHelpCtx() {
    return new HelpCtx(InstallToPaletteAction.class);
  }

  /** Icon resource.
  * @return name of resource for icon
  */
  protected String iconResource () {
    return "/org/openide/resources/actions/empty.gif";
  }

  /**
  * Standard perform action extended by actually activated nodes.
  *
  * @param activatedNodes gives array of actually activated nodes.
  */
  protected void performAction (Node[] activatedNodes) {
    InstanceCookie[] cookies = new InstanceCookie[activatedNodes.length];
    for (int i = 0; i < activatedNodes.length; i++) {
      cookies[i] = (InstanceCookie)activatedNodes[i].getCookie (InstanceCookie.class);
    }
    BeanInstaller.installBeans (cookies);
  }

}

/*
 * Log
 *  1    Gandalf   1.0         7/18/99  Ian Formanek    
 * $
 */
