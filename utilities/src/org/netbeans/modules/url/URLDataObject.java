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

package com.netbeans.developer.modules.loaders.url;

import java.io.IOException;
import java.net.URL;

import com.netbeans.ide.*;
import com.netbeans.ide.cookies.OpenCookie;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.windows.*;
import com.netbeans.ide.actions.OpenAction;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.*;

/** Object that represents one file containing url in the tree of
* beans representing data systems.
*
* @author Ian Formanek
*/
public class URLDataObject extends MultiDataObject {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = -6035788991669336965L;

  private static final String URL_ICON_BASE =
    "com/netbeans/developer/modules/resources/urlObject";

  /** New instance.
  * @param pf primary file object for this data object
  */
  public URLDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
    super(pf, loader);
    getCookieSet ().add (new OpenCookie () {
        /** Invokes the open action */
        public void open () {
          String urlString = "http://www.netbeans.com/";
          java.net.URL url = null;
          try {
            url = new java.net.URL (urlString);
            TopManager.getDefault ().showUrl (url);
          } catch (java.net.MalformedURLException e) {
            // TopManager.notify (...)  // [PENDING - display message box with error]
          }
        }
      }
    );
  }

  /** Help context for this object.
  * @return help context
  */
  public com.netbeans.ide.util.HelpCtx getHelpCtx () {
    return new HelpCtx ();
    //[PENDING]
  }

  /** Provides node that should represent this data object. When a node for representation
  * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
  * with only parent changed. This implementation creates instance
  * <CODE>DataNode</CODE>.
  * <P>
  * This method is called only once.
  *
  * @return the node representation for this data object
  * @see DataNode
  */
  protected Node createNodeDelegate () {
    DataNode node = new DataNode (this, Children.LEAF);
    node.setIconBase(URL_ICON_BASE);
    node.setDefaultAction (SystemAction.get (OpenAction.class));
    return node;
  }

}


/*
 * Log
 *  1    Gandalf   1.0         1/22/99  Ian Formanek    
 * $
 */
