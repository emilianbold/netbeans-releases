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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.ImageIcon;

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
public class ImageDataObject extends MultiDataObject implements OpenCookie {
  /** generated Serialized Version UID */
  static final long serialVersionUID = -6035788991669336965L;

  public static final String PROP_FILE_PARAMS = "params";

  private static final String IMAGE_ICON_BASE =
    "/com.netbeans.developer.modules/resources/imageObject";

  /** cached open action */
  private transient static OpenAction openAction;

  /** Viewers counter */
  CloneableTopComponent.Ref viewers = CloneableTopComponent.EMPTY;

  /** New instance.
  * @param pf primary file object for this data object
  */
  public ImageDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
    super(pf, loader);
  }

  /** Help context for this object.
  * @return help context
  */
  public com.netbeans.ide.util.HelpCtx getHelpCtx () {
    return new com.netbeans.ide.util.HelpCtx ("com.netbeans.developer.docs.Users_Guide.usergd-using-div-12", "USERGD-USING-TABLE-2");
  }

  /**
  * @return ImageIcon loaded from primary FileObject of this DataObject.
  */
  URL getImageURL() throws IOException {
    return getPrimaryFile().getURL();
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
    return new ImageNode(this);
  }

  public void open() {
    try {
      if (viewers.isEmpty()) {
        ImageViewer v = new ImageViewer(this);
        v.open ();

        viewers = v.getReference();
      }
      else {
        ((ImageViewer) viewers.getAnyComponent()).requestFocus();
      }
    }
    catch (IOException e) {
      TopManager.getDefault().notifyException(e);
    }
  }

  /** Deals with deleting of the object.
  * @exception IOException if an error occures
  */
  protected void handleDelete () throws IOException {
    Enumeration en = viewers.getComponents();
    while (en.hasMoreElements()) {
      ImageViewer comp = (ImageViewer)en.nextElement();
      comp.close();
    }
    super.handleDelete();
  }

  /**
  */
  private static class ImageNode extends DataNode {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4631326702811432357L;

    public ImageNode (DataObject obj) {
      super(obj, Children.LEAF);
      setIconBase(IMAGE_ICON_BASE);
    }

    /** Overrides default action from DataNode.
    * Instantiate a template, if isTemplate() returns true.
    * Opens otherwise.
    */
    public SystemAction getDefaultAction () {
      SystemAction result = super.getDefaultAction();
      if (result != null)
        return result;
      // not template, so try to open at least
      return OpenAction.get (OpenAction.class);
    }
  } // end of ImageDataObject inner class
}

/*
 * Log
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
