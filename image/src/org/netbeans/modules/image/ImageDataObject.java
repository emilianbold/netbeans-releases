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

import com.netbeans.ide.*;
import com.netbeans.ide.cookies.OpenCookie;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.windows.*;
import com.netbeans.ide.actions.OpenAction;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.*;

/** Object that represents one file containing image in the tree of
* beans representing data systems.
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
  */
  public ImageDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
    super(pf, loader);
    getCookieSet ().add (new Open (getPrimaryEntry ()));
  }

  /** Help context for this object.
  * @return help context
  */
  public com.netbeans.ide.util.HelpCtx getHelpCtx () {
    return new com.netbeans.ide.util.HelpCtx ("com.netbeans.developer.docs.Users_Guide.usergd-using-div-12", "USERGD-USING-TABLE-2");
  }

  /**
  * @return Image url loaded from primary FileObject of this DataObject.
  */
  java.net.URL getImageURL() {
    try {
      return getPrimaryFile().getURL();
    } catch (FileStateInvalidException ex) {
      return null;
    }
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
    node.setIconBase(IMAGE_ICON_BASE);
    node.setDefaultAction (SystemAction.get (OpenAction.class));
    return node;
  }

  /** Implementation of open cookie.
  */
  private class Open extends OpenSupport implements OpenCookie {
    public Open (MultiDataObject.Entry ent) {
      super (ent);
    }

    /** Creates the viewer */
    public CloneableTopComponent createCloneableTopComponent () {
      return new ImageViewer(ImageDataObject.this);
    }
  }

}


/*
 * Log
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
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach Changed number of parameters in constructor
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    Icon change
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    reflecting changes in cookies
 *  0    Tuborg    0.17        --/--/98 Jan Formanek    templates
 */
