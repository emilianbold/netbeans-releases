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

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;

/** SystemExit action.
* @author   Ian Formanek
* @version  0.14, Feb 13, 1998
*/
public class SystemExit extends CallableSystemAction {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 5198683109749927396L;

 /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName() {
    return NbBundle.getBundle(SystemExit.class).getString("Exit");
  }

  /** Help context where to find more about the action.
  * @return the help context for this action
  */
  public HelpCtx getHelpCtx() {
    return new HelpCtx (SystemExit.class);
  }

  /** Name of this action's icon.
  * @return name of the action's icon
  */
  protected String iconResource () {
    return "/com/netbeans/developer/impl/resources/actions/exit.gif"; // NOI18N
  }

  public void performAction() {
    org.openide.TopManager.getDefault().exit();
  }


  
}

/*
 * Log
 *  17   Gandalf   1.16        1/12/00  Ales Novak      i18n
 *  16   Gandalf   1.15        12/1/99  Petr Hrebejk    Save before exit moved 
 *       to NbTopManager
 *  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  13   Gandalf   1.12        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  12   Gandalf   1.11        6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   Gandalf   1.9         5/26/99  Ian Formanek    Actions cleanup
 *  9    Gandalf   1.8         3/29/99  Ian Formanek    CoronaDialog -> 
 *       DialogDescriptor
 *  8    Gandalf   1.7         3/9/99   Jaroslav Tulach ButtonBar  
 *  7    Gandalf   1.6         3/5/99   Ales Novak      
 *  6    Gandalf   1.5         1/20/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         1/14/99  David Simonek   
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    extends CallableSystemAction because the actions hierarchy has changed
 *  0    Tuborg    0.12        --/--/98 Jan Jancura     Icon ...
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    action name localization
 */
