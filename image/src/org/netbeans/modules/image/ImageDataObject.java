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

package com.netbeans.developer.modules.loaders.image;

import org.openide.*;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.actions.OpenAction;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;

/** Object that represents one file containing an image.
*
* @author Petr Hamernik, Jaroslav Tulach, Ian Formanek
*/
public class ImageDataObject extends MultiDataObject {
  /** generated Serialized Version UID */
  static final long serialVersionUID = -6035788991669336965L;

  private static final String IMAGE_ICON_BASE =
    "com/netbeans/developer/modules/loaders/image/imageObject";

  /** New instance.
  * @param pf primary file object for this data object
  * @param loader the data loader creating it
  * @exception DataObjectExistsException if there was already a data object for it 
  */
  public ImageDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
    super(pf, loader);
    // Support OpenCookie.
    getCookieSet ().add (new Open (getPrimaryEntry ()));
  }

  /** Help context for this object.
  * @return the help context
  */
  public org.openide.util.HelpCtx getHelpCtx () {
    return new org.openide.util.HelpCtx ("com.netbeans.developer.docs.Users_Guide.usergd-using-div-12", "USERGD-USING-TABLE-2");
  }

  /** Get a URL for the image.
  * @return the image url
  */
  java.net.URL getImageURL() {
    try {
      return getPrimaryFile().getURL();
    } catch (FileStateInvalidException ex) {
      return null;
    }
  }

  /** Create a node to represent the image.
  * @return the node
  */
  protected Node createNodeDelegate () {
    DataNode node = new DataNode (this, Children.LEAF);
    node.setIconBase(IMAGE_ICON_BASE);
    node.setDefaultAction (SystemAction.get (OpenAction.class));
    return node;
  }

  private class Open extends OpenSupport implements OpenCookie {
    public Open (MultiDataObject.Entry ent) {
      super (ent);
    }

    // Creates the viewer
    public CloneableTopComponent createCloneableTopComponent () {
      return new ImageViewer(ImageDataObject.this);
    }
  }

}
