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

package com.netbeans.developer.modules.beans.beaninfo;
 
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/** 
* Toggles selection.
*
* @author   Petr Hrebejk
*/
public class BiToggleAction extends NodeAction  {

static final long serialVersionUID =3773842179168178798L;
  /** generated Serialized Version UID */
  //static final long serialVersionUID = 1391479985940417455L;

  //private static final Class[] cookieClasses = new Class[] { BiFeatureNode.class };


 
  /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName () {
    return NbBundle.getBundle (GenerateBeanInfoAction.class).getString ("CTL_TOGGLE_MenuItem");
  }

 
 /*
  public Class[] cookieClasses() {
    return cookieClasses;
  }
  
*/
  /** The action's icon location.
  * @return the action's icon location
  */
  protected String iconResource () {
    return null;
    //return "/com/netbeans/developer/modules/javadoc/resources/searchDoc.gif"; // NOI18N
  }
  
  /*
  public int mode () {
    return CookieAction.MODE_ALL;
  }
  */

  /** Help context where to find more about the action.
  * @return the help context for this action
  */
  public HelpCtx getHelpCtx () {
    return new HelpCtx (BiToggleAction.class);
  }

  protected boolean enable( Node[] activatedNodes ) {
   return true;
  }


  /** This method is called by one of the "invokers" as a result of
  * some user's action that should lead to actual "performing" of the action.
  * This default implementation calls the assigned actionPerformer if it
  * is not null otherwise the action is ignored.
  */
  public void performAction ( Node[] nodes ) {
     
    nodes = BiPanel.getSelectedNodes();
   
    if ( nodes.length < 1 )
      return;
    
    for(int i = 0; i < nodes.length; i++ ) {
      ((BiFeatureNode)nodes[i].getCookie( BiFeatureNode.class )).toggleSelection();
    }
    
   }

}

/*
 * Log
 *  4    Gandalf   1.3         1/13/00  Petr Hrebejk    i18n mk3
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         7/26/99  Petr Hrebejk    
 * $
 */
