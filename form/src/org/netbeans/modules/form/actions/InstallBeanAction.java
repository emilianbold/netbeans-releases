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

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import com.netbeans.developer.modules.loaders.form.palette.BeanInstaller;

/** This action installs new bean into the system.
*
* @author Petr Hamernik
*/
public class InstallBeanAction extends CallableSystemAction {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 7755319389083740521L;

  /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName() {
    return NbBundle.getBundle (InstallBeanAction.class).getString("ACT_InstallBean");
  }

  /** Help context where to find more about the action.
  * @return the help context for this action
  */
  public HelpCtx getHelpCtx() {
    return new HelpCtx (InstallBeanAction.class);
  }

  /** Icon resource.
  * @return name of resource for icon
  */
  protected String iconResource () {
    return "/com/netbeans/developer/modules/loaders/form/resources/installBean.gif";
  }
  
  /** This method is called by one of the "invokers" as a result of
  * some user's action that should lead to actual "performing" of the action.
  */
  public void performAction() {
    BeanInstaller.installBean();
  }

}

/*
 * Log
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/26/99  Ian Formanek    Actions cleanup
 *  1    Gandalf   1.0         5/17/99  Petr Hamernik   
 * $
 */
