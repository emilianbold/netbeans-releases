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

package com.netbeans.developer.impl;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.options.*;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import com.netbeans.developer.impl.actions.*;

/** This object represents environment settings in the Corona system.
* This class is final only for performance purposes.
* Can be unfinaled if desired.
*
* @author Petr Hamernik, Dafe Simonek
*/
final class EnvironmentNode extends AbstractNode {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 4782447107972624693L;
  /** icon base for icons of this node */
  private static final String EN_ICON_BASE = "/com/netbeans/developer/impl/resources/environment";

  /** base instance of the node */
  private static EnvironmentNode node;

  /** children to use */
  private static Children children;
  

  /** Constructor */
  private EnvironmentNode () {
    super (children = new Children.Array());
    setName(NbBundle.getBundle(EnvironmentNode.class).
                   getString("CTL_Environment_name"));
    setIconBase(EN_ICON_BASE);
  }

  public HelpCtx getHelpCtx () {
    return new HelpCtx (EnvironmentNode.class);
  }

  /** Method to add an node to the environment.
  */
  public static void addNode (Node n) {
    getDefault ();
    children.add (new Node[] { n });
  }
  
  /** Method to add an node to the environment.
  */
  public static void removeNode (Node n) {
    children.remove (new Node[] { n });
  }

  /** Default instance */
  public static synchronized Node getDefault () {
    if (node == null) {
      node = new EnvironmentNode ();
    }
    return node;
  }

  /** Getter for set of actions that should be present in the
  * popup menu of this node. This set is used in construction of
  * menu returned from getContextMenu and specially when a menu for
  * more nodes is constructed.
  *
  * @return array of system actions that should be in popup menu
  */
  public SystemAction[] createActions () {
    return new SystemAction[] {
      SystemAction.get(ToolsAction.class),
      SystemAction.get(PropertiesAction.class)
    };
  }
}

/*
 * Log
 *  17   Gandalf   1.16        7/8/99   Jesse Glick     Context help.
 *  16   Gandalf   1.15        6/9/99   Ian Formanek    ToolsAction
 *  15   Gandalf   1.14        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  14   Gandalf   1.13        5/9/99   Ian Formanek    Fixed bug 1655 - 
 *       Renaming of top level nodes is not persistent (removed the possibility 
 *       to rename).
 *  13   Gandalf   1.12        3/28/99  David Simonek   menu support improved 
 *       (icons, actions...)
 *  12   Gandalf   1.11        3/26/99  Jaroslav Tulach 
 *  11   Gandalf   1.10        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  10   Gandalf   1.9         3/18/99  Jaroslav Tulach 
 *  9    Gandalf   1.8         2/25/99  Jaroslav Tulach Change of clipboard 
 *       management  
 *  8    Gandalf   1.7         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  7    Gandalf   1.6         1/25/99  Jaroslav Tulach Added default project, 
 *       its desktop and changed default explorer in Main.
 *  6    Gandalf   1.5         1/20/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         1/7/99   Ian Formanek    
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Fixed outerclass 
 *       specifiers uncompilable under JDK 1.2
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach ide.* extended to 
 *       ide.loaders.*
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
