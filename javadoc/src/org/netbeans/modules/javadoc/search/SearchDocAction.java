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

package org.netbeans.modules.javadoc.search;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/** 
* Search doc action.
*
* @author   Petr Hrebejk
*/
public class SearchDocAction extends CallableSystemAction {

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
    return "/org/netbeans/modules/javadoc/resources/searchDoc.gif"; // NOI18N
  }
  
  /** Help context where to find more about the action.
   * @return the help context for this action
   */
  public HelpCtx getHelpCtx () {
    return new HelpCtx (SearchDocAction.class);
  }

  /** This method is called by one of the "invokers" as a result of
   * some user's action that should lead to actual "performing" of the action.
   * This default implementation calls the assigned actionPerformer if it
   * is not null otherwise the action is ignored.
   */
  public void performAction () {
    
    IndexSearch indexSearch = IndexSearch.getDefault();
    
    String toFind = GetJavaWord.getCurrentJavaWord();

    if (toFind != null) 
      indexSearch.setTextToFind( toFind );
      
    indexSearch.open ();
    indexSearch.requestFocus();
  }
}

/*
 * Log
 *  14   Gandalf   1.13        1/12/00  Petr Hrebejk    i18n
 *  13   Gandalf   1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        8/17/99  Petr Hrebejk    IndexSearch window 
 *       serialization
 *  11   Gandalf   1.10        8/13/99  Petr Hrebejk    Exception icopn added & 
 *       Jdoc repository moved to this package
 *  10   Gandalf   1.9         7/30/99  Petr Hrebejk    Search uses 
 *       FileSystemCapabilities
 *  9    Gandalf   1.8         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  8    Gandalf   1.7         6/23/99  Petr Hrebejk    HTML doc view & sort 
 *       modes added
 *  7    Gandalf   1.6         6/11/99  Petr Hrebejk    Better support for 
 *       search from editor; Enter for start searching
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/27/99  Petr Hrebejk    Crtl+F1 documentation 
 *       search form editor added
 *  4    Gandalf   1.3         5/26/99  Ian Formanek    Fixed last change
 *  3    Gandalf   1.2         5/26/99  Ian Formanek    touch-ups
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $
 */
