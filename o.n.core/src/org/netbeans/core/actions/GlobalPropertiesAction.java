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

package org.netbeans.core.actions;

import org.openide.TopManager;
import org.openide.windows.TopComponent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.core.*;

/** Opens properties that listen on global changes of selected nodes and update itself.
*
* @author Jaroslav Tulach
*/
public final class GlobalPropertiesAction extends CallableSystemAction {

static final long serialVersionUID =-4072717465854016148L;
  /** Opens std IO top component */
  public void performAction() {
    TopComponent c = NbNodeOperation.Sheet.getDefault ();
    c.open ();
    c.requestFocus();
  }

  public String getName() {
    return NbBundle.getBundle(GlobalPropertiesAction.class).getString("GlobalProperties");
  }

  /** @return the action's help context */
  public HelpCtx getHelpCtx() {
    return new HelpCtx (GlobalPropertiesAction.class);
  }

  /**
  * @return resource for the action icon
  */
  protected String iconResource () {
    return "/org/netbeans/core/resources/frames/globalProperties.gif"; // NOI18N
  }

}

/*
* Log
*  7    src-jtulach1.6         1/12/00  Ales Novak      i18n
*  6    src-jtulach1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    src-jtulach1.4         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  4    src-jtulach1.3         6/24/99  Jesse Glick     Gosh-honest HelpID's.
*  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    src-jtulach1.1         4/27/99  Jesse Glick     new HelpCtx () -> 
*       HelpCtx.DEFAULT_HELP.
*  1    src-jtulach1.0         4/2/99   Jaroslav Tulach 
* $
*/
