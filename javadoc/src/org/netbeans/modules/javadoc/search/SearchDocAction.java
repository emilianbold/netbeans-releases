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

import java.net.URL;
import java.util.ResourceBundle;

import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.actions.ActionPerformer;
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
    return NbBundle.getBundle (getClass ()).getString ("CTL_SEARCH_MenuItem");
  }

  /** The action's icon location.
  * @return the action's icon location
  */
  protected String iconResource () {
    return NbBundle.getBundle (getClass ()).getString ("ICO_SEARCH");
    //return "/com/netbeans/developer/modules/javadoc/resources/SearchDoc.gif";
  }
  
  /** Help context where to find more about the action.
  * @return the help context for this action
  */
  public HelpCtx getHelpCtx () {
    //return new HelpCtx ();
      return null;
  }

  /** This method is called by one of the "invokers" as a result of
  * some user's action that should lead to actual "performing" of the action.
  * This default implementation calls the assigned actionPerformer if it
  * is not null otherwise the action is ignored.
  */
  public void performAction () {

    //DocFileSystem.getFolders();  

    if (indexSearch != null) 
      indexSearch.open ();
    else 
      (indexSearch = new IndexSearch ()).open ();
  
  }
  
  /** Returns instance of object browser.
  * @return instance of object browser.
  */
  /*
  public ObjectBrowser getObjectBrowser () {
    if (objectBrowser == null) 
      objectBrowser = new ObjectBrowser ();
    return objectBrowser;
  }
  */
}

/*
 * Log
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    changes (position, serialization)
 */
