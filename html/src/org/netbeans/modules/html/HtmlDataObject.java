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

package com.netbeans.developer.modules.loaders.html;

import org.openide.*;
import org.openide.actions.ViewAction;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

/** Object that represents one html file.
*
* @author Ian Formanek
*/
public class HtmlDataObject extends MultiDataObject {

  private static final String HTML_ICON_BASE =
    "/com/netbeans/developer/modules/loaders/html/htmlObject";

static final long serialVersionUID =8354927561693097159L;
  /** New instance.
  * @param pf primary file object for this data object
  * @param loader the data loader creating it
  * @exception DataObjectExistsException if there was already a data object for it 
  */
  public HtmlDataObject(FileObject pf, UniFileLoader loader) throws DataObjectExistsException {
    super(pf, loader);
  }

  protected org.openide.nodes.Node createNodeDelegate () {
    DataNode n = new DataNode (this, Children.LEAF);
    n.setIconBase (HTML_ICON_BASE);
    n.setDefaultAction (SystemAction.get (ViewAction.class));
    return n;
  }

  public HelpCtx getHelpCtx () {
    return new HelpCtx (HtmlLoader.class.getName () + ".Obj");
  }

}
