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

package com.netbeans.developer.impl.actions;

import com.netbeans.ide.TopManager;
import com.netbeans.ide.windows.TopComponent;
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.actions.ActionPerformer;
import com.netbeans.ide.util.actions.CallableSystemAction;

import com.netbeans.developer.impl.*;

/** Opens properties that listen on global changes of selected nodes and update itself.
*
* @author Jaroslav Tulach
*/
public final class GlobalPropertiesAction extends CallableSystemAction {

  /** Opens std IO top component */
  public void performAction() {
    TopComponent c = NbNodeOperation.Sheet.getDefault ();
    c.open ();
    c.requestFocus();
  }

  public String getName() {
    return NbBundle.getBundle(OutputWindowAction.class).getString("GlobalProperties");
  }

  /** @return the action's help context */
  public HelpCtx getHelpCtx() {
    // PENDING
    return new HelpCtx();
  }

  /**
  * @return resource for the action icon
  */
  protected String iconResource () {
    return "/com/netbeans/developer/impl/resources/frames/globalProperties.gif";
  }

}

/*
* Log
*  1    src-jtulach1.0         4/2/99   Jaroslav Tulach 
* $
*/
