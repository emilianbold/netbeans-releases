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

package com.netbeans.developer.modules.javadoc.search;

import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.actions.CallableSystemAction;

/** 
* Search doc action.
*
* @author   Petr Hrebejk
*/
public class SearchDocAction extends CallableSystemAction {

  /** generated Serialized Version UID */
  //static final long serialVersionUID = 1391479985940417455L;

  /** Link to the documentation index search window. */
  static IndexSearch indexSearch = null;

  /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName () {
    return NbBundle.getBundle (SearchDocAction.class).getString ("CTL_SEARCH_MenuItem");
  }

  /** The action's icon location.
  * @return the action's icon location
  */
  protected String iconResource () {
    return "/com/netbeans/developer/modules/javadoc/resources/searchDoc.gif"
  }
  
  /** Help context where to find more about the action.
  * @return the help context for this action
  */
  public HelpCtx getHelpCtx () {
    return HelpCtx.DEFAULT_HELP;
  }

  /** This method is called by one of the "invokers" as a result of
  * some user's action that should lead to actual "performing" of the action.
  * This default implementation calls the assigned actionPerformer if it
  * is not null otherwise the action is ignored.
  */
  public void performAction () {
    if (indexSearch != null) 
      indexSearch.open ();
    else 
      (indexSearch = new IndexSearch ()).open ();
  }
  
}

/*
 * Log
 *  3    Gandalf   1.2         5/26/99  Ian Formanek    touch-ups
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $
 */
