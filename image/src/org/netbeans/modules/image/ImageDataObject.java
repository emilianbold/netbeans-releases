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

package org.netbeans.modules.image;

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
    "org/netbeans/modules/image/imageObject"; // NOI18N

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
    return new org.openide.util.HelpCtx (ImageDataObject.class);
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

/*
 * Log
 *  14   Gandalf   1.13        1/5/00   Ian Formanek    NOI18N
 *  13   Gandalf   1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  11   Gandalf   1.10        6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  10   Gandalf   1.9         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         4/13/99  Jesse Glick     Clean-ups of comments 
 *       and such for public perusal.
 *  8    Gandalf   1.7         3/22/99  Ian Formanek    Icons moved from 
 *       modules/resources to this package
 *  7    Gandalf   1.6         2/3/99   Jaroslav Tulach 
 *  6    Gandalf   1.5         1/22/99  Ian Formanek    
 *  5    Gandalf   1.4         1/15/99  Petr Hamernik   image source repaired
 *  4    Gandalf   1.3         1/7/99   Jaroslav Tulach Uses OpenSupport
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
