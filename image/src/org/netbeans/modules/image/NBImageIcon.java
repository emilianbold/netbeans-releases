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
import javax.swing.*;

import com.netbeans.ide.util.io.*;

/** ImageIcon with the changed serialization.
*
* @author Petr Hamernik
* @version 0.18, Jun 3, 1998
*/
class NBImageIcon extends ImageIcon implements Replaceable {
  /** generated Serialized Version UID */
  static final long serialVersionUID = -1730253055388017036L;
  /** Appropriate image data object */
  ImageDataObject obj;

  /** Constructs new ImageIcon for the dataobject */
  public NBImageIcon(ImageDataObject obj) throws IOException {
    super(obj.getImageURL());
    this.obj = obj;
  }

  /** @return an object to be written to the stream instead of this object. */
  public Object writeReplace() {
    return new ResolvableHelper(obj);
  }

  /** Helper class for serialization */
  static class ResolvableHelper implements Resolvable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1120520132882774882L;
    /** DataObject for this image */
    ImageDataObject obj;
 
    ResolvableHelper(ImageDataObject obj) {
      this.obj = obj;
    }

    /**
    * Return an object to replace the object extracted from the stream.
    * The object will be used in the graph in place of the original.
    */
    public Object readResolve() {
      try {
        return new NBImageIcon(obj);
      }
      catch (IOException e) {
        return new ImageIcon();
      }
    }
  }
}

/*
 * Log
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
